package net.onlyid.trusted_device;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.R;
import net.onlyid.databinding.ActivityCustomNameBinding;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomNameActivity extends AppCompatActivity {
    ActivityCustomNameBinding binding;
    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            jsonObject.put("customName", customName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.showLoadingDialog(this);
        HttpUtil.post("app/sessions/custom-name", jsonObject, (c, s) -> {
            Utils.loadingDialog.dismiss();
            Utils.showToast("已保存", Toast.LENGTH_SHORT);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.save:
                submit();
                return true;
            default:
                return false;
        }
    }
}