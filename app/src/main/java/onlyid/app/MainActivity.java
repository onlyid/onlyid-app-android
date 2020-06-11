package onlyid.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import okhttp3.Call;
import okhttp3.HttpUrl;
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
        HttpUrl url = HttpUtil.urlBuilder().addPathSegment("user").build();
        HttpUtil.get(url, (Call call, String s) -> {
            User user = Utils.objectMapper.readValue(s, User.class);
            Log.d(TAG, "refreshUserInfo: " + user.nickname);
        });
    }
}
