package net.onlyid.push_otp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityOtpModalBinding;
import net.onlyid.entity.Otp;

import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class OtpModalActivity extends BaseActivity {
    static final String TAG = "OtpModalActivity";
    ActivityOtpModalBinding binding;
    Otp otp;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpModalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);

        initData();
        initView();

        updateMinute();
        getVerifyResult();
    }

    void initData() {
        otp = (Otp) getIntent().getSerializableExtra("otp");
    }

    void initView() {
        int radius = Utils.dp2px(this, 10);
        Glide.with(this)
                .load(otp.clientIconUrl)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.iconImageView);

        binding.clientTextView.setText(otp.clientName);

        int codeLength = otp.code.length();
        int splitIndex = codeLength / 2;
        String codeText = otp.code.substring(0, splitIndex) + " " + otp.code.substring(splitIndex, codeLength);
        binding.otpTextView.setText(codeText);

        binding.otpTextView.setOnClickListener((v) -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("验证码", otp.code);
            clipboardManager.setPrimaryClip(clipData);
            Utils.showToast("已复制", Toast.LENGTH_SHORT);
        });

        binding.notMyButton.setOnClickListener((v) -> {
            MyHttp.post("/otp/" + otp.id + "/invalidate", new JSONObject(), (resp) -> {
                Utils.showToast("验证码已失效", Toast.LENGTH_SHORT);
                finish();
            });
        });
    }

    // 更新有效期
    void updateMinute() {
        long duration = Duration.between(LocalDateTime.now(), otp.expireDate).toMillis();
        countDownTimer = new CountDownTimer(duration, 10000) {
            public void onTick(long millisUntilFinished) {
                long minutes = Duration.between(LocalDateTime.now(), otp.expireDate).toMinutes();
                binding.tipTextView.setText((minutes + 1) + "分钟内有效，请勿泄露于他人");
            }

            public void onFinish() {
                Utils.showToast("验证码已失效", Toast.LENGTH_SHORT);
                finish();
            }
        }.start();
    }

    // 轮询检查验证码是否已使用/已失效
    void getVerifyResult() {
        MyHttp.get("/otp/" + otp.id + "/verify-result", (resp) -> {
            if (isFinishing()) return;

            if (TextUtils.isEmpty(resp)) {
                getVerifyResult();
            } else {
                JSONObject respObj = new JSONObject(resp);
                if (!respObj.getBoolean("success"))
                    Utils.showToast("验证码已失效", Toast.LENGTH_SHORT);

                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.pref.edit().putInt("ignoredOtp", otp.id).apply();
    }

    public static void startIfNecessary(Activity activity) {
        MyHttp.get("/otp", (resp) -> {
            Otp otp = Utils.gson.fromJson(resp, Otp.class);
            int ignoredOtp = Utils.pref.getInt("ignoredOtp", 0);

            if (otp != null && !otp.id.equals(ignoredOtp)) {
                Intent intent = new Intent(activity, OtpModalActivity.class);
                intent.putExtra("otp", otp);
                activity.startActivity(intent);
            }
        });
    }
}
