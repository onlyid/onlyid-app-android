package net.onlyid.scan_login;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.R;
import net.onlyid.authorization.AuthorizeActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.entity.Client;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanLoginActivity extends AppCompatActivity {
    static final int AUTHORIZE = 1;
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
                throw new Exception("uid或clientId为空");

            MyHttp.get("/user-client-links/" + clientId + "/check", (resp) -> {
                JSONObject respBody = new JSONObject(resp);
                String clientString = respBody.getString("client");
                Client client = Utils.gson.fromJson(clientString, Client.class);
                if (respBody.getBoolean("linked")) {
                    handleAuthResult(true);
                } else {
                    Intent intent = new Intent(this, AuthorizeActivity.class);
                    intent.putExtra("client", client);
                    intent.putExtra("uid", uid);
                    //noinspection deprecation
                    startActivityForResult(intent, AUTHORIZE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

            Drawable drawable = getDrawable(R.drawable.ic_error);
            drawable.setTint(0xeff44336);
            new MaterialAlertDialogBuilder(this)
                    .setIcon(drawable)
                    .setTitle("这不是唯ID的二维码")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    void handleAuthResult(boolean result) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("result", result);
            obj.put("uid", uid);
            obj.put("clientId", clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyHttp.post("/scan-login", obj, (resp) -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == AUTHORIZE) {
            handleAuthResult(resultCode == RESULT_OK);
        }
    }
}
