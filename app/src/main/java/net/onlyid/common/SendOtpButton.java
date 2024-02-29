package net.onlyid.common;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

public class SendOtpButton extends MaterialButton implements View.OnClickListener {
    public RecipientCallback recipientCallback;

    public SendOtpButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLetterSpacing(0);
        setText("发送验证码");
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String recipient = recipientCallback.get();

        if (TextUtils.isEmpty(recipient)) return;

        JSONObject obj = new JSONObject();
        try {
            obj.put("recipient", recipient);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyHttp.post("/send-otp", obj, (resp) -> {
            setEnabled(false);

            new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    setText(String.valueOf(millisUntilFinished / 1000));
                }
                public void onFinish() {
                    setText("发送验证码");
                    setEnabled(true);
                }
            }.start();
        });
    }

    public interface RecipientCallback {
        String get();
    }
}
