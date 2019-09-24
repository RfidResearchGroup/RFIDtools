#ifndef _CRYPTO1_BS_CRACK_H
#define _CRYPTO1_BS_CRACK_H
#include <stdint.h>
#include "crypto1_bs.h"
#include "craptev1.h"
uint64_t crack_states_bitsliced(uint32_t **task);
size_t keys_found;
uint64_t total_states_tested;
uint64_t total_states;

// linked from crapto1.c file
extern uint8_t lfsr_rollback_byte(uint64_t* s, uint32_t in, int fb);

#define ONLINE_COUNT
#define EXACT_COUNT

// arrays of bitsliced states with identical values in all slices
bitslice_t bitsliced_encrypted_nonces[NONCE_TESTS][STATE_SIZE];
bitslice_t bitsliced_encrypted_parity_bits[NONCE_TESTS][STATE_SIZE];
bitslice_t bitsliced_rollback_byte[ROLLBACK_SIZE];

#endif // _CRYPTO1_BS_CRACK_H
