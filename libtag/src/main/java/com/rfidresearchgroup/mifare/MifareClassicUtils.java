package com.rfidresearchgroup.mifare;

/*
 * MifareClassic标签用得到的工具!
 * */
public class MifareClassicUtils {

    public static boolean validateSector(int sector) {
        // Do not be too strict on upper bounds checking, since some cards
        // have more addressable memory than they report. For example,
        // MIFARE Plus 2k cards will appear as MIFARE Classic 1k cards when in
        // MIFARE Classic compatibility mode.
        // Note that issuing a command to an out-of-bounds block is safe - the
        // tag should report error causing IOException. This validation is a
        // helper to guard against obvious programming mistakes.

        int NR_TRAILERS_4k = 40;
        if (sector < 0 || sector >= NR_TRAILERS_4k) {
            return false;
        }
        return true;
    }

    public static boolean validateBlock(int block) {
        // Just looking for obvious out of bounds...
        int NR_BLOCKS_4k = 0xFF;
        if (block < 0 || block >= NR_BLOCKS_4k) {
            return false;
        }
        return true;
    }

    public static boolean validateValueOperand(int value) {
        if (value < 0) {
            return false;
        }
        return true;
    }

    public static int blockToSector(int blockIndex) {
        if (!validateBlock(blockIndex)) return 0;
        if (blockIndex < 32 * 4) {
            return (blockIndex / 4);
        } else {
            return (32 + (blockIndex - 32 * 4) / 16);
        }
    }

    public static int sectorToBlock(int sectorIndex) {
        if (!validateSector(sectorIndex)) {
            return -1;
        }
        if (sectorIndex < 32) {
            return (sectorIndex * 4);
        } else {
            return (32 * 4 + (sectorIndex - 32) * 16);
        }
    }

    public static boolean isFirstBlock(int uiBlock) {
        // 测试我们是否处于小扇区或者大扇区？
        if (uiBlock < 128)
            return ((uiBlock) % 4 == 0);
        else
            return ((uiBlock) % 16 == 0);
    }

    public static boolean isTrailerBlock(int uiBlock) {
        // 测试我们处于小区块还是大扇区
        if (uiBlock < 128)
            return ((uiBlock + 1) % 4 == 0);
        else
            return ((uiBlock + 1) % 16 == 0);
    }

    public static int getBlockCountInSector(int sectorIndex) {
        if (!validateSector(sectorIndex)) return -1;
        if (sectorIndex < 32) {
            return 4;
        } else {
            return 16;
        }
    }

    public static int get_trailer_block(int uiFirstBlock) {
        // Test if we are in the small or big sectors
        int trailer_block;
        if (uiFirstBlock < 128) {
            trailer_block = uiFirstBlock + (3 - (uiFirstBlock % 4));
        } else {
            trailer_block = uiFirstBlock + (15 - (uiFirstBlock % 16));
        }
        return trailer_block;
    }

    public static int getIndexOnSector(int block, int sector) {
        int index = 0;
        //得到当前的块在扇区中的具体索引!
        for (int i = 0; i < getBlockCountInSector(sector); i++) { //得到当前扇区的块总数!
            if (block == (sectorToBlock(block) + i)) break;
            ++index;
        }
        return index;
    }
}
