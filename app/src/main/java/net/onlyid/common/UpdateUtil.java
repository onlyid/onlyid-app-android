package net.onlyid.common;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.BuildConfig;
import net.onlyid.R;
import net.onlyid.databinding.DialogUpdateBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UpdateUtil {
    static final String TAG = UpdateUtil.class.getSimpleName();
    static final String APK_NAME = "唯ID.apk";

    Activity activity;
    int current, oldest;
    List<String> featureList;
    BroadcastReceiver completeReceiver;

    public UpdateUtil(Activity activity) {
        this.activity = activity;
    }

    public void check() {
        MyHttp.get("/android-version", (s) -> {
            JSONObject jsonObject = new JSONObject(s);
            current = jsonObject.getInt("current");
            oldest = jsonObject.getInt("oldest");
            JSONArray jsonArray = jsonObject.getJSONArray("featureList");
            featureList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                featureList.add((i + 1) + ". " + jsonArray.opt(i));
            }
            showDialogIfNecessary();
        });
    }

    void showDialogIfNecessary() {
        if (BuildConfig.VERSION_CODE < oldest) {
            new MaterialAlertDialogBuilder(activity)
                    .setMessage("当前版本已过期，请更新。")
                    .setPositiveButton("更新", (dialog, which) -> downloadPackage())
                    .setNegativeButton("退出", (d, w) -> activity.finish())
                    .setCancelable(false)
                    .show();
        } else if (BuildConfig.VERSION_CODE < current) {
            if (Utils.pref.getInt("silentVersionCode", -1) == current) return;

            DialogUpdateBinding binding = DialogUpdateBinding.inflate(activity.getLayoutInflater());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.item_dialog_list, featureList) {
                @Override
                public boolean isEnabled(int position) {
                    return false;
                }
            };
            binding.listView.setAdapter(adapter);
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("有新版本，请更新：")
                    .setView(binding.getRoot())
                    .setPositiveButton("更新", (dialog, which) -> downloadPackage())
                    .setNegativeButton("取消", (d, w) -> {
                        if (binding.checkBox.isChecked())
                            Utils.pref.edit().putInt("silentVersionCode", current).apply();
                    })
                    .show();
        }
    }

    void downloadPackage() {
        Utils.showToast("开始下载，稍后会自动开始安装", Toast.LENGTH_LONG);

        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME);
        if (file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://www.onlyid.net/static/downloads/唯ID.apk"));
        request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, APK_NAME);
        long id = downloadManager.enqueue(request);

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id1 = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id1 == id) installPackage();
            }
        };

        activity.registerReceiver(completeReceiver, intentFilter);
    }

    void installPackage() {
        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);
    }

    public void destroy() {
        if (completeReceiver != null) activity.unregisterReceiver(completeReceiver);
    }
}
