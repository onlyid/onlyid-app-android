package net.onlyid.user_info;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yalantis.ucrop.UCrop;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditAvatarBinding;
import net.onlyid.entity.User;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class EditAvatarActivity extends BaseActivity {
    ActivityEditAvatarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        User user = MyApplication.getCurrentUser();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pick_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.pick) {
            pick();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //noinspection deprecation
        startActivityForResult(intent, 1);
    }

    void crop(Uri uri) {
        File file = new File(getExternalCacheDir(), "cropped-avatar");
        UCrop.of(uri, Uri.fromFile(file)).withAspectRatio(1, 1).start(this);
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
            MyHttp.postFile("/image", file, "image/jpeg",
                    (s) -> MyHttp.put("/user/avatar", new JSONObject(s),
                            (s1) -> Utils.hideLoading()));
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
