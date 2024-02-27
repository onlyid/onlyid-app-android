package net.onlyid.scan_login;

import android.os.Bundle;
import android.view.View;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityIllegalQrCodeBinding;

public class IllegalQrCodeActivity extends BaseActivity {
    ActivityIllegalQrCodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIllegalQrCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void back(View v) {
        finish();
    }
}
