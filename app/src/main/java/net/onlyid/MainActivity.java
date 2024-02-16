package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import net.onlyid.authorized_app.AuthorizedAppActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.PermissionUtil;
import net.onlyid.common.UpdateUtil;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityMainBinding;
import net.onlyid.entity.Otp;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.trusted_device.TrustedDeviceActivity;
import net.onlyid.user_info.UserInfoActivity;

import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    static final String PUSH_TAG = "Push";
    static final String PUSH_APP_ID = "2882303761520030422";
    static final String PUSH_APP_KEY = "5222003035422";

    ActivityMainBinding binding;
    UpdateUtil updateUtil;
    PermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        refreshUserInfo();

        initPush();
        getOtp();

        updateUtil = new UpdateUtil(this);
        updateUtil.check();

        permissionUtil = new PermissionUtil(this);
        permissionUtil.check();

        MyApplication.mainActivity = this;
    }

    void refreshUserInfo() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        if (TextUtils.isEmpty(userString)) {
            login();
            return;
        }

        MyHttp.get("app/user", new MyHttp.Callback() {
            @Override
            public void onSuccess(Call c, String s) {
                Utils.sharedPreferences.edit().putString(Constants.USER, s).apply();
                updateSessionAndDeviceLink();
            }

            @Override
            public boolean onResponseFailure(Call c, int code, String s) {
                if (code == 401) {
                    login();

                    Utils.showToast("登录已失效", Toast.LENGTH_SHORT);
                    return true;
                }

                return false;
            }
        });
    }

    void updateSessionAndDeviceLink() {
        MyHttp.put("app/session-and-device-link", new JSONObject(), new MyHttp.Callback() {
            @Override
            public void onSuccess(Call c, String s) {
            }

            @Override
            public boolean onResponseFailure(Call c, int code, String s) {
                return true;
            }
        });
    }

    void initPush() {
        Logger.setLogger(this, new LoggerInterface() {
            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(PUSH_TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(PUSH_TAG, content);
            }
        });

        MiPushClient.registerPush(this, PUSH_APP_ID, PUSH_APP_KEY);
    }

    void getOtp() {
        MyHttp.get("app/otp", (c, s) -> {
            Log.d(TAG, "onSuccess: " + s);
            if (TextUtils.isEmpty(s)) return;

            try {
                Otp otp = Utils.objectMapper.readValue(s, Otp.class);
                binding.otpLayout.setVisibility(View.VISIBLE);
                binding.otpTextView.setText(otp.code);
                Glide.with(this).load(otp.clientIconUrl).into(binding.iconImageView);
                binding.otpProgressBar.setProgress(100);
                long duration = Duration.between(otp.createDate, otp.expireDate).toMillis();

                // 更新有效期进度条
                CountDownTimer countDownTimer = new CountDownTimer(duration, 100) {
                    public void onTick(long millisUntilFinished) {
                        long newDuration = Duration.between(LocalDateTime.now(), otp.expireDate).toMillis();
                        int progress = (int) (newDuration * 100 / duration);
                        binding.otpProgressBar.setProgress(progress);
                    }

                    public void onFinish() {
                        Utils.showToast("验证码已过期，请重新发送", Toast.LENGTH_LONG);
                    }
                }.start();

                // 轮询检查验证码是否已使用
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        MyHttp.get("app/otp", (c1, s1) -> {
                            if (TextUtils.isEmpty(s1)) {
                                countDownTimer.cancel();
                                timer.cancel();
                                binding.otpLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }, 1000, 5000);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void userInfo(View v) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }

    public void scanLogin(View v) {
        Intent intent = new Intent(this, ScanLoginActivity.class);
        startActivity(intent);
    }

    public void trustedDevice(View v) {
        Intent intent = new Intent(this, TrustedDeviceActivity.class);
        startActivity(intent);
    }

    public void authorizedApp(View v) {
        Intent intent = new Intent(this, AuthorizedAppActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUtil.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
    }
}
