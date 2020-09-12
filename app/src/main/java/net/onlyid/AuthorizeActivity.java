package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.databinding.ActivityAuthorizeBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.OAuthConfig;
import net.onlyid.entity.User;
import net.onlyid.scan_login.ResultActivity;

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
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        String clientString = getIntent().getStringExtra("client");
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
            client = Utils.objectMapper.readValue(clientString, Client.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Glide.with(this).load(user.avatarUrl).into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);

        Glide.with(this).load(client.iconUrl).into(binding.iconImageView);
        binding.clientNameTextView.setText(client.name);
        binding.tipTextView.setText("「" + client.name + "」将获得你的手机号、昵称等账号信息。");
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

    public void login(View v) {
        goResult(true);
    }

    public void reject(View v) {
        goResult(false);
    }

    void goResult(boolean result) {
        if (Client.Type.WEB.equals(client.type)) {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("result", result);
            intent.putExtra("client", getIntent().getStringExtra("client"));
            intent.putExtra("uid", getIntent().getStringExtra("uid"));
            startActivity(intent);
            finish();
        } else {
            OAuthConfig config = (OAuthConfig) getIntent().getSerializableExtra("oauthConfig");
            OAuthActivity.callback(this, config, client, result);
        }
    }
}