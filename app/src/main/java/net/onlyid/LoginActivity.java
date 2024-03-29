package net.onlyid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.databinding.ActivityLoginBinding;
import net.onlyid.entity.OAuthConfig;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    static final String TAG = LoginActivity.class.getSimpleName();
    static final String MY_URL = "https://www.onlyid.net/oauth?client-id=fc5d31c48bdc4f8aa9766ecb0adc17d2&package-name=net.onlyid";
    ActivityLoginBinding binding;
    ValueCallback<Uri[]> filePathCallback;

    class JsInterface {
        @JavascriptInterface
        public void onCode(final String code, final String state) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("code", code);
                jsonObject.put("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                jsonObject.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL);
                jsonObject.put("type", "ANDROID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpUtil.post("app/login", jsonObject, (Call c, String s) -> {
                Utils.sharedPreferences.edit().putString(Constants.USER, s).apply();

                OAuthConfig config = (OAuthConfig) getIntent().getSerializableExtra("oauthConfig");
                if (config == null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    OAuthActivity.promptAuthorizeIfNecessary(LoginActivity.this, config);
                }
            });
        }

        @JavascriptInterface
        public void setTitle(final String title) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initWebView();

        if (BuildConfig.DEBUG) new JsInterface().onCode("ab0d80cb1705426680767c7e129893b0", null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    void initWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setProgress(newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                LoginActivity.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                return true;
            }
        });
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "onReceivedError: " + description);
                Utils.showToast("打开登录页失败，请稍后重试", Toast.LENGTH_LONG);
                finish();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }
        });
        binding.webView.addJavascriptInterface(new JsInterface(), "android");

        binding.webView.loadUrl(MY_URL);
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) filePathCallback.onReceiveValue(new Uri[]{data.getData()});
            else filePathCallback.onReceiveValue(null);
        } else if (requestCode == OAuthActivity.REQUEST_OAUTH) {
            setResult(resultCode, data);
            finish();
        }
    }
}