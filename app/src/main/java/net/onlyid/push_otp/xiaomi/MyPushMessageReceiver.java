package net.onlyid.push_otp.xiaomi;

import android.content.Context;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import net.onlyid.push_otp.Push;

public class MyPushMessageReceiver extends PushMessageReceiver {
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        if (message.getResultCode() == ErrorCode.SUCCESS) {
            String regId = message.getCommandArguments().get(0);
            Push.sendPushId(context, regId);
        }
    }
}
