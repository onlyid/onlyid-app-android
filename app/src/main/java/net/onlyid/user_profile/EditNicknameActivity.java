package net.onlyid.user_profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditNicknameBinding;
import net.onlyid.entity.User;
import net.onlyid.login.SignUpActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class EditNicknameActivity extends BaseActivity {
    static final String TAG = "EditNicknameActivity";
    ActivityEditNicknameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNicknameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();
        binding.nicknameEditText.setText(user.nickname);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            validateFields();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void validateFields() {
        String nickname = binding.nicknameEditText.getText().toString();
        String errMsg = null;

        if (TextUtils.isEmpty(nickname))
            errMsg = "昵称不能为空";
        else if (SignUpActivity.getLength(nickname) > 20)
            errMsg = "昵称不能超10字（英文字母算半个字）";

        if (TextUtils.isEmpty(errMsg))
            submit();
        else
            Utils.showAlert(this, errMsg);
    }

    void submit() {
        User user = MyApplication.getCurrentUser();
        user.nickname = binding.nicknameEditText.getText().toString();
        try {
            JSONObject obj = new JSONObject(Utils.gson.toJson(user));
            MyHttp.put("/user", obj, (resp) -> {
                Utils.showToast("保存成功", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
