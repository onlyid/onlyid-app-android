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
import net.onlyid.R;
import net.onlyid.databinding.ActivityEditBasicBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

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
            case "bio":
                binding.tipTextView.setText("填写个人简介，让大家了解你。");
                binding.bioInput.setVisibility(View.VISIBLE);
                binding.bioInput.getEditText().setText(user.bio);
                actionBar.setTitle("修改简介");
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
            case "bio":
                // do nothing
                break;
        }

        submit();
    }

    void submit() {
        switch (type) {
            case "nickname":
                user.nickname = binding.nicknameInput.getEditText().getText().toString();
                break;
            case "bio":
                user.bio = binding.bioInput.getEditText().getText().toString();
                break;
        }
        Utils.showLoadingDialog(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.objectMapper.writeValueAsString(user));
            HttpUtil.put("app/user", jsonObject, (c, s) -> {
                Utils.loadingDialog.dismiss();
                Utils.showToast("已保存", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}