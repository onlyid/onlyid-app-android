package net.onlyid.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import net.onlyid.common.BaseActivity;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityLogin1Binding;
import net.onlyid.entity.Entity1;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class LoginActivity extends BaseActivity {
    static final String TAG = "LoginActivity";
    ActivityLogin1Binding binding;
    Entity1 user;
    String loginType = "password";
    EditText passwordEditText, otpEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogin1Binding.inflate(getLayoutInflater());
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
        Log.e(TAG, "todo submit");
    }
}
