package onlyid.app;

import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtil {
    static final String BASE_URL;
    static OkHttpClient httpClient = new OkHttpClient();
    static Handler handler = new Handler();

    static {
        if (BuildConfig.DEBUG) BASE_URL = "http://192.168.0.146:8000/api/app/";
        else BASE_URL = "https://www.onlyid.net/api/app/";
    }

    public static HttpUrl.Builder urlBuilder() {
        return HttpUrl.get(BASE_URL).newBuilder();
    }

    public static void get(HttpUrl url, MyCallback myCallback) {
        Request request = new Request.Builder().url(url).build();
        enqueue(request, myCallback);
    }

    public static void delete(HttpUrl url, MyCallback myCallback) {
        Request request = new Request.Builder().url(url).delete().build();
        enqueue(request, myCallback);
    }

    public static void post(HttpUrl url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(obj.toString(), MediaType.get("application/json; charset=utf-8")))
                .build();
        enqueue(request, myCallback);
    }

    public static void put(HttpUrl url, JSONObject obj, MyCallback myCallback) {
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(obj.toString(), MediaType.get("application/json; charset=utf-8")))
                .build();
        enqueue(request, myCallback);
    }

    public static void enqueue(Request request, final MyCallback myCallback) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(
                        MyApplication.context, "网络连接不可用，请稍后重试", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                ResponseBody body = response.body();
                final String s = body.string();

                handler.post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            myCallback.onSuccess(call, s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (myCallback.onResponseFailure(call, response.code(), s)) return;

                        String msg;
                        try {
                            JSONObject obj = new JSONObject(s);
                            msg = obj.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            msg = "请求失败，状态码：" + response.code();
                        }
                        String msg1 = msg;
                        Toast.makeText(MyApplication.context, msg1, Toast.LENGTH_LONG).show();
                    }
                    response.close();
                });
            }
        });
    }

    public interface MyCallback {
        default boolean onResponseFailure(Call call, int code, String s) {
            return false;
        }

        void onSuccess(Call call, String s) throws Exception;
    }
}
