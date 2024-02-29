package net.onlyid.user_info;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.InputOtpBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class OtpInput extends RelativeLayout {
    public GetParams getParams;
    public String updateField;
    InputOtpBinding binding;

    public OtpInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = InputOtpBinding.inflate(LayoutInflater.from(context), this);

        binding.sendButton.setOnClickListener((v) -> {
            String recipient = getParams.recipient();
            if (TextUtils.isEmpty(recipient)) return;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("recipient", recipient);
                jsonObject.put("updateField", updateField);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.showLoading(getContext());
            MyHttp.post("/send-otp", jsonObject, (s) -> {
                binding.sendButton.setEnabled(false);
                Utils.hideLoading();
                new CountDownTimer(60000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        binding.sendButton.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    public void onFinish() {
                        binding.sendButton.setText("发送验证码");
                        binding.sendButton.setEnabled(true);
                    }
                }.start();
            });
        });
    }

    public interface GetParams {
        String recipient();
    }

    public String getOtp() {
        return binding.otpInput.getEditText().getText().toString();
    }
}
