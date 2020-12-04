package net.onlyid.user_info;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.databinding.ActivityEditLocationBinding;
import net.onlyid.entity.User;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

public class EditLocationActivity extends AppCompatActivity {
    ActivityEditLocationBinding binding;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    void init() {
        String userString = Utils.sharedPreferences.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction().add(R.id.container, new ProvinceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
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
        user.location = location;

        Utils.showLoadingDialog(this);
        try {
            JSONObject jsonObject = new JSONObject(Utils.objectMapper.writeValueAsString(user));
            HttpUtil.put("app/user", jsonObject, (c, s) -> {
                Utils.loadingDialog.dismiss();
                Utils.showToast("已保存", Toast.LENGTH_SHORT);
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}