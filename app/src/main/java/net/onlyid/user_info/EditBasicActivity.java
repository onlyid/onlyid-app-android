package net.onlyid.user_info;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.HttpUtil;
import net.onlyid.R;
import net.onlyid.Utils;
import net.onlyid.databinding.ActivityEditBasicBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class EditBasicActivity extends AppCompatActivity {
    ActivityEditBasicBinding binding;
    String type;
    ActionBar actionBar;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBasicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        type = getIntent().getStringExtra(UserInfoActivity.TYPE);
        switch (type) {
            case "nickname":
                binding.tipTextView.setText("起一个好名字，让大家更容易记住你。");
                binding.nicknameInput.setVisibility(View.VISIBLE);
                binding.nicknameInput.getEditText().setText(user.nickname);
                actionBar.setTitle("修改昵称");
                break;
            case "gender":
                binding.tipTextView.setText("选择你的性别。");
                binding.genderRadioGroup.setVisibility(View.VISIBLE);
                actionBar.setTitle("修改性别");
                if (user.gender != null) {
                    switch (user.gender) {
                        case MALE:
                            binding.genderRadioGroup.check(R.id.male);
                            break;
                        case FEMALE:
                            binding.genderRadioGroup.check(R.id.female);
                            break;
                        case OTHER:
                            binding.genderRadioGroup.check(R.id.other);
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.save:
                validate();
                return true;
            default:
                return false;
        }
    }

    void validate() {
        switch (type) {
            case "nickname":
                String nickname = binding.nicknameInput.getEditText().getText().toString();
                if (TextUtils.isEmpty(nickname)) {
                    Utils.showAlertDialog(this, "昵称不能为空");
                    return;
                }
                break;
            case "gender":
                int checkedId = binding.genderRadioGroup.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Utils.showAlertDialog(this, "请选择一项");
                    return;
                }
                break;
        }

        submit();
    }

    void submit() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", type);
            switch (type) {
                case "nickname":
                    String nickname = binding.nicknameInput.getEditText().getText().toString();
                    jsonObject.put("value", nickname);
                    break;
                case "gender":
                    User.Gender gender;
                    switch (binding.genderRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.male:
                            gender = User.Gender.MALE;
                            break;
                        case R.id.female:
                            gender = User.Gender.FEMALE;
                            break;
                        default:
                            gender = User.Gender.OTHER;
                            break;
                    }
                    jsonObject.put("value", gender.toString());
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoadingDialog(this);
        HttpUtil.put("app/user", jsonObject, (c, s) -> {
            Utils.loadingDialog.dismiss();
            Utils.showToast("已保存", Toast.LENGTH_SHORT);
            finish();
        });
    }
}