//---------------------------------------------------------------------------

#ifndef crapto1H
#define crapto1H
//---------------------------------------------------------------------------
#include "crypto1.h"
#include <stdint.h>

struct Crypto1State {
    unsigned int odd, even;
};

struct Crypto1State *crypto1_create(uint64_t key);

void crypto1_destroy(struct Crypto1State *state);

void crypto1_get_lfsr(struct Crypto1State *state, uint64_t *lfsr);

unsigned int crypto1_word(struct Crypto1State *state, unsigned int in_word, int fb);

unsigned int prng_successor(unsigned int x, unsigned int n);

struct Crypto1State *lfsr_recovery(unsigned int ks2, unsigned int ks3);

struct Crypto1State *lfsr_recovery_borked(unsigned int ks2, unsigned int ks3);

void lfsr_rollback(struct Crypto1State *s, unsigned int in, int fb);

#define LF_POLY_ODD (0x29CE5C)
#define LF_POLY_EVEN (0x870804)
#define BIT(x, n) ((x) >> (n) & 1)
#define BEBIT(x, n) ((x) >> ((n) ^ 24) & 1)

static inline int parity(unsigned int x) {
    x ^= x >> 16;
    x ^= x >> 8;
    x ^= x >> 4;
    return BIT(0x6996, x & 0xf);
}

static inline int par64(uint64_t x) {
    return parity(x ^ x >> 32);
}

static inline int filter(unsigned int const x) {
    unsigned int f;

    f = 0xf22c0 >> (x & 0xf) & 16;
    f |= 0x6c9c0 >> (x >> 4 & 0xf) & 8;
    f |= 0x3c8b0 >> (x >> 8 & 0xf) & 4;
    f |= 0x1e458 >> (x >> 12 & 0xf) & 2;
    f |= 0x0d938 >> (x >> 16 & 0xf) & 1;
    return BIT(0xEC57E80A, f);
}

#endif

