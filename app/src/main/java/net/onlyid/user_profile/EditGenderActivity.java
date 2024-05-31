package net.onlyid.user_profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditGenderBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class EditGenderActivity extends BaseActivity {
    ActivityEditGenderBinding binding;
    User.Gender gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditGenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initView();
    }

    void initData() {
        gender = MyApplication.getCurrentUser().gender;
    }

    void initView() {
        binding.maleCheck.setVisibility(View.GONE);
        binding.femaleCheck.setVisibility(View.GONE);
        binding.clearCheck.setVisibility(View.GONE);

        if (gender == null) {
            binding.clearCheck.setVisibility(View.VISIBLE);
        } else if (gender.equals(User.Gender.male)) {
            binding.maleCheck.setVisibility(View.VISIBLE);
        } else {
            binding.femaleCheck.setVisibility(View.VISIBLE);
        }

        binding.maleLayout.setOnClickListener((v) -> submit(User.Gender.male));
        binding.femaleLayout.setOnClickListener((v) -> submit(User.Gender.female));
        binding.clearLayout.setOnClickListener((v) -> submit(null));
    }

    void submit(User.Gender gender) {
        this.gender = gender;
        initView();

        User user = MyApplication.getCurrentUser();
        user.gender = gender;
        try {
            JSONObject obj = new JSONObject(Utils.gson.toJson(user));
            MyHttp.put("/user", obj, (resp) -> {
                Utils.showToast("保存成功", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
