//---------------------------------------------------------------------------
#ifndef crypto1H
#define crypto1H
//---------------------------------------------------------------------------
#include "crapto1.h"
#include <stdlib.h>

struct Crypto1State *crypto1_create(uint64_t key);

void crypto1_destroy(struct Crypto1State *state);

void crypto1_get_lfsr(struct Crypto1State *state, uint64_t *lfsr);

unsigned int crypto1_word(struct Crypto1State *s, unsigned int in_word, int fb);

unsigned int prng_successor(unsigned int x, unsigned int n);

#endif

