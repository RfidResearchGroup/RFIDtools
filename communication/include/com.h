#ifndef __NFC_BUS_UART_H__
#  define __NFC_BUS_UART_H__

#include <sys/time.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>

// can't use, he always is null!
typedef void *serial_port;

jint initBuffer(JNIEnv *env, jclass type, jobject send_buffer, jobject recv_buffer);

jint syncLength(JNIEnv *env, jclass type);

jint syncTimeout(JNIEnv *env, jclass type);

int c_open();

int c_close();

int c_flush();

int c_read(uint8_t *pbtRx, const size_t szRx, int timeout);

int c_write(const uint8_t *pbtTx, const size_t szTx, int timeout);

#endif // __NFC_BUS_UART_H__
