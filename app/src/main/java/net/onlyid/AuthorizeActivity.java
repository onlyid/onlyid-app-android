package net.onlyid;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityAuthorizeBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.User;

public class AuthorizeActivity extends BaseActivity {
    static final String TAG = AuthorizeActivity.class.getSimpleName();
    ActivityAuthorizeBinding binding;
    Client client;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        client = (Client) getIntent().getSerializableExtra("client");
        user = MyApplication.getCurrentUser();

        Glide.with(this).load(user.avatar).into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);

        Glide.with(this).load(client.iconUrl).into(binding.iconImageView);
        binding.clientNameTextView.setText(client.name);
        binding.tipTextView.setText("「" + client.name + "」将获得你的手机号、昵称等账号信息。");
    }

    public void login(View v) {
        setResult(RESULT_OK);
        finish();
    }

    public void reject(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
