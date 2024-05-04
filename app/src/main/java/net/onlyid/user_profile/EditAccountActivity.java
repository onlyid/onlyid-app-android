package net.onlyid.user_profile;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.util.PatternsCompat;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditAccountBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class EditAccountActivity extends BaseActivity {
    static final String TAG = "EditAccountActivity";
    ActivityEditAccountBinding binding;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();
        type = getIntent().getStringExtra("type");
        String title, tip, hint;
        int inputType;

        if ("mobile".equals(type)) {
            if (TextUtils.isEmpty(user.mobile)) {
                title = "绑定手机号";
                tip = "绑定手机号后，可以使用手机号登录";
                hint = "手机号";
            } else {
                title = "修改手机号";
                tip = "当前手机号：" + user.mobile;
                hint = "新手机号";
            }
            inputType = InputType.TYPE_CLASS_PHONE;
        } else {
            if (TextUtils.isEmpty(user.email)) {
                title = "绑定邮箱";
                tip = "绑定邮箱后，可以使用邮箱登录";
                hint = "邮箱";
            } else {
                title = "修改邮箱";
                tip = "当前邮箱：" + user.email;
                hint = "新邮箱";
            }
            inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        }

        getSupportActionBar().setTitle(title);
        binding.tipTextView.setText(tip);
        binding.accountInput.setHint(hint);
        binding.accountEditText.setInputType(inputType);
        binding.sendOtpButton.recipientCallback = () -> binding.accountEditText.getText().toString();
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
        String account = binding.accountEditText.getText().toString();
        String otp = binding.otpEditText.getText().toString();
        String errMsg = null;

        if ("mobile".equals(type)) {
            if (TextUtils.isEmpty(account))
                errMsg = "手机号不能为空";
            else if (!Utils.isMobile(account))
                errMsg = "手机号格式不正确";
        } else {
            if (TextUtils.isEmpty(account))
                errMsg = "邮箱不能为空";
            else if (!PatternsCompat.EMAIL_ADDRESS.matcher(account).matches())
                errMsg = "邮箱格式不正确";
        }

        if (!TextUtils.isEmpty(errMsg)) {
            Utils.showAlert(this, errMsg);
            return;
        }

        if (TextUtils.isEmpty(otp)) {
            Utils.showAlert(this, "请输入验证码");
            return;
        }

        submit();
    }

    void submit() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("otp", binding.otpEditText.getText().toString());
            obj.put("account", binding.accountEditText.getText().toString());
            MyHttp.put("/user/account", obj, (resp) -> {
                Utils.showToast("保存成功", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
