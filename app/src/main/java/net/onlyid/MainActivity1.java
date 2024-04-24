package net.onlyid;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityMain1Binding;
import net.onlyid.entity.User;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MainActivity1 extends AppCompatActivity {
    static final String TAG = "MainActivity1";
    ActivityMain1Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setIcon(R.drawable.ic_logo);
        actionBar.setDisplayShowHomeEnabled(true);

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();

        int radius = Utils.dp2px(this, 5);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);

        binding.myAccountLayout.setOnClickListener((v) -> {});
        binding.scanLoginLayout.setOnClickListener((v) -> {});
    }
}
