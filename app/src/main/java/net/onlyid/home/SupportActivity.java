package net.onlyid.home;

import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivitySupportBinding;

public class SupportActivity extends BaseActivity {
    ActivitySupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.loadUrl("https://www.onlyid.net/oauth/support");
    }
}
