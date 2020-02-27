#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include "craptev1.h"
#include "crypto1_bs.h"
#include "crypto1_bs_crack.h"
#include <inttypes.h>
#include <math.h>
#define __STDC_FORMAT_MACROS
#define llx PRIx64
#define lli PRIi64
#define llu PRIu64
#define lu PRIu32
#define VT100_cleareol "\r\33[2K"

uint32_t **space;
uint8_t thread_count = 1;

uint64_t *readnonces(char* fname) {
    int i, j;
    FILE *f = fopen(fname, "r");
    if (f == NULL) {
        fprintf(stderr, "Cannot open file.\n");
        exit(EXIT_FAILURE);
    }
    uint64_t *nonces = malloc(sizeof (uint64_t) <<  24);
    uint32_t nt;
    char par;

    i = 0;
    while(!feof(f)){
        nonces[i] = 0;
        for(j = 0; j < 32; j += 8) {
            if(2 != fscanf(f, "%02x%c ", &nt, &par)) {
                fprintf(stderr, "Input format error at line:%d\n", i);
                fflush(stderr);
                exit(EXIT_FAILURE);
            }
            nonces[i] |= nt << j | (uint64_t)((par == '!') ^ parity(nt)) << (32 + j);
        }
        i++;
    }
    nonces[i] = -1;
    fclose(f);
    return nonces;
}

void* crack_states_thread(void* x){
    const size_t thread_id = (size_t)x;
    int j;
    for(j = thread_id; space[j * 5]; j += thread_count) {
        const uint64_t key = crack_states_bitsliced(space + j * 5);
        if(key != -1){
            printf("Found key: %012"llx"\n", key);
            break;
        } else if(keys_found){
            break;
        }
    }
    return NULL;
}

void notify_status_offline(int sig){
    printf(VT100_cleareol "Cracking... %6.02f%%", (100.0*total_states_tested/(total_states)));
    alarm(1);
    fflush(stdout);
    signal(SIGALRM, notify_status_offline);
}

int main(int argc, char* argv[]){
    if(argc != 3){
        printf("Usage: %s <nonces.txt> <uid>\n", argv[0]);
        return -1;
    }
    printf("Reading nonces...\n");
    uint64_t *nonces = readnonces(argv[1]);
    uint32_t uid = strtoul(argv[2], NULL, 16);
    printf("Deriving search space...\n");
    space = craptev1_get_space(nonces, 95, uid);
    total_states = craptev1_sizeof_space(space);

#ifndef __WIN32
    thread_count = sysconf(_SC_NPROCESSORS_CONF);
#else
    thread_count = 1;
#endif
    // append some zeroes to the end of the space to make sure threads don't go off into the wild
    size_t j = 0;
    for(j = 0; space[j]; j+=5){
    }
    size_t fill = j + (5*thread_count);
    for(; j < fill; j++) {
        space[j] = 0;
    }
    pthread_t threads[thread_count];
    size_t i;

    printf("Initializing BS crypto-1\n");
    crypto1_bs_init();
    printf("Using %u-bit bitslices\n", MAX_BITSLICES);

    uint8_t rollback_byte = **space;
    printf("Bitslicing rollback byte: %02x...\n", rollback_byte);
    // convert to 32 bit little-endian
    crypto1_bs_bitslice_value32(rev32((rollback_byte)), bitsliced_rollback_byte, 8);

    printf("Bitslicing nonces...\n");
    for(size_t tests = 0; tests < NONCE_TESTS; tests++){
        // pre-xor the uid into the decrypted nonces, and also pre-xor the uid parity into the encrypted parity bits - otherwise an exta xor is required in the decryption routine
        uint32_t test_nonce = uid^rev32(nonces[tests]);
        uint32_t test_parity = (nonces[tests]>>32)^rev32(uid);
        test_parity = ((parity(test_parity >> 24 & 0xff) & 1) | (parity(test_parity>>16 & 0xff) & 1)<<1 | (parity(test_parity>>8 & 0xff) & 1)<<2 | (parity(test_parity & 0xff) & 1) << 3);
        crypto1_bs_bitslice_value32(test_nonce, bitsliced_encrypted_nonces[tests], 32);
        // convert to 32 bit little-endian
        crypto1_bs_bitslice_value32(~(test_parity)<<24, bitsliced_encrypted_parity_bits[tests], 4);
    }

    total_states_tested = 0;
    keys_found = 0;

    printf("Starting %u threads to test %"llu" (~2^%0.2f) states\n", thread_count, total_states, log(total_states) / log(2));

    signal(SIGALRM, notify_status_offline);
    alarm(1);

    for(i = 0; i < thread_count; i++){
        pthread_create(&threads[i], NULL, crack_states_thread, (void*) i);
    }
    for(i = 0; i < thread_count; i++){
        pthread_join(threads[i], 0);
    }

    alarm(0);

    printf("\nTested %"llu" states\n", total_states_tested);

    if(!keys_found) fprintf(stderr, "No solution found :(\n");

    craptev1_destroy_space(space);
    return 0;
}

