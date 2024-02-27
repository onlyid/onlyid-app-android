package net.onlyid.scan_login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivitySuccessBinding;
import net.onlyid.entity.Client;

public class SuccessActivity extends BaseActivity {
    static final String TAG = SuccessActivity.class.getSimpleName();
    ActivitySuccessBinding binding;
    Client client;
    int countDown = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        Intent intent = getIntent();
        client = (Client) intent.getSerializableExtra("client");

        binding.titleTextView.setText("已登录「" + client.name + "」");
        binding.resultImageView.setImageResource(R.drawable.ic_baseline_verified_user_24);

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                binding.tipTextView.setText("已登录，" + countDown + " 秒后自动返回");
                binding.backButton.setText("返 回（" + countDown + "）");
                countDown--;
            }

            public void onFinish() {
                finish();
            }
        }.start();
    }

    public void back(View v) {
        finish();
    }
}
