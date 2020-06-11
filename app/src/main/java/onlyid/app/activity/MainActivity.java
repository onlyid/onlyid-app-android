package onlyid.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.HttpUrl;
import onlyid.app.HttpUtil;
import onlyid.app.MyApplication;
import onlyid.app.R;
import onlyid.app.Utils;
import onlyid.app.entity.User;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.refreshUserInfo();
    }

    void refreshUserInfo() {
        User user = Utils.deserialize(Utils.preferences.getString("user", null));
        if (user == null) {
            login();
            return;
        }

        HttpUrl url = HttpUtil.urlBuilder().addPathSegment("user").build();
        HttpUtil.get(url, new HttpUtil.MyCallback() {
            @Override
            public void onSuccess(Call call, String s) throws Exception {
                User user = Utils.objectMapper.readValue(s, User.class);
                Log.d(TAG, "refreshUserInfo: " + user.nickname);
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
}
