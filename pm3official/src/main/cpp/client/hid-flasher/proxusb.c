//-----------------------------------------------------------------------------
// Copyright (C) 2009 Michael Gernoth <michael at gernoth.net>
// Copyright (C) 2010 iZsh <izsh at fail0verflow.com>
//
// This code is licensed to you under the terms of the GNU GPL, version 2 or,
// at your option, any later version. See the LICENSE.txt file for the text of
// the license.
//-----------------------------------------------------------------------------
// USB utilities
//-----------------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <unistd.h>
#include <strings.h>
#include <errno.h>

#include "proxusb.h"
#include "proxmark3.h"
#include "usb_cmd.h"
#include "com.h"
#include "../../../libnfc/buses/com.h"

// It seems to be missing for mingw
#ifndef ETIMEDOUT
#define ETIMEDOUT 116
#endif

static unsigned int claimed_iface = 0;
unsigned char return_on_error = 0;
unsigned char error_occured = 0;

void SendCommand(UsbCommand *c) {
    int ret;

#if 0
    printf("Sending %d bytes\n", sizeof(UsbCommand));
#endif
    ret = c_write((const uint8_t *) c, sizeof(UsbCommand), 1000);
    if (ret < 0) {
        error_occured = 1;
        if (return_on_error)
            return;
        fprintf(stderr, "write failed: Trying to reopen devices...\n");
        while (!OpenProxmark(0)) { sleep(1); }
        printf(PROXPROMPT);
        fflush(NULL);
        return;
    }
}

bool ReceiveCommandPoll(UsbCommand *c) {
    int ret;

    memset(c, 0, sizeof(UsbCommand));
    ret = c_read((uint8_t *) c, sizeof(UsbCommand), 500);
    if (ret < 0) {
        if (ret != -ETIMEDOUT) {
            error_occured = 1;
            if (return_on_error)
                return false;
            fprintf(stderr, "read failed: Trying to reopen devices...\n", ret);
            while (!OpenProxmark(0)) { sleep(1); }
            printf(PROXPROMPT);
            fflush(NULL);
            return false;
        }
    } else {
        if (ret && (ret < sizeof(UsbCommand))) {
            fprintf(stderr, "Read only %d instead of requested %d bytes!\n",
                    ret, (int) sizeof(UsbCommand));
        }
    }
    return ret > 0;
}

void ReceiveCommand(UsbCommand *c) {
//  printf("%s()\n", __FUNCTION__);
    int retval = 0;
    do {
        retval = ReceiveCommandPoll(c);
        if (retval != 1) printf("ReceiveCommandPoll returned %d\n", retval);
    } while (retval < 0);
//  printf("recv %x\n", c->cmd);
}

bool findProxmark(int verbose, unsigned int *iface) {
    return true;
}

bool OpenProxmark(int verbose) {
    bool devFind;
    unsigned int iface;

    devFind = findProxmark(verbose, &iface);
    if (!devFind)
        return NULL;

    return true;
}

void CloseProxmark() {
    printf("\n空操作，暂时当作关闭成功!\n");
}
