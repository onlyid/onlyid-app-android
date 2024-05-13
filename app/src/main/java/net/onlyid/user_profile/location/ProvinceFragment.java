package net.onlyid.user_profile.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import net.onlyid.R;
import net.onlyid.common.MyAdapter;
import net.onlyid.common.Utils;
import net.onlyid.databinding.FragmentProvinceBinding;
import net.onlyid.databinding.ItemLocationBinding;

import java.util.List;

public class ProvinceFragment extends Fragment implements AMapLocationListener, AdapterView.OnItemClickListener {
    static final String TAG = "ProvinceFragment";
    static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    FragmentProvinceBinding binding;
    ItemLocationBinding locationBinding;
    AMapLocationClient locationClient;
    AMapLocation location; // 当前位置
    List<String> provinceList;
    Parcelable listViewState; // 保存滚动位置

    MyAdapter adapter = new MyAdapter(() -> provinceList) {
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

            binding.textView.setText(provinceList.get(position));
            return convertView;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        checkLocationPermission();
    }

    void initData() {
        provinceList = getArguments().getStringArrayList("provinceList");
    }

    void checkLocationPermission() {
        for (String permission : PERMISSIONS) {
            //noinspection ConstantConditions
            if (PackageManager.PERMISSION_GRANTED != getContext().checkSelfPermission(permission)) {
                //noinspection deprecation
                requestPermissions(PERMISSIONS, 1);
                return;
            }
        }

        initLocation();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int count = 0;
        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED == result) count++;
        }

        // fine和coarse，有任一权限，都尝试定位
        if (count > 0) initLocation();
        else locationBinding.textView.setText("没有定位权限");
    }

    void initLocation() {
        AMapLocationClient.updatePrivacyShow(getContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(getContext(), true);
        try {
            locationClient = new AMapLocationClient(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationClient.setLocationListener(this);
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setOnceLocation(true).setNeedAddress(true);
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProvinceBinding.inflate(inflater, container, false);

        ItemLocationBinding clearBinding = ItemLocationBinding.inflate(inflater);
        clearBinding.textView.setText("不设置");
        binding.getRoot().addHeaderView(clearBinding.getRoot());

        locationBinding = ItemLocationBinding.inflate(inflater);
        locationBinding.textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on, 0, 0, 0);
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.primary, null));
        locationBinding.textView.setCompoundDrawableTintList(colorStateList);
        locationBinding.textView.setCompoundDrawablePadding(Utils.dp2px(getContext(), 3));
        locationBinding.textView.setText("定位中...");
        binding.getRoot().addHeaderView(locationBinding.getRoot());

        binding.getRoot().setAdapter(adapter);
        binding.getRoot().setOnItemClickListener(this);

        if (listViewState != null) binding.getRoot().onRestoreInstanceState(listViewState);

        return binding.getRoot();
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        this.location = location;

        if (location == null || location.getErrorCode() != 0 || TextUtils.isEmpty(location.getProvince())) {
            locationBinding.textView.setText("定位失败");
        } else {
            if (location.getProvince().equals(location.getCity()))
                locationBinding.textView.setText(location.getCity() + " " + location.getDistrict());
            else
                locationBinding.textView.setText(location.getProvince() + " " + location.getCity());
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EditLocationActivity activity = (EditLocationActivity) getActivity();
        if (position == 0) {
            activity.submit(null, null);
        } else if (position == 1) {
            if (location == null || location.getErrorCode() != 0) return;

            if (location.getProvince().equals(location.getCity()))
                activity.submit(location.getCity(), location.getDistrict());
            else
                activity.submit(location.getProvince(), location.getCity());
        } else {
            activity.showCityList(position - 2);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationClient != null) locationClient.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        listViewState = binding.getRoot().onSaveInstanceState();
    }
}
