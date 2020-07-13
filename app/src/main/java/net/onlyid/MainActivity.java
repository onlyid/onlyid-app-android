package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.authorized_app.AuthorizedAppActivity;
import net.onlyid.entity.User;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.trusted_device.TrustedDeviceActivity;
import net.onlyid.user_info.UserInfoActivity;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.refreshUserInfo();
    }

    void refreshUserInfo() {
        User user = null;
        try {
            String s = Utils.preferences.getString(Constants.USER, null);
            if (s != null) user = Utils.objectMapper.readValue(s, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (user == null) {
            login();
            return;
        }

        HttpUtil.get("app/user", new HttpUtil.MyCallback() {
            @Override
            public void onSuccess(Call c, String s) {
                Utils.preferences.edit().putString(Constants.USER, s).apply();
            }

            @Override
            public boolean onResponseFailure(Call c, int code, String s) {
                if (code == 401) {
                    login();

                    Toast.makeText(MyApplication.context, "登录已失效", Toast.LENGTH_LONG).show();
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
}
