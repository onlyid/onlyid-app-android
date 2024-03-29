package net.onlyid.trusted_device;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.JavaType;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.Constants;
import net.onlyid.LoginActivity;
import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.databinding.ActivityTrustedDeviceBinding;
import net.onlyid.databinding.GroupSessionBinding;
import net.onlyid.databinding.ItemSessionBinding;
import net.onlyid.entity.Device;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrustedDeviceActivity extends AppCompatActivity {
    static final String TAG = TrustedDeviceActivity.class.getSimpleName();
    ActivityTrustedDeviceBinding binding;
    List<Device> browserSessionList = new ArrayList<>();
    List<Device> deviceSessionList = new ArrayList<>();
    boolean loading = true;

    BaseExpandableListAdapter adapter = new BaseExpandableListAdapter() {
        @Override
        public int getGroupCount() {
            if (loading) return 0;
            else return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition == 0) return browserSessionList.size();
            else return deviceSessionList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (groupPosition == 0) return browserSessionList;
            else return deviceSessionList;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (groupPosition == 0) return browserSessionList.get(childPosition);
            else return deviceSessionList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupSessionBinding binding;
            if (convertView == null) {
                binding = GroupSessionBinding.inflate(getLayoutInflater());
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (GroupSessionBinding) convertView.getTag();
            }

            if (groupPosition == 0) {
                binding.textView.setText("我的浏览器");
                if (browserSessionList.isEmpty()) binding.emptyView.setVisibility(View.VISIBLE);
                else binding.emptyView.setVisibility(View.GONE);
            } else {
                binding.textView.setText("我的手机");
                binding.emptyView.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ItemSessionBinding binding;
            if (convertView == null) {
                binding = ItemSessionBinding.inflate(getLayoutInflater());
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemSessionBinding) convertView.getTag();
            }

            Device session;
            int drawable;
            if (groupPosition == 0) {
                session = browserSessionList.get(childPosition);

                String osName, browserName;
                if (session.userAgent.contains("Windows")) osName = "Windows";
                else if (session.userAgent.contains("Mac OS X")) osName = "macOS";
                else if (session.userAgent.contains("Android")) osName = "Android";
                else if (session.userAgent.contains("iPhone OS")) osName = "iOS";
                else if (session.userAgent.contains("Linux")) osName = "Linux";
                else osName = "未知系统";

                if (session.userAgent.contains("Edg")) {
                    browserName = "Edge";
                    drawable = R.drawable.browser_edge;
                } else if (session.userAgent.contains("Chrome")) {
                    browserName = "Chrome";
                    drawable = R.drawable.browser_chrome;
                } else if (session.userAgent.contains("Safari")) {
                    browserName = "Safari";
                    drawable = R.drawable.browser_safari;
                } else if (session.userAgent.contains("Firefox")) {
                    browserName = "Firefox";
                    drawable = R.drawable.browser_firefox;
                } else if (session.userAgent.contains("Trident")) {
                    browserName = "Internet Explorer";
                    drawable = R.drawable.browser_explorer;
                } else {
                    browserName = "未知浏览器";
                    drawable = R.drawable.browser_unkown;
                }

                if (!TextUtils.isEmpty(session.userDeviceName)) {
                    binding.deviceTextView.setText(session.userDeviceName);
                } else {
                    binding.deviceTextView.setText(osName + " 上的 " + browserName);
                }
            } else {
                session = deviceSessionList.get(childPosition);

                if (session.type.equals(Device.Type.ANDROID)) {
                    drawable = R.drawable.device_android;
                } else {
                    drawable = R.drawable.device_apple;
                }

                String text;
                if (!TextUtils.isEmpty(session.userDeviceName)) text = session.userDeviceName;
                else text = session.deviceName;

                if (Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).equals(session.deviceId))
                    text += "（本机）";

                binding.deviceTextView.setText(text);
            }

            binding.deviceImageView.setImageResource(drawable);
            binding.lastActiveDateTextView.setText("最近活跃时间：" + session.lastDate.format(Constants.DATE_TIME_FORMATTER));
            binding.lastActiveLocationTextView.setText("最近活跃地点：" +
                    (TextUtils.isEmpty(session.lastLocation) ? "-" : session.lastLocation));
            binding.lastActiveIpTextView.setText("最近活跃IP：" + (TextUtils.isEmpty(session.lastIp) ? "-" : session.lastIp));
            binding.expireDateTextView.setText("登录过期时间：" + session.expireDate.format(Constants.DATE_TIME_FORMATTER));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrustedDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        initData();
    }

    void init() {
        binding.expandableListView.setAdapter(adapter);
        binding.expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> true);
        binding.expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            new MaterialAlertDialogBuilder(TrustedDeviceActivity.this, R.style.MyAlertDialog)
                    .setItems(new String[]{"自定义名称", "退出登录"}, (dialog, which) -> {
                        onDialogItemClick(which, groupPosition, childPosition);
                    }).show();
            return true;
        });
    }

    void initData() {
        binding.expandableListView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        loading = true;

        HttpUtil.get("app/devices/by-user", (c, s) -> {
            JavaType javaType = Utils.objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Device.class);
            List<Device> list = Utils.objectMapper.readValue(s, javaType);
            browserSessionList = list.stream()
                    .filter((device) -> !TextUtils.isEmpty(device.userAgent))
                    .collect(Collectors.toList());
            deviceSessionList = list.stream()
                    .filter((device -> !TextUtils.isEmpty(device.deviceId)))
                    .collect(Collectors.toList());

            binding.expandableListView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            loading = false;
            binding.expandableListView.expandGroup(0);
            binding.expandableListView.expandGroup(1);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    void onDialogItemClick(int which, int groupPosition, int childPosition) {
        Device session;
        if (groupPosition == 0) session = browserSessionList.get(childPosition);
        else session = deviceSessionList.get(childPosition);

        if (which == 0) {
            Intent intent = new Intent(TrustedDeviceActivity.this, CustomNameActivity.class);
            intent.putExtra("sessionId", session.sessionId);
            intent.putExtra("customName", session.userDeviceName);
            startActivityForResult(intent, 1);
        } else {
            // 退出当前设备，二次确认
            String myDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (groupPosition == 1 && myDeviceId.equals(session.deviceId)) {
                new MaterialAlertDialogBuilder(this, R.style.MyAlertDialog)
                        .setMessage("要退出当前设备吗？")
                        .setPositiveButton("确定", (d, w) -> {
                            invalidateSession(session.sessionId, true);
                        })
                        .show();
            } else {
                invalidateSession(session.sessionId, false);
            }
        }
    }

    void invalidateSession(String sessionId, boolean logout) {
        Utils.showLoadingDialog(this);
        HttpUtil.post("app/devices/" + sessionId + "/logout", new JSONObject(), (c, s) -> {
            Utils.loadingDialog.dismiss();
            Utils.showToast("已退出登录", Toast.LENGTH_SHORT);
            if (logout) {
                Utils.sharedPreferences.edit().putString(Constants.USER, null).apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                MyApplication.mainActivity.finish();
            } else {
                initData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) initData();
    }
}