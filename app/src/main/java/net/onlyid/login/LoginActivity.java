package net.onlyid.login;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityLoginBinding;
import net.onlyid.entity.Entity1;
import net.onlyid.entity.Session;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        getSupportActionBar().setElevation(0);

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
        String token = respBody.getString(Constants.TOKEN);
        String userString = respBody.getString(Constants.USER);
        Utils.pref.edit().putString(Constants.TOKEN, token).putString("user", userString).apply();

        updateSession(token);

        activity.setResult(RESULT_OK);
        activity.finish();
    }

    static void updateSession(String token) {
        List<Session> sessionList;
        String sessionListString = Utils.pref.getString(Constants.SESSION_LIST, null);

        if (TextUtils.isEmpty(sessionListString)) sessionList = new ArrayList<>();
        else sessionList = Utils.gson.fromJson(sessionListString, new TypeToken<List<Session>>() {});

        User user = MyApplication.getCurrentUser();
        Session session = new Session();
        session.token = token;
        session.user = user;
        session.expireDate = LocalDateTime.now().plusDays(90);

        // 在列表有这个用户的情况下，用户仍然新增了同一个账号，那就删掉原来的
        sessionList.removeIf((session1 -> session1.user.id.equals(user.id)));

        sessionList.add(session);

        if (sessionList.size() > 3) sessionList.remove(0);

        Utils.pref.edit().putString(Constants.SESSION_LIST, Utils.gson.toJson(sessionList)).apply();
    }
}
