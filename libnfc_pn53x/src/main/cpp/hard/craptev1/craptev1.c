/**
 * CraptEV1
 * Copyright (c) 2015-2016 blapost@gmail.com
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted for non-commercial use only.
 *
 * No redistribution. No modifications.
 */
#include "craptev1.h"
static uint8_t halfsum[2][1 << 20];
static uint8_t filterflip[1 << 20];
static uint8_t filterlut[1 << 20];
static uint32_t hsum_off[2][0x89];
static double prob[257];
void __attribute__((constructor)) craptev1_init() {
    uint32_t i, j, s, t, p, q;
    uint32_t esum, osum;
    uint64_t ocnt[9] = {0}, ecnt[9] = {0};

    if(**halfsum)
        return;

    for(i = 0; i < 1 << 20; i++) {
        osum = esum = 0;
        for(j = 0; j < 1 << 4; j++) {
            s = i << 4 | j;
            t = filter(s) ^ filter(s >> 1) ^ filter(s >> 2) ^ filter(s >> 3);
            osum += t ^ filter(i);
            esum += t;
        }
        halfsum[0][i] = esum >> 1;
        halfsum[1][i] = osum >> 1;
        ecnt[esum >> 1]++;
        ocnt[osum >> 1]++;
        filterflip[i] = filter(i) ^ filter(i ^ 1);
        filterlut[i] = filter(i);
    }

    for(p = 0; p < 9; ++p)
        for(q = 0; q < 9; ++q)
            prob[8 * (4 * p + 4 * q - p * q)] +=  ecnt[p] * ocnt[q];
    for(i = 0; i < 257; ++i)
        prob[i] /= 1ull << 40;

    for(j = 0; j < 1 << 4; ++j)
        for(i = 0; i < 1 << 20; ++i) {
            hsum_off[0][halfsum[0][j << 16 | i >> 4] << 4 | halfsum[0][i]]++;
            hsum_off[1][halfsum[1][j << 16 | i >> 4] << 4 | halfsum[1][i]]++;
        }
}
#define filter(x) (filterlut[(x) & 0xfffff])
#define LF_POLY (0x8708040029CE5C)
#define ROR(x, n) ((x) >> (n) | (x) << (32 - (n)))
#define DIVIDE(s, p) ROR((unsigned)(((int)(s - (p) * 32)) / (int)(4 - (p))), 3)
#define FACTOR(s, p) ((s & 1) || ((p) == 4 ? s == 128 : DIVIDE(s, (p)) < 9))
/** getsum0
 * Calculate the sum property at time zero
 */
uint32_t  getsum0(uint64_t *nonce) {
    uint32_t unique[256] = {0};
    uint32_t i, numfound = 0 , sum = 0;

    for(i = 0; nonce[i] != -1 && numfound < 256; ++i)
        if(!unique[0xff & nonce[i]]) {
            sum += parity(0xff & nonce[i]) ^ BIT(nonce[i], 32);
            unique[0xff & nonce[i]] = 1;
            numfound++;
        }

    return numfound == 256 ? sum : -1;
}
/** eliminate
 * build initial sorted candidate list based on sumproperties
 */
uint32_t* eliminate(uint32_t sum0, uint32_t sum8, uint32_t isodd) {
    uint32_t y, yy, *set, p, r, *wrt[0x89] = {0}, *w, irr8 = sum8 >> 1 == 64;
    uint8_t *hsum = halfsum[isodd], i, irr0 = sum0 >> 1 == 64;
    set = w = malloc((sizeof(uint32_t) << 24) + 4);

    for(p = 0; p != 4 && !irr0; p = (p + 1) * 2 % 11)
        for(r = 0; r != 4; r = (r + 1) * 2 % 11)
            if(FACTOR(sum0, p) && FACTOR(sum8, r))
                w = (wrt[p << 4 | r] = w) + hsum_off[isodd][p << 4 | r];
    for(r = 0; r != 4 && irr0; r = (r + 1) * 2 % 11)
        for(p = 0; p != 4; p = (p + 1) * 2 % 11)
            if(FACTOR(sum0, p) && FACTOR(sum8, r))
                w = (wrt[p << 4 | r] = w) + hsum_off[isodd][p << 4 | r];

    for(p = 0; p != 4; p = (p + 1) * 2 % 11)
        if(FACTOR(sum0, p) && FACTOR(sum8, 4))
            w = (wrt[p << 4 | 4] = w) + hsum_off[isodd][p << 4 | 4];
    for(p = 0; p < 9; p = (p + 1) * 2 % 11)
        if(FACTOR(sum0, 4) && FACTOR(sum8, p))
            w = (wrt[64 | p] = w) + hsum_off[isodd][64 | p];

    for(y = 0; y < 1 << 20; ++y)
        for(yy = 0; yy < 1 << 4; ++yy)
            if(wrt[i = (p = hsum[yy << 16 | y >> 4]) << 4 | (r = hsum[y])]) {
                *wrt[i] = (irr0 ? p == 4 : p) << 28 | (irr8 ? r == 4 : r) << 24;
                *wrt[i]++ |= yy << 20 | y;
            }

    return *w = -1, set;
}

/** differential
 * prune more states using filter flips and differential analysis
 */
uint32_t differential(uint32_t *list, uint32_t isodd, uint8_t byte,
                      uint8_t bbyte, uint16_t bsum8, uint32_t flip) {
    uint32_t j, possible, k, invariant, i;
    uint32_t y, yprime, lsb, jdiv;
    uint32_t *read, *write, bit;
    uint8_t *hsum = halfsum[isodd];

    if(!flip && (bsum8 & 1)) return 0;

    for(i = 0; i < 8 && BIT(byte, i) == BIT(bbyte, i); ++i);
    k = (8 - i + !!isodd) >> 1;

    for(write = read = list; *read != -1; ++read){
        y = *read;
        yprime = *read & ~((1 << k) - 1);

        for(j = i, jdiv = k; j < 7 + !!isodd; ++j) {
            invariant = BIT(byte, j) ^ BIT(bbyte, j);
            invariant ^= BIT(y, 2 + jdiv) ^  BIT(yprime, 2 + jdiv);
            invariant ^= filter(y >> jdiv) ^ filter(yprime >> jdiv);

            if((j & 1) != !!isodd && invariant != 0) break;
            j += (j & 1) != !!isodd;
            jdiv--;

            bit = BIT(y, jdiv);
            bit ^= BIT(byte, j) ^ BIT(bbyte, j);
            bit ^= BIT(y, 3 + jdiv) ^ BIT(yprime, 3 + jdiv);
            bit ^= BIT(y, 4 + jdiv) ^ BIT(yprime, 4 + jdiv);

            yprime |= bit << jdiv;
        }

        for(lsb = possible = 0; lsb < 1 << jdiv; ++lsb){
            if(FACTOR(bsum8, hsum[0xfffff & (yprime | lsb)]))
            if((flip & 1) == 0 || filterflip[0xfffff & (yprime | lsb)])
            if((flip & 2) == 0 || filterflip[0xfffff & (yprime | lsb) >> 1])
            if((flip & 4) == 0 || filterflip[0xfffff & (yprime | lsb) >> 2])

            if((flip & 16) == 0 || !filterflip[0xfffff & (yprime | lsb)])
            if((flip & 32) == 0 || !filterflip[0xfffff & (yprime | lsb) >> 1])
            if((flip & 64) == 0 || !filterflip[0xfffff & (yprime | lsb) >> 2])

                possible = 1;
        }
        if(possible) *write++ = y;

    }
    *write = -1;

    return (uint32_t)(read - write);
}
/** binom
 * calculate the binomial coefficient
 */
static double binom(uint32_t n, uint32_t k) {
    double num = 1.0;
    uint32_t i, t = (n - k > k) ? n - k : k;

    if(k > n)
        return 0;
    for(i = t + 1; i <= n; ++i)
        num *= i;
    for(i = 2; i <= n - t; ++i)
        num /= i;

    return num;
}
/** predictsum
 * passable prediction logic based on hypergeometric distribution
 */
static uint32_t predictsum(uint64_t *nonces, uint8_t byte, uint32_t *conf) {
    uint32_t k, K, n, N = 256, bestK = 0, i;
    uint8_t seen[256] = {0}, nonceb1, nonceb2;
    double num, sum = 0.0, max = 0.0;

    for(i = k = n = 0; nonces[i] != -1; ++i){
        nonceb1 = nonces[i];
        nonceb2 = nonces[i] >> 8;
        if(nonceb1 == byte && !seen[nonceb2]) {
            seen[nonceb2] = 1;
            ++n;
            k += parity(nonceb2) ^ BIT(nonces[i], 40);
        }
    }

    for(K = 0; K <= 256; K += 1) {
        num = binom(K, k) * (binom(N - K, n - k) / binom(N, n));
        sum += num * prob[K];
        max = (num > max) ? bestK = K, num : max;
    }

    *conf = 100.0 * max * prob[bestK] / sum + 0.5;
    return bestK;
}


/** getpredictions
 * guess the sumproperty at time 8 for all possible first 8 bits
 */
uint32_t getpredictions(uint64_t *nonces, int tresh, uint32_t *pred) {
    uint32_t i, none = 1, conf, sum8;

    for(i = 0; i < 256; ++i){
        sum8 = predictsum(nonces, i, &conf);
        none &= pred[i] = (conf >= tresh) ? sum8 | conf << 16 : 129;
    }

    return !none;
}
/** bestb
 * poor heuristic to find reasonable base for differential analysis
 */
uint8_t bestb(uint32_t *pred) {
    uint32_t i, j, h, k;
    uint32_t max = 0;
    for(i = 0; i < 256; ++i) {
        if(pred[i] & 1) continue;

        for(j = 0, h = i; j < 256; ++j) {
            if(i == j || (pred[j] & 1)) continue;
            for(k = 0; k < 8 && BIT(i, k) == BIT(j, k); ++k);
            h += k << 8;
        }
        max = (h > max) ? h : max;
    }
    return max;
}
/** findflips
 * Detect some filter flip conditions
 */
uint32_t findflips(uint64_t *nonces, uint32_t *flips) {
    uint32_t parities[256] = {0};
    uint32_t i, status = 0;

    for(i = 0; nonces[i] != -1; ++i)
        parities[nonces[i] & 0xff] = BIT(nonces[i], 32);

    for(i = 0; i < 0x100; ++i){
        flips[i] = 0;

        flips[i] |= (parities[i] == parities[i ^ 0x80]) << 0;
        flips[i] |= (parities[i] == parities[i ^ 0x20]) << 1;
        flips[i] |= (parities[i] == parities[i ^ 0x08]) << 2;

        flips[i] |= (parities[i] == parities[i ^ 0x40]) << 8;
        flips[i] |= (parities[i] == parities[i ^ 0x10]) << 9;
        flips[i] |= (parities[i] == parities[i ^ 0x04]) << 10;

        status |= flips[i];
    }
    for(i = 0; i < 0x30; ++i) {
        flips[i] |= ((~flips[i] & 0x001) == 0x001) << 4;
        flips[i] |= ((~flips[i] & 0x101) == 0x101) << 12;
        flips[i] |= ((~flips[i] & 0x103) == 0x103) << 5;

        flips[i] |= ((~flips[i] & 0x303) == 0x303) << 13;
        flips[i] |= ((~flips[i] & 0x307) == 0x307) << 6;
        flips[i] |= ((~flips[i] & 0x707) == 0x707) << 14;
    }
    for(i = 0; i < 0x100; ++i){
        if(status & 1 << 0) flips[i] &= ~0x6066;
        if(status & 1 << 1) flips[i] &= ~0x4044;
        if(status & 1 << 8) flips[i] &= ~0x6640;
        if(status & 1 << 9) flips[i] &= ~0x4400;
        if((status & 7) == 7) flips[i] &= ~0x400;
    }

    return status;
}
static void __lfsr_rollback(uint64_t *s, uint32_t in) {
    uint32_t bit, i;
    uint64_t state = *s;

    for(i = 0; i < 8; ++i) {
        bit = state & 1;
        state = state >> 32 | (state & 0xffffff) << 31;
        bit ^= parity64(LF_POLY & state);
        bit ^= in >> (7 - i);
        bit ^= filter(state);
        state |= (uint64_t)bit << 55;
    }
    *s = state;
}
static uint8_t inline paritycheck(uint64_t *s, uint32_t in) {
    uint32_t feedin, i;
    uint8_t ret = in >> 8;

    for(i = 0; i < 8; ++i) {
        ret ^= feedin = filter(*s);
        feedin ^= parity64(LF_POLY & *s) ^ in >> i;

        *s = *s << 32 | (uint32_t)(*s >> 31);
        *s &= ~1ull;
        *s |= feedin & 1;
    }
    return ret ^ filter(*s);
}
#define FOR_EACH_BYTE(X) (X) && (X) && (X) && (X)
uint64_t brute(uint32_t **task) {
    uint32_t *oe = task[2], *p, i;
    uint64_t *e, *eb, *ee, savestate, state, o, key;

    eb = ee = malloc((1 << 20) +  sizeof(uint64_t) * (task[4] - task[3]));
    for(p = task[3]; p < task[4]; ++p) {
        *ee = (uint64_t)*p << 32;
        __lfsr_rollback(ee++, **task);
    }

    for(; task[1] < oe; ++task[1]) {
        o = *task[1];
        __lfsr_rollback(&o, 0);

        for(e = eb; e < ee; ++e) {
            state = savestate = o ^ *e;
            i = 0;
            p = task[0] + 10;
            while(FOR_EACH_BYTE(!paritycheck(&state, *p++))) {
                state = savestate;
                if(++i == 100) goto out;
            }
        }
    }
    free(eb);
    return -1;

out:
    free(eb);
    for(key = 0, i = 23; i < 24; --i)
        key = key << 2 | BIT(state, i ^ 3) << 1 | BIT(state, 32  | (i ^ 3));
    return key;
}
/** sumsplit
 * Split sorted list of candidates into ranges. Based on msb.
 */
void sumsplit(uint32_t *list, uint32_t **ranges, uint32_t sum0, uint32_t sum8) {
    uint32_t *last, p, i;

    ranges[*list >> 24] = list;
    for(last = list; *last != -1; ++last)
        if(!ranges[*last >> 24]) {
            ranges[*last >> 24] = last;
            ranges[256 | *(last - 1) >> 24] = last;
        }
    ranges[256 | *(last - 1) >> 24] = last;

    for(i = 0, p = 1; i < 16 && sum0 >> 1 == 64; i += p ^= 1)
        ranges[p << 8 | 0x20 | i] = ranges[p << 8 | 0x10 | i];
    for(i = 0; i < 32 && sum8 >> 1 == 64; ++i)
        ranges[i << 4 | 2] = ranges[i << 4 | 1];
    for(i = 0; i < 32 && (sum8 & 1); ++i)
        ranges[i << 4 | 3] = ranges[i << 4];
}
/** mkspace
 * split candidate lists into list of lists by matching halfsums
 */
uint32_t **mkspace(uint32_t *o, uint32_t *e, uint32_t sum0, uint32_t sum8) {
    uint32_t *ohead[512] = {0}, **otail = ohead + 256, p, q, r, s;
    uint32_t *ehead[512] = {0}, **etail = ehead + 256, **jobs, **j;

    sumsplit(o, ohead, sum0, sum8);
    sumsplit(e, ehead, sum0, sum8);

    j = 1024 + (jobs = malloc(sizeof(uint32_t*) << 14));
    *j++ = o;
    *j++ = e;

    for(p = 0; p != 4; p = (p + 1) * 2 % 11) {
        for(r = 0; r != 4; r = (r + 1) * 2 % 11) {
            q = (sum0 >> 1 == 64) ? !(p & 1) : DIVIDE(sum0, p);
            s = (sum8 >> 1 == 64) ? !(r & 1) : DIVIDE(sum8, r);
            if(q < 9 && s < 9 && ohead[p << 4 | r] && ehead[q << 4 | s]) {
                *j++ = (uint32_t*)jobs;
                *j++ = ohead[p << 4 | r];
                *j++ = otail[p << 4 | r];
                *j++ = ehead[q << 4 | s];
                *j++ = etail[q << 4 | s];
            }
        }
    }

    return *j = 0, jobs;
}
/** craptev1_get_space
 * Derive reduced search space from list of nested nonces.
 *  - returns a zero terminated list of partitions (5 pointers each)
 *    add 5 to the return value to get a pointer to the second partition.
 *  - uid is stored for use by search functions, it can be omitted.
 */
uint32_t** craptev1_get_space(uint64_t *nonces, uint32_t tresh, uint32_t uid) {
    uint32_t sum0, sum8, pred[256], haspred, flips[256];
    uint32_t *olist, *elist, i, **space, byte, *pre, b;
    uint64_t t;

    sum0 = getsum0(nonces);
    if(sum0 == -1) return 0;

    haspred = getpredictions(nonces, tresh, pred);
    byte = haspred ? bestb(pred): 0xa5;
    sum8 = pred[byte] & 0xffff;
    findflips(nonces, flips);

    olist = eliminate(sum0, sum8, 1);
    elist = eliminate(sum0, sum8, 0);

    for(i = 0; i < 256; ++i) {
        differential(olist, 1, byte, i, pred[i], flips[i] & 255);
        differential(elist, 0, byte, i, pred[i], flips[i] >> 8);
    }

    space = mkspace(olist, elist, sum0, sum8);

    pre = (uint32_t*)space;
    pre[0] = byte ^ uid >> 24;
    pre[1] = uid;
    for(i = 0, pre += 10; i < 400;)
        for(b = 24, t = *nonces++; b < 32; b -= 8, t >>= 8, ++i)
            pre[i] = parity((t ^ t >> 32) & 255) << 8 | ((t ^ uid >> b) & 255);

    return space + 1026;
}
/** craptev1_sizeof_space
 * Calculate the size of the search space
 */
uint64_t craptev1_sizeof_space(uint32_t **space) {
    uint64_t i, c = 0, o, e;

    for(i = 0; space[i]; i += 5) {
        o = space[i + 2] - space[i + 1];
		e = space[i + 4] - space[i + 3];
        c += o * e;
    }

    return c;
}
/** craptev1_destroy_space
 * Free all memory associated with a search space.
 */
void craptev1_destroy_space(uint32_t **space) {
    free(*--space);
    free(*--space);
    free(space - 1024);
}
/** craptev1_search_partition
 * Search one partition of the search space. Return key if found.
 */
uint64_t craptev1_search_partition(uint32_t **partition) {
    return brute(partition);
}
/** craptev1_search_space
 * Search entire search space.Return key if found.
 */
uint64_t craptev1_search_space(uint32_t **space) {
    uint64_t i, key = -1;

    for(i = 0; space[i] && key == -1; i += 5)
        key = brute(space + i);

    return key;
}
