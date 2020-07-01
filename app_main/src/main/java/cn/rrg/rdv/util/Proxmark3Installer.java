package cn.rrg.rdv.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import com.termux.app.TermuxService;

import java.io.File;

import cn.dxl.common.util.AssetsUtil;
import cn.rrg.rdv.R;

public class Proxmark3Installer {

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
                String zipName = "proxmark3.zip";
                String zipFile = TermuxService.HOME_PATH + File.separator + zipName;
                new AssetsUtil(activity).copyFile(zipName, zipFile);
                try {
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
                } catch (Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.error)
                                    .setMessage(e.getMessage())
                                    .show();
                            activity.runOnUiThread(() -> {
                                try {
                                    progress.dismiss();
                                } catch (RuntimeException e) {
                                    // Activity already dismissed - ignore.
                                }
                            });
                            if (whenDone != null)
                                whenDone.run();
                        }
                    });
                    e.printStackTrace();
                }
                activity.runOnUiThread(() -> {
                    try {
                        progress.dismiss();
                    } catch (RuntimeException e) {
                        // Activity already dismissed - ignore.
                    }
                });
                if (whenDone != null)
                    activity.runOnUiThread(whenDone);
            }
        }).start();
    }
}
