package net.onlyid.security;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivitySecurityBinding;
import net.onlyid.entity.User;
import net.onlyid.user_profile.EditAccountActivity;

public class SecurityActivity extends BaseActivity {
    ActivitySecurityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();

        binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "-" : user.mobile);
        binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "-" : user.email);

        binding.mobileLayout.setOnClickListener(v -> mobile());
        binding.emailLayout.setOnClickListener(v -> email());
        binding.passwordLayout.setOnClickListener(v -> password());
        binding.deviceLayout.setOnClickListener(v -> loginDevice());
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

    void refresh() {
        MyHttp.get("/user", (resp) -> {
            Utils.pref.edit().putString(Constants.USER, resp).apply();
            initView();
        });
    }

    void mobile() {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("type", "mobile");
        startActivity(intent);
    }

    void email() {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("type", "email");
        startActivity(intent);
    }

    void password() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    void loginDevice() {
        Intent intent = new Intent(this, LoginDeviceActivity.class);
        startActivity(intent);
    }
}
