package net.onlyid.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.yalantis.ucrop.UCrop;

import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivitySignUpBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SignUpActivity extends BaseActivity {
    static final String TAG = "SignUpActivity";
    static final int PICK_IMAGE = 1;
    ActivitySignUpBinding binding;
    String account, imageType, filename;
    EditText accountEditText, nicknameEditText, otpEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setElevation(0);

        account = getIntent().getStringExtra("account");

        initView();
    }

    void initView() {
        accountEditText = binding.accountInput.getEditText();
        nicknameEditText = binding.nicknameInput.getEditText();
        otpEditText = binding.otpInput.getEditText();
        passwordEditText = binding.passwordInput.getEditText();

        accountEditText.setText(account);
        binding.submitButton.setOnClickListener((v) -> validateFields());
        binding.avatarImageView.setOnClickListener((v) -> pickImage());
    }

    void validateFields() {
        String nickname = nicknameEditText.getText().toString();
        String otp = otpEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String errMsg = null;

        if (TextUtils.isEmpty(nickname))
            errMsg = "请填写昵称";
        else if (getLength(nickname) > 20)
            errMsg = "昵称不能超10字（英文字母算半个字）";
        else if (TextUtils.isEmpty(otp))
            errMsg = "请输入验证码";
        else if (TextUtils.isEmpty(password))
            errMsg = "请输入密码";

        if (!TextUtils.isEmpty(errMsg)) {
            Utils.showAlert(this, errMsg);
            return;
        }

        if (validatePassword(this, password)) submit();
    }

    /**
     * 一个英文算1个字，一个中文算2个字
     */
    public static int getLength(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c < 128) count++;
            else count += 2;
        }
        return count;
    }

    public static boolean validatePassword(Activity activity, String password) {
        if (password.length() < 6) {
            Utils.showAlert(activity, "密码最少要6位");
            return false;
        }
        if (password.length() > 50) {
            Utils.showAlert(activity, "密码不能超50位");
            return false;
        }

        int upper = 0, lower = 0, num = 0;
        for (char c : password.toCharArray()) {
            if ('0' <= c && c <= '9') num = 1;
            else if ('a' <= c && c <= 'z') lower = 1;
            else if ('A' <= c && c <= 'Z') upper = 1;
        }
        if (upper + lower + num < 2) {
            Utils.showAlert(activity, "密码至少包含数字、小写字母、大写字母中的两种");
            return false;
        }

        return true;
    }

    void submit() {
        JSONObject obj = new JSONObject();
        //noinspection HardwareIds
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        try {
            obj.put("password", passwordEditText.getText().toString());
            obj.put("otp", otpEditText.getText().toString());
            obj.put("account", account);
            obj.put("filename", filename);
            obj.put("nickname", nicknameEditText.getText().toString());
            obj.put("deviceId", deviceId);
            obj.put("deviceName", Build.MODEL);
            obj.put("deviceBrand", Build.BRAND.toLowerCase());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyHttp.post("/auth/sign-up", obj, resp -> LoginActivity.completeLogin(this, resp));
    }

    void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //noinspection deprecation
        startActivityForResult(intent, PICK_IMAGE);
    }

    void cropImage(Uri uri) {
        Uri outUri = Uri.fromFile(new File(getExternalCacheDir(), "avatar"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        options.setCompressionFormat(getImageFormat());

        UCrop.of(uri, outUri).withAspectRatio(1, 1).withOptions(options).start(this);
    }

    void uploadImage(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

            int radius = Utils.dp2px(this, 5);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            drawable.setCornerRadius(radius);
            binding.avatarImageView.setImageTintList(null);
            binding.avatarImageView.setImageDrawable(drawable);

            File file = new File(getExternalCacheDir(), "resized-avatar");
            bitmap.compress(getImageFormat(), 90, new FileOutputStream(file));

            MyHttp.postFile("/image", file, imageType, resp ->
                    filename = new JSONObject(resp).getString("filename"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    Bitmap.CompressFormat getImageFormat() {
        return "image/jpeg".equals(imageType) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                //noinspection ConstantConditions
                imageType = getContentResolver().getType(uri);
                if ("image/jpeg".equals(imageType) || "image/png".equals(imageType))
                    cropImage(uri);
                else
                    Utils.showAlert(this, "只支持JPG/PNG格式照片");
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                uploadImage(UCrop.getOutput(data));
            } else if (resultCode == UCrop.RESULT_ERROR) {
                //noinspection ConstantConditions
                UCrop.getError(data).printStackTrace();
            }
        }
    }
}
