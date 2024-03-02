package net.onlyid.trusted_device;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityCustomNameBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomNameActivity extends BaseActivity {
    ActivityCustomNameBinding binding;
    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        String customName = intent.getStringExtra("customName");

        binding.customNameInput.getEditText().setText(customName);
    }

    void submit() {
        String customName = binding.customNameInput.getEditText().getText().toString();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sessionId", sessionId);
            jsonObject.put("deviceName", customName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoading(this);
        MyHttp.post("/devices/rename", jsonObject, (s) -> {
            Utils.hideLoading();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            submit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
