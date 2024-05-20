package net.onlyid.switch_account;

import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivitySwitchAccountBinding;

public class SwitchAccountActivity extends BaseActivity {
    ActivitySwitchAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySwitchAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
