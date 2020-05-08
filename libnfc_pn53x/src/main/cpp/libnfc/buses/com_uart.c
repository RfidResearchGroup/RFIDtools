#include <uart.h>
#include <nfc/nfc.h>
#include <tools.h>
#include "com.h"

// Serial port that we are communicating with the PM3 on.
static serial_port sp = INVALID_SERIAL_PORT;

bool c_open() {
    if (sp != INVALID_SERIAL_PORT) uart_close(sp);
    sp = uart_open("socket:DXL.COM.ASL", 115200);
    return sp != INVALID_SERIAL_PORT;
}

void c_close() {
    if (sp != INVALID_SERIAL_PORT) {
        uart_close(sp);
        sp = INVALID_SERIAL_PORT;
    }
}

int c_read(uint8_t *pbtRx, size_t szRx, int timeout) {
    if (sp != INVALID_SERIAL_PORT) {
        size_t recvLen = 0;
        size_t *pRecvLen = &recvLen;
        if (uart_receive(sp, pbtRx, szRx, pRecvLen) == NFC_SUCCESS) {
            return recvLen;
        }
    }
    sp = INVALID_SERIAL_PORT;
    return -1;
}

int c_write(const uint8_t *pbtTx, size_t szTx, int timeout) {
    if (sp != INVALID_SERIAL_PORT) {
        if (uart_send(sp, pbtTx, szTx) == NFC_SUCCESS) {
            return szTx;
        }
    }
    sp = INVALID_SERIAL_PORT;
    return -1;
}