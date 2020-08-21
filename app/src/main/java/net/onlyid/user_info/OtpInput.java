package net.onlyid.user_info;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import net.onlyid.HttpUtil;
import net.onlyid.Utils;
import net.onlyid.databinding.InputOtpBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class OtpInput extends RelativeLayout {
    public GetRecipient getRecipient;
    InputOtpBinding binding;

    public OtpInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = InputOtpBinding.inflate(LayoutInflater.from(context), this);

        binding.sendButton.setOnClickListener((v) -> {
            String recipient = getRecipient.getRecipient();
            if (TextUtils.isEmpty(recipient)) return;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("recipient", recipient);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.showLoadingDialog(getContext());
            HttpUtil.post("app/send-otp", jsonObject, (c, s) -> {
                binding.sendButton.setEnabled(false);
                Utils.loadingDialog.dismiss();
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

    public interface GetRecipient {
        String getRecipient();
    }

    public String getOtp() {
        return binding.otpInput.getEditText().getText().toString();
    }
}
