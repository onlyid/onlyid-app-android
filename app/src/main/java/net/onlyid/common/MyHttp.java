package net.onlyid.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.onlyid.BuildConfig;
import net.onlyid.MainActivity;
import net.onlyid.MyApplication;
import net.onlyid.OAuthActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyHttp {
    static final String TAG = MyHttp.class.getSimpleName();
    static final String BASE_URL;

    static OkHttpClient httpClient;
    static Handler handler = new Handler();

    public interface Callback {
        void onSuccess(String resp) throws Exception;
    }

    static Interceptor loginInterceptor = chain -> {
        Request original = chain.request();
        String token = Utils.pref.getString(Constants.TOKEN, "x");
        //noinspection ConstantConditions
        Request request = original.newBuilder().header("Auth", token).build();
        return chain.proceed(request);
    };

    static {
        if (BuildConfig.DEBUG) BASE_URL = "http://192.168.31.117:8003/api/app";
        else BASE_URL = "https://onlyid.net/api/app";

        httpClient = new OkHttpClient.Builder().addInterceptor(loginInterceptor).build();
    }

    public static void get(String url, Callback callback) {
        Request request = new Request.Builder().url(BASE_URL + url).build();
        enqueue(request, callback);
    }

    public static void delete(String url, Callback callback) {
        Request request = new Request.Builder().url(BASE_URL + url).delete().build();
        enqueue(request, callback);
    }

    public static void post(String url, JSONObject obj, Callback callback) {
        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"), obj.toString());
        Request request = new Request.Builder().url(BASE_URL + url).post(body).build();
        enqueue(request, callback);
    }

    public static void postFile(String url, File file, String type, Callback callback) {
        RequestBody part = RequestBody.create(MediaType.get(type), file);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", null, part)
                .build();
        Request request = new Request.Builder().url(BASE_URL + url).post(body).build();
        enqueue(request, callback);
    }

    public static void put(String url, JSONObject obj, Callback callback) {
        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"), obj.toString());
        Request request = new Request.Builder().url(BASE_URL + url).put(body).build();
        enqueue(request, callback);
    }

    public static void enqueue(Request request, Callback callback) {
        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.post(() ->
                        Utils.showToast("⚠️网络不可用，请稍后重试", Toast.LENGTH_LONG));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //noinspection ConstantConditions
                String string = response.body().string();

                handler.post(() -> {
                    try {
                        Activity current = MyApplication.currentActivity;
                        if (response.isSuccessful()) {
                            callback.onSuccess(string);
                        } else if (response.code() == 401) {
                            Utils.pref.edit().remove(Constants.USER).apply();

                            if (current == null) {
                                Utils.showToast("登录已过期，请重新登录", Toast.LENGTH_LONG);
                            } else {
                                Class<?> target = MainActivity.class;
                                if (current instanceof OAuthActivity) target = OAuthActivity.class;

                                Intent intent = new Intent(current, target);
                                // 两个flag都要设置，这样不管target是在栈顶还是在栈下面，都会调起onNewIntent
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.putExtra("login", true);
                                current.startActivity(intent);
                            }
                        } else {
                            String errMsg = new JSONObject(string).getString("error");
                            // 如果还停留在界面上，则用dialog展示错误信息，体验更友好
                            if (current != null)
                                Utils.showAlert(MyApplication.currentActivity, errMsg);
                            else
                                Utils.showToast("⚠️" + errMsg, Toast.LENGTH_LONG);
                            Log.w(TAG, errMsg);
                        }
                        response.close();
                    } catch (Exception e) {
                        Log.w(TAG, "onResponse: " + string, e);
                    }
                });
            }
        });
    }
}
