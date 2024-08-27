package net.onlyid.push_otp.huawei;

import android.text.TextUtils;

import com.huawei.hms.push.HmsMessageService;

import net.onlyid.push_otp.Push;

public class MyHmsMessageService extends HmsMessageService {
    @Override
    public void onNewToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            Push.sendPushId(this, token);
        }
    }
}
