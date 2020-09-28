package net.onlyid.user_info;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.databinding.ActivityEditPasswordBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class EditPasswordActivity extends AppCompatActivity {
    ActivityEditPasswordBinding binding;
    User user;
    String recipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(user.mobile)) recipient = user.email;
        else recipient = user.mobile;

        binding.otpInput1.getRecipient = () -> recipient;

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
        String otp = binding.otpInput1.getOtp();
        if (TextUtils.isEmpty(otp)) {
            Utils.showAlertDialog(this, "验证码不能为空");
            return;
        }

        String password = binding.passwordInput.getEditText().getText().toString();
        if (TextUtils.isEmpty(password)) {
            Utils.showAlertDialog(this, "新密码不能为空");
            return;
        }
        if (password.length() < 6) {
            Utils.showAlertDialog(this, "密码最少要输入6位");
            return;
        }

        String password1 = binding.password1Input.getEditText().getText().toString();
        if (TextUtils.isEmpty(password1)) {
            Utils.showAlertDialog(this, "重复新密码不能为空");
            return;
        }
        if (!password1.equals(password)) {
            Utils.showAlertDialog(this, "两次输入的密码不一致");
            return;
        }

        submit();
    }

    void submit() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("password", binding.passwordInput.getEditText().getText().toString());
            jsonObject.put("otp", binding.otpInput1.getOtp());
            jsonObject.put("accountName", recipient);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoadingDialog(this);
        HttpUtil.put("app/user/password", jsonObject, (c, s) -> {
            Utils.loadingDialog.dismiss();
            Utils.showToast("已保存", Toast.LENGTH_SHORT);
            finish();
        });
    }
}