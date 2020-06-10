package onlyid.app;

import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtils {
    static final String BASE_URL;
    static OkHttpClient httpClient = new OkHttpClient();
    static Handler handler = new Handler();

    static {
        if (BuildConfig.DEBUG) BASE_URL = "http://192.168.0.146:8000/api/app/";
        else BASE_URL = "https://www.onlyid.net/api/app/";
    }

    static HttpUrl.Builder urlBuilder() {
        return HttpUrl.get(BASE_URL).newBuilder();
    }

    static void get(HttpUrl url, MyCallback myCallback) {
        Request request = new Request.Builder().url(url).build();
        enqueue(request, myCallback);
    }

    static void delete(HttpUrl url, MyCallback myCallback) {
        Request request = new Request.Builder().url(url).delete().build();
        enqueue(request, myCallback);
    }

    static void post(HttpUrl url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), obj.toString()))
                .build();
        enqueue(request, myCallback);
    }

    static void put(HttpUrl url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(MediaType.get("application/json; charset=utf-8"), obj.toString()))
                .build();
        enqueue(request, myCallback);
    }

    static void enqueue(Request request, final MyCallback myCallback) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                String msg = e instanceof ConnectException ? "网络连接不可用，请稍后重试" : e.getMessage();

                handler.post(() -> Toast.makeText(MyApplication.context, msg, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                ResponseBody body = response.body();
                final String s = body.string();

                if (response.isSuccessful()) {
                    handler.post(() -> myCallback.onSuccess(call, s));
                } else {
                    String msg;
                    if (response.code() == 401) {
//                            Intent intent = new Intent(activity, LoginActivity.class);
//                            activity.startActivity(intent);
//                            activity.finish();
                        msg = "登录已失效";
                    } else {
                        try {
                            JSONObject obj = new JSONObject(s);
                            msg = obj.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            msg = "请求失败，状态码：" + response.code();
                        }
                    }

                    String msg1 = msg;
                    handler.post(() -> Toast.makeText(MyApplication.context, msg1, Toast.LENGTH_SHORT).show());
                }
                response.close();
            }
        });
    }

    interface MyCallback {
        void onSuccess(Call call, String s);
    }
}
