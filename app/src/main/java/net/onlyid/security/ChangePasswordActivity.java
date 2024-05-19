package net.onlyid.security;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityChangePasswordBinding;
import net.onlyid.entity.User;
import net.onlyid.login.SignUpActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends BaseActivity {
    ActivityChangePasswordBinding binding;
    String recipient;
    String authType = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();
        recipient = TextUtils.isEmpty(user.mobile) ? user.email : user.mobile;

        binding.sendOtpButton.recipientCallback = () -> recipient;
        binding.tipTextView.setText("修改密码后，下次登录请使用新密码");

        binding.forgetButton.setOnClickListener(v -> {
            authType = "otp";

            SpannableString ss = new SpannableString("将发送验证码到 " + recipient);
            ForegroundColorSpan span = new ForegroundColorSpan(getColor(R.color.text_primary));
            ss.setSpan(span, 8, 8 + recipient.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tipTextView.setText(ss);

            binding.otpLayout.setVisibility(View.VISIBLE);
            binding.currentPasswordInput.setVisibility(View.GONE);
        });
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
        String currentPassword = binding.currentPasswordEditText.getText().toString();
        String otp = binding.otpEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String password1 = binding.password1EditText.getText().toString();

        if ("password".equals(authType)) {
            if (TextUtils.isEmpty(currentPassword)) {
                Utils.showAlert(this, "请输入原密码");
                return;
            }
        } else {
            if (TextUtils.isEmpty(otp)) {
                Utils.showAlert(this, "请输入验证码");
                return;
            }
        }

        if (TextUtils.isEmpty(password)) {
            Utils.showAlert(this, "请输入新密码");
            return;
        }
        if (!SignUpActivity.validatePassword(this, password))
            return;

        if (TextUtils.isEmpty(password1)) {
            Utils.showAlert(this, "请重复输入新密码");
            return;
        }
        if (!password1.equals(password)) {
            Utils.showAlert(this, "两次输入的密码不一致");
            return;
        }

        submit();
    }

    void submit() {
        String currentPassword = binding.currentPasswordEditText.getText().toString();
        String otp = binding.otpEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        JSONObject obj = new JSONObject();
        try {
            obj.put("currentPassword", currentPassword);
            obj.put("otp", otp);
            obj.put("account", recipient);
            obj.put("newPassword", password);
            MyHttp.put("/user/password", obj, (resp) -> {
                Utils.showToast("保存成功", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
