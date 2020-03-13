package com.proxgrind.pm3flasher;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 0. Open the device and create a connection!
 * 1. Enter bootloader mode, the device will restart and the USB device will regenerate!
 * 2. Connect the USB device and judge whether the current mode is bootloader mode. If yes, flash in bootloader!
 * 3. Like the above steps, here I recommend that the two brush write steps be separated to avoid errors,
 * but you can also flash the bootrom and image at the same time.
 * 4. If you want to flash once, must to use the {@link Proxmark3Flasher#flashModeClose()} function to restart PM3 to os mode.
 * Otherwise, you don't need to call the {@link Proxmark3Flasher#flashModeClose()} function to turn off PM3's flash mode, thus flash all the firmware in once.
 * 5. Due to the differences in communication between different versions of the client, we cannot verify the client version!
 * 6. All operations should be placed in the child thread
 *
 * @author DXL
 * @version 1.0
 */
public class Proxmark3Flasher {

    private static final Proxmark3Flasher flasher;
    private static String LOG = "Proxmark3Flasher";

    /**
     * 警告！！！！！！！！！！！！！！！！！！！！！！！！！！
     * 使用刷写器的时候尽量不要频繁关闭Client
     * openProxmark3() 请在Activity -> onCreate() 中执行
     * closeProxmark3() 请求Activity -> onDestroy() 中执行
     * 频繁关闭客户端可能会导致JNI Crash！！！！！！！！！！！！
     * */

    static {
        //在静态块加载对应的模块
        System.loadLibrary("pm3_flasher");
        flasher = new Proxmark3Flasher();
    }

    // can't use!
    private Proxmark3Flasher() {
    }

    /**
     * The flasher singleton
     *
     * @return Instance
     */
    public static Proxmark3Flasher getFlasher() {
        return flasher;
    }

    public boolean start(Target target) {
        synchronized (flasher) {
            switch (target) {
                case CLIENT:
                    return openProxmark3();
                case BOOT:
                    return enterBootloader();
            }
            return false;
        }
    }

    public boolean isStart(Target target) {
        synchronized (flasher) {
            switch (target) {
                case CLIENT:
                    return isPM3Opened();
                case BOOT:
                    return isBootloaderMode();
            }
            return false;
        }
    }

    public void close(Target target) {
        synchronized (flasher) {
            switch (target) {
                case CLIENT:
                    closeProxmark3();
                    break;
                case BOOT:
                    flashModeClose();
                    break;
            }
        }
    }

    /**
     * The pm3 client state check!
     * if client is close, the UART communication will error.
     * we need to open client before all operation.
     *
     * @return status, true is open, false is close.
     */
    private native boolean isPM3Opened();

    /**
     * Open the client of pm3(Communication basic)
     *
     * @return true is opened, false is open failed!
     */
    private native boolean openProxmark3();

    /**
     * Close clint of pm3(Communication basic)
     */
    private native void closeProxmark3();

    /**
     * Request to enter bootloader mode
     * <p>
     * If the current device is already in bootloader mode, it will not enter repeatedly
     * If the current device is in OS mode, the device will be restarted to bootloader mode (USB will also be reconnected)
     * If the current bootloader version does not match, or the device firmware is damaged, you may fail to enter the bootloader,
     * At this time, please press and hold the button and insert the USB to forcibly enter the bootloader recovery mode (do not release the button),
     * and skip this step
     *
     * @return Request result, true is the request success, false is the request failure
     */
    private native boolean enterBootloader() throws IllegalStateException;

    /**
     * Check if bootloader mode is currently available
     *
     * @return true is available, false is unavailable
     */
    private native boolean isBootloaderMode() throws IllegalStateException;

    /**
     * flash BootRom, if flash once please invoke {@link Proxmark3Flasher#flashModeClose()} behind finished.
     *
     * @param file The bootrom image file.
     * @return true is flash successful, false is flash failed.
     */
    public boolean flashBootRom(String file) throws IllegalStateException {
        printLog();
        try {
            checkFile(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        synchronized (flasher) {
            return flash(file, true);
        }
    }

    /**
     * flash OS image, if flash once please invoke {@link Proxmark3Flasher#flashModeClose()} behind finished.
     *
     * @param file The bootrom image file.
     * @return true is flash successful, false is flash failed.
     */
    public boolean flashFullImg(String file) throws IllegalStateException {
        printLog();
        try {
            checkFile(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        synchronized (flasher) {
            return flash(file, false);
        }
    }

    /**
     * flash native implement, don't reflect.
     */
    private native boolean flash(String file, boolean isBootROM) throws IllegalStateException;

    /**
     * close flash mode for pm3.
     * warning! if you flash once, please invoke this function!
     * if you flash bootrom and fullimage same time,
     * please invoke this function at all operation finished.
     */
    private native void flashModeClose();

    // show warning
    private void printLog() {
        Log.w(LOG, "Try not to flash bootrom and fullmg at the same time");
    }

    // check file
    private void checkFile(File file) throws FileNotFoundException {
        boolean pass = file.exists() && file.isFile() && file.canRead();
        if (!pass) {
            throw new FileNotFoundException("The file is no exists or can't read.");
        }
    }
}
