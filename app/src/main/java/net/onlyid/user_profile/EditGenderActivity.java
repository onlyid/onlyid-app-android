package net.onlyid.user_profile;

import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityEditGenderBinding;

public class EditGenderActivity extends BaseActivity {
    ActivityEditGenderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditGenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
