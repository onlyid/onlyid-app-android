package net.onlyid.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityTermsBinding;

public class TermsActivity extends BaseActivity {
    ActivityTermsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String type = getIntent().getStringExtra("type");
        String title;
        String url = "https://www.onlyid.net/static/";
        if ("terms".equals(type)) {
            title = "服务协议";
            url += "terms.html";
        } else {
            title = "隐私政策";
            url += "privacy.html";
        }
        getSupportActionBar().setTitle(title);
        binding.webView.loadUrl(url);
    }

    static void start(Context context, String type) {
        Intent intent = new Intent(context, TermsActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }
}
