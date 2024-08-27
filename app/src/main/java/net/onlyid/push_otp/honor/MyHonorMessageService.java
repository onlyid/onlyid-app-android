package net.onlyid.push_otp.honor;

import android.text.TextUtils;

import com.hihonor.push.sdk.HonorMessageService;

import net.onlyid.push_otp.Push;

public class MyHonorMessageService extends HonorMessageService {
    @Override
    public void onNewToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            Push.sendPushId(this, token);
        }
    }
}
