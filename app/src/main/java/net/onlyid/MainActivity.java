package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.authorized_apps.AuthorizedAppsActivity;
import net.onlyid.databinding.ActivityMainBinding;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.trusted_devices.TrustedDevicesActivity;
import net.onlyid.user_info.UserInfoActivity;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        refreshUserInfo();
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

    public void trustedDevices(View v) {
        Intent intent = new Intent(this, TrustedDevicesActivity.class);
        startActivity(intent);
    }

    public void authorizedApps(View v) {
        Intent intent = new Intent(this, AuthorizedAppsActivity.class);
        startActivity(intent);
    }
}
