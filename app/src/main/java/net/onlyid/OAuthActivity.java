package net.onlyid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.authorization.AuthorizeActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityOauthBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.OAuthConfig;
import net.onlyid.login.AccountActivity;

import org.json.JSONObject;

public class OAuthActivity extends AppCompatActivity {
    static final String TAG = "OAuthActivity";
    static final int LOGIN = 2, AUTHORIZE = 3;
    static final String EXTRA_CODE = "extraCode";
    static final String EXTRA_STATE = "extraState";
    ActivityOauthBinding binding;
    OAuthConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOauthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("login", false)) {
            //noinspection deprecation
            startActivityForResult(new Intent(this, AccountActivity.class), LOGIN);
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

    void init() {
        String configString = getIntent().getStringExtra("oauthConfig");
        config = Utils.gson.fromJson(configString, OAuthConfig.class);

        // 首先判断当前是否已经登录
        MyHttp.get("/user", (resp) -> promptAuthorizeIfNecessary());
    }

    void promptAuthorizeIfNecessary() {
        MyHttp.get("/user-client-links/" + config.clientId + "/check", (resp) -> {
            JSONObject respBody = new JSONObject(resp);
            String clientString = respBody.getString("client");
            Client client = Utils.gson.fromJson(clientString, Client.class);
            if (respBody.getBoolean("linked")) {
                handleAuthResult(true);
            } else {
                Intent intent = new Intent(this, AuthorizeActivity.class);
                intent.putExtra("client", client);
                //noinspection deprecation
                startActivityForResult(intent, AUTHORIZE);
            }
        });
    }

    void handleAuthResult(boolean result) {
        if (result) {
            MyHttp.post("/authorize-client/" + config.clientId, new JSONObject(), (resp) -> {
                JSONObject respBody = new JSONObject(resp);
                String code = respBody.getString("authorizationCode");

                Intent data = new Intent();
                data.putExtra(EXTRA_CODE, code);
                data.putExtra(EXTRA_STATE, config.state);
                setResult(RESULT_OK, data);
                finish();
            });
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN) {
            if (resultCode == RESULT_OK) {
                promptAuthorizeIfNecessary();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else if (requestCode == AUTHORIZE) {
            // 到时再看一下，back和reject是否需要区分对待
            handleAuthResult(resultCode == RESULT_OK);
        }
    }
}
