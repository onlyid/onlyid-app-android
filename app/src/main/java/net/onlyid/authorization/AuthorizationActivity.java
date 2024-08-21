package net.onlyid.authorization;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.reflect.TypeToken;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyAdapter;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityAuthorizationBinding;
import net.onlyid.databinding.DialogHelp1Binding;
import net.onlyid.databinding.DialogTitleTextBinding;
import net.onlyid.databinding.ItemClientBinding;
import net.onlyid.entity.Client1;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class AuthorizationActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    static final String TAG = "AuthorizationActivity";
    ActivityAuthorizationBinding binding;
    List<Client1> clientList = new ArrayList<>();

    BaseAdapter adapter = new MyAdapter(() -> clientList) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemClientBinding binding1;
            if (convertView == null) {
                binding1 = ItemClientBinding.inflate(getLayoutInflater());
                convertView = binding1.getRoot();
                convertView.setTag(binding1);
            } else {
                binding1 = (ItemClientBinding) convertView.getTag();
            }

            Client1 client = clientList.get(position);
            int radius = Utils.dp2px(AuthorizationActivity.this, 5);
            Glide.with(convertView).load(client.iconUrl)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding1.imageView);

            binding1.nameTextView.setText(client.name);
            binding1.typeTextView.setText(client.type.toString());
            binding1.lastLoginTextView.setText("最近登录：" + client.lastDate.format(Constants.DATE_TIME_FORMATTER_H));

            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.a_authorization, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            DialogHelp1Binding binding1 = DialogHelp1Binding.inflate(getLayoutInflater());
            new MaterialAlertDialogBuilder(this)
                    .setView(binding1.getRoot())
                    .setPositiveButton("确定", null)
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void initView() {
        binding.listView.setAdapter(adapter);
        binding.listView.setOnItemClickListener(this);
    }

    void initData() {
        MyHttp.get("/user/clients", (resp) -> {
            clientList = Utils.gson.fromJson(resp, new TypeToken<List<Client1>>() {});
            if (clientList.isEmpty()) {
                binding.emptyView.getRoot().setVisibility(View.VISIBLE);
                binding.listView.setVisibility(View.INVISIBLE);
            } else {
                binding.emptyView.getRoot().setVisibility(View.INVISIBLE);
                binding.listView.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Client1 client = clientList.get(position);
        DialogTitleTextBinding binding1 = DialogTitleTextBinding.inflate(getLayoutInflater());
        binding1.title.setText("解除授权");
        binding1.text.setText("\"" + client.name + "\"将不能再访问你的账号资料。");

        new MaterialAlertDialogBuilder(this).setView(binding1.getRoot())
                .setPositiveButton("确定", (d, w) -> {
                    MyHttp.delete("/user/client-links/" + client.id, (resp) -> {
                        Utils.showToast("操作成功", Toast.LENGTH_SHORT);
                        initData();
                    });
                })
                .setNegativeButton("取消", null).show();
    }
}
