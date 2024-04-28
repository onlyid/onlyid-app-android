package net.onlyid.user_profile;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditPasswordBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class EditPasswordActivity extends BaseActivity {
    ActivityEditPasswordBinding binding;
    User user;
    String recipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        user = MyApplication.getCurrentUser();

        if (TextUtils.isEmpty(user.mobile)) recipient = user.email;
        else recipient = user.mobile;

        binding.otpInput1.getParams = () -> recipient;
        binding.otpInput1.updateField = "密码";

        SpannableString ss = new SpannableString("将发送验证码到 " + recipient + " 以重设密码。");
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_primary)), 8, 8 + recipient.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tipTextView.setText(ss);
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
        String otp = binding.otpInput1.getOtp();
        if (TextUtils.isEmpty(otp)) {
            Utils.showAlert(this, "验证码不能为空");
            return;
        }

        String password = binding.passwordInput.getEditText().getText().toString();
        if (TextUtils.isEmpty(password)) {
            Utils.showAlert(this, "新密码不能为空");
            return;
        }
        if (password.length() < 6) {
            Utils.showAlert(this, "密码最少要输入6位");
            return;
        }

        String password1 = binding.password1Input.getEditText().getText().toString();
        if (TextUtils.isEmpty(password1)) {
            Utils.showAlert(this, "重复新密码不能为空");
            return;
        }
        if (!password1.equals(password)) {
            Utils.showAlert(this, "两次输入的密码不一致");
            return;
        }

        submit();
    }

    void submit() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("password", binding.passwordInput.getEditText().getText().toString());
            jsonObject.put("otp", binding.otpInput1.getOtp());
            jsonObject.put("account", recipient);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoading(this);
        MyHttp.put("/user/password", jsonObject, (s) -> {
            Utils.hideLoading();
            finish();
        });
    }
}
