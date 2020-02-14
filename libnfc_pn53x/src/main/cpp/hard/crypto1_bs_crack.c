// Bit-sliced Crypto-1 brute-forcing implementation
// Builds on the data structures returned by CraptEV1 craptev1_get_space(nonces, threshold, uid)
/*
Copyright (c) 2015-2016 Aram Verstegen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

#include <stdlib.h>

#ifndef __APPLE__

#include <malloc.h>

#endif

#include "crypto1_bs_crack.h"

inline uint64_t crack_states_bitsliced(uint32_t **task) {
    // the idea to roll back the half-states before combining them was suggested/explained to me by bla
    // first we pre-bitslice all the even state bits and roll them back, then bitslice the odd bits and combine the two in the inner loop
    uint64_t key = -1;
#ifdef EXACT_COUNT
    size_t bucket_states_tested = 0;
    size_t bucket_size[(task[4] - task[3]) / MAX_BITSLICES];
#else
    const size_t bucket_states_tested = (task[4]-task[3])*(task[2]-task[1]);
#endif
    // bitslice all the even states
    bitslice_t *restrict bitsliced_even_states[(task[4] - task[3]) / MAX_BITSLICES];
    size_t bitsliced_blocks = 0;
    for (uint32_t const *restrict p_even = task[3]; p_even < task[4]; p_even += MAX_BITSLICES) {
#ifdef __WIN32
#ifdef __MINGW32__
        bitslice_t * restrict lstate_p = __mingw_aligned_malloc((STATE_SIZE+ROLLBACK_SIZE) * sizeof(bitslice_t), sizeof(bitslice_t));
#else
        bitslice_t * restrict lstate_p = _aligned_malloc((STATE_SIZE+ROLLBACK_SIZE) * sizeof(bitslice_t), sizeof(bitslice_t));
#endif
#else
#ifdef __APPLE__
        bitslice_t * restrict lstate_p = malloc((STATE_SIZE+ROLLBACK_SIZE) * sizeof(bitslice_t));
#else
        bitslice_t *restrict lstate_p = memalign(sizeof(bitslice_t),
                                                 (STATE_SIZE + ROLLBACK_SIZE) * sizeof(bitslice_t));
#endif
#endif
        memset(lstate_p, 0x0, (STATE_SIZE) * sizeof(bitslice_t));
        // bitslice even half-states
        const size_t max_slices =
                (task[4] - p_even) < MAX_BITSLICES ? task[4] - p_even : MAX_BITSLICES;
#ifdef EXACT_COUNT
        bucket_size[bitsliced_blocks] = max_slices;
#endif
        for (size_t slice_idx = 0; slice_idx < max_slices; ++slice_idx) {
            // set even bits
            uint32_t e = *(p_even + slice_idx);
            for (size_t bit_idx = 1; bit_idx < STATE_SIZE; bit_idx += 2, e >>= 1) {
                if (e & 1) {
                    lstate_p[bit_idx].bytes64[slice_idx >> 6] |= 1ull << (slice_idx & 63);
                }
            }
        }
        // compute the rollback bits
        for (size_t rollback = 0; rollback < ROLLBACK_SIZE; ++rollback) {
            // inlined crypto1_bs_lfsr_rollback
            const bitslice_value_t feedout = lstate_p[0].value;
            ++lstate_p;
            const bitslice_value_t ks_bits = crypto1_bs_f20(lstate_p);
            const bitslice_value_t feedback = (feedout ^ ks_bits ^ lstate_p[47 - 5].value ^
                                               lstate_p[47 - 9].value ^
                                               lstate_p[47 - 10].value ^ lstate_p[47 - 12].value ^
                                               lstate_p[47 - 14].value ^
                                               lstate_p[47 - 15].value ^ lstate_p[47 - 17].value ^
                                               lstate_p[47 - 19].value ^
                                               lstate_p[47 - 24].value ^ lstate_p[47 - 25].value ^
                                               lstate_p[47 - 27].value ^
                                               lstate_p[47 - 29].value ^ lstate_p[47 - 35].value ^
                                               lstate_p[47 - 39].value ^
                                               lstate_p[47 - 41].value ^ lstate_p[47 - 42].value ^
                                               lstate_p[47 - 43].value);
            lstate_p[47].value = feedback ^ bitsliced_rollback_byte[rollback].value;
        }
        bitsliced_even_states[bitsliced_blocks++] = lstate_p;
    }
    // bitslice every odd state to every block of even half-states with half-finished rollback
    for (uint32_t const *restrict p_odd = task[1]; p_odd < task[2]; ++p_odd) {
        // early abort
        if (keys_found) {
            goto out;
        }

        // set the odd bits and compute rollback
        uint64_t o = (uint64_t) *p_odd;
        lfsr_rollback_byte(&o, 0, 1);
        // pre-compute part of the odd feedback bits (minus rollback)
        bool odd_feedback_bit = parity(o & 0x9ce5c);

        crypto1_bs_rewind_a0();
        // set odd bits
        for (size_t state_idx = 0;
             state_idx < (STATE_SIZE - ROLLBACK_SIZE); o >>= 1, state_idx += 2) {
            if (o & 1) {
                state_p[state_idx] = bs_ones;
            } else {
                state_p[state_idx] = bs_zeroes;
            }
        }
        const bitslice_value_t odd_feedback = odd_feedback_bit ? bs_ones.value : bs_zeroes.value;

        // set even and rollback bits
        for (size_t block_idx = 0; block_idx < bitsliced_blocks; ++block_idx) {
            const bitslice_t *const restrict bitsliced_even_state = bitsliced_even_states[block_idx];
            size_t state_idx;
            // set even bits
            for (state_idx = 0; state_idx < (STATE_SIZE - ROLLBACK_SIZE); state_idx += 2) {
                state_p[1 + state_idx] = bitsliced_even_state[1 + state_idx];
            }
            // set rollback bits
            uint64_t lo = o;
            for (; state_idx < STATE_SIZE; lo >>= 1, state_idx += 2) {
                // set the odd bits and take in the odd rollback bits from the even states
                if (lo & 1) {
                    state_p[state_idx].value = ~bitsliced_even_state[state_idx].value;
                } else {
                    state_p[state_idx] = bitsliced_even_state[state_idx];
                }

                // set the even bits and take in the even rollback bits from the odd states
                if ((lo >> 32) & 1) {
                    state_p[1 + state_idx].value = ~bitsliced_even_state[1 + state_idx].value;
                } else {
                    state_p[1 + state_idx] = bitsliced_even_state[1 + state_idx];
                }
            }

#ifdef EXACT_COUNT
            // Fix a "1000000% bug". Looks like here is a problem with OS X gcc
            size_t current_bucket_size =
                    bucket_size[block_idx] > MAX_BITSLICES ? MAX_BITSLICES : bucket_size[block_idx];

            bucket_states_tested += current_bucket_size;
#ifdef ONLINE_COUNT
            __atomic_fetch_add(&total_states_tested, current_bucket_size, __ATOMIC_RELAXED);
#endif
#else
#ifdef ONLINE_COUNT
            __atomic_fetch_add(&total_states_tested, MAX_BITSLICES, __ATOMIC_RELAXED);
#endif
#endif
            // pre-compute first keystream and feedback bit vectors
            const bitslice_value_t ksb = crypto1_bs_f20(state_p);
            const bitslice_value_t fbb = (odd_feedback ^ state_p[47 - 0].value ^
                                          state_p[47 - 5].value ^
                                          // take in the even and rollback bits
                                          state_p[47 - 10].value ^ state_p[47 - 12].value ^
                                          state_p[47 - 14].value ^
                                          state_p[47 - 24].value ^ state_p[47 - 42].value);

            // vector to contain test results (1 = passed, 0 = failed)
            bitslice_t results = bs_ones;

            for (size_t tests = 0; tests < NONCE_TESTS; ++tests) {
                size_t parity_bit_idx = 0;
                bitslice_value_t fb_bits = fbb;
                bitslice_value_t ks_bits = ksb;
                state_p = &states[KEYSTREAM_SIZE - 1];
                bitslice_value_t parity_bit_vector = bs_zeroes.value;

                // highest bit is transmitted/received first
                for (int32_t ks_idx = KEYSTREAM_SIZE - 1; ks_idx >= 0; --ks_idx, --state_p) {
                    // decrypt nonce bits
                    const bitslice_value_t encrypted_nonce_bit_vector = bitsliced_encrypted_nonces[tests][ks_idx].value;
                    const bitslice_value_t decrypted_nonce_bit_vector = (
                            encrypted_nonce_bit_vector ^ ks_bits);

                    // compute real parity bits on the fly
                    parity_bit_vector ^= decrypted_nonce_bit_vector;

                    // update state
                    state_p[0].value = (fb_bits ^ decrypted_nonce_bit_vector);

                    // compute next keystream bit
                    ks_bits = crypto1_bs_f20(state_p);

                    // for each byte:
                    if ((ks_idx & 7) == 0) {
                        // get encrypted parity bits
                        const bitslice_value_t encrypted_parity_bit_vector = bitsliced_encrypted_parity_bits[tests][parity_bit_idx++].value;

                        // decrypt parity bits
                        const bitslice_value_t decrypted_parity_bit_vector = (
                                encrypted_parity_bit_vector ^ ks_bits);

                        // compare actual parity bits with decrypted parity bits and take count in results vector
                        results.value &= (parity_bit_vector ^ decrypted_parity_bit_vector);

                        // make sure we still have a match in our set
                        // if(memcmp(&results, &bs_zeroes, sizeof(bitslice_t)) == 0){

                        // this is much faster on my gcc, because somehow a memcmp needlessly spills/fills all the xmm registers to/from the stack - ???
                        // the short-circuiting also helps
                        if (results.bytes64[0] == 0
#if MAX_BITSLICES > 64
                            && results.bytes64[1] == 0
#endif
#if MAX_BITSLICES > 128
                            && results.bytes64[2] == 0
                            && results.bytes64[3] == 0
#endif
                                ) {
                            goto stop_tests;
                        }
                        // this is about as fast but less portable (requires -std=gnu99)
                        // asm goto ("ptest %1, %0\n\t"
                        //           "jz %l2" :: "xm" (results.value), "xm" (bs_ones.value) : "cc" : stop_tests);
                        parity_bit_vector = bs_zeroes.value;
                    }
                    // compute next feedback bit vector
                    fb_bits = (state_p[47 - 0].value ^ state_p[47 - 5].value ^
                               state_p[47 - 9].value ^
                               state_p[47 - 10].value ^ state_p[47 - 12].value ^
                               state_p[47 - 14].value ^
                               state_p[47 - 15].value ^ state_p[47 - 17].value ^
                               state_p[47 - 19].value ^
                               state_p[47 - 24].value ^ state_p[47 - 25].value ^
                               state_p[47 - 27].value ^
                               state_p[47 - 29].value ^ state_p[47 - 35].value ^
                               state_p[47 - 39].value ^
                               state_p[47 - 41].value ^ state_p[47 - 42].value ^
                               state_p[47 - 43].value);
                }
            }
            // all nonce tests were successful: we've found the key in this block!
            state_t keys[MAX_BITSLICES];
            crypto1_bs_convert_states(&states[KEYSTREAM_SIZE], keys);
            for (size_t results_idx = 0; results_idx < MAX_BITSLICES; ++results_idx) {
                if (get_vector_bit(results_idx, results)) {
                    key = keys[results_idx].value;
                    __atomic_fetch_add(&keys_found, 1, __ATOMIC_RELAXED);
                    goto out;
                }
            }
            stop_tests:
            // prepare to set new states
            crypto1_bs_rewind_a0();
            continue;
        }
    }
    out:
    for (size_t block_idx = 0; block_idx < bitsliced_blocks; ++block_idx) {
#ifdef __WIN32
#ifdef __MINGW32__
        __mingw_aligned_free(bitsliced_even_states[block_idx]-ROLLBACK_SIZE);
#else
        _aligned_free(bitsliced_even_states[block_idx]-ROLLBACK_SIZE);
#endif
#else
        free(bitsliced_even_states[block_idx] - ROLLBACK_SIZE);
#endif
    }
#ifndef ONLINE_COUNT
    __atomic_fetch_add(&total_states_tested, bucket_states_tested, __ATOMIC_RELAXED);
#endif
    return key;
}
