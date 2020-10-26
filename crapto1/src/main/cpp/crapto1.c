#include <stdlib.h>
#include "crapto1.h"
#include "crypto1.h"

#pragma package(smart_init)
#if !defined LOWMEM && defined __GNUC__
static uint8_t filterlut[1 << 20];

static void __attribute__((constructor)) fill_lut() {
    uint32_t i;
    for (i = 0; i < 1 << 20; ++i)
        filterlut[i] = filter(i);
}

#define filter(x) (filterlut[(x) & 0xfffff])
#endif

/* quicksort
 * in place quicksort of table
 */
static void
quicksort(uint32_t *const start, uint32_t *const stop) {
    uint32_t *it = start + 1, *rit = stop;

    if (it > rit)
        return;

    while (it < rit)
        if (*it < *start)
            ++it;
        else if (*rit >= *start)
            --rit;
        else
            *it ^= (*it ^= *rit, *rit ^= *it);

    if (*rit >= *start)
        --rit;
    if (rit != start)
        *rit ^= (*rit ^= *start, *start ^= *rit);

    quicksort(start, rit - 1);
    quicksort(rit + 1, stop);
}

/* binsearch
 * Binary search for the first occurence of *stop's MSB in sorted [start,stop]
 */
static inline uint32_t *
binsearch(uint32_t *start, uint32_t *stop) {
    uint32_t mid, val = *stop & 0xff000000;
    while (start != stop)
        if (start[mid = (stop - start) >> 1] > val)
            stop = &start[mid];
        else
            start += mid + 1;

    return start;
}

/* update_contribution
 * helper, calculates the partial linear feedback contributions and puts in MSB
 */
static inline void
update_contribution(uint32_t *item, const uint32_t mask1, const uint32_t mask2) {
    uint32_t p = *item >> 25;

    p = p << 1 | parity(*item & mask1);
    p = p << 1 | parity(*item & mask2);
    *item = p << 24 | (*item & 0xffffff);
}

/* extend_table
 * using a bit of the keystream extend the table of possible lfsr states
 */
static inline void
extend_table(uint32_t *tbl, uint32_t **end, int bit, uint32_t m1, uint32_t m2) {
    for (; tbl <= *end; tbl++)
        if (filter(*tbl <<= 1) == bit) {
            if (filter(*tbl | 1) == bit) {
                *++*end = tbl[1];
                tbl[1] = tbl[0] | 1;
                update_contribution(tbl++, m1, m2);
            }
            update_contribution(tbl, m1, m2);
        } else if (filter(*tbl |= 1) == bit)
            update_contribution(tbl, m1, m2);
        else
            *tbl-- = *(*end)--;
}


/* recover
 * recursively narrow down the search space, 4 bits of keystream at a time
 */
static uint32_t *
recover(uint32_t *o_head, uint32_t *o_tail, uint32_t oks,
        uint32_t *e_head, uint32_t *e_tail, uint32_t eks, int rem) {
    uint32_t *o, *e, i;

    if (rem == -1) {
        o_head[1] = *e_head << 1;
        *e_head &= LF_POLY_EVEN;
        o_head[1] |= parity(*e_head ^ *o_head & LF_POLY_ODD);
        return o_head;
    }

    for (i = 0; i < 4 && rem--; i++) {
        extend_table(o_head, &o_tail, (oks >>= 1) & 1, LF_POLY_ODD * 2, LF_POLY_EVEN * 2 + 1);
        extend_table(e_head, &e_tail, (eks >>= 1) & 1, LF_POLY_EVEN * 2 + 1, LF_POLY_ODD);
    }

    quicksort(o_head, o_tail);
    quicksort(e_head, e_tail);

    while (o_tail >= o_head && e_tail >= e_head)
        if ((*o_tail ^ *e_tail) >> 24 == 0) {
            o_tail = binsearch(o_head, o = o_tail);
            e_tail = binsearch(e_head, e = e_tail);
            if (e = recover(o_tail--, o, oks, e_tail--, e, eks, rem))
                return e;
        } else if (*o_tail > *e_tail)
            --o_tail;
        else
            --e_tail;

    return 0;
}

/* lfsr_recovery
 * recover the state of the lfsr given a part of the keystream
 */
struct Crypto1State *lfsr_recovery(uint32_t ks2, uint32_t ks3) {
    uint32_t *odd_head = 0, *odd_tail = 0, oks = 0;
    uint32_t *even_head = 0, *even_tail = 0, eks = 0, *res;

    struct Crypto1State *state = crypto1_create(-1);
    int i;

    for (i = 31; i >= 0; i -= 2)
        oks = oks << 1 | BEBIT(ks2, i) | BEBIT(ks3, i) << 16;
    for (i = 30; i >= 0; i -= 2)
        eks = eks << 1 | BEBIT(ks2, i) | BEBIT(ks3, i) << 16;


    odd_head = odd_tail = (uint32_t *) malloc(sizeof(uint32_t) << 21);
    even_head = even_tail = (uint32_t *) malloc(sizeof(uint32_t) << 21);
    if (!odd_tail-- || !even_tail--)
        goto out;

    for (i = 1 << 20; i >= 0; --i) {
        if (filter(i) == (oks & 1))
            *++odd_tail = i;
        if (filter(i) == (eks & 1))
            *++even_tail = i;
    }

    for (i = 0; i < 4; i++) {
        extend_table(odd_head, &odd_tail, (oks >>= 1) & 1, 0, 0);
        extend_table(even_head, &even_tail, (eks >>= 1) & 1, 0, 0);
    }

    res = recover(odd_head, odd_tail, oks, even_head, even_tail, eks, 27);

    if (res) {
        state->odd = res[1];
        state->even = res[0];
    }
    out:
    free(odd_head);
    free(even_head);
    return state;
}

/* Variation mentioned in the paper. Borked due to omitted constants.
 * comform the above version given 64 bits of keystream, it returns a possible
 * cipher state if any.
 * It can be optimized dramatically but since it was borked a somewhat readable
 * and above all short version is included.
 */
#define MAGIX1 (0)
#define MAGIX2 (0) /* Well this number isn't that magical ... */

struct Crypto1State *lfsr_recovery_borked(uint32_t ks2, uint32_t ks3) {
    struct Crypto1State *state = crypto1_create(-1);
    uint32_t win = 0, oks = 0, eks = 0, tes, tos;
    uint64_t cmb, *table = (uint64_t *) malloc(1 << 25), *tail = table - 1, *tbl;
    int i;

    for (i = 0; i < 32; i += 2)
        oks = oks << 1 | BEBIT(ks3, i) | BEBIT(ks2, i) << 16;
    for (i = 1; i < 32; i += 2)
        eks = eks << 1 | BEBIT(ks3, i) | BEBIT(ks2, i) << 16;

    for (i = 0; i < 1 << 20; ++i)
        if (filter(i) == BIT(oks, 31))
            *++tail = i;

    for (i = 30; i > 2; --i)
        for (*(tbl = table) <<= 1; tbl <= tail; *++tbl <<= 1)
            if (filter(*tbl) ^ filter(*tbl | 1))
                *tbl |= (filter(*tbl) != BIT(oks, i));
            else if (filter(*tbl) == BIT(oks, i)) {
                *++tail = *++tbl;
                *tbl = tbl[-1] | 1;
            } else
                *tbl-- = *tail--;

    for (; tail >= table; --tail) {
        cmb = MAGIX1;
        for (i = 0; i < 51; ++i) {
            win = win << 1 ^ par64(*tail & cmb);
            tes = tes << 1 ^ filter(win);
            cmb = cmb >> 1 ^ -(cmb & 1) & MAGIX2;
        }

        for (i = 2; i >= 0; --i) {
            *tail = *tail << 1 | par64(*tail & MAGIX2);
            tos = tos << 1 | filter(*tail);
        }

        if (tes == eks && (tos & 7) == (oks & 7)) {
            state->odd = *tail << 1 | par64(*tail & MAGIX2);
            state->even = win;
        }
    }

    free(table);
    return state;
}

/* lfsr_rollback
 * Rollback the shift register in order to get previous states
 */
void lfsr_rollback(struct Crypto1State *s, uint32_t in, int fb) {
    int i, out;

    s->odd &= 0xffffff;
    s->even &= 0xffffff;
    for (i = 31; i >= 0; --i) {
        s->odd ^= (s->odd ^= s->even, s->even ^= s->odd);

        out = s->even & 1;
        out ^= LF_POLY_EVEN & (s->even >>= 1);
        out ^= LF_POLY_ODD & s->odd;
        out ^= BEBIT(in, i);
        out ^= filter(s->odd) & !!fb;

        s->even |= parity(out) << 23;
    }
}

