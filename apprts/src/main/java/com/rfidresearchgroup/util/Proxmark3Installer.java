package com.rfidresearchgroup.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.termux.app.TermuxService;

import java.io.File;

import com.rfidresearchgroup.common.util.AssetsUtil;
import com.rfidresearchgroup.common.util.FileUtils;
import com.rfidresearchgroup.common.util.LogUtils;
import com.rfidresearchgroup.rfidtools.R;

public class Proxmark3Installer {
    static String zipName = "proxmark3.zip";
    static String verName = "pm3_version.txt";
    static String zipFile = TermuxService.HOME_PATH + File.separator + zipName;
    static String verFile = TermuxService.HOME_PATH + File.separator + verName;

    public static boolean isCanInstall(Context context) {
        try {
            String version_assets = AssetsUtil.readLines(context, verName);

            // read version for res installed
            String version_installed = FileUtils.readLine(new File(verFile));
            // check version
            if (version_installed.length() == 0) {
                // no file found or file have some problem, we need reinstall
                return true;
            }
            if (version_installed.equals(version_assets)) {
                // if version same, we can stop install
                LogUtils.d("Proxmark3Installer->installIfNeed(): version same, we can stop install.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Install the pm3 client and resource if need!
     *
     * @param activity Current activity
     * @param whenDone call when task finish.
     */
    public static void installIfNeed(Activity activity, Runnable whenDone) {
        final ProgressDialog progress = new ProgressDialog(activity);
        progress.setTitle(activity.getString(com.termux.R.string.bootstrap_installer_body));
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isCanInstall(activity)) {
                        if (whenDone != null) activity.runOnUiThread(whenDone);
                        progress.cancel();
                        return;
                    }

                    // before install, we need delete all old res
                    FileUtils.delete(new File(TermuxService.HOME_PATH));
                    // and recreate path
                    FileUtils.createPaths(new File(TermuxService.HOME_PATH));

                    // copy zip file from apk to local
                    AssetsUtil.copyFile(activity, zipName, zipFile);
                    AssetsUtil.copyFile(activity, verName, verFile);

                    // unzip to home path
                    ZipUtils.UnZipFolder(zipFile, TermuxService.HOME_PATH, new ZipUtils.Progress() {
                        @Override
                        public void onProgress(int max, int current, String file) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setMax(max);
                                    progress.setProgress(current);
                                }
                            });
                        }
                    });

                    LogUtils.d("PM3 Resource install finish.");

                } catch (Exception e) {
                    showDialogOnErr(activity, progress, e.getMessage(), whenDone);
                    e.printStackTrace();
                }
                showDialogOnOk(activity, progress);
                if (whenDone != null) activity.runOnUiThread(whenDone);
            }
        }).start();
    }

    //  show dialog if task execute success
    public static void showDialogOnOk(Activity activity, ProgressDialog dialog) {
        activity.runOnUiThread(() -> {
            try {
                dialog.dismiss();
            } catch (RuntimeException e) {
                // Activity already dismissed - ignore.
            }
        });
    }

    // show dialog if has err
    public static void showDialogOnErr(Activity activity, ProgressDialog dialog, String errMsg, Runnable call) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.error)
                        .setMessage(errMsg)
                        .show();
                activity.runOnUiThread(() -> {
                    try {
                        dialog.dismiss();
                    } catch (RuntimeException e) {
                        // Activity already dismissed - ignore.
                    }
                });
                if (call != null)
                    call.run();
            }
        });
    }
}
