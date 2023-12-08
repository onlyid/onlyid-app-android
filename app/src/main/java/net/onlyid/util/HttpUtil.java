package net.onlyid.util;

import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.onlyid.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtil {
    static final String TAG = HttpUtil.class.getSimpleName();
    static final String BASE_URL;
    static final String COOKIE = "cookie";
    static OkHttpClient httpClient;
    static Handler handler = new Handler();

    static {
        if (BuildConfig.DEBUG) BASE_URL = "http://192.168.31.117:8003/api/";
        else BASE_URL = "https://www.onlyid.net/api/";

        httpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                for (Cookie cookie : cookies) {
                    if (!"JSESSIONID".equals(cookie.name())) continue;

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("value", cookie.value());
                        jsonObject.put("domain", cookie.domain());
                        Utils.sharedPreferences.edit().putString(COOKIE, jsonObject.toString()).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @NonNull
            @Override
            public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                List<Cookie> list = new ArrayList<>();
                String cookieString = Utils.sharedPreferences.getString(COOKIE, null);
                if (cookieString != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(cookieString);
                        Cookie cookie = new Cookie.Builder()
                                .name("JSESSIONID")
                                .value(jsonObject.getString("value"))
                                .domain(jsonObject.getString("domain"))
                                .build();
                        list.add(cookie);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return list;
            }
        }).build();
    }

    public static void get(String url, MyCallback myCallback) {
        Request request = new Request.Builder().url(BASE_URL + url).build();
        enqueue(request, myCallback);
    }

    public static void delete(String url, MyCallback myCallback) {
        Request request = new Request.Builder().url(BASE_URL + url).delete().build();
        enqueue(request, myCallback);
    }

    public static void post(String url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), obj.toString()))
                .build();
        enqueue(request, myCallback);
    }

    public static void post(String url, RequestBody requestBody, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .post(requestBody)
                .build();
        enqueue(request, myCallback);
    }

    public static void put(String url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .put(RequestBody.create(MediaType.get("application/json; charset=utf-8"), obj.toString()))
                .build();
        enqueue(request, myCallback);
    }

    public static void enqueue(Request request, final MyCallback myCallback) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (Utils.loadingDialog != null) Utils.loadingDialog.dismiss();

                    Utils.showToast("网络连接不可用，请稍后重试", Toast.LENGTH_LONG);
                });
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                ResponseBody body = response.body();
                String s = body.string();

                handler.post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            myCallback.onSuccess(call, s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (myCallback.onResponseFailure(call, response.code(), s)) return;

                        if (Utils.loadingDialog != null) Utils.loadingDialog.dismiss();

                        String msg;
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            msg = jsonObject.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            msg = "请求失败，状态码：" + response.code();
                        }
                        Utils.showToast(msg, Toast.LENGTH_LONG);
                    }
                    response.close();
                });
            }
        });
    }

    public interface MyCallback {
        default boolean onResponseFailure(Call c, int code, String s) {
            return false;
        }

        void onSuccess(Call c, String s) throws Exception;
    }
}
