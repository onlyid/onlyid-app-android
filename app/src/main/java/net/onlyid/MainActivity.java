package net.onlyid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import net.onlyid.authorization.AuthorizationActivity;
import net.onlyid.common.CheckUpdate;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityMainBinding;
import net.onlyid.entity.Session;
import net.onlyid.entity.User;
import net.onlyid.home.AboutActivity;
import net.onlyid.home.SupportActivity;
import net.onlyid.login.AccountActivity;
import net.onlyid.login_history.LoginHistoryActivity;
import net.onlyid.push_otp.OtpModalActivity;
import net.onlyid.push_otp.Push;
import net.onlyid.scan_login.ScanCodeActivity;
import net.onlyid.scan_login.ScanLoginActivity;
import net.onlyid.security.SecurityActivity;
import net.onlyid.switch_account.SwitchAccountActivity;
import net.onlyid.user_profile.UserProfileActivity;

import java.time.LocalDateTime;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    static final int LOGIN = 1, SCAN_CODE = 2, SWITCH_ACCOUNT = 3;

    ActivityMainBinding binding;
    boolean stopped = false;

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
        binding.userProfileLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
        binding.scanLoginLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, ScanCodeActivity.class);
            //noinspection deprecation
            startActivityForResult(intent, SCAN_CODE);
        });
        binding.authorizationLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, AuthorizationActivity.class);
            startActivity(intent);
        });
        binding.loginHistoryLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, LoginHistoryActivity.class);
            startActivity(intent);
        });
        binding.securityLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, SecurityActivity.class);
            startActivity(intent);
        });
        binding.switchAccountLayout.setOnClickListener((v) -> {
            Intent intent = new Intent(this, SwitchAccountActivity.class);
            //noinspection deprecation
            startActivityForResult(intent, SWITCH_ACCOUNT);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.a_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.support) {
            startActivity(new Intent(this, SupportActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
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
        User user = MyApplication.getCurrentUser();
        if (user == null) {
            login();
            return;
        }

        MyHttp.get("/user", (resp) -> {
            Utils.pref.edit().putString(Constants.USER, resp).apply();
            // 现在是登录着的状态，执行一些初始化操作
            loadAvatar();
            updateSession();
            OtpModalActivity.startIfNecessary(this);
            Push.init(this);
        });
    }

    void loadAvatar() {
        User user = MyApplication.getCurrentUser();
        if (user != null) {
            int radius = Utils.dp2px(this, 5);
            Glide.with(this).load(user.avatar)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding.avatarImageView);
        }
    }

    void updateSession() {
        String sessionListString = Utils.pref.getString(Constants.SESSION_LIST, null);
        if (TextUtils.isEmpty(sessionListString)) return;

        User user = MyApplication.getCurrentUser();
        // MyHttp在返回401后，会清除currentUser，再回来首页发起登录
        if (user == null) return;

        List<Session> sessionList = Utils.gson.fromJson(sessionListString, new TypeToken<List<Session>>() {});
        String token = Utils.pref.getString(Constants.TOKEN, null);

        for (int i = 0; i < sessionList.size(); i++) {
            Session session = sessionList.get(i);
            if (session.token.equals(token)) {
                session.user = user;
                session.expireDate = LocalDateTime.now().plusDays(90);
                break;
            }
        }

        Utils.pref.edit().putString(Constants.SESSION_LIST, Utils.gson.toJson(sessionList)).apply();
    }

    void login() {
        //noinspection deprecation
        startActivityForResult(new Intent(this, AccountActivity.class), LOGIN);
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
    protected void onStart() {
        super.onStart();

        // 不要放到onCreate，因为可能是从修改头像页回来的
        loadAvatar();

        // 只有从停止状态恢复，才调这个方法，如果是newly created的，在onCreate会调
        // 如果从切换账号页回来，退出了当前账号，也不用调
        if (stopped && MyApplication.getCurrentUser() != null) {
            updateSession(); // 修改用户资料回来，同步到sessionList
            OtpModalActivity.startIfNecessary(this);
            stopped = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopped = true;
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
            if (resultCode == RESULT_OK) {
                Push.init(this);
            } else {
                finish();
            }
        } else if (requestCode == SCAN_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ScanLoginActivity.class);
                intent.putExtra("scanResult", data.getStringExtra("scanResult"));
                startActivity(intent);
            }
        } else if (requestCode == SWITCH_ACCOUNT) {
            // 切换账号成功，就好像用另一个用户打开了app
            if (resultCode == RESULT_OK) {
                this.syncUserInfo();
            }
            // 如果退出（或者注销）当前账号，重新打开登录页
            else if (MyApplication.getCurrentUser() == null) {
                this.login();
            }
        }
    }
}
