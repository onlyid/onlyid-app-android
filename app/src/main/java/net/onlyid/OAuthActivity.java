package net.onlyid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.authorization.AuthorizeActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.login.AccountActivity;

import org.json.JSONObject;

public class OAuthActivity extends AppCompatActivity {
    static final String TAG = "OAuthActivity";
    static final int LOGIN = 2;
    static final String EXTRA_CODE = "extraCode";
    static final String EXTRA_STATE = "extraState";
    String clientId, state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        getSupportActionBar().hide();

        Utils.showLoading(this);
        Utils.loadingDialog.setOnDismissListener(d -> finish());

        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("login", false)) {
            //noinspection deprecation
            startActivityForResult(new Intent(this, AccountActivity.class), LOGIN);
        }
    }

    void handleIntent() {
        Intent intent = getIntent();
        clientId = intent.getStringExtra("clientId");
        state = intent.getStringExtra("state");

        // 首先判断当前是否已经登录
        MyHttp.get("/user", (resp) -> afterLogin());
    }

    void afterLogin() {
        AuthorizeActivity.startIfNecessary(this, clientId, () -> handleAuthorizeResult(true));
    }

    void handleAuthorizeResult(boolean result) {
        if (result) {
            MyHttp.post("/authorize-client/" + clientId, new JSONObject(), (resp) -> {
                JSONObject respBody = new JSONObject(resp);
                String code = respBody.getString("authorizationCode");

                Intent data = new Intent();
                data.putExtra(EXTRA_CODE, code);
                data.putExtra(EXTRA_STATE, state);
                setResult(RESULT_OK, data);
                finish();
            });
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN) {
            if (resultCode == RESULT_OK) {
                afterLogin();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else if (requestCode == AuthorizeActivity.AUTHORIZE) {
            handleAuthorizeResult(resultCode == RESULT_OK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.currentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.currentActivity = null;
    }
}
