package net.onlyid.user_info;

import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.R;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditAccountBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class EditAccountActivity extends AppCompatActivity {
    static final String TAG = "EditAccountActivity";
    ActivityEditAccountBinding binding;
    String type;
    ActionBar actionBar;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        binding.otpInput1.getParams = () -> {
            String account = binding.accountInput.getEditText().getText().toString();
            return validateAccount(account) ? account : null;
        };

        String userString = Utils.pref.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        type = getIntent().getStringExtra(UserInfoActivity.TYPE);
        switch (type) {
            case "mobile":
                if (TextUtils.isEmpty(user.mobile)) {
                    binding.tipTextView.setText("绑定手机号后，下次登录可使用手机号。");
                    binding.accountInput.setHint("手机号");
                } else {
                    SpannableStringBuilder ssb = new SpannableStringBuilder("当前手机号是 ");
                    ssb.append(user.mobile);
                    ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_primary)), 7, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.append("，更换手机号后，下次登录可使用新手机号。");
                    binding.tipTextView.setText(ssb);
                    binding.accountInput.setHint("新手机号");
                }
                binding.accountInput.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
                binding.otpInput1.updateField = "手机号";
                actionBar.setTitle("修改手机号");
                break;
            case "email":
                if (TextUtils.isEmpty(user.email)) {
                    binding.tipTextView.setText("绑定邮箱后，下次登录可使用邮箱。");
                    binding.accountInput.setHint("邮箱");
                } else {
                    SpannableStringBuilder ssb = new SpannableStringBuilder("当前邮箱是 ");
                    ssb.append(user.email);
                    ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_primary)), 6, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.append("，更换邮箱后，下次登录可使用新邮箱。");
                    binding.tipTextView.setText(ssb);
                    binding.accountInput.setHint("新邮箱");
                }
                binding.accountInput.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                binding.otpInput1.updateField = "邮箱";
                actionBar.setTitle("修改邮箱");
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
                submit();
                return true;
            default:
                return false;
        }
    }

    void submit() {
        String account = binding.accountInput.getEditText().getText().toString();
        if (!validateAccount(account)) return;

        String otp = binding.otpInput1.getOtp();
        if (!validateOtp(otp)) return;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put("otp", otp);
            jsonObject.put("account", account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoading(this);
        MyHttp.put("/user/account", jsonObject, (s) -> {
            Utils.hideLoading();
            finish();
        });
    }

    boolean validateOtp(String otp) {
        if (TextUtils.isEmpty(otp)) {
            Utils.showAlert(this, "验证码不能为空");
            return false;
        }

        return true;
    }

    boolean validateAccount(String account) {
        if ("mobile".equals(type)) {
            if (TextUtils.isEmpty(account)) {
                Utils.showAlert(this, "新手机号不能为空");
                return false;
            }

            if (!Utils.isMobile(account)) {
                Utils.showAlert(this, "手机号格式不正确");
                return false;
            }
        } else {
            if (TextUtils.isEmpty(account)) {
                Utils.showAlert(this, "新邮箱不能为空");
                return false;
            }

            if (!PatternsCompat.EMAIL_ADDRESS.matcher(account).matches()) {
                Utils.showAlert(this, "邮箱格式不正确");
                return false;
            }
        }

        return true;
    }
}
