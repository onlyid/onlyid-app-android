package net.onlyid.user_profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.Glide;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityUserProfileBinding;
import net.onlyid.entity.User;

import java.time.format.DateTimeFormatter;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class UserProfileActivity extends BaseActivity {
    static final String TAG = "UserProfileActivity";
    ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();
        int radius = Utils.dp2px(this, 7);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);

        binding.nicknameTextView.setText(user.nickname);
        binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "-" : user.mobile);
        binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "-" : user.email);
        binding.genderTextView.setText(user.gender == null ? "-" : user.gender.toLocalizedString());
        binding.birthDateTextView.setText(user.birthDate == null ? "-" : user.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        binding.locationTextView.setText(TextUtils.isEmpty(user.province) ? "-" : user.province + "-" + user.city);

        binding.avatarLayout.setOnClickListener((v) -> avatar());
        binding.nicknameLayout.setOnClickListener((v) -> nickname());
        binding.mobileLayout.setOnClickListener((v) -> mobile());
        binding.emailLayout.setOnClickListener((v) -> email());
        binding.genderLayout.setOnClickListener((v) -> gender());
        binding.birthDateLayout.setOnClickListener((v) -> birthDate());
        binding.locationLayout.setOnClickListener((v) -> location());
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

    void avatar() {
        Intent intent = new Intent(this, EditAvatarActivity.class);
        startActivity(intent);
    }

    void nickname() {
        Intent intent = new Intent(this, EditNicknameActivity.class);
        startActivity(intent);
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

    void gender() {
        Intent intent = new Intent(this, EditGenderActivity.class);
        startActivity(intent);
    }

    void birthDate() {
        Intent intent = new Intent(this, EditBirthDateActivity.class);
        startActivity(intent);
    }

    void location() {
        Intent intent = new Intent(this, EditLocationActivity.class);
        startActivity(intent);
    }
}
