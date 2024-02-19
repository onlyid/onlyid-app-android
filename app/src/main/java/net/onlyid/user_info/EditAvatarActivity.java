package net.onlyid.user_info;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yalantis.ucrop.UCrop;

import net.onlyid.R;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditAvatarBinding;
import net.onlyid.entity.User;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class EditAvatarActivity extends AppCompatActivity {
    static final String TAG = "AvatarActivity";
    static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
    };

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

        String userString = Utils.pref.getString(Constants.USER, null);
        try {
            User user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatar).listener(new RequestListener<Drawable>() {
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
        for (String permission : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                return;
            }
        }

        File file = new File(getExternalCacheDir(), "avatar");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 1) return;

        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                Utils.showAlertDialog(this, "你禁止了相机权限，拍照功能不可用");
                return;
            }
        }

        capture();
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

            Utils.showLoading(this);
            MyHttp.postFile("/image", file, "image/jpeg", (s) -> {
                MyHttp.put("/user/avatar", new JSONObject(s), (s1) -> {
                    Utils.hideLoading();
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
