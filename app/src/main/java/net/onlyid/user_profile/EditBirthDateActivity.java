package net.onlyid.user_profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import net.onlyid.MyApplication;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditBirthDateBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

public class EditBirthDateActivity extends BaseActivity {
    ActivityEditBirthDateBinding binding;
    LocalDate birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBirthDateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initView();
    }

    void initData() {
        if (!TextUtils.isEmpty(MyApplication.getCurrentUser().birthDate))
            birthDate = LocalDate.parse(MyApplication.getCurrentUser().birthDate);
    }

    void initView() {
        LocalDate y1960 = LocalDate.of(1960, 1, 1);

        binding.p60sCheck.setVisibility(View.GONE);
        binding.p70sCheck.setVisibility(View.GONE);
        binding.p80sCheck.setVisibility(View.GONE);
        binding.p90sCheck.setVisibility(View.GONE);
        binding.p00sCheck.setVisibility(View.GONE);
        binding.clearCheck.setVisibility(View.GONE);

        if (birthDate == null) {
            binding.clearCheck.setVisibility(View.VISIBLE);
        } else if (birthDate.equals(y1960)) {
            binding.p60sCheck.setVisibility(View.VISIBLE);
        } else if (birthDate.equals(y1960.plusYears(10))) {
            binding.p70sCheck.setVisibility(View.VISIBLE);
        } else if (birthDate.equals(y1960.plusYears(20))) {
            binding.p80sCheck.setVisibility(View.VISIBLE);
        } else if (birthDate.equals(y1960.plusYears(30))) {
            binding.p90sCheck.setVisibility(View.VISIBLE);
        } else {
            binding.p00sCheck.setVisibility(View.VISIBLE);
        }

        binding.p60sLayout.setOnClickListener((v) -> submit(y1960));
        binding.p70sLayout.setOnClickListener((v) -> submit(y1960.plusYears(10)));
        binding.p80sLayout.setOnClickListener((v) -> submit(y1960.plusYears(20)));
        binding.p90sLayout.setOnClickListener((v) -> submit(y1960.plusYears(30)));
        binding.p00sLayout.setOnClickListener((v) -> submit(y1960.plusYears(40)));
        binding.clearLayout.setOnClickListener((v) -> submit(null));
    }

    void submit(LocalDate localDate) {
        birthDate = localDate;
        initView();

        User user = MyApplication.getCurrentUser();
        user.birthDate = localDate == null ? null : localDate.toString();
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
