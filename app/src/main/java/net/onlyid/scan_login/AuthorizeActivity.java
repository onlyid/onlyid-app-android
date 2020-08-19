package net.onlyid.scan_login;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.databinding.ActivityAuthorizeBinding;

public class AuthorizeActivity extends AppCompatActivity {
    static final String TAG = AuthorizeActivity.class.getSimpleName();
    ActivityAuthorizeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String result = getIntent().getStringExtra("result");
        Log.d(TAG, "onCreate: " + result);
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
}