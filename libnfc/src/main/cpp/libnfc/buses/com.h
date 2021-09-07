#ifndef __NFC_BUS_UART_H__
#  define __NFC_BUS_UART_H__

#include <sys/time.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <jni.h>

bool c_open();

void c_close();

int c_read(uint8_t *pbtRx, size_t szRx, int timeout);

int c_write(const uint8_t *pbtTx, size_t szTx, int timeout);

#endif // __NFC_BUS_UART_H__
