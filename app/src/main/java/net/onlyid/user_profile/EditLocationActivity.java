package net.onlyid.user_profile;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentTransaction;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditLocationBinding;
import net.onlyid.entity.User;

import org.json.JSONObject;

import java.util.ArrayList;

public class EditLocationActivity extends BaseActivity {
    ActivityEditLocationBinding binding;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    void init() {
        user = MyApplication.getCurrentUser();

        getSupportFragmentManager().beginTransaction().add(R.id.container, new ProvinceFragment()).commit();
    }

    void showCityList(String province, ArrayList<String> cityList) {
        CityFragment fragment = new CityFragment();
        Bundle args = new Bundle();
        args.putString("province", province);
        args.putStringArrayList("cityList", cityList);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    void submit(String location) {
        if (!TextUtils.isEmpty(location)) {
            String[] arr = location.split(" ");
            user.province = arr[0];
            user.city = arr[1];
        }

        Utils.showLoading(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.gson.toJson(user));
            MyHttp.put("/user", jsonObject, (s) -> {
                Utils.hideLoading();
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
