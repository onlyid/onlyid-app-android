package net.onlyid.authorized_app;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.JavaType;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityAuthorizedAppBinding;
import net.onlyid.databinding.ItemClientBinding;
import net.onlyid.entity.Client1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorizedAppActivity extends BaseActivity {
    static final String TAG = AuthorizedAppActivity.class.getSimpleName();
    ActivityAuthorizedAppBinding binding;
    List<Client1> clientList = new ArrayList<>();
    boolean loading = true;

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return clientList.size();
        }

        @Override
        public Object getItem(int position) {
            return clientList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemClientBinding binding;
            if (convertView == null) {
                binding = ItemClientBinding.inflate(getLayoutInflater());
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemClientBinding) convertView.getTag();
            }

            Client1 client = clientList.get(position);
            Glide.with(convertView).load(client.iconUrl).into(binding.imageView);
            String type = "（" + client.type.toLocalizedString() + "）";
            SpannableString ss = new SpannableString(client.name + type);
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.gray)),
                    client.name.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new RelativeSizeSpan(0.88f),
                    client.name.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.nameTextView.setText(ss);
            binding.createDateTextView.setText("首次登录时间：" + client.firstDate.format(Constants.DATE_TIME_FORMATTER_H));
            binding.lastLoginDateTextView.setText("最近登录时间：" +
                    (client.lastDate == null ? "-" : client.lastDate.format(Constants.DATE_TIME_FORMATTER_H)));
            binding.lastLoginLocationTextView.setText("最近登录地点：" +
                    (TextUtils.isEmpty(client.lastLocation) ? "-" : client.lastLocation));
            binding.lastLoginIpTextView.setText("最近登录IP：" +
                    (TextUtils.isEmpty(client.lastIp) ? "-" : client.lastIp));

            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizedAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        initData();
    }

    void init() {
        View header = View.inflate(this, R.layout.header_authorized_app, null);
        binding.listView.addHeaderView(header);
        binding.listView.setAdapter(adapter);
        binding.listView.setOnItemClickListener((parent, view, position, id) -> {
            // 要把header排除在外
            int p = position - 1;
            if (p < 0) return;

            new MaterialAlertDialogBuilder(AuthorizedAppActivity.this)
                    .setItems(new String[]{"取消授权"}, (dialog, which) -> onDialogItemClick(which, p)).show();
        });
    }

    void initData() {
        binding.listView.setVisibility(View.GONE);
        binding.emptyView.getRoot().setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        loading = true;

        MyHttp.get("/clients/by-user", (s) -> {
            JavaType type = Utils.objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Client1.class);
            clientList = Utils.objectMapper.readValue(s, type);
            // 过滤掉唯ID的应用
            clientList = clientList.stream()
                    .filter((client -> !client.name.startsWith("唯ID")))
                    .collect(Collectors.toList());

            if (clientList.isEmpty()) binding.emptyView.getRoot().setVisibility(View.VISIBLE);
            else binding.listView.setVisibility(View.VISIBLE);

            binding.progressBar.setVisibility(View.GONE);
            loading = false;
            adapter.notifyDataSetChanged();
        });
    }

    void onDialogItemClick(int which, int position) {
        Utils.showLoading(this);
        MyHttp.delete("/user-client-links/" + clientList.get(position).id, (s) -> {
            Utils.hideLoading();
            initData();
        });
    }
}
