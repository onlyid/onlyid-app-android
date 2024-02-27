package net.onlyid.common;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.LoginActivity;
import net.onlyid.MainActivity;
import net.onlyid.MyApplication;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 这两个Activity不需要setup navigation
        if (this instanceof MainActivity
                || this instanceof LoginActivity)
            return;

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.currentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication.currentActivity = null;
    }
}
