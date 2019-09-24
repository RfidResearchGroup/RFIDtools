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
 * Copyright (C) 2017 Adam Laurie
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
 * @file mifare.h
 * @brief provide samples structs and functions to manipulate MIFARE Classic and Ultralight tags using libnfc
 */

#ifndef _LIBNFC_MIFARE_H_
#  define _LIBNFC_MIFARE_H_

#  include <nfc/nfc-types.h>

// 编译器指令，将结构调整设置为1 uint8t，以供兼容性。
#  pragma pack(1)

// Number of trailers == number of sectors
// Mifare Classic 1k 16x64b = 16
#define NR_TRAILERS_1k  (16)
// Mifare Classic Mini
#define NR_TRAILERS_MINI (5)
// Mifare Classic 4k 32x64b + 8*256b = 40
#define NR_TRAILERS_4k  (40)
// Mifare Classic 2k 32x64b
#define NR_TRAILERS_2k  (32)

// Number of blocks
// Mifare Classic 1k
#define NR_BLOCKS_1k 0x3f
// Mifare Classic Mini
#define NR_BLOCKS_MINI 0x13
// Mifare Classic 4k
#define NR_BLOCKS_4k 0xff
// Mifare Classic 2k
#define NR_BLOCKS_2k 0x7f

#define MAX_FRAME_LEN 264

// SpclMf 命令
typedef enum {
  MC_AUTH_A = 0x60,
  MC_AUTH_B = 0x61,
  MC_READ = 0x30,
  MC_WRITE = 0xA0,
  MC_TRANSFER = 0xB0,
  MC_DECREMENT = 0xC0,
  MC_INCREMENT = 0xC1,
  MC_STORE = 0xC2
} mifare_cmd;

// 非接触式读卡器命令参数
struct mifare_param_auth {
  uint8_t  abtKey[6];
  uint8_t  abtAuthUid[4];
};

//块数据结构体
struct mifare_param_data {
  uint8_t  abtData[16];
};

//块总数数据结构
struct mifare_param_value {
  uint8_t  abtValue[4];
};

//尾部块参数
struct mifare_param_trailer {
  uint8_t  abtKeyA[6];
  uint8_t  abtAccessBits[4];
  uint8_t  abtKeyB[6];
};

typedef union {
  struct mifare_param_auth mpa;
  struct mifare_param_data mpd;
  struct mifare_param_value mpv;
  struct mifare_param_trailer mpt;
} mifare_param;

// 将结构调整为默认值
#  pragma pack()

bool    nfc_initiator_mifare_cmd(nfc_device *pnd, const mifare_cmd mc, const uint8_t ui8Block, mifare_param *pmp);

// 编译器指令，将结构调整设置为1 uint8t，以供兼容性。
#  pragma pack(1)

// 非接触式读卡器经典
typedef struct {
  uint8_t  abtUID[4];  // beware for 7bytes UID it goes over next fields
  uint8_t  btBCC;
  uint8_t  btSAK;      // beware it's not always exactly SAK
  uint8_t  abtATQA[2];
  uint8_t  abtManufacturer[8];
} mifare_classic_block_manufacturer;

typedef struct {
  uint8_t  abtData[16];
} mifare_classic_block_data;

typedef struct {
  uint8_t  abtKeyA[6];
  uint8_t  abtAccessBits[4];
  uint8_t  abtKeyB[6];
} mifare_classic_block_trailer;

typedef union {
  mifare_classic_block_manufacturer mbm;
  mifare_classic_block_data mbd;
  mifare_classic_block_trailer mbt;
} mifare_classic_block;

typedef struct {
  mifare_classic_block amb[256];
} mifare_classic_tag;

// 微型 MIfare classic
typedef struct {
  uint8_t  sn0[3];
  uint8_t  btBCC0;
  uint8_t  sn1[4];
  uint8_t  btBCC1;
  uint8_t  internal;
  uint8_t  lock[2];
  uint8_t  otp[4];
} mifareul_block_manufacturer;

// MIFARE Ultralight EV1 MF0UL11 Config Pages
typedef struct {
  uint8_t  mod;
  uint8_t  rfui1[2];
  uint8_t  auth0;
  uint8_t  access;
  uint8_t  vctid;
  uint8_t  rfui2[2];
  uint8_t  pwd[4];
  uint8_t  pack[2];
  uint8_t  rfui3[2];
} mifareul_block_config11;

// MIFARE Ultralight EV1 MF0UL21 ConfigA Pages
typedef struct {
  uint8_t  lock[3];
  uint8_t  rfui0;
  uint8_t  mod;
  uint8_t  rfui1[2];
  uint8_t  auth0;
  uint8_t  access;
  uint8_t  vctid;
  uint8_t  rfui2[2];
  uint8_t  pwd[4];
} mifareul_block_config21A;

// MIFARE Ultralight EV1 MF0UL21 ConfigB Pages
typedef struct {
  uint8_t  pack[2];
  uint8_t  rfui3[2];
  uint8_t  dummy[12];
} mifareul_block_config21B;

typedef struct {
  uint8_t  abtData[16];
} mifareul_block_data;

typedef union {
  mifareul_block_manufacturer mbm;
  mifareul_block_data mbd;
  mifareul_block_config11 mbc11;
  mifareul_block_config21A mbc21a;
  mifareul_block_config21B mbc21b;
} mifareul_block;

// standard UL tag - 1 manuf block + 3 user blocks
typedef struct {
  mifareul_block amb[4];
} mifareul_tag;

// UL EV1 MF0UL11 tag - 1 manuf block + 3 user blocks + 1 config block
typedef struct {
  mifareul_block amb[5];
} mifareul_ev1_mf0ul11_tag;

// UL EV1 MF0UL21 tag - 1 manuf block + 8 user blocks + 1/4 lock block + 1 config block
typedef struct {
  mifareul_block amb[11];
} mifareul_ev1_mf0ul21_tag;

// Reset struct alignment to default
#  pragma pack()

#endif // _LIBNFC_MIFARE_H_
