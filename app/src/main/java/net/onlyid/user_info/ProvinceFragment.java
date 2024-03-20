package net.onlyid.user_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.reflect.TypeToken;

import net.onlyid.R;
import net.onlyid.common.Utils;
import net.onlyid.databinding.FragmentProvinceBinding;
import net.onlyid.databinding.ItemLocationBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProvinceFragment extends Fragment implements AMapLocationListener, AdapterView.OnItemClickListener {
    static final String TAG = ProvinceFragment.class.getSimpleName();

    static final class Province {
        public String province;
        public ArrayList<String> city;
    }

    FragmentProvinceBinding binding;
    ItemLocationBinding locationBinding;
    AMapLocationClient locationClient;
    AMapLocation location;
    List<Province> chinaCityList;

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return chinaCityList.size();
        }

        @Override
        public Object getItem(int position) {
            return chinaCityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemLocationBinding binding;
            if (convertView == null) {
                binding = ItemLocationBinding.inflate(getLayoutInflater());
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemLocationBinding) convertView.getTag();
            }

            Province province = chinaCityList.get(position);
            binding.textView.setText(province.province);

            return convertView;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    void init() {
        AMapLocationClient.updatePrivacyShow(getContext(),true,true);
        AMapLocationClient.updatePrivacyAgree(getContext(),true);
        try {
            locationClient = new AMapLocationClient(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationClient.setLocationListener(this);
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setOnceLocation(true);
        locationOption.setNeedAddress(true);
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.china_city_list));
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine()) stringBuilder.append(scanner.nextLine());

        scanner.close();

        chinaCityList = Utils.gson.fromJson(stringBuilder.toString(), new TypeToken<List<Province>>() {});
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProvinceBinding.inflate(inflater, container, false);

        ItemLocationBinding binding1 = ItemLocationBinding.inflate(inflater);
        binding1.textView.setText("暂不设置");
        binding1.arrowRight.getRoot().setVisibility(View.INVISIBLE);
        binding.getRoot().addHeaderView(binding1.getRoot());

        locationBinding = ItemLocationBinding.inflate(inflater);
        locationBinding.textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
        locationBinding.textView.setText("当前位置：定位中。。。");
        locationBinding.arrowRight.getRoot().setVisibility(View.INVISIBLE);
        binding.getRoot().addHeaderView(locationBinding.getRoot());

        binding.getRoot().setAdapter(adapter);
        binding.getRoot().setOnItemClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        this.location = location;

        if (location == null || location.getErrorCode() != 0) {
            locationBinding.textView.setText("当前位置：定位失败");
        } else {
            if (location.getProvince().equals(location.getCity())) {
                locationBinding.textView.setText("当前位置：" + location.getCity() + "-" + location.getDistrict());
            } else {
                locationBinding.textView.setText("当前位置：" + location.getProvince() + "-" + location.getCity());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EditLocationActivity activity = (EditLocationActivity) getActivity();
        if (position == 0) {
            activity.submit(null);
        } else if (position == 1) {
            if (location == null || location.getErrorCode() != 0) return;

            if (location.getProvince().equals(location.getCity())) {
                activity.submit(location.getCity() + " " + location.getDistrict());
            } else {
                activity.submit(location.getProvince() + " " + location.getCity());
            }
        } else {
            Province province = chinaCityList.get(position - 2);
            activity.showCityList(province.province, province.city);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationClient.onDestroy();
    }
}
