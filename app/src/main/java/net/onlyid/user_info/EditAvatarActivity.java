package net.onlyid.user_info;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yalantis.ucrop.UCrop;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.databinding.ActivityEditAvatarBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditAvatarActivity extends AppCompatActivity {
    static final String TAG = "AvatarActivity";
    ActivityEditAvatarBinding binding;
    Uri captureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        binding.photoView.setMinimumScale(0.5F);
        binding.photoView.setMediumScale(0.75F);
        binding.photoView.setMaximumScale(1F);
        binding.photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float scale = binding.photoView.getScale();
                if (scale < binding.photoView.getMaximumScale())
                    binding.photoView.setScale(binding.photoView.getMaximumScale(), true);
                else
                    binding.photoView.setScale(binding.photoView.getMinimumScale(), true);

                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            User user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatarUrl).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                            Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                               DataSource dataSource, boolean isFirstResource) {
                    new Handler().post(() -> binding.photoView.setScale(binding.photoView.getMinimumScale()));
                    return false;
                }
            }).into(binding.photoView);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pick_or_capture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pick:
                pick();
                return true;
            case R.id.capture:
                capture();
                return true;
            default:
                return false;
        }
    }

    void pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    void capture() {
        File file = new File(getExternalCacheDir(), "avatar");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
        startActivityForResult(intent, 2);
    }

    void crop(Uri uri) {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(getResources().getColor(R.color.gray));
        File file = new File(getExternalCacheDir(), "cropped-avatar");
        UCrop.of(uri, Uri.fromFile(file))
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);
    }

    void submit(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            binding.photoView.setImageBitmap(bitmap);
            binding.photoView.setScale(binding.photoView.getMinimumScale());

            File file = new File(getExternalCacheDir(), "resized-avatar");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("file", null, RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .build();
            Utils.showLoadingDialog(this);
            HttpUtil.post("app/image", requestBody, (c, s) -> {
                HttpUtil.put("app/user/avatar", new JSONObject(s), (c1, s1) -> {
                    Utils.loadingDialog.dismiss();
                    Utils.showToast("已保存", Toast.LENGTH_SHORT);
                });
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode != RESULT_OK) return;

            crop(data.getData());
        } else if (requestCode == 2) {
            if (resultCode != RESULT_OK) return;

            crop(captureUri);
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == UCrop.RESULT_ERROR) {
                UCrop.getError(data).printStackTrace();
                return;
            } else if (resultCode != RESULT_OK) {
                return;
            }

            submit(UCrop.getOutput(data));
        }
    }
}