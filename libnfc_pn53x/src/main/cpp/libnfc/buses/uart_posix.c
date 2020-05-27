/*
 * Generic uart / rs232/ serial port library
 *
 * Copyright (c) 2013, Roel Verdult
 * Copyright (c) 2018 Google
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @file uart_posix.c
 *
 * This version of the library has functionality removed which was not used by
 * proxmark3 project.
 */

// Test if we are dealing with posix operating systems
#ifndef _WIN32
#define _DEFAULT_SOURCE

#include "uart.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <fcntl.h>
#include <netinet/tcp.h>
#include <netdb.h>
#include <tools.h>
#include <nfc/nfc.h>
#include "sys/socket.h"
#include "sys/un.h"

// Taken from https://github.com/unbit/uwsgi/commit/b608eb1772641d525bfde268fe9d6d8d0d5efde7
#ifndef SOL_TCP
# define SOL_TCP IPPROTO_TCP
#endif

typedef struct termios term_info;
typedef struct {
    int fd;           // Serial port file descriptor
    term_info tiOld;  // Terminal info before using the port
    term_info tiNew;  // Terminal info during the transaction
} serial_port_unix;

#define UART_FPC_CLIENT_RX_TIMEOUT_MS 200
#define UART_TCP_CLIENT_RX_TIMEOUT_MS 500

uint32_t newtimeout_value = 0;
bool newtimeout_pending = false;

int uart_reconfigure_timeouts(uint32_t value) {
    newtimeout_pending = true;
    newtimeout_value = value;
    return NFC_SUCCESS;
}

serial_port uart_open(const char *pcPortName, uint32_t speed) {
    serial_port_unix *sp = calloc(sizeof(serial_port_unix), sizeof(uint8_t));
    if (sp == 0) return INVALID_SERIAL_PORT;

    if (memcmp(pcPortName, "tcp:", 4) == 0) {
        struct addrinfo *addr = NULL, *rp;
        char *addrstr = strdup(pcPortName + 4);

        if (addrstr == NULL) {
            printf("Error: strdup\n");
            free(sp);
            return INVALID_SERIAL_PORT;
        }

        char *colon = strrchr(addrstr, ':');
        const char *portstr;
        if (colon) {
            portstr = colon + 1;
            *colon = '\0';
        } else {
            portstr = "7901";
        }

        struct addrinfo info;

        memset(&info, 0, sizeof(info));

        info.ai_socktype = SOCK_STREAM;

        int s = getaddrinfo(addrstr, portstr, &info, &addr);
        if (s != 0) {
            printf("Error: getaddrinfo: %s\n", gai_strerror(s));
            freeaddrinfo(addr);
            free(addrstr);
            free(sp);
            return INVALID_SERIAL_PORT;
        }

        int sfd;
        for (rp = addr; rp != NULL; rp = rp->ai_next) {
            sfd = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);

            if (sfd == -1)
                continue;

            if (connect(sfd, rp->ai_addr, rp->ai_addrlen) != -1)
                break;

            close(sfd);
        }

        if (rp == NULL) {               /* No address succeeded */
            printf("Error: Could not connect\n");
            freeaddrinfo(addr);
            free(addrstr);
            free(sp);
            return INVALID_SERIAL_PORT;
        }

        freeaddrinfo(addr);
        free(addrstr);

        sp->fd = sfd;

        int one = 1;
        int res = setsockopt(sp->fd, SOL_TCP, TCP_NODELAY, &one, sizeof(one));
        if (res != 0) {
            free(sp);
            return INVALID_SERIAL_PORT;
        }
        return sp;
    }

    // The socket for abstract namespace implement.
    // Is local socket buffer, not a TCP or any net connection!
    // so, you can't connect with address like: 127.0.0.1, or any IP
    // see http://man7.org/linux/man-pages/man7/unix.7.html
    if (memcmp(pcPortName, "socket:", 7) == 0) {
        if (strlen(pcPortName) <= 7) {
            free(sp);
            return INVALID_SERIAL_PORT;
        }

        size_t servernameLen = (strlen(pcPortName) - 7) + 1;
        char serverNameBuf[servernameLen];
        memset(serverNameBuf, '\0', servernameLen);
        for (int i = 7, j = 0; j < servernameLen; ++i, ++j) {
            serverNameBuf[j] = pcPortName[i];
        }
        serverNameBuf[servernameLen - 1] = '\0';

        int localsocket, len;
        struct sockaddr_un remote;

        remote.sun_path[0] = '\0';  // abstract namespace
        strcpy(remote.sun_path + 1, serverNameBuf);
        remote.sun_family = AF_LOCAL;
        int nameLen = strlen(serverNameBuf);
        len = 1 + nameLen + offsetof(struct sockaddr_un, sun_path);

        if ((localsocket = socket(PF_LOCAL, SOCK_STREAM, 0)) == -1) {
            free(sp);
            return INVALID_SERIAL_PORT;
        }

        if (connect(localsocket, (struct sockaddr *) &remote, len) == -1) {
            free(sp);
            LOGD("连接套接字失败，请检查连接服务是否已经被停止!");
            return INVALID_SERIAL_PORT;
        }

        sp->fd = localsocket;

        return sp;
    }

    sp->fd = open(pcPortName, O_RDWR | O_NOCTTY | O_NDELAY | O_NONBLOCK);
    if (sp->fd == -1) {
        uart_close(sp);
        return INVALID_SERIAL_PORT;
    }

    // Finally figured out a way to claim a serial port interface under unix
    // We just try to set a (advisory) lock on the file descriptor
    struct flock fl;
    fl.l_type = F_WRLCK;
    fl.l_whence = SEEK_SET;
    fl.l_start = 0;
    fl.l_len = 0;
    fl.l_pid = getpid();

    // Does the system allows us to place a lock on this file descriptor
    if (fcntl(sp->fd, F_SETLK, &fl) == -1) {
        // A conflicting lock is held by another process
        free(sp);
        return CLAIMED_SERIAL_PORT;
    }

    // Try to retrieve the old (current) terminal info struct
    if (tcgetattr(sp->fd, &sp->tiOld) == -1) {
        uart_close(sp);
        return INVALID_SERIAL_PORT;
    }

    // Duplicate the (old) terminal info struct
    sp->tiNew = sp->tiOld;

    // Configure the serial port
    sp->tiNew.c_cflag = CS8 | CLOCAL | CREAD;
    sp->tiNew.c_iflag = IGNPAR;
    sp->tiNew.c_oflag = 0;
    sp->tiNew.c_lflag = 0;

    // Block until n bytes are received
    sp->tiNew.c_cc[VMIN] = 0;
    // Block until a timer expires (n * 100 mSec.)
    sp->tiNew.c_cc[VTIME] = 0;

    // Try to set the new terminal info struct
    if (tcsetattr(sp->fd, TCSANOW, &sp->tiNew) == -1) {
        uart_close(sp);
        return INVALID_SERIAL_PORT;
    }

    // Flush all lingering data that may exist
    tcflush(sp->fd, TCIOFLUSH);

    if (!uart_set_speed(sp, speed)) {
        // try fallback automatically
        speed = 115200;
        if (!uart_set_speed(sp, speed)) {
            uart_close(sp);
            printf("[!] UART error while setting baudrate\n");
            return INVALID_SERIAL_PORT;
        }
    }
    return sp;
}

void uart_close(const serial_port sp) {
    serial_port_unix *spu = (serial_port_unix *) sp;
    tcflush(spu->fd, TCIOFLUSH);
    tcsetattr(spu->fd, TCSANOW, &(spu->tiOld));
    struct flock fl;
    fl.l_type = F_UNLCK;
    fl.l_whence = SEEK_SET;
    fl.l_start = 0;
    fl.l_len = 0;
    fl.l_pid = getpid();

    // Does the system allows us to place a lock on this file descriptor
    int err = fcntl(spu->fd, F_SETLK, &fl);
    if (err == -1) {
        //silent error message as it can be called from uart_open failing modes, e.g. when waiting for port to appear
        //printf("[!] UART error while closing port\n");
    }
    close(spu->fd);
    free(sp);
}

int uart_receive(const serial_port sp, uint8_t *pbtRx, uint32_t pszMaxRxLen, uint32_t *pszRxLen) {
    uint32_t byteCount;  // FIONREAD returns size on 32b
    fd_set rfds;

    // Reset the output count
    *pszRxLen = 0;
    do {
        // Reset file descriptor
        FD_ZERO(&rfds);
        FD_SET(((serial_port_unix *) sp)->fd, &rfds);

        // int res = select(((serial_port_unix *) sp)->fd + 1, &rfds, NULL, NULL, NULL);
        struct timeval timeval = {.tv_sec  = 3};
        int res = select(((serial_port_unix *) sp)->fd + 1, &rfds, NULL, NULL, &timeval);

        // Read error
        if (res < 0) {
            LOGD("Read error");
            return NFC_EIO;
        }

        // Read time-out
        if (res == 0) {
            if (*pszRxLen == 0) {
                // We received no data
                LOGD("We received no data");
                return NFC_ETIMEOUT;
            } else {
                // We received some data, but nothing more is available
                // LOGD("We received some data, but nothing more is available");
                return NFC_SUCCESS;
            }
        }

        LOGD("Has data can received");

        // Retrieve the count of the incoming bytes
        res = ioctl(((serial_port_unix *) sp)->fd, FIONREAD, &byteCount);
//        printf("UART:: RX ioctl res %d byteCount %u\n", res, byteCount);
        if (res < 0) return NFC_EIO;

        // Cap the number of bytes, so we don't overrun the buffer
        if (pszMaxRxLen - (*pszRxLen) < byteCount) {
//            printf("UART:: RX prevent overrun (have %u, need %u)\n", pszMaxRxLen - (*pszRxLen), byteCount);
            byteCount = pszMaxRxLen - (*pszRxLen);
        }

        // There is something available, read the data
        res = read(((serial_port_unix *) sp)->fd, pbtRx + (*pszRxLen), byteCount);

        // Stop if the OS has some troubles reading the data
        if (res <= 0) {
            return NFC_EIO;
        } else if (res > 0 && pszMaxRxLen >= 255) { // 只有USB通信下才有大于255个字节的通信请求
            *pszRxLen += res;
            /*
             * TODO Look me!
             *
             * This UART implementation is copied from the PM3 open source library.
             * The communication process of PM3 is very different from the implementation of libnfc
             * (libnfc always requires about 265 bytes of data).
             * If we keep blocking and waiting, the program will be very slow,
             * so we need to return the data directly without waiting for enough 255 bytes.
             *
             * 这个UART实现是从PM3的开源库里拷贝过来的。
             * PM3的通信过程跟LIBNFC的实现有非常大的区别（LIBNFC某些驱动实现总是要求255个字节左右的数据），
             * 如果我们一直堵塞等待，将会导致程序非常缓慢，因此我们需要将数据直接返回，
             * 而不等待足够的255以上个字节。
             * */
            return NFC_SUCCESS;
        }

        *pszRxLen += res;

        if (*pszRxLen == pszMaxRxLen) {
            // We have all the data we wanted.
            return NFC_SUCCESS;
        }
    } while (byteCount);

    return NFC_SUCCESS;
}

int uart_send(const serial_port sp, const uint8_t *pbtTx, const uint32_t len) {
    uint32_t pos = 0;
    fd_set rfds;

    while (pos < len) {
        // Reset file descriptor
        FD_ZERO(&rfds);
        FD_SET(((serial_port_unix *) sp)->fd, &rfds);
        struct timeval timeval = {.tv_sec  = 3};
        int res = select(((serial_port_unix *) sp)->fd + 1, NULL, &rfds, NULL, &timeval);

        // Write error
        if (res < 0) {
            printf("UART:: write error (%d)\n", res);
            return NFC_EIO;
        }

        // Write time-out
        if (res == 0) {
            printf("UART:: write time-out\n");
            return NFC_ETIMEOUT;
        }

        // Send away the bytes
        res = write(((serial_port_unix *) sp)->fd, pbtTx + pos, len - pos);

        // Stop if the OS has some troubles sending the data
        if (res <= 0)
            return NFC_EIO;

        pos += res;
    }
    return NFC_SUCCESS;
}

bool uart_set_speed(serial_port sp, const uint32_t uiPortSpeed) {
    const serial_port_unix *spu = (serial_port_unix *) sp;
    speed_t stPortSpeed;
    switch (uiPortSpeed) {
        case 0:
            stPortSpeed = B0;
            break;
        case 50:
            stPortSpeed = B50;
            break;
        case 75:
            stPortSpeed = B75;
            break;
        case 110:
            stPortSpeed = B110;
            break;
        case 134:
            stPortSpeed = B134;
            break;
        case 150:
            stPortSpeed = B150;
            break;
        case 300:
            stPortSpeed = B300;
            break;
        case 600:
            stPortSpeed = B600;
            break;
        case 1200:
            stPortSpeed = B1200;
            break;
        case 1800:
            stPortSpeed = B1800;
            break;
        case 2400:
            stPortSpeed = B2400;
            break;
        case 4800:
            stPortSpeed = B4800;
            break;
        case 9600:
            stPortSpeed = B9600;
            break;
        case 19200:
            stPortSpeed = B19200;
            break;
        case 38400:
            stPortSpeed = B38400;
            break;
#  ifdef B57600
        case 57600:
            stPortSpeed = B57600;
            break;
#  endif
#  ifdef B115200
        case 115200:
            stPortSpeed = B115200;
            break;
#  endif
#  ifdef B230400
        case 230400:
            stPortSpeed = B230400;
            break;
#  endif
#  ifdef B460800
        case 460800:
            stPortSpeed = B460800;
            break;
#  endif
#  ifdef B921600
        case 921600:
            stPortSpeed = B921600;
            break;
#  endif
#  ifdef B1382400
        case 1382400:
            stPortSpeed = B1382400;
            break;
#  endif

        default:
            return false;
    };

    struct termios ti;
    if (tcgetattr(spu->fd, &ti) == -1)
        return false;

    // Set port speed (Input and Output)
    cfsetispeed(&ti, stPortSpeed);
    cfsetospeed(&ti, stPortSpeed);
    bool result = tcsetattr(spu->fd, TCSANOW, &ti) != -1;
    return result;
}

uint32_t uart_get_speed(const serial_port sp) {
    struct termios ti;
    uint32_t uiPortSpeed;
    const serial_port_unix *spu = (serial_port_unix *) sp;

    if (tcgetattr(spu->fd, &ti) == -1)
        return 0;

    // Set port speed (Input)
    speed_t stPortSpeed = cfgetispeed(&ti);
    switch (stPortSpeed) {
        case B0:
            uiPortSpeed = 0;
            break;
        case B50:
            uiPortSpeed = 50;
            break;
        case B75:
            uiPortSpeed = 75;
            break;
        case B110:
            uiPortSpeed = 110;
            break;
        case B134:
            uiPortSpeed = 134;
            break;
        case B150:
            uiPortSpeed = 150;
            break;
        case B300:
            uiPortSpeed = 300;
            break;
        case B600:
            uiPortSpeed = 600;
            break;
        case B1200:
            uiPortSpeed = 1200;
            break;
        case B1800:
            uiPortSpeed = 1800;
            break;
        case B2400:
            uiPortSpeed = 2400;
            break;
        case B4800:
            uiPortSpeed = 4800;
            break;
        case B9600:
            uiPortSpeed = 9600;
            break;
        case B19200:
            uiPortSpeed = 19200;
            break;
        case B38400:
            uiPortSpeed = 38400;
            break;
#  ifdef B57600
        case B57600:
            uiPortSpeed = 57600;
            break;
#  endif
#  ifdef B115200
        case B115200:
            uiPortSpeed = 115200;
            break;
#  endif
#  ifdef B230400
        case B230400:
            uiPortSpeed = 230400;
            break;
#  endif
#  ifdef B460800
        case B460800:
            uiPortSpeed = 460800;
            break;
#  endif
#  ifdef B921600
        case B921600:
            uiPortSpeed = 921600;
            break;
#  endif
        default:
            return 0;
    };
    return uiPortSpeed;
}

#endif
