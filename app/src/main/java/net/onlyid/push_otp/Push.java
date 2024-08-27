package net.onlyid.push_otp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.heytap.msp.push.HeytapPushManager;
import com.hihonor.push.sdk.HonorPushClient;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.vivo.push.PushClient;
import com.vivo.push.PushConfig;
import com.vivo.push.listener.IPushQueryActionListener;
import com.vivo.push.util.VivoPushException;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.DialogTitleTextBinding;
import net.onlyid.push_otp.oppo.MyCallBackResultService;

import org.json.JSONException;
import org.json.JSONObject;

public class Push {
    static final String TAG = "Push";
    static final String HUAWEI_ID = "111255281";
    static final String XIAOMI_ID = "2882303761520030422";
    static final String XIAOMI_KEY = "5222003035422";
    static final String OPPO_KEY = "ea148a3185e340cf832556c407525b4e";
    static final String OPPO_SECRET = "7d237b86f87b443db26d0bafc2ced66c";


    public static void init(Context context) {
        switch (Build.BRAND.toLowerCase()) {
            case "huawei":
                new Thread(() -> {
                    try {
                        String tokenScope = "HCM";
                        HmsInstanceId.getInstance(context).getToken(HUAWEI_ID, tokenScope);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case "xiaomi":
                MiPushClient.registerPush(context, XIAOMI_ID, XIAOMI_KEY);

                Logger.setLogger(context, new LoggerInterface() {
                    @Override
                    public void setTag(String tag) {
                        // ignore
                    }

                    @Override
                    public void log(String content, Throwable t) {
                        Log.d(TAG, content, t);
                    }

                    @Override
                    public void log(String content) {
                        Log.d(TAG, content);
                    }
                });
                break;
            case "oppo":
                // 如果不延时的话，有时输入法会弹出来
                new Handler().postDelayed(() -> {
                    HeytapPushManager.init(context, false);
                    HeytapPushManager.register(context, OPPO_KEY, OPPO_SECRET,
                            (MyCallBackResultService) (responseCode, registerId, s1, s2) -> {
                                if (responseCode == 0) {
                                    Push.sendPushId(context, registerId);
                                }
                            });
                }, 10);
                break;
            case "vivo":
                try {
                    PushConfig config = new PushConfig.Builder().agreePrivacyStatement(true).build();
                    PushClient.getInstance(context).initialize(config);
                } catch (VivoPushException e) {
                    e.printStackTrace();
                }

                PushClient.getInstance(context).turnOnPush(state -> {
                    if (state == 0) {
                        PushClient.getInstance(context).getRegId(new IPushQueryActionListener() {
                            @Override
                            public void onSuccess(String regId) {
                                Push.sendPushId(context, regId);
                            }

                            @Override
                            public void onFail(Integer errorCode) {
                                // do nothing
                            }
                        });
                    }
                });
                break;
            case "honor":
                HonorPushClient.getInstance().init(context.getApplicationContext(), true);
                break;
        }

        enableNotification(context);
    }

    static void enableNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (Build.BRAND.equalsIgnoreCase("oppo")) {
            // oppo需要手动创建channel
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("login", "登录通知", importance);
            notificationManager.createNotificationChannel(channel);

            // oppo会自动请求一次通知权限，此时不要弹出app自己的
            if (!Utils.pref.getBoolean("showNotificationDialog", false)) {
                Utils.pref.edit().putBoolean("showNotificationDialog", true).apply();
                return;
            }
        }

        if (notificationManager.areNotificationsEnabled()) return;

        DialogTitleTextBinding binding = DialogTitleTextBinding.inflate(LayoutInflater.from(context));
        binding.title.setText("打开通知开关");
        binding.text.setText("及时察觉账号异动、接收唯ID的验证码");

        new MaterialAlertDialogBuilder(context).setView(binding.getRoot())
                .setPositiveButton("去设置", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    context.startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 这里如果叫initXxx的话，容易和上面的init混淆，所以叫sendPushId
    public static void sendPushId(Context context, String pushId) {
        //noinspection HardwareIds
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        JSONObject obj = new JSONObject();
        try {
            obj.put("deviceId", deviceId);
            obj.put("pushId", pushId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyHttp.post("/devices/init-push", obj, resp -> {
            // do nothing
        });
    }
}
