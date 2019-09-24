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
#define _GNU_SOURCE

#include "craptev1.h"

#include <stdio.h>
#include <unistd.h>
#include <sched.h>
#include <signal.h>
#include <sys/mman.h>
#include <linux/futex.h>
#include <sys/syscall.h>
#include <sys/sysinfo.h>

uint32_t **job;
uint64_t origsize;

void progress(int sig) {
    uint64_t left = craptev1_sizeof_space(job);
    double p = (origsize - left) * 100.0 / origsize;

    printf("\x1b[2K\x1b[G""%.2f%% done", p);
    fflush(stdout);
    alarm(1);
}

void progress_init(uint32_t **space) {
    origsize = craptev1_sizeof_space(job = space);
    signal(SIGALRM, progress);
    alarm(1);
}

int active;

int tmain(void *task) {
    uint64_t key = craptev1_search_partition(task);
    if (key != -1) {
        alarm(0);
        printf("\nFOUND: %"PRIx64"\n", key);
        exit(1);
    }
    __sync_sub_and_fetch(&active, 1);
    syscall(__NR_futex, &active, FUTEX_WAKE, 1);
    syscall(__NR_exit, 0);
    return 0;
}

#define CLONE_FLAGS (CLONE_SIGHAND | CLONE_FS | CLONE_VM | CLONE_FILES | CLONE_THREAD | CLONE_SYSVSEM)

void multithread(uint32_t **space, int maxthread) {
    char *stack;
    int j;

    for (j = 0; space[j * 5]; ++j) {
        __sync_add_and_fetch(&active, 1);
        stack = mmap(0, 4096, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
        clone(tmain, stack + 4092, CLONE_FLAGS, space + j * 5);
        syscall(__NR_futex, &active, FUTEX_WAIT, maxthread, 0);
    }
    while ((j = active))
        syscall(__NR_futex, &active, FUTEX_WAIT, j, 0);
}

uint64_t *readnonces(char *fname) {
    int i, j, r;
    FILE *f = fopen(fname, "r");
    uint64_t *nonces = malloc(sizeof(uint64_t) << 24);
    uint32_t byte;
    char parities;

    for (i = 0; !feof(f); ++i) {
        for (j = nonces[i] = 0; j < 4; ++j) {
            r = fscanf(f, "%02x%c ", &byte, &parities);
            if (r != 2) {
                fprintf(stderr, "Input parse error pos:%ld\n", ftell(f));
                fflush(stderr);
                abort();
            }
            parities = (parities == '!') ^ parity(byte);
            nonces[i] |= byte << 8 * j;
            nonces[i] |= ((uint64_t) parities) << (32 + j * 8);
        }
    }
    nonces[i] = -1;
    fclose(f);
    return nonces;
}

void usage(char *exename) {
    printf("Usage:\n\t%s -f [filename] -u [uid] [-t treshold] [-n threads]\n\n", exename);
    _exit(0);
}

int main(int argc, char **argv) {
    uint64_t *nonces = 0, c;
    uint32_t **space, uid = 0, tresh = 95;
    int option, max_thread = sysconf(_SC_NPROCESSORS_CONF);

    while ((option = getopt(argc, argv, "f:u:n:t:")) != -1)
        switch (option) {
            case 'f':
                nonces = readnonces(optarg);
                break;
            case 'u':
                uid = strtoul(optarg, 0, 16);
                break;
            case 'n':
                max_thread = atoi(optarg);
                break;
            case 't':
                tresh = atoi(optarg);
                break;
            default:
                usage(argv[0]);
        }
    if (optind != argc || nonces == 0)
        usage(*argv);
    space = craptev1_get_space(nonces, tresh, uid);
    c = craptev1_sizeof_space(space);
    printf("Leftover complexity: %"PRIx64"\n", c);

    progress_init(space);
    multithread(space, max_thread);
    craptev1_destroy_space(space);

    return 0;
}
