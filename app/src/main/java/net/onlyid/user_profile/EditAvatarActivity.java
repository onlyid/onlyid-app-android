package net.onlyid.user_profile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityEditAvatarBinding;
import net.onlyid.entity.User;

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
}
