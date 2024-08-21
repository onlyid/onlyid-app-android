package net.onlyid.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import net.onlyid.BuildConfig;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityAboutBinding;
import net.onlyid.login.TermsActivity;

public class AboutActivity extends BaseActivity {
    static final String TAG = "AboutActivity";
    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle("");

        initView();
    }

    void initView() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        float radius = bitmap.getWidth() / 10f;
        drawable.setCornerRadius(radius);
        binding.iconImageView.setImageDrawable(drawable);

        binding.versionTextView.setText("v" + BuildConfig.VERSION_NAME);

        binding.termsLayout.setOnClickListener(v -> TermsActivity.start(this, "terms"));
        binding.privacyLayout.setOnClickListener(v -> TermsActivity.start(this, "privacy"));

        binding.icpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://beian.miit.gov.cn"));
            startActivity(intent);
        });
    }
}
