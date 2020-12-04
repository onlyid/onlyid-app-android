package net.onlyid.user_info;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.databinding.ActivityUserInfoBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UserInfoActivity extends AppCompatActivity {
    static final String TAG = UserInfoActivity.class.getSimpleName();
    static final String TYPE = "type";
    ActivityUserInfoBinding binding;
    User user;

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
            user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatarUrl).into(binding.avatarImageView);
            binding.nicknameTextView.setText(user.nickname);
            binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "点击设置" : user.mobile);
            binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "点击设置" : user.email);
            binding.genderTextView.setText(user.gender == null ? "点击设置" : user.gender.toLocalizedString());
            binding.birthdayTextView.setText(user.birthday == null ? "点击设置" : user.birthday.format(Constants.DATE_FORMATTER));
            binding.locationTextView.setText(TextUtils.isEmpty(user.location) ? "点击设置" : TextUtils.join("-", user.location.split(" ")));
            binding.bioTextView.setText(TextUtils.isEmpty(user.bio) ? "点击设置" : user.bio);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
        HttpUtil.get("app/user", (c, s) -> {
            Utils.sharedPreferences.edit().putString(Constants.USER, s).apply();
            init();
        });
    }

    void submit() {
        Utils.showLoadingDialog(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.objectMapper.writeValueAsString(user));
            HttpUtil.put("app/user", jsonObject, (c, s) -> {
                Utils.loadingDialog.dismiss();
                Utils.showToast("已保存", Toast.LENGTH_SHORT);
                refresh();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String[] genderOptions = {"男", "女", "其他", "暂不设置"};
        new MaterialAlertDialogBuilder(this, R.style.MyAlertDialog)
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
        LocalDate localDate = user.birthday == null ? LocalDate.now() : user.birthday;
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialog,  (view, year, month, dayOfMonth) -> {
            user.birthday = LocalDate.of(year, month + 1, dayOfMonth);
            submit();
        }, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "暂不设置", (dialog, which) -> {
            user.birthday = null;
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

    public void bio(View v) {
        Intent intent = new Intent(this, EditBasicActivity.class);
        intent.putExtra(TYPE, "bio");
        startActivity(intent);
    }

    public void password(View v) {
        Intent intent = new Intent(this, EditPasswordActivity.class);
        startActivity(intent);
    }
}