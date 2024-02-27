package net.onlyid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    // 保存MainActivity，在注销时结束掉
    @SuppressLint("StaticFieldLeak")
    public static Activity mainActivity;
    // 保存当前Activity，用于showAlert
    @SuppressLint("StaticFieldLeak")
    public static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
