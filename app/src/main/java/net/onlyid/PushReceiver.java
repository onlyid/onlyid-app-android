package net.onlyid;

import android.content.Context;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import net.onlyid.entity.User;

public class PushReceiver extends PushMessageReceiver {
    static final String TAG = PushReceiver.class.getSimpleName();

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                User user = MyApplication.getCurrentUser();
                MiPushClient.setUserAccount(context, user.id, null);
            }
        }
    }
}
