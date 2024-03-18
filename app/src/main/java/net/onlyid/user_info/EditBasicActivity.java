package net.onlyid.user_info;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditBasicBinding;
import net.onlyid.entity.User;

import org.json.JSONObject;

public class EditBasicActivity extends BaseActivity {
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

        init();
    }

    void init() {
        user = MyApplication.getCurrentUser();

        type = getIntent().getStringExtra(UserInfoActivity.TYPE);
        switch (type) {
            case "nickname":
                binding.tipTextView.setText("起一个好名字，让大家更容易记住你。");
                binding.nicknameInput.setVisibility(View.VISIBLE);
                binding.nicknameInput.getEditText().setText(user.nickname);
                actionBar.setTitle("修改昵称");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            validate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void validate() {
        switch (type) {
            case "nickname":
                String nickname = binding.nicknameInput.getEditText().getText().toString();
                if (TextUtils.isEmpty(nickname)) {
                    Utils.showAlert(this, "昵称不能为空");
                    return;
                }
                break;
        }

        submit();
    }

    void submit() {
        switch (type) {
            case "nickname":
                user.nickname = binding.nicknameInput.getEditText().getText().toString();
                break;
        }
        Utils.showLoading(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.gson.toJson(user));
            MyHttp.put("/user", jsonObject, (s) -> {
                Utils.hideLoading();
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
