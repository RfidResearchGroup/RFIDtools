/*-
 * Free/Libre Near Field Communication (NFC) library
 *
 * Libnfc historical contributors:
 * Copyright (C) 2009      Roel Verdult
 * Copyright (C) 2009-2013 Romuald Conty
 * Copyright (C) 2010-2012 Romain Tartière
 * Copyright (C) 2010-2013 Philippe Teuwen
 * Copyright (C) 2012-2013 Ludovic Rousseau
 * See AUTHORS file for a more comprehensive list of contributors.
 * Additional contributors of this file:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1) Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2 )Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Note that this license only applies on the examples, NFC library itself is under LGPL
 *
 */
/**
 * @file mifare.c
 * @brief provide samples structs and functions to manipulate MIFARE Classic and Ultralight tags using libnfc
 */
#include "mifare.h"

#include <string.h>

#include <nfc/nfc.h>

/**
 * @brief Execute a MIFARE Classic Command
 * @return Returns true if action was successfully performed; otherwise returns false.
 * @param pmp Some commands need additional information. This information should be supplied in the mifare_param union.
 *
 * The specified MIFARE command will be executed on the tag. There are different commands possible, they all require the destination block number.
 * @note There are three different types of information (Authenticator, Data and Value).
 *
 * First an authentication must take place using Key A or B. It requires a 48 bit Key (6 bytes) and the UID.
 * They are both used to initialize the internal cipher-state of the PN53X chip.
 * After a successful authentication it will be possible to execute other commands (e.g. Read/Write).
 * The MIFARE Classic Specification (http://www.nxp.com/acrobat/other/identification/M001053_MF1ICS50_rev5_3.pdf) explains more about this process.
 */
bool
nfc_initiator_mifare_cmd(nfc_device *pnd, const mifare_cmd mc, const uint8_t ui8Block,
                         mifare_param *pmp) {
    uint8_t abtRx[265];
    size_t szParamLen;
    uint8_t abtCmd[265];
    //bool    bEasyFraming;

    abtCmd[0] = mc;               // MIFARE Classic命令
    abtCmd[1] = ui8Block;         // 块地址(1 k = 0 x00 . .0 x39 4 k = 0 x00 . . 0 xff)

    switch (mc) {
        // Read and store command have no parameter
        //读取和存储命令没有参数
        case MC_READ:
        case MC_STORE:
            szParamLen = 0;
            break;

            // 验证命令
        case MC_AUTH_A:
        case MC_AUTH_B:
            szParamLen = sizeof(struct mifare_param_auth);
            break;

            // 数据操作命令
        case MC_WRITE:
            szParamLen = sizeof(struct mifare_param_data);
            break;

            // 值块操作命令
        case MC_DECREMENT:
        case MC_INCREMENT:
        case MC_TRANSFER:
            szParamLen = sizeof(struct mifare_param_value);
            break;

            // 请修改你的代码，你永远都不应该达到这个要求
        default:
            return false;
    }

    // 当可用时，复制参数字节
    if (szParamLen)
        memcpy(abtCmd + 2, (uint8_t *) pmp, szParamLen);

    // FIXME:保存和恢复easy帧
    // bEasyFraming = nfc_device_get_property_bool (pnd, NP_EASY_FRAMING, &bEasyFraming);
    if (nfc_device_set_property_bool(pnd, NP_EASY_FRAMING, true) < 0) {
        nfc_perror(pnd, "nfc_device_set_property_bool");
        return false;
    }

    // 修复MifareClassic命令
    int res;
    if ((res = nfc_initiator_transceive_bytes(pnd, abtCmd, 2 + szParamLen, abtRx, sizeof(abtRx),
                                              -1)) < 0) {
        if (res == NFC_ERFTRANS) {
            // "Invalid received frame",  usual means we are
            // authenticated on a sector but the requested MIFARE cmd (read, write)
            // is not permitted by current acces bytes;
            // So there is nothing to do here.
            //上文大概的意思就是验证了密钥后想做一些操作（读写）但是控制位限制了，无法操作
        } else {
            nfc_perror(pnd, "nfc_initiator_transceive_bytes");
        }
        // XXX nfc_device_set_property_bool (pnd, NP_EASY_FRAMING, bEasyFraming);
        return false;
    }
    /* XXX
    if (nfc_device_set_property_bool (pnd, NP_EASY_FRAMING, bEasyFraming) < 0) {
      nfc_perror (pnd, "nfc_device_set_property_bool");
      return false;
    }
    */

    // 当我们执行一个读命令时，将接收到的字节复制到param中
    if (mc == MC_READ) {
        if (res == 16) {
            memcpy(pmp->mpd.abtData, abtRx, 16);
        } else {
            return false;
        }
    }
    // 命令成功执行
    return true;
}
