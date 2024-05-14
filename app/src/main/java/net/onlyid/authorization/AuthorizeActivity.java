package net.onlyid.authorization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setElevation(0);

        initView();
    }

    void initView() {
        Client client = (Client) getIntent().getSerializableExtra("client");
        User user = MyApplication.getCurrentUser();

        int radius = Utils.dp2px(this, 10);
        Glide.with(this).load(client.iconUrl)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.iconImageView);
        binding.clientTextView.setText(client.name);

        int radius1 = Utils.dp2px(this, 5);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius1, 0))
                .into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);
        binding.accountTextView.setText(TextUtils.isEmpty(user.email) ? user.mobile : user.email);

        binding.loginButton.setOnClickListener((v) -> authorize());
        binding.cancelButton.setOnClickListener((v) -> cancel());
    }

    void authorize() {
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
        MyHttp.get("/user/client-links/" + clientId + "/check", (resp) -> {
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
