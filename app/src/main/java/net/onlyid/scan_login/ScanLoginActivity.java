package net.onlyid.scan_login;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.authorization.AuthorizeActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanLoginActivity extends AppCompatActivity {
    String uid, clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_login);

        Utils.showLoading(this);

        String text = getIntent().getStringExtra("scanResult");
        handleScanResult(text);
    }

    void handleScanResult(String text) {
        try {
            JSONObject obj = new JSONObject(text);
            uid = obj.getString("uid");
            clientId = obj.getString("clientId");

            if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(clientId))
                throw new Exception();

            AuthorizeActivity.startIfNecessary(this, clientId, () -> handleAuthorizeResult(true));
        } catch (Exception e) {
            e.printStackTrace();

            Drawable drawable = getDrawable(R.drawable.ic_error);
            drawable.setTint(0xeff44336);
            new MaterialAlertDialogBuilder(this)
                    .setIcon(drawable)
                    .setTitle("这不是唯ID的二维码")
                    .setPositiveButton("确定", (d, w) -> finish())
                    .show();
        }
    }

    void handleAuthorizeResult(boolean result) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("result", result);
            obj.put("uid", uid);
            obj.put("clientId", clientId);
            MyHttp.post("/scan-login", obj, (resp) -> finish());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AuthorizeActivity.AUTHORIZE) {
            handleAuthorizeResult(resultCode == RESULT_OK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.currentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication.currentActivity = null;
    }
}
