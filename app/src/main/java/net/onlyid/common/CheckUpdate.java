package net.onlyid.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.BuildConfig;
import net.onlyid.MyApplication;

import org.json.JSONObject;

import java.io.File;

public class CheckUpdate {
    static final String TAG = "CheckUpdate";
    static final String APK_NAME = "onlyid.apk";
    static final String DOWNLOAD_URL = "https://www.onlyid.net/static/downloads/";

    @SuppressLint("StaticFieldLeak")
    static Activity activity; // MainActivity
    static BroadcastReceiver downloadReceiver;
    static boolean needInstall;

    public interface Callback {
        void onFinish();
    }

    public static void start(Activity activity, Callback callback) {
        CheckUpdate.activity = activity;

        MyHttp.get("/min-version/android", (resp) -> {
            JSONObject respObject = new JSONObject(resp);
            int minVersion = respObject.getInt("minVersion");

            if (BuildConfig.VERSION_CODE < minVersion) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle("APP版本过期，请下载新版本。")
                        .setPositiveButton("下载", (dialog, which) -> downloadPackage())
                        .setNegativeButton("退出", (dialog, which) -> activity.finish())
                        .setCancelable(false)
                        .show();
            } else {
                callback.onFinish();
            }
        });
    }

    static void downloadPackage() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle("开始下载，稍后会自动安装。")
                .setPositiveButton("下载", null)
                .setNegativeButton("退出", null)
                .setCancelable(false)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME);
        if (file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DOWNLOAD_URL + APK_NAME));
        request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, APK_NAME);
        long id = downloadManager.enqueue(request);

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id1 = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id1 == id) {
                    // 如果应用不可见，则稍后再安装
                    if (MyApplication.currentActivity != null)
                        installPackage();
                    else
                        needInstall = true;
                }
            }
        };
        activity.registerReceiver(downloadReceiver, intentFilter);
    }

    static void installPackage() {
        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void installIfNecessary() {
        if (needInstall) {
            installPackage();
            needInstall = false;
        }
    }

    public static void destroy() {
        if (downloadReceiver != null) activity.unregisterReceiver(downloadReceiver);

        activity = null;
    }
}
