package net.onlyid.security;

import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivitySecurityBinding;

public class SecurityActivity extends BaseActivity {
    ActivitySecurityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
