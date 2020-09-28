package net.onlyid.authorized_app;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.JavaType;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.databinding.ActivityAuthorizedAppBinding;
import net.onlyid.databinding.ItemClientBinding;
import net.onlyid.entity.Client;
import net.onlyid.entity.UserClientLink;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorizedAppActivity extends AppCompatActivity {
    static final String TAG = AuthorizedAppActivity.class.getSimpleName();
    ActivityAuthorizedAppBinding binding;
    List<UserClientLink> userClientLinkList = new ArrayList<>();
    boolean loading = true;

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return userClientLinkList.size();
        }

        @Override
        public Object getItem(int position) {
            return userClientLinkList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return userClientLinkList.get(position).id;
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

            UserClientLink userClientLink = userClientLinkList.get(position);
            Glide.with(convertView).load(userClientLink.client.iconUrl).into(binding.imageView);
            String clientName = userClientLink.client.name;
            String type = "（" + userClientLink.client.type.toLocalizedString() + "）";
            SpannableString ss = new SpannableString(clientName + type);
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.gray)),
                    clientName.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new RelativeSizeSpan(0.88f),
                    clientName.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.nameTextView.setText(ss);
            binding.createDateTextView.setText("首次登录时间：" + userClientLink.createDate.format(Constants.MY_FORMATTER));
            if (userClientLink.lastLoginDate == null) {
                binding.lastLoginDateTextView.setText("最近登录时间：-");
            } else {
                binding.lastLoginDateTextView.setText("最近登录时间：" + userClientLink.lastLoginDate.format(Constants.MY_FORMATTER));
            }

            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizedAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

            new MaterialAlertDialogBuilder(AuthorizedAppActivity.this, R.style.MyAlertDialog)
                    .setItems(new String[]{"取消授权"}, (dialog, which) -> {
                        onDialogItemClick(which, p);
                    }).show();
        });
    }

    void initData() {
        binding.listView.setVisibility(View.GONE);
        binding.emptyView.getRoot().setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        loading = true;

        HttpUtil.get("app/user-clients", (c, s) -> {
            JavaType type = Utils.objectMapper.getTypeFactory().constructParametricType(ArrayList.class, UserClientLink.class);
            userClientLinkList = Utils.objectMapper.readValue(s, type);
            // 过滤掉唯ID的应用
            userClientLinkList = userClientLinkList.stream()
                    .filter((userClientLink -> !userClientLink.client.name.startsWith("唯ID")))
                    .collect(Collectors.toList());

            if (userClientLinkList.isEmpty()) binding.emptyView.getRoot().setVisibility(View.VISIBLE);
            else binding.listView.setVisibility(View.VISIBLE);

            binding.progressBar.setVisibility(View.GONE);
            loading = false;
            adapter.notifyDataSetChanged();
        });
    }

    void onDialogItemClick(int which, int position) {
        Client client = userClientLinkList.get(position).client;
        Utils.showLoadingDialog(this);
        HttpUtil.delete("app/user-client-links/" + client.id, (c, s) -> {
            Utils.loadingDialog.dismiss();
            Utils.showToast("已取消授权", Toast.LENGTH_SHORT);
            initData();
        });
    }
}