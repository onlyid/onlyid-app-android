package net.onlyid;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.common.Constants;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityAuthorizeBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.OAuthConfig;
import net.onlyid.entity.User;
import net.onlyid.scan_login.ScanLoginActivity;

public class AuthorizeActivity extends AppCompatActivity {
    static final String TAG = AuthorizeActivity.class.getSimpleName();
    ActivityAuthorizeBinding binding;
    Client client;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        client = (Client) getIntent().getSerializableExtra("client");
        String userString = Utils.pref.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Glide.with(this).load(user.avatar).into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);

        Glide.with(this).load(client.iconUrl).into(binding.iconImageView);
        binding.clientNameTextView.setText(client.name);
        binding.tipTextView.setText("「" + client.name + "」将获得你的手机号、昵称等账号信息。");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    public void login(View v) {
        handleResult(true);
    }

    public void reject(View v) {
        handleResult(false);
    }

    void handleResult(boolean result) {
        if (Client.Type.WEB.equals(client.type)) {
            String uid = getIntent().getStringExtra("uid");
            ScanLoginActivity.callback(this, uid, client, result);
        } else {
            OAuthConfig config = (OAuthConfig) getIntent().getSerializableExtra("oauthConfig");
            OAuthActivity.callback(this, config, client, result);
        }
    }
}
