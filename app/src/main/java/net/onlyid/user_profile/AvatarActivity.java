package net.onlyid.user_profile;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityAvatarBinding;
import net.onlyid.entity.User;

public class AvatarActivity extends BaseActivity {
    static final String TAG = "AvatarActivity";
    static final float MIN_SCALE = 0.5f;
    static final float MAX_SCALE = 1f;
    ActivityAvatarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Drawable arrowBack = getDrawable(R.drawable.abc_ic_ab_back_material);
        arrowBack.setColorFilter(0xffe0e0e0, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(arrowBack);

        initView();
    }

    void initView() {
        binding.photoView.setMinimumScale(MIN_SCALE);
        binding.photoView.setMediumScale(0.75f);
        binding.photoView.setMaximumScale(MAX_SCALE);

        binding.photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float scale = binding.photoView.getScale();
                if (scale < MAX_SCALE)
                    binding.photoView.setScale(MAX_SCALE, true);
                else
                    binding.photoView.setScale(MIN_SCALE, true);

                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        User user = MyApplication.getCurrentUser();
        Glide.with(this).load(user.avatar).into(new DrawableImageViewTarget(binding.photoView) {
            @Override
            protected void setResource(Drawable resource) {
                super.setResource(resource);
                binding.photoView.setScale(MIN_SCALE);
            }
        });
    }
}
