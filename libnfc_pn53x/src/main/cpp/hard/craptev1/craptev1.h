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
#ifndef CRAPTEV1_INCLUDED
#define CRAPTEV1_INCLUDED

#include <stdlib.h>
#include <inttypes.h>

#ifdef __cplusplus
extern "C" {
#endif

void craptev1_init();
uint32_t** craptev1_get_space(uint64_t *nonces,  uint32_t tresh, uint32_t uid);
uint64_t craptev1_sizeof_space(uint32_t **space);
void craptev1_destroy_space(uint32_t **space);
uint64_t craptev1_search_partition(uint32_t **partition);
uint64_t craptev1_search_space(uint32_t **space);


#define parity(n) (__builtin_popcountl(n) & 1)
#define parity64(n) __builtin_popcountll(n)
#define BIT(x, n) ((x) >> (n) & 1)
static inline int filter(uint32_t const x) {
        uint32_t f;

        f  = 0xf22c0 >> (x       & 0xf) & 16;
        f |= 0x6c9c0 >> (x >>  4 & 0xf) &  8;
        f |= 0x3c8b0 >> (x >>  8 & 0xf) &  4;
        f |= 0x1e458 >> (x >> 12 & 0xf) &  2;
        f |= 0x0d938 >> (x >> 16 & 0xf) &  1;
        return BIT(0xEC57E80A, f);
}

#ifdef __cplusplus
}
#endif
#endif

