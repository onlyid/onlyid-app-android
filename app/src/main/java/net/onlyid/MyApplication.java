package net.onlyid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import net.onlyid.common.Constants;
import net.onlyid.common.Utils;
import net.onlyid.entity.User;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    // 保存当前Activity，用于showAlert
    @SuppressLint("StaticFieldLeak")
    public static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    // 当前登录用户
    public static User getCurrentUser() {
        String json = Utils.pref.getString(Constants.USER, null);
        return Utils.gson.fromJson(json, User.class);
    }
}
