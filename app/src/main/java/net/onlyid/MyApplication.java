package net.onlyid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    // 保存MainActivity以在退出时结束掉
    public static Activity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
