package net.onlyid.common;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.onlyid.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyHttp {
    static final String TAG = MyHttp.class.getSimpleName();
    static final String BASE_URL;

    static OkHttpClient httpClient;
    static List<Cookie> cookies;
    static Handler handler = new Handler();

    public interface Callback {
        void onSuccess(String resp) throws Exception;
    }

    static CookieJar cookieJar = new CookieJar() {
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> list) {
            cookies = list;

            for (Cookie cookie : list) {
                if (!"JSESSIONID".equals(cookie.name())) continue;

                try {
                    JSONObject object = new JSONObject();
                    object.put("value", cookie.value());
                    object.put("domain", cookie.domain());
                    Utils.pref.edit().putString("cookie", object.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            if (cookies != null) return cookies;

            List<Cookie> list = new ArrayList<>();
            String string = Utils.pref.getString("cookie", null);
            if (string != null) {
                try {
                    JSONObject object = new JSONObject(string);
                    Cookie cookie = new Cookie.Builder()
                            .name("JSESSIONID")
                            .value(object.getString("value"))
                            .domain(object.getString("domain"))
                            .build();
                    list.add(cookie);
                    cookies = list;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }
    };

    static {
        if (BuildConfig.DEBUG) BASE_URL = "http://192.168.31.117:8003/api/app";
        else BASE_URL = "https://onlyid.net/api/app";

        httpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build();
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
                ResponseBody body = response.body();
                assert body != null;
                String string = body.string();

                handler.post(() -> {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess(string);
                        } else {
                            String errMsg = new JSONObject(string).getString("error");
                            Utils.showToast("⚠️" + errMsg, Toast.LENGTH_LONG);
                            Log.w(TAG, errMsg);
                        }
                        response.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
