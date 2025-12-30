//-----------------------------------------------------------------------------
// Copyright (C) 2010 Hector Martin "marcan" <marcan@marcansoft.com>
//
// This code is licensed to you under the terms of the GNU GPL, version 2 or,
// at your option, any later version. See the LICENSE.txt file for the text of
// the license.
//-----------------------------------------------------------------------------
// ELF file flasher
//-----------------------------------------------------------------------------

#include "flash.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include "ui.h"
#include "elf.h"
#include "proxendian.h"
#include "at91sam7s512.h"
#include "util_posix.h"
#include "comms.h"
#include "tools.h"

#define FLASH_START            0x100000

#define BOOTLOADER_SIZE        0x2000
#define BOOTLOADER_END         (FLASH_START + BOOTLOADER_SIZE)

#define BLOCK_SIZE             0x200

#define FLASHER_VERSION        BL_VERSION_1_0_0

static const uint8_t elf_ident[] = {
        0x7f, 'E', 'L', 'F',
        ELFCLASS32,
        ELFDATA2LSB,
        EV_CURRENT
};

static int chipid_to_mem_avail(uint32_t iChipID) {
    int mem_avail = 0;
    switch ((iChipID & 0xF00) >> 8) {
        case 0:
            mem_avail = 0;
            break;
        case 1:
            mem_avail = 8;
            break;
        case 2:
            mem_avail = 16;
            break;
        case 3:
            mem_avail = 32;
            break;
        case 5:
            mem_avail = 64;
            break;
        case 7:
            mem_avail = 128;
            break;
        case 9:
            mem_avail = 256;
            break;
        case 10:
            mem_avail = 512;
            break;
        case 12:
            mem_avail = 1024;
            break;
        case 14:
            mem_avail = 2048;
    }
    return mem_avail;
}

// Turn PHDRs into flasher segments, checking for PHDR sanity and merging adjacent
// unaligned segments if needed
static int build_segs_from_phdrs(flash_file_t *ctx, FILE *fd, Elf32_Phdr *phdrs, uint16_t num_phdrs,
                                 uint32_t flash_end) {
    Elf32_Phdr *phdr = phdrs;
    flash_seg_t *seg;
    uint32_t last_end = 0;

    ctx->segments = calloc(sizeof(flash_seg_t) * num_phdrs, sizeof(uint8_t));
    if (!ctx->segments) {
        PrintAndLogEx(ERR, "Out of memory");
        return PM3_EMALLOC;
    }
    ctx->num_segs = 0;
    seg = ctx->segments;

    PrintAndLogEx(SUCCESS, "Loading usable ELF segments:");
    for (int i = 0; i < num_phdrs; i++) {
        if (le32(phdr->p_type) != PT_LOAD) {
            phdr++;
            continue;
        }
        uint32_t vaddr = le32(phdr->p_vaddr);
        uint32_t paddr = le32(phdr->p_paddr);
        uint32_t filesz = le32(phdr->p_filesz);
        uint32_t memsz = le32(phdr->p_memsz);
        uint32_t offset = le32(phdr->p_offset);
        uint32_t flags = le32(phdr->p_flags);
        if (!filesz) {
            phdr++;
            continue;
        }
        PrintAndLogEx(SUCCESS,
                      "   "_YELLOW_("%d")": V 0x%08x P 0x%08x (0x%08x->0x%08x) [%c%c%c] @0x%x",
                      i, vaddr, paddr, filesz, memsz,
                      (flags & PF_R) ? 'R' : ' ',
                      (flags & PF_W) ? 'W' : ' ',
                      (flags & PF_X) ? 'X' : ' ',
                      offset);
        if (filesz != memsz) {
            PrintAndLogEx(ERR, "Error: PHDR file size does not equal memory size\n"
                               "(DATA+BSS PHDRs do not make sense on ROM platforms!)");
            return PM3_EFILE;
        }
        if (paddr < last_end) {
            PrintAndLogEx(ERR, "Error: PHDRs not sorted or overlap");
            return PM3_EFILE;
        }
        if (paddr < FLASH_START || (paddr + filesz) > flash_end) {
            PrintAndLogEx(ERR, "Error: PHDR is not contained in Flash");
            return PM3_EFILE;
        }
        if (vaddr >= FLASH_START && vaddr < flash_end && (flags & PF_W)) {
            PrintAndLogEx(ERR, "Error: Flash VMA segment is writable");
            return PM3_EFILE;
        }

        uint8_t *data;
        // make extra space if we need to move the data forward
        data = calloc(filesz + BLOCK_SIZE, sizeof(uint8_t));
        if (!data) {
            PrintAndLogEx(ERR, "Error: Out of memory");
            return PM3_EMALLOC;
        }
        if (fseek(fd, offset, SEEK_SET) < 0 || fread(data, 1, filesz, fd) != filesz) {
            PrintAndLogEx(ERR, "Error while reading PHDR payload");
            free(data);
            return PM3_EFILE;
        }

        uint32_t block_offset = paddr & (BLOCK_SIZE - 1);
        if (block_offset) {
            if (ctx->num_segs) {
                flash_seg_t *prev_seg = seg - 1;
                uint32_t this_end = paddr + filesz;
                uint32_t this_firstblock = paddr & ~(BLOCK_SIZE - 1);
                uint32_t prev_lastblock = (last_end - 1) & ~(BLOCK_SIZE - 1);

                if (this_firstblock == prev_lastblock) {
                    uint32_t new_length = this_end - prev_seg->start;
                    uint32_t this_offset = paddr - prev_seg->start;
                    uint32_t hole = this_offset - prev_seg->length;
                    uint8_t *new_data = calloc(new_length, sizeof(uint8_t));
                    if (!new_data) {
                        PrintAndLogEx(ERR, "Error: Out of memory");
                        free(data);
                        return PM3_EMALLOC;
                    }
                    memset(new_data, 0xff, new_length);
                    memcpy(new_data, prev_seg->data, prev_seg->length);
                    memcpy(new_data + this_offset, data, filesz);
                    PrintAndLogEx(INFO, "Note: Extending previous segment from 0x%x to 0x%x bytes",
                                  prev_seg->length, new_length);
                    if (hole)
                        PrintAndLogEx(INFO, "Note: 0x%x-byte hole created", hole);
                    free(data);
                    free(prev_seg->data);
                    prev_seg->data = new_data;
                    prev_seg->length = new_length;
                    last_end = this_end;
                    phdr++;
                    continue;
                }
            }
            PrintAndLogEx(WARNING, "Warning: segment does not begin on a block boundary, will pad");
            memmove(data + block_offset, data, filesz);
            memset(data, 0xFF, block_offset);
            filesz += block_offset;
            paddr -= block_offset;
        }

        seg->data = data;
        seg->start = paddr;
        seg->length = filesz;
        seg++;
        ctx->num_segs++;

        last_end = paddr + filesz;
        phdr++;
    }
    return PM3_SUCCESS;
}

// Sanity check segments and check for bootloader writes
static int check_segs(flash_file_t *ctx, int can_write_bl, uint32_t flash_end) {
    for (int i = 0; i < ctx->num_segs; i++) {
        flash_seg_t *seg = &ctx->segments[i];

        if (seg->start & (BLOCK_SIZE - 1)) {
            PrintAndLogEx(ERR, "Error: Segment is not aligned");
            return PM3_EFILE;
        }
        if (seg->start < FLASH_START) {
            PrintAndLogEx(ERR, "Error: Segment is outside of flash bounds");
            return PM3_EFILE;
        }
        if (seg->start + seg->length > flash_end) {
            PrintAndLogEx(ERR, "Error: Segment is outside of flash bounds");
            return PM3_EFILE;
        }
        if (!can_write_bl && seg->start < BOOTLOADER_END) {
            PrintAndLogEx(ERR,
                          "Attempted to write bootloader but bootloader writes are not enabled");
            return PM3_EINVARG;
        }
        if (can_write_bl && seg->start < BOOTLOADER_END &&
            (seg->start + seg->length > BOOTLOADER_END)) {
            PrintAndLogEx(ERR, "Error: Segment is outside of bootloader bounds");
            return PM3_EFILE;
        }
    }
    return PM3_SUCCESS;
}

// Load an ELF file and prepare it for flashing
int flash_load(flash_file_t *ctx, const char *name, int can_write_bl, int flash_size) {
    FILE *fd;
    Elf32_Ehdr ehdr;
    Elf32_Phdr *phdrs = NULL;
    uint16_t num_phdrs;
    uint32_t flash_end = FLASH_START + flash_size;
    int res = PM3_EUNDEF;

    fd = fopen(name, "rb");
    if (!fd) {
        LOGD(_RED_("Could not open file")
                     "%s  >>> ", name);
        res = PM3_EFILE;
        goto fail;
    }

    PrintAndLogEx(SUCCESS, _BLUE_("Loading ELF file") _YELLOW_("%s"), name);

    if (fread(&ehdr, sizeof(ehdr), 1, fd) != 1) {
        LOGD("Error while reading ELF file header");
        res = PM3_EFILE;
        goto fail;
    }
    if (memcmp(ehdr.e_ident, elf_ident, sizeof(elf_ident))
        || le32(ehdr.e_version) != 1) {
        LOGD("Not an ELF file or wrong ELF type");
        res = PM3_EFILE;
        goto fail;
    }
    if (le16(ehdr.e_type) != ET_EXEC) {
        LOGD("ELF is not executable");
        res = PM3_EFILE;
        goto fail;
    }
    if (le16(ehdr.e_machine) != EM_ARM) {
        LOGD("Wrong ELF architecture");
        res = PM3_EFILE;
        goto fail;
    }
    if (!ehdr.e_phnum || !ehdr.e_phoff) {
        LOGD("ELF has no PHDRs");
        res = PM3_EFILE;
        goto fail;
    }
    if (le16(ehdr.e_phentsize) != sizeof(Elf32_Phdr)) {
        // could be a structure padding issue...
        LOGD("Either the ELF file or this code is made of fail");
        res = PM3_EFILE;
        goto fail;
    }
    num_phdrs = le16(ehdr.e_phnum);

    phdrs = calloc(le16(ehdr.e_phnum) * sizeof(Elf32_Phdr), sizeof(uint8_t));
    if (!phdrs) {
        LOGD("Out of memory");
        res = PM3_EMALLOC;
        goto fail;
    }
    if (fseek(fd, le32(ehdr.e_phoff), SEEK_SET) < 0) {
        LOGD("Error while reading ELF PHDRs");
        res = PM3_EFILE;
        goto fail;
    }
    if (fread(phdrs, sizeof(Elf32_Phdr), num_phdrs, fd) != num_phdrs) {
        res = PM3_EFILE;
        LOGD("Error while reading ELF PHDRs");
        goto fail;
    }

    res = build_segs_from_phdrs(ctx, fd, phdrs, num_phdrs, flash_end);
    if (res != PM3_SUCCESS)
        goto fail;
    res = check_segs(ctx, can_write_bl, flash_end);
    if (res != PM3_SUCCESS)
        goto fail;

    free(phdrs);
    fclose(fd);
    ctx->filename = name;
    return PM3_SUCCESS;

    fail:
    if (phdrs)
        free(phdrs);
    if (fd)
        fclose(fd);
    flash_free(ctx);
    return res;
}

// Get the state of the proxmark, backwards compatible
static int get_proxmark_state(uint32_t *state) {
    SendCommandBL(CMD_DEVICE_INFO, 0, 0, 0, NULL, 0);
    PacketResponseNG resp;
    // WaitForResponse(CMD_UNKNOWN, &resp);  // wait for any response. No timeout.
    WaitForResponseTimeout(CMD_UNKNOWN, &resp, 3000);

    // Three outcomes:
    // 1. The old bootrom code will ignore CMD_DEVICE_INFO, but respond with an ACK
    // 2. The old os code will respond with CMD_DEBUG_PRINT_STRING and "unknown command"
    // 3. The new bootrom and os codes will respond with CMD_DEVICE_INFO and flags

    switch (resp.cmd) {
        case CMD_ACK:
            *state = DEVICE_INFO_FLAG_CURRENT_MODE_BOOTROM;
            break;
        case CMD_DEBUG_PRINT_STRING:
            *state = DEVICE_INFO_FLAG_CURRENT_MODE_OS;
            break;
        case CMD_DEVICE_INFO:
            *state = resp.oldarg[0];
            break;
        default:
            return PM3_EFATAL;
    }
    LOGD("get_proxmark_state() Current state：%d", *state);
    if (*state == 0) return get_proxmark_state(state);
    return PM3_SUCCESS;
}

static int wait_for_ack(PacketResponseNG *ack) {
    WaitForResponse(CMD_UNKNOWN, ack);

    if (ack->cmd != CMD_ACK) {
        PrintAndLogEx(ERR, "Error: Unexpected reply 0x%04x %s (expected ACK)",
                      ack->cmd,
                      (ack->cmd == CMD_NACK) ? "NACK" : ""
        );
        return PM3_ESOFT;
    }
    return PM3_SUCCESS;
}

static void flash_suggest_update_bootloader(void) {
    PrintAndLogEx(ERR, _RED_("It is recommended that you first "
                                     _YELLOW_("update your bootloader")
                                     _RED_("alone,")));
    PrintAndLogEx(ERR, _RED_("reboot the Proxmark3 then only update the main firmware") "\n");
}

static void flash_suggest_update_flasher(void) {
    PrintAndLogEx(ERR, _RED_("It is recommended that you first "
                                     _YELLOW_("update your flasher")));
}

static int write_block(uint32_t address, uint8_t *data, uint32_t length) {
    uint8_t block_buf[BLOCK_SIZE];
    memset(block_buf, 0xFF, BLOCK_SIZE);
    memcpy(block_buf, data, length);
    PacketResponseNG resp;
    SendCommandBL(CMD_FINISH_WRITE, address, 0, 0, block_buf, length);
    int ret = wait_for_ack(&resp);
    if (ret && resp.oldarg[0]) {
        uint32_t lock_bits = resp.oldarg[0] >> 16;
        bool lock_error = resp.oldarg[0] & AT91C_MC_LOCKE;
        bool prog_error = resp.oldarg[0] & AT91C_MC_PROGE;
        bool security_bit = resp.oldarg[0] & AT91C_MC_SECURITY;
        PrintAndLogEx(NORMAL, "%s", lock_error ? "       Lock Error" : "");
        PrintAndLogEx(NORMAL, "%s", prog_error ? "       Invalid Command or bad Keyword" : "");
        PrintAndLogEx(NORMAL, "%s", security_bit ? "       Security Bit is set!" : "");
        PrintAndLogEx(NORMAL, "       Lock Bits:      0x%04x", lock_bits);
    }
    return ret;
}

// Write a file's segments to Flash
int flash_write(flash_file_t *ctx) {

    LOGD("Writing segments for file: %s", ctx->filename);

    for (int i = 0; i < ctx->num_segs; i++) {
        flash_seg_t *seg = &ctx->segments[i];

        uint32_t length = seg->length;
        uint32_t blocks = (length + BLOCK_SIZE - 1) / BLOCK_SIZE;
        uint32_t end = seg->start + length;

        LOGD(" 0x%08x..0x%08x [0x%x / %u blocks]", seg->start, end - 1, length,
             blocks);
        int block = 0;
        uint8_t *data = seg->data;
        uint32_t baddr = seg->start;

        while (length) {
            uint32_t block_size = length;
            if (block_size > BLOCK_SIZE)
                block_size = BLOCK_SIZE;

            if (write_block(baddr, data, block_size) < 0) {
                LOGD("Error writing block %d of %u", block, blocks);
                return PM3_EFATAL;
            }

            data += block_size;
            baddr += block_size;
            length -= block_size;
            block++;
        }
        LOGD("OK");
    }
    return PM3_SUCCESS;
}

// free a file context
void flash_free(flash_file_t *ctx) {
    if (!ctx)
        return;
    if (ctx->segments) {
        for (int i = 0; i < ctx->num_segs; i++) {
            free(ctx->segments[i].data);
        }
        free(ctx->segments);
        ctx->segments = NULL;
        ctx->num_segs = 0;
    }
}

// just reset the unit
int flash_stop_flashing(void) {
    if (conn.run) {
        SendCommandBL(CMD_HARDWARE_RESET, 0, 0, 0, NULL, 0);
    }
    return PM3_SUCCESS;
}

int flash_start_flashing_no_enter_bootloader(int enable_bl_writes, uint32_t *max_allowed) {
    uint32_t state;
    uint32_t chipinfo = 0;
    int ret;

    ret = get_proxmark_state(&state);
    if (ret != PM3_SUCCESS)
        return ret;

    if (state & DEVICE_INFO_FLAG_UNDERSTANDS_CHIP_INFO) {
        SendCommandBL(CMD_CHIP_INFO, 0, 0, 0, NULL, 0);
        PacketResponseNG resp;
        WaitForResponse(CMD_CHIP_INFO, &resp);
        chipinfo = resp.oldarg[0];
    }

    int version = BL_VERSION_INVALID;
    if (state & DEVICE_INFO_FLAG_UNDERSTANDS_VERSION) {
        SendCommandBL(CMD_BL_VERSION, 0, 0, 0, NULL, 0);
        PacketResponseNG resp;
        WaitForResponse(CMD_BL_VERSION, &resp);
        version = resp.oldarg[0];
        if ((BL_VERSION_MAJOR(version) < BL_VERSION_FIRST_MAJOR) ||
            (BL_VERSION_MAJOR(version) > BL_VERSION_LAST_MAJOR)) {
            // version info seems fishy
            version = BL_VERSION_INVALID;
            LOGD("====================== OBS ! ===========================");
            LOGD("Note: Your bootloader reported an invalid version number");
            flash_suggest_update_bootloader();
            //
        } else if (BL_VERSION_MAJOR(version) < BL_VERSION_MAJOR(FLASHER_VERSION)) {
            LOGD("====================== OBS ! ===================================");
            LOGD("Note: Your bootloader reported a version older than this flasher");
            flash_suggest_update_bootloader();
        } else if (BL_VERSION_MAJOR(version) > BL_VERSION_MAJOR(FLASHER_VERSION)) {
            LOGD("====================== OBS ! =========================");
            LOGD("Note: Your bootloader is more recent than this flasher");
            flash_suggest_update_flasher();
        }
    } else {
        LOGD("====================== OBS ! ===========================================");
        LOGD("Note: Your bootloader does not understand the new %d command", CMD_BL_VERSION);
        flash_suggest_update_bootloader();
    }

    uint32_t flash_end = FLASH_START + AT91C_IFLASH_PAGE_SIZE * AT91C_IFLASH_NB_OF_PAGES / 2;
    *max_allowed = 256;

    int mem_avail = chipid_to_mem_avail(chipinfo);
    if (mem_avail != 0) {
        LOGD("Available memory on this board: %uK bytes", mem_avail);
        if (mem_avail > 256) {
            if (BL_VERSION_MAJOR(version) < BL_VERSION_MAJOR(BL_VERSION_1_0_0)) {
                LOGD("====================== OBS ! ======================");
                LOGD("Your bootloader does not support writing above 256k");
                flash_suggest_update_bootloader();
            } else {
                flash_end = FLASH_START + AT91C_IFLASH_PAGE_SIZE * AT91C_IFLASH_NB_OF_PAGES;
                *max_allowed = mem_avail;
            }
        }
    } else {
        LOGD("Available memory on this board: UNKNOWN");
        LOGD("====================== OBS ! ======================================");
        LOGD("Note: Your bootloader does not understand the new CHIP_INFO command");
        flash_suggest_update_bootloader();
    }

    if (enable_bl_writes) {
        LOGD("Permitted flash range: 0x%08x-0x%08x", FLASH_START, flash_end);
    } else {
        LOGD("Permitted flash range: 0x%08x-0x%08x", BOOTLOADER_END, flash_end);
    }
    if (state & DEVICE_INFO_FLAG_UNDERSTANDS_START_FLASH) {
        PacketResponseNG resp;

        if (enable_bl_writes) {
            SendCommandBL(CMD_START_FLASH, FLASH_START, flash_end, START_FLASH_MAGIC, NULL, 0);
        } else {
            SendCommandBL(CMD_START_FLASH, BOOTLOADER_END, flash_end, 0, NULL, 0);
        }
        return wait_for_ack(&resp);
    } else {
        LOGD("====================== OBS ! ========================================");
        LOGD("Note: Your bootloader does not understand the new START_FLASH command");
        flash_suggest_update_bootloader();
    }
    return PM3_SUCCESS;
}

void flash_stop_implement(JNIEnv *env, jobject thiz) {
    if (conn.run) {
        flash_stop_flashing();
    } else {
        LOGE("The client is closed!");
    }
}

jboolean is_bootloader_mode(JNIEnv *env, jobject thiz) {
    jclass iae_clz = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (!conn.run) {
        (*env)->ThrowNew(env, iae_clz, "The pm3 client is closed, please open first.");
        return false;
    }

    if (conn.run) {
        uint32_t state;
        if (get_proxmark_state(&state) != PM3_SUCCESS) {
            return false;
        }
        /* Already in flash state, we're done. */
        if (state & DEVICE_INFO_FLAG_CURRENT_MODE_BOOTROM) {
            return true;
        }
        return false;
    } else {
        LOGE("The client is closed!");
        return false;
    }
}

jboolean flash_implement(JNIEnv *env, jobject thiz, jstring file, jboolean is_boot_rom) {
    jclass iae_clz = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (!conn.run) {
        (*env)->ThrowNew(env, iae_clz, "The pm3 client is closed, please open first.");
        goto finish;
    }

    const char *file_path = (*env)->GetStringUTFChars(env, file, false);
    uint32_t max_allowed = 0;

    int ret = flash_start_flashing_no_enter_bootloader(is_boot_rom, &max_allowed);
    if (ret != PM3_SUCCESS) {
        goto finish;
    }

    flash_file_t files[1];
    char *filepaths[1] = {0};

    filepaths[0] = malloc(sizeof(char) * strlen(file_path));
    strcpy(filepaths[0], file_path);

    // mem release!
    (*env)->ReleaseStringUTFChars(env, file, file_path);

    ret = flash_load(&files[0], filepaths[0], is_boot_rom, max_allowed * ONE_KB);
    if (ret != PM3_SUCCESS) {
        goto finish;
    }

    LOGD("Flashing...");

    ret = flash_write(&files[0]);
    if (ret != PM3_SUCCESS) {
        goto finish;
    }
    flash_free(files);

    finish:
    (*env)->DeleteLocalRef(env, iae_clz);
    if (filepaths[0] != NULL) free(filepaths[0]);
    if (ret == PM3_SUCCESS) LOGD("All done.");
    else return false;
    return true;
}

void close_proxmkar3(JNIEnv *env, jobject thiz) {
    if (conn.run) {
        CloseProxmark();
    }
    LOGD("关闭完成.");
}

jboolean enter_bootloader_mode(JNIEnv *env, jobject thiz) {

    jclass iae_clz = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (!conn.run) {
        (*env)->ThrowNew(env, iae_clz, "The pm3 client is closed, please open first.");
        return false;
    }

    uint32_t state;

    if (get_proxmark_state(&state) != PM3_SUCCESS) {
        return false;
    }

    /* Already in flash state, we're done. */
    if (state & DEVICE_INFO_FLAG_CURRENT_MODE_BOOTROM) {
        return true;
    }

    if (state & DEVICE_INFO_FLAG_CURRENT_MODE_OS) {
        LOGD("Entering bootloader...");

        if ((state & DEVICE_INFO_FLAG_BOOTROM_PRESENT)
            && (state & DEVICE_INFO_FLAG_OSIMAGE_PRESENT)) {
            // New style handover: Send CMD_START_FLASH, which will reset the board
            // and enter the bootrom on the next boot.
            SendCommandBL(CMD_START_FLASH, 0, 0, 0, NULL, 0);
            LOGD("Press and release the button only to abort");
        } else {
            // Old style handover: Ask the user to press the button, then reset the board
            SendCommandBL(CMD_HARDWARE_RESET, 0, 0, 0, NULL, 0);
            LOGD("Press and hold down button NOW if your bootloader requires it.");
        }
        return true;
    }
    return false;
}

jboolean is_proxmark3_opened(JNIEnv *env, jobject thiz) {
    return (jboolean) ((jboolean) conn.run);
}

jboolean open_proxmark3(JNIEnv *env, jobject thiz) {
    if (conn.run) {
        LOGD("设备已经是打开状态，将会跳过打开！");
        return true;
    }
    return (jboolean) OpenProxmark("socket:DXL.COM.ASL", false, 1000, true, FLASHMODE_SPEED);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *jniEnv = NULL;
    if ((*vm)->GetEnv(vm, (void **) &jniEnv, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    (*jniEnv)->GetJavaVM(jniEnv, &g_JavaVM);
    jclass clazz_flasher = (*jniEnv)->FindClass(jniEnv,
                                                "com/rfidresearchgroup/pm3flasher/Proxmark3Flasher");
    //构建和初始化函数结构体,分别是java层的函数名称，签名，对应的函数指针
    JNINativeMethod methods_flasher[] = {
            {"isPM3Opened",      "()Z",                    is_proxmark3_opened},
            {"openProxmark3",    "()Z",                    open_proxmark3},
            {"enterBootloader",  "()Z",                    enter_bootloader_mode},
            {"isBootloaderMode", "()Z",                    is_bootloader_mode},
            {"flash",            "(Ljava/lang/String;Z)Z", flash_implement},
            {"flashModeClose",   "()V",                    flash_stop_implement},
            {"closeProxmark3",   "()V",                    close_proxmkar3}
    };
    //注册函数
    if ((*jniEnv)->RegisterNatives(jniEnv, clazz_flasher, methods_flasher,
                                   sizeof(methods_flasher) / sizeof(methods_flasher[0])) !=
        JNI_OK) {
        return -1;
    }
    (*jniEnv)->DeleteLocalRef(jniEnv, clazz_flasher);
    return JNI_VERSION_1_4;
}