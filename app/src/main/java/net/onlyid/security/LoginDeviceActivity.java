package net.onlyid.security;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.reflect.TypeToken;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyAdapter;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityLoginDeviceBinding;
import net.onlyid.databinding.DialogTitleTextBinding;
import net.onlyid.databinding.HeaderLoginDeviceBinding;
import net.onlyid.databinding.ItemLoginDeviceBinding;
import net.onlyid.entity.Entity3;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    static final String TAG = "LoginDeviceActivity";
    ActivityLoginDeviceBinding binding;
    List<Entity3> list = new ArrayList<>();

    BaseAdapter adapter = new MyAdapter(() -> list) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemLoginDeviceBinding binding1;
            if (convertView == null) {
                binding1 = ItemLoginDeviceBinding.inflate(getLayoutInflater());
                convertView = binding1.getRoot();
                convertView.setTag(binding1);
            } else {
                binding1 = (ItemLoginDeviceBinding) convertView.getTag();
            }

            Entity3 entity = list.get(position);

            int drawable;
            if ("apple".equals(entity.brand)) drawable = R.drawable.device_apple;
            else drawable = R.drawable.device_android;
            binding1.deviceImageView.setImageResource(drawable);

            //noinspection HardwareIds
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            int labelVisible = deviceId.equals(entity.deviceId) ? View.VISIBLE : View.INVISIBLE;
            binding1.thisDeviceLabel.setVisibility(labelVisible);

            binding1.deviceTextView.setText(entity.brand + " " + entity.name);
            binding1.lastActiveTextView.setText("最近活跃：" + entity.lastDate.format(Constants.DATE_TIME_FORMATTER_H));

            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

        initData();
    }

    void initView() {
        HeaderLoginDeviceBinding binding1 = HeaderLoginDeviceBinding.inflate(getLayoutInflater());
        binding.listView.addHeaderView(binding1.getRoot(), null, false);
        binding.listView.setAdapter(adapter);
        binding.listView.setOnItemClickListener(this);
    }

    void initData() {
        MyHttp.get("/devices", (resp) -> {
            list = Utils.gson.fromJson(resp, new TypeToken<List<Entity3>>() {});
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int realPosition = position - 1;
        Entity3 entity = list.get(realPosition);

        //noinspection HardwareIds
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId.equals(entity.deviceId)) {
            Utils.showToast("不能退出当前设备", Toast.LENGTH_LONG);
            return;
        }

        DialogTitleTextBinding binding1 = DialogTitleTextBinding.inflate(getLayoutInflater());
        binding1.title.setText("退出登录");
        binding1.text.setText("退出后要再次使用需要重新登录。");

        new MaterialAlertDialogBuilder(this).setView(binding1.getRoot())
                .setPositiveButton("确定", (d, w) -> {
                    MyHttp.post("/devices/logout/" + entity.sessionId, new JSONObject(), (resp) -> {
                        Utils.showToast("退出成功", Toast.LENGTH_SHORT);
                        list.remove(realPosition);
                        adapter.notifyDataSetChanged();
                    });
                }).setNegativeButton("取消", null).show();
    }
}
