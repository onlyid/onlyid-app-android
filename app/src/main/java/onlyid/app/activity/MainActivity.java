package onlyid.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;

import okhttp3.Call;
import onlyid.app.Constants;
import onlyid.app.HttpUtil;
import onlyid.app.MyApplication;
import onlyid.app.R;
import onlyid.app.Utils;
import onlyid.app.entity.User;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        this.refreshUserInfo();
    }

    void refreshUserInfo() {
        User user = null;
        try {
            String s = Utils.preferences.getString(Constants.USER, null);
            if (s != null) {
                user = Utils.objectMapper.readValue(s, User.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (user == null) {
            login();
            return;
        }

        HttpUtil.get("user", new HttpUtil.MyCallback() {
            @Override
            public void onSuccess(Call call, String s) throws Exception {
                User user = Utils.objectMapper.readValue(s, User.class);
                Log.d(TAG, "refreshUserInfo: " + Utils.objectMapper.writeValueAsString(user));
            }

            @Override
            public boolean onResponseFailure(Call call, int code, String s) {
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
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    public void scanLogin(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    public void devices(View v) {
        Intent intent = new Intent(this, DevicesActivity.class);
        startActivity(intent);
    }

    public void apps(View v) {
        Intent intent = new Intent(this, AppsActivity.class);
        startActivity(intent);
    }
}
