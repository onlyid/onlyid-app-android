package net.onlyid.scan_login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.HttpUtil;
import net.onlyid.Utils;
import net.onlyid.databinding.ActivityAuthorizeBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthorizeActivity extends AppCompatActivity {
    static final String TAG = AuthorizeActivity.class.getSimpleName();
    ActivityAuthorizeBinding binding;
    String uid, clientId;
    Client client;
    User user;
    JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Glide.with(this).load(user.avatarUrl).into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);

        String result = getIntent().getStringExtra("result");
        Log.d(TAG, "result: " + result);
        try {
            jsonObject = new JSONObject(result);
            uid = jsonObject.getString("uid");
            clientId = jsonObject.getString("clientId");
            if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(clientId)) {
                throw new Exception("uid或clientId为空");
            }

            HttpUtil.get("app/clients/" + clientId, (c, s) -> {
                client = Utils.objectMapper.readValue(s, Client.class);
                Glide.with(this).load(client.iconUrl).into(binding.iconImageView);
                binding.clientNameTextView.setText(client.name);
                binding.tipTextView.setText("「" + client.name + "」将获得你的手机号、昵称等账号信息。");
            });
        } catch (Exception e) {
            e.printStackTrace();
            binding.illegalQrCode.setVisibility(View.VISIBLE);
            binding.authorizeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    public void back(View v) {
        finish();
    }

    public void login(View v) {
        handleResult(true);
    }

    public void reject(View v) {
        handleResult(false);
    }

    void handleResult(boolean result) {
        try {
            jsonObject.put("result", result);
            jsonObject.put("keepLoggedIn", binding.checkBox.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpUtil.post("oauth/scan-login-result", jsonObject, (c, s) -> {
            finish();
        });
    }
}