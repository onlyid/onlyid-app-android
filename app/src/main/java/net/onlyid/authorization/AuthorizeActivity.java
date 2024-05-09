package net.onlyid.authorization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityAuthorizeBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.User;

import org.json.JSONObject;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class AuthorizeActivity extends BaseActivity {
    public static final int AUTHORIZE = 1;
    static final String TAG = "AuthorizeActivity";

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

        int radius = Utils.dp2px(this, 5);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);

        Glide.with(this).load(client.iconUrl)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.iconImageView);
        binding.clientNameTextView.setText(client.name);
        binding.tipTextView.setText("「" + client.name + "」将获得你的手机号、昵称等账号信息。");

        binding.loginButton.setOnClickListener((v) -> login());
        binding.cancelButton.setOnClickListener((v) -> cancel());
    }

    void login() {
        setResult(RESULT_OK);
        finish();
    }

    void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public interface CheckCallback {
        void authorized();
    }

    public static void startIfNecessary(Activity activity, String clientId, CheckCallback callback) {
        MyHttp.get("/user-client-links/" + clientId + "/check", (resp) -> {
            JSONObject obj = new JSONObject(resp);
            String clientString = obj.getString("client");
            Client client = Utils.gson.fromJson(clientString, Client.class);
            if (obj.getBoolean("linked")) {
                callback.authorized();
            } else {
                Intent intent = new Intent(activity, AuthorizeActivity.class);
                intent.putExtra("client", client);
                activity.startActivityForResult(intent, AUTHORIZE);
            }
        });
    }
}
