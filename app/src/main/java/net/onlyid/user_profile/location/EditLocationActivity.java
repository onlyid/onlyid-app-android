package net.onlyid.user_profile.location;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.google.gson.reflect.TypeToken;

import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityEditLocationBinding;
import net.onlyid.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EditLocationActivity extends BaseActivity {
    ActivityEditLocationBinding binding;
    List<Province> chinaCityList;

    static class Province {
        public String province;
        public ArrayList<String> cityList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initView();
    }

    void initData() {
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.china_city_list));
        StringBuilder stringBuilder = new StringBuilder();

        while (scanner.hasNextLine()) stringBuilder.append(scanner.nextLine());

        scanner.close();
        chinaCityList = Utils.gson.fromJson(stringBuilder.toString(), new TypeToken<List<Province>>() {});
    }

    void initView() {
        ProvinceFragment fragment = new ProvinceFragment();
        Bundle args = new Bundle();
        ArrayList<String> provinceList = (ArrayList<String>) chinaCityList.stream()
                .map(province -> province.province)
                .collect(Collectors.toList());
        args.putStringArrayList("provinceList", provinceList);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
    }

    void showCityList(int index) {
        Province province = chinaCityList.get(index);

        CityFragment fragment = new CityFragment();
        Bundle args = new Bundle();
        args.putString("province", province.province);
        args.putStringArrayList("cityList", province.cityList);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    void submit(String province, String city) {
        User user = MyApplication.getCurrentUser();

        if (TextUtils.isEmpty(province)) {
            user.province = null;
            user.city = null;
        } else {
            user.province = province;
            user.city = city;
        }

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
