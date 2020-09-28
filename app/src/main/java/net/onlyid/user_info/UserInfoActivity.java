package net.onlyid.user_info;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.databinding.ActivityUserInfoBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

public class UserInfoActivity extends AppCompatActivity {
    static final String TAG = "UserInfoActivity";
    static final String TYPE = "type";
    ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            User user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatarUrl).into(binding.avatarImageView);
            binding.nicknameTextView.setText(user.nickname);
            binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "-" : user.mobile);
            binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "-" : user.email);
            binding.genderTextView.setText(user.gender == null ? "-" : user.gender.toLocalizedString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        HttpUtil.get("app/user", (c, s) -> {
            Utils.sharedPreferences.edit().putString(Constants.USER, s).apply();
            init();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    public void avatar(View v) {
        Intent intent = new Intent(this, EditAvatarActivity.class);
        startActivity(intent);
    }

    public void nickname(View v) {
        Intent intent = new Intent(this, EditBasicActivity.class);
        intent.putExtra(TYPE, "nickname");
        startActivity(intent);
    }

    public void mobile(View v) {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra(TYPE, "mobile");
        startActivity(intent);
    }

    public void email(View v) {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra(TYPE, "email");
        startActivity(intent);
    }

    public void gender(View v) {
        Intent intent = new Intent(this, EditBasicActivity.class);
        intent.putExtra(TYPE, "gender");
        startActivity(intent);
    }

    public void password(View v) {
        Intent intent = new Intent(this, EditPasswordActivity.class);
        startActivity(intent);
    }
}