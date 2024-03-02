package net.onlyid.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;

import net.onlyid.common.BaseActivity;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivitySignUpBinding;

import java.io.File;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SignUpActivity extends BaseActivity {
    static final String TAG = "SignUpActivity";
    static final int PICK_IMAGE = 1;
    ActivitySignUpBinding binding;
    String account, imageType;
    EditText accountEditText, nicknameEditText, otpEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        else if (nickname.length() > 20)
            errMsg = "昵称不能超20字";
        else if (TextUtils.isEmpty(otp))
            errMsg = "请输入验证码";
        else if (TextUtils.isEmpty(password) || password.length() < 6)
            errMsg = "密码最少要6位";
        else if (password.length() > 50)
            errMsg = "密码不能超50位";
        else {
            int upper = 0, lower = 0, num = 0;
            for (char c : password.toCharArray()) {
                if ('0' <= c && c <= '9') num = 1;
                else if ('a' <= c && c <= 'z') lower = 1;
                else if ('A' <= c && c <= 'Z') upper = 1;
            }
            if (upper + lower + num < 2)
                errMsg = "密码至少包含数字、小写字母、大写字母中的两种";
        }

        if (TextUtils.isEmpty(errMsg)) {
            submit();
        } else {
            Utils.showAlert(this, errMsg);
        }
    }

    void submit() {
        Log.e(TAG, "todo submit");
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
        options.setCompressionFormat(
                "image/jpeg".equals(imageType) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG);

        UCrop.of(uri, outUri).withAspectRatio(1, 1).withOptions(options).start(this);
    }

    void uploadImage(Uri uri) {
        Log.e(TAG, "todo uploadImage");
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
                Uri uri = UCrop.getOutput(data);
                uploadImage(uri);
                binding.avatarImageView.setImageTintList(null);

                int radius = Utils.dp2px(this, 5);
                Glide.with(this).load(uri)
                        .transform(new RoundedCornersTransformation(radius, 0))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.avatarImageView);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                //noinspection ConstantConditions
                UCrop.getError(data).printStackTrace();
            }
        }
    }
}
