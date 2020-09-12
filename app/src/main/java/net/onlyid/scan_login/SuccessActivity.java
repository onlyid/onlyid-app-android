package net.onlyid.scan_login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.R;
import net.onlyid.databinding.ActivitySuccessBinding;
import net.onlyid.entity.Client;

public class SuccessActivity extends AppCompatActivity {
    static final String TAG = SuccessActivity.class.getSimpleName();
    ActivitySuccessBinding binding;
    Client client;
    int countDown = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    public void back(View v) {
        finish();
    }
}