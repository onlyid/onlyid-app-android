package net.onlyid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    static final String TAG = "LoginActivity";
    static final String MY_URL;

    static {
        if (BuildConfig.DEBUG)
            MY_URL = "http://192.168.0.146:3000/oauth?client-id=8bfc826f39954d54b0e583c4f4edd3c7&package-name=net.onlyid";
        else
            MY_URL = "https://www.onlyid.net/oauth?client-id=fc5d31c48bdc4f8aa9766ecb0adc17d2&package-name=net.onlyid";
    }

    ProgressBar progressBar;

    class JsInterface {
        @JavascriptInterface
        public void onCode(final String code, final String state) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("code", code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpUtil.post("login", obj, (Call c, String s) -> {
                Utils.preferences.edit().putString(Constants.USER, s).apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        @JavascriptInterface
        public void setTitle(final String title) {
            runOnUiThread(() -> {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) actionBar.setTitle(title);
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.progress_bar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        initWebView();

        setSupportActionBar(toolbar);
    }

    @SuppressLint("SetJavaScriptEnabled")
    void initWebView() {
        WebView webView = findViewById(R.id.web_view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
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
}