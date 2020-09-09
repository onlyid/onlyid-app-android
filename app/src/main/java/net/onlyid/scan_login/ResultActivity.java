package net.onlyid.scan_login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.HttpUtil;
import net.onlyid.R;
import net.onlyid.Utils;
import net.onlyid.databinding.ActivityResultBinding;
import net.onlyid.entity.Client;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {
    static final String TAG = ResultActivity.class.getSimpleName();
    ActivityResultBinding binding;
    ActionBar actionBar;
    Client client;
    int countDown = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        Intent intent = getIntent();
        boolean result = intent.getBooleanExtra("result", false);
        try {
            client = Utils.objectMapper.readValue(intent.getStringExtra("client"), Client.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (result) {
            actionBar.setTitle("登录成功");
            binding.titleTextView.setText("已登录" + "「" + client.name + "」");
            binding.resultImageView.setImageResource(R.drawable.ic_baseline_verified_user_24);
        } else {
            actionBar.setTitle("拒绝登录");
            binding.titleTextView.setText("已拒绝" + "「" + client.name + "」");
            binding.resultImageView.setImageResource(R.drawable.ic_baseline_info_24);
        }
        handleResult(result);

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (result) {
                    binding.tipTextView.setText("已登录，" + countDown + " 秒后自动返回");
                } else {
                    binding.tipTextView.setText("如你还需登录，请重复扫码，" + countDown + " 秒后自动返回");
                }
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    public void back(View v) {
        finish();
    }

    void handleResult(boolean result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", result);
            jsonObject.put("uid", getIntent().getStringExtra("uid"));
            jsonObject.put("clientId", client.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpUtil.post("oauth/scan-login-result", jsonObject, (c, s) -> {
        });
    }
}