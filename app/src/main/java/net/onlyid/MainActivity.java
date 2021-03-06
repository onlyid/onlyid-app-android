package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.authorized_app.AuthorizedAppActivity;
import net.onlyid.databinding.ActivityMainBinding;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.trusted_device.TrustedDeviceActivity;
import net.onlyid.user_info.UserInfoActivity;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.PermissionUtil;
import net.onlyid.util.UpdateUtil;
import net.onlyid.util.Utils;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    ActivityMainBinding binding;
    UpdateUtil updateUtil;
    PermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        refreshUserInfo();

        updateUtil = new UpdateUtil(this);
        updateUtil.check();

        permissionUtil = new PermissionUtil(this);
        permissionUtil.check();

        MyApplication.mainActivity = this;
    }

    void refreshUserInfo() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        if (TextUtils.isEmpty(userString)) {
            login();
            return;
        }

        HttpUtil.get("app/user", new HttpUtil.MyCallback() {
            @Override
            public void onSuccess(Call c, String s) {
                Utils.sharedPreferences.edit().putString(Constants.USER, s).apply();
            }

            @Override
            public boolean onResponseFailure(Call c, int code, String s) {
                if (code == 401) {
                    login();

                    Utils.showToast("登录已失效", Toast.LENGTH_SHORT);
                    return true;
                }

                return false;
            }
        });
    }

    void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void userInfo(View v) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }

    public void scanLogin(View v) {
        Intent intent = new Intent(this, ScanLoginActivity.class);
        startActivity(intent);
    }

    public void trustedDevice(View v) {
        Intent intent = new Intent(this, TrustedDeviceActivity.class);
        startActivity(intent);
    }

    public void authorizedApp(View v) {
        Intent intent = new Intent(this, AuthorizedAppActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUtil.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
    }
}
