package net.onlyid.login_history;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import net.onlyid.databinding.ActivityLoginHistoryBinding;
import net.onlyid.databinding.DialogHelp2Binding;
import net.onlyid.databinding.DialogTitleTextBinding;
import net.onlyid.databinding.ItemLoginHistoryBinding;
import net.onlyid.entity.Entity2;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class LoginHistoryActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    static final String TAG = "LoginHistoryActivity";
    ActivityLoginHistoryBinding binding;
    List<Entity2> list = new ArrayList<>();

    BaseAdapter adapter = new MyAdapter(() -> list) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemLoginHistoryBinding binding1;
            if (convertView == null) {
                binding1 = ItemLoginHistoryBinding.inflate(getLayoutInflater());
                convertView = binding1.getRoot();
                convertView.setTag(binding1);
            } else {
                binding1 = (ItemLoginHistoryBinding) convertView.getTag();
            }

            Entity2 entity = list.get(position);
            int radius = Utils.dp2px(LoginHistoryActivity.this, 4);
            Glide.with(convertView).load(entity.clientIconUrl)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding1.imageView);

            binding1.nameTextView.setText(setSpan("登录应用：" + entity.clientName));
            binding1.typeTextView.setText(entity.clientType.toString());
            String location = TextUtils.isEmpty(entity.location) ? "-" : entity.location;
            binding1.locationTextView.setText(setSpan("登录地点：" + location));
            String date = entity.createDate.format(Constants.DATE_TIME_FORMATTER_H);
            binding1.dateTextView.setText(setSpan("登录时间：" + date));

            return convertView;
        }
    };

    CharSequence setSpan(String string) {
        SpannableString ss = new SpannableString(string);
        ForegroundColorSpan span = new ForegroundColorSpan(getResources().getColor(R.color.text_primary, null));
        ss.setSpan(span, 5, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.a_login_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            DialogHelp2Binding binding1 = DialogHelp2Binding.inflate(getLayoutInflater());
            new MaterialAlertDialogBuilder(this).setView(binding1.getRoot())
                    .setPositiveButton("确定", null).show();
            return true;
        } else if (item.getItemId() == R.id.empty) {
            DialogTitleTextBinding binding1 = DialogTitleTextBinding.inflate(getLayoutInflater());
            binding1.title.setText("清空记录");
            binding1.text.setText("清空全部历史记录，清空后不能恢复。");

            new MaterialAlertDialogBuilder(this).setView(binding1.getRoot())
                    .setPositiveButton("确定", (d, w) -> {
                        MyHttp.delete("/user-logs", (resp) -> {
                            Utils.showToast("删除成功", Toast.LENGTH_SHORT);
                            list.clear();
                            updateView();
                        });
                    }).setNegativeButton("取消", null).show();
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
        MyHttp.get("/user-logs", (resp) -> {
            list = Utils.gson.fromJson(resp, new TypeToken<List<Entity2>>() {});
            updateView();
        });
    }

    void updateView() {
        if (list.isEmpty()) {
            binding.emptyView.getRoot().setVisibility(View.VISIBLE);
            binding.listView.setVisibility(View.INVISIBLE);
        } else {
            binding.emptyView.getRoot().setVisibility(View.INVISIBLE);
            binding.listView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Entity2 entity = list.get(position);
        DialogTitleTextBinding binding1 = DialogTitleTextBinding.inflate(getLayoutInflater());
        binding1.title.setText("删除记录");
        binding1.text.setText("删除这条历史记录，删除后不能恢复。");

        new MaterialAlertDialogBuilder(this).setView(binding1.getRoot())
                .setPositiveButton("确定", (d, w) -> {
                    MyHttp.delete("/user-logs/" + entity.id, (resp) -> {
                        Utils.showToast("删除成功", Toast.LENGTH_SHORT);
                        list.remove(position);
                        updateView();
                    });
                }).setNegativeButton("取消", null).show();
    }
}
