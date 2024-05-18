package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import net.onlyid.authorization.AuthorizationActivity;
import net.onlyid.common.CheckUpdate;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityMainBinding;
import net.onlyid.entity.Otp;
import net.onlyid.entity.User;
import net.onlyid.home.SupportActivity;
import net.onlyid.login.AccountActivity;
import net.onlyid.login_history.LoginHistoryActivity;
import net.onlyid.scan_login.ScanCodeActivity;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.security.SecurityActivity;
import net.onlyid.user_profile.UserProfileActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    static final String PUSH_TAG = "Push";
    static final String PUSH_APP_ID = "2882303761520030422";
    static final String PUSH_APP_KEY = "5222003035422";
    static final int LOGIN = 1, SCAN_CODE = 2;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_logo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(" 唯ID");

        initView();

        CheckUpdate.start(this, this::syncUserInfo);
    }

    void initView() {
        binding.userProfileLayout.setOnClickListener((v) -> userProfile());
        binding.scanLoginLayout.setOnClickListener((v) -> scanLogin());
        binding.authorizationLayout.setOnClickListener((v) -> authorization());
        binding.loginHistoryLayout.setOnClickListener((v) -> loginHistory());
        binding.securityLayout.setOnClickListener((v) -> security());
        binding.switchAccountLayout.setOnClickListener((v) -> {});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.support) {
            startActivity(new Intent(this, SupportActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("login", false)) {
            login();
        }
    }

    void syncUserInfo() {
        String userString = Utils.pref.getString(Constants.USER, null);
        if (TextUtils.isEmpty(userString)) {
            login();
            return;
        }

        MyHttp.get("/user", (resp) -> {
            Utils.pref.edit().putString(Constants.USER, resp).apply();

            // 到时再看一下，首页的api怎么组织
            initPush();
            getOtp();

            User user = MyApplication.getCurrentUser();
            int radius = Utils.dp2px(this, 5);
            Glide.with(this).load(user.avatar)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding.avatarImageView);
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
        MyHttp.get("/otp", (resp) -> {
            Log.d(TAG, "onSuccess: " + resp);
            if (TextUtils.isEmpty(resp)) return;

            Otp otp = Utils.gson.fromJson(resp, Otp.class);
            binding.otpLayout.setVisibility(View.VISIBLE);
            binding.otpTextView.setText(otp.code);
            int radius = Utils.dp2px(this, 5);
            Glide.with(this).load(otp.clientIconUrl)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding.iconImageView);
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
                    MyHttp.get("/otp", (resp1) -> {
                        if (TextUtils.isEmpty(resp1)) {
                            countDownTimer.cancel();
                            timer.cancel();
                            binding.otpLayout.setVisibility(View.GONE);
                        }
                    });
                }
            }, 1000, 5000);
        });
    }

    void login() {
        //noinspection deprecation
        startActivityForResult(new Intent(this, AccountActivity.class), LOGIN);
    }

    void userProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    void scanLogin() {
        Intent intent = new Intent(this, ScanCodeActivity.class);
        //noinspection deprecation
        startActivityForResult(intent, SCAN_CODE);
    }

    void authorization() {
        Intent intent = new Intent(this, AuthorizationActivity.class);
        startActivity(intent);
    }

    void loginHistory() {
        Intent intent = new Intent(this, LoginHistoryActivity.class);
        startActivity(intent);
    }

    void security() {
        Intent intent = new Intent(this, SecurityActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.currentActivity = this;
        CheckUpdate.installIfNecessary();
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication.currentActivity = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CheckUpdate.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        } else if (requestCode == SCAN_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ScanLoginActivity.class);
                intent.putExtra("scanResult", data.getStringExtra("scanResult"));
                startActivity(intent);
            }
        }
    }
}
