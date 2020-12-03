package net.onlyid.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.R;
import net.onlyid.databinding.DialogPermissionBinding;

public class PermissionUtil {
    static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    Activity activity;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    public void check() {
        for (String permission : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, permission)) {
                DialogPermissionBinding binding = DialogPermissionBinding.inflate(activity.getLayoutInflater());
                new MaterialAlertDialogBuilder(activity, R.style.MyAlertDialog)
                        .setTitle("申请权限")
                        .setView(binding.getRoot())
                        .setPositiveButton("同意，开始使用", (dialog, which) -> ActivityCompat.requestPermissions(activity, PERMISSIONS, 1))
                        .setNegativeButton("不同意，退出", (d, w) -> activity.finish())
                        .setCancelable(false)
                        .show();
                return;
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode != 1) return;

        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                new MaterialAlertDialogBuilder(activity, R.style.MyAlertDialog)
                        .setMessage("你禁止了唯ID运行必需的权限，应用即将退出，请到设置页开启后重新打开应用。")
                        .setPositiveButton("去设置", (d, w) -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivity(intent);
                            activity.finish();
                        })
                        .setCancelable(false)
                        .show();

                return;
            }
        }
    }
}
