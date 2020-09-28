package net.onlyid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.databinding.ActivityOauthBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.OAuthConfig;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONObject;

import okhttp3.Call;

public class OAuthActivity extends AppCompatActivity {
    public static final int REQUEST_OAUTH = 2;
    static final String TAG = OAuthActivity.class.getSimpleName();
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

    void init() {
        try {
            String configString = getIntent().getStringExtra("oauthConfig");
            config = Utils.objectMapper.readValue(configString, OAuthConfig.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HttpUtil.get("app/user", new HttpUtil.MyCallback() {
            @Override
            public void onSuccess(Call c, String s) {
                promptAuthorizeIfNecessary(OAuthActivity.this, config);
            }

            @Override
            public boolean onResponseFailure(Call c, int code, String s) {
                if (code == 401) {

                    Intent intent = new Intent(OAuthActivity.this, LoginActivity.class);
                    intent.putExtra("oauthConfig", config);
                    startActivityForResult(intent, REQUEST_OAUTH);

                    return true;
                }

                return false;
            }
        });
    }

    public static void promptAuthorizeIfNecessary(Activity activity, OAuthConfig config) {
        HttpUtil.get("app/user-client-links/" + config.clientId + "/check", (c, s) -> {
            JSONObject respBody = new JSONObject(s);
            String clientString = respBody.getString("client");
            Client client = Utils.objectMapper.readValue(clientString, Client.class);
            if (respBody.getBoolean("linked")) {
                callback(activity, config, client, true);
            } else {
                Intent intent = new Intent(activity, AuthorizeActivity.class);
                intent.putExtra("client", client);
                intent.putExtra("oauthConfig", config);
                activity.startActivityForResult(intent, REQUEST_OAUTH);
            }
        });
    }

    public static void callback(Activity activity, OAuthConfig config, Client client, boolean result) {
        Intent data = new Intent();
        if (result) {
            HttpUtil.post("app/authorize-client/" + client.id, new JSONObject(), (c, s) -> {
                JSONObject respBody = new JSONObject(s);
                String code = respBody.getString("authorizationCode");
                data.putExtra(EXTRA_CODE, code);
                data.putExtra(EXTRA_STATE, config.state);
                activity.setResult(RESULT_OK, data);
                activity.finish();
            });
        } else {
            activity.setResult(RESULT_CANCELED);
            activity.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OAUTH) {
            setResult(resultCode, data);
            finish();
        }
    }
}