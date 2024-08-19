package net.onlyid.switch_account;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityWarnDeleteBinding;

public class WarnDeleteActivity extends BaseActivity {
    ActivityWarnDeleteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWarnDeleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle("");

        initView();
    }

    void initView() {
        binding.cancelButton.setOnClickListener((v) -> {
            finish();
        });

        binding.nextButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            startActivity(intent);
        });
    }
}
