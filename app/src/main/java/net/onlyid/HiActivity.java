package net.onlyid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.onlyid.databinding.ActivityHiBinding;

public class HiActivity extends AppCompatActivity {
    static final String TAG = "HiActivity";
    ActivityHiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
