package net.onlyid.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.MyApplication;

public class BaseActivity extends AppCompatActivity {
    static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // suppress NPE inspection
    @NonNull
    @Override
    public ActionBar getSupportActionBar() {
        //noinspection ConstantConditions
        return super.getSupportActionBar();
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
