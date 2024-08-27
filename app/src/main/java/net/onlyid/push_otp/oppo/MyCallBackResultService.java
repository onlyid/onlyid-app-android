package net.onlyid.push_otp.oppo;

import com.heytap.msp.push.callback.ICallBackResultService;

public interface MyCallBackResultService extends ICallBackResultService {

    @Override
    default void onUnRegister(int i, String s, String s1) {
    }

    @Override
    default void onSetPushTime(int i, String s) {
    }

    @Override
    default void onGetPushStatus(int i, int i1) {
    }

    @Override
    default void onGetNotificationStatus(int i, int i1) {
    }

    @Override
    default void onError(int i, String s, String s1, String s2) {
    }
}
