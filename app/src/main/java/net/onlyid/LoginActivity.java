package net.onlyid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    static final String TAG = "LoginActivity";
    static final String MY_URL;

    ValueCallback<Uri[]> filePathCallback;
    WebView webView;
    ProgressBar progressBar;

    static {
        if (BuildConfig.DEBUG)
            MY_URL = "http://192.168.0.132:3001/oauth?client-id=8bfc826f39954d54b0e583c4f4edd3c7&package-name=net.onlyid";
        else
            MY_URL = "https://www.onlyid.net/oauth?client-id=fc5d31c48bdc4f8aa9766ecb0adc17d2&package-name=net.onlyid";
    }

    class JsInterface {
        @JavascriptInterface
        public void onCode(final String code, final String state) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("code", code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpUtil.post("app/login", obj, (Call c, String s) -> {
                Utils.preferences.edit().putString(Constants.USER, s).apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        @JavascriptInterface
        public void setTitle(final String title) {
            runOnUiThread(() -> getSupportActionBar().setTitle(title));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.progress_bar);
        webView = findViewById(R.id.web_view);

        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "onReceivedError: " + description);
                Toast.makeText(LoginActivity.this, "打开登录页失败，请稍后重试", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        webView.addJavascriptInterface(new JsInterface(), "android");

        webView.loadUrl(MY_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 1) return;

        if (resultCode == RESULT_OK) filePathCallback.onReceiveValue(new Uri[]{data.getData()});
        else filePathCallback.onReceiveValue(null);
    }
}