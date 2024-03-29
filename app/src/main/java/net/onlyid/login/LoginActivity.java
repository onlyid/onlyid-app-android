package net.onlyid.login;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityLoginBinding;
import net.onlyid.entity.Entity1;

import org.json.JSONException;
import org.json.JSONObject;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class LoginActivity extends BaseActivity {
    static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;
    Entity1 user;
    String loginType = "password";
    EditText passwordEditText, otpEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (Entity1) getIntent().getSerializableExtra("user");

        initView();
    }

    void initView() {
        int radius = Utils.dp2px(this, 5);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);

        binding.nicknameTextView.setText(user.nickname);
        binding.accountTextView.setText(user.account);
        binding.submitButton.setOnClickListener((v) -> validateFields());
        binding.toggleLoginButton.setOnClickListener((v) -> toggleLogin());
        binding.sendOtpButton.recipientCallback = () -> user.account;

        passwordEditText = binding.passwordInput.getEditText();
        otpEditText = binding.otpInput.getEditText();
    }

    void toggleLogin() {
        loginType = "password".equals(loginType) ? "otp" : "password";

        if ("password".equals(loginType)) {
            binding.otpLayout.setVisibility(View.GONE);
            binding.passwordInput.setVisibility(View.VISIBLE);
            binding.toggleLoginButton.setText("验证码登录");
        } else {
            binding.otpLayout.setVisibility(View.VISIBLE);
            binding.passwordInput.setVisibility(View.GONE);
            binding.toggleLoginButton.setText("密码登录");
        }
    }

    void validateFields() {
        if ("password".equals(loginType)) {
            String text = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(text)) {
                Utils.showAlert(this, "请输入密码");
                return;
            }
        } else {
            String text = otpEditText.getText().toString();

            if (TextUtils.isEmpty(text)) {
                Utils.showAlert(this, "请输入验证码");
                return;
            }
        }

        submit();
    }

    void submit() {
        JSONObject obj = new JSONObject();
        //noinspection HardwareIds
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        try {
            obj.put("account", user.account);
            obj.put("otp", otpEditText.getText().toString());
            obj.put("password", passwordEditText.getText().toString());
            obj.put("deviceId", deviceId);
            obj.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL);
            obj.put("deviceType", "android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyHttp.post("/auth/login", obj, resp -> completeLogin(this, resp));
    }

    static void completeLogin(Activity activity, String resp) throws JSONException {
        JSONObject respBody = new JSONObject(resp);
        Utils.pref.edit()
                .putString("token", respBody.getString("token"))
                .putString("user", respBody.getString(Constants.USER))
                .apply();

        activity.setResult(RESULT_OK);
        activity.finish();
    }
}
