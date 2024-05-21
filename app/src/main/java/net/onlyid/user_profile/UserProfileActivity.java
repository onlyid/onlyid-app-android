package net.onlyid.user_profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityUserProfileBinding;
import net.onlyid.entity.User;
import net.onlyid.user_profile.location.EditLocationActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class UserProfileActivity extends BaseActivity {
    static final String TAG = "UserProfileActivity";
    static final int PICK_IMAGE = 1;
    ActivityUserProfileBinding binding;
    String imageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();
        int radius = Utils.dp2px(this, 7);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);

        binding.nicknameTextView.setText(user.nickname);
        binding.mobileTextView.setText(TextUtils.isEmpty(user.mobile) ? "-" : user.mobile);
        binding.emailTextView.setText(TextUtils.isEmpty(user.email) ? "-" : user.email);
        binding.genderTextView.setText(user.gender == null ? "-" : user.gender.toLocalizedString());
        binding.birthDateTextView.setText(birthDate2String(user.birthDate));
        binding.locationTextView.setText(TextUtils.isEmpty(user.province) ? "-" : user.province + " " + user.city);

        binding.avatarLayout.setOnClickListener((v) -> pickImage());
        binding.avatarImageView.setOnClickListener((v) -> avatar());
        binding.nicknameLayout.setOnClickListener((v) -> nickname());
        binding.mobileLayout.setOnClickListener((v) -> mobile());
        binding.emailLayout.setOnClickListener((v) -> email());
        binding.genderLayout.setOnClickListener((v) -> gender());
        binding.birthDateLayout.setOnClickListener((v) -> birthDate());
        binding.locationLayout.setOnClickListener((v) -> location());
    }

    String birthDate2String(LocalDate birthDate) {
        LocalDate y1960 = LocalDate.of(1960, 1, 1);

        if (birthDate == null) {
            return "-";
        } else if (birthDate.isBefore(y1960.plusYears(10))) {
            return "60后";
        } else if (birthDate.isBefore(y1960.plusYears(20))) {
            return "70后";
        } else if (birthDate.isBefore(y1960.plusYears(30))) {
            return "80后";
        }  else if (birthDate.isBefore(y1960.plusYears(40))) {
            return "90后";
        } else {
            return "00后";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

    void refresh() {
        MyHttp.get("/user", (resp) -> {
            Utils.pref.edit().putString(Constants.USER, resp).apply();
            initView();
        });
    }

    void avatar() {
        Intent intent = new Intent(this, AvatarActivity.class);
        startActivity(intent);
    }

    void nickname() {
        Intent intent = new Intent(this, EditNicknameActivity.class);
        startActivity(intent);
    }

    void mobile() {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("type", "mobile");
        startActivity(intent);
    }

    void email() {
        Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("type", "email");
        startActivity(intent);
    }

    void gender() {
        Intent intent = new Intent(this, EditGenderActivity.class);
        startActivity(intent);
    }

    void birthDate() {
        Intent intent = new Intent(this, EditBirthDateActivity.class);
        startActivity(intent);
    }

    void location() {
        Intent intent = new Intent(this, EditLocationActivity.class);
        startActivity(intent);
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

    void submitAvatar(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

            File file = new File(getExternalCacheDir(), "resized-avatar");
            bitmap.compress(getImageFormat(), 90, new FileOutputStream(file));

            Utils.showLoading(this);
            MyHttp.postFile("/image", file, imageType, (resp) ->
                    MyHttp.put("/user/avatar", new JSONObject(resp),  (resp1) -> {
                            Utils.hideLoading();
                            Utils.showToast("保存成功", Toast.LENGTH_SHORT);
                            refresh();
                    }));
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
                submitAvatar(UCrop.getOutput(data));
            } else if (resultCode == UCrop.RESULT_ERROR) {
                //noinspection ConstantConditions
                UCrop.getError(data).printStackTrace();
            }
        }
    }
}
