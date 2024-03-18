package net.onlyid.user_info;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityUserInfoBinding;
import net.onlyid.entity.User;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class UserInfoActivity extends BaseActivity {
    static final String TAG = UserInfoActivity.class.getSimpleName();
    static final String TYPE = "type";
    ActivityUserInfoBinding binding;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        user = MyApplication.getCurrentUser();
        Glide.with(this).load(user.avatar).into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);
        binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "点击设置" : user.mobile);
        binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "点击设置" : user.email);
        binding.genderTextView.setText(user.gender == null ? "点击设置" : user.gender.toLocalizedString());
        binding.birthdayTextView.setText(user.birthDate == null ? "点击设置" : user.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        binding.locationTextView.setText(TextUtils.isEmpty(user.province) ? "点击设置" : user.province + "-" + user.city);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
        MyHttp.get("/user", (s) -> {
            Utils.pref.edit().putString(Constants.USER, s).apply();
            init();
        });
    }

    void submit() {
        Utils.showLoading(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.gson.toJson(user));
            MyHttp.put("/user", jsonObject, (s) -> {
                Utils.hideLoading();
                refresh();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String[] genderOptions = {"男", "女", "其他", "暂不设置"};
        new MaterialAlertDialogBuilder(this)
                .setItems(genderOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            user.gender = User.Gender.MALE;
                            break;
                        case 1:
                            user.gender = User.Gender.FEMALE;
                            break;
                        case 2:
                            user.gender = User.Gender.OTHER;
                            break;
                        case 3:
                            user.gender = null;
                    }
                    submit();
                })
                .show();
    }

    public void birthday(View v) {
        LocalDate localDate = user.birthDate == null ? LocalDate.now() : user.birthDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DatePickerDialog, (view, year, month, dayOfMonth) -> {
            user.birthDate = LocalDate.of(year, month + 1, dayOfMonth);
            submit();
        }, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "暂不设置", (dialog, which) -> {
            user.birthDate = null;
            submit();
        });
        LocalDateTime min = LocalDateTime.of(1900, 1, 1, 0, 0);
        datePickerDialog.getDatePicker().setMinDate(min.toInstant(ZoneOffset.ofHours(0)).toEpochMilli());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void location(View v) {
        Intent intent = new Intent(this, EditLocationActivity.class);
        startActivity(intent);
    }

    public void password(View v) {
        Intent intent = new Intent(this, EditPasswordActivity.class);
        startActivity(intent);
    }
}
