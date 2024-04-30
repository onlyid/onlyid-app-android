package net.onlyid.user_profile;

import android.os.Bundle;

import net.onlyid.common.BaseActivity;
import net.onlyid.databinding.ActivityEditBirthDateBinding;

public class EditBirthDateActivity extends BaseActivity {
    ActivityEditBirthDateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBirthDateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
