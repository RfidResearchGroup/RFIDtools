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

uint64_t split(uint8_t p){
    return (((p & 0x8) >>3 )| ((p & 0x4) >> 2) << 8 | ((p & 0x2) >> 1) << 16 | (p & 0x1) << 24 );
}

uint32_t uid;
uint64_t *readnonces(char* fname){
    int i;
    FILE *f = fopen(fname, "rb");
    if (f == NULL) {
        fprintf(stderr, "Cannot open file.\n");
        exit(EXIT_FAILURE);
    }
    uint64_t *nonces = malloc(sizeof (uint64_t) <<  24);
    if(fread(&uid, 1, 4, f)){
        uid = rev32(uid);
    }
    fseek(f, 6, SEEK_SET);
    i = 0;
    while(!feof(f)){
        uint32_t nt_enc1, nt_enc2;
        uint8_t par_enc;
        if(fread(&nt_enc1, 1, 4, f) && fread(&nt_enc2, 1, 4, f) && fread(&par_enc, 1, 1, f)){
            nonces[i  ] = split(~(par_enc >>   4)) << 32 | nt_enc1;
            nonces[i+1] = split(~(par_enc & 0xff)) << 32 | nt_enc2;
            i += 2;
        }
    }
    nonces[i] = -1;
    fclose(f);
    return nonces;
}

uint32_t **space;
uint8_t thread_count = 1;

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
    if(argc != 2){
        printf("Usage: %s <nonces.bin>\n", argv[0]);
        return -1;
    }
    printf("Reading nonces...\n");
    uint64_t *nonces = readnonces(argv[1]);
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
        test_parity = ((parity(test_parity >> 24 & 0xff) & 1) | (parity(test_parity>>16 & 0xff) & 1)<<1 | (parity(test_parity>>8 & 0xff) & 1)<<2 | (parity(test_parity &0xff) & 1) << 3);
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

    printf("Tested %"llu" states\n", total_states_tested);

    craptev1_destroy_space(space);
    return 0;
}


