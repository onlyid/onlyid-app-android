package net.onlyid.switch_account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.reflect.TypeToken;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyAdapter;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivitySwitchAccountBinding;
import net.onlyid.databinding.ItemSwitchAccountBinding;
import net.onlyid.entity.Session;
import net.onlyid.entity.User;
import net.onlyid.login.AccountActivity;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SwitchAccountActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    static final int LOGIN = 1;
    ActivitySwitchAccountBinding binding;
    List<Session> list = new ArrayList<>();
    String token; // 当前用户的token

    BaseAdapter adapter = new MyAdapter(() -> list) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //noinspection ViewHolder
            ItemSwitchAccountBinding binding1 = ItemSwitchAccountBinding.inflate(getLayoutInflater());
            Session session = list.get(position);
            User user = session.user;

            int radius = Utils.dp2px(SwitchAccountActivity.this, 6);
            Glide.with(SwitchAccountActivity.this).load(user.avatar)
                    .transform(new RoundedCornersTransformation(radius, 0))
                    .into(binding1.avatarImageView);

            binding1.nicknameTextView.setText(user.nickname);
            binding1.accountTextView.setText(TextUtils.isEmpty(user.email) ? user.mobile : user.email);

            if (token.equals(session.token)) {
                binding1.usingLabel.setVisibility(View.VISIBLE);
                binding1.arrowRight.getRoot().setVisibility(View.GONE);
            }

            return binding1.getRoot();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySwitchAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle("");

        initData();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_switch_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            for (int i = 0; i < list.size(); i++) {
                Session session  = list.get(i);
                if (session.token.equals(token)) {
                    logout(token, i);
                    break;
                }
            }
            return true;
        } else if (item.getItemId() == R.id.delete_account) {
            Intent intent = new Intent(this, WarnDeleteActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void initData() {
        String sessionListString = Utils.pref.getString(Constants.SESSION_LIST, null);
        list = Utils.gson.fromJson(sessionListString, new TypeToken<List<Session>>() {});

        // 过滤掉已经过期的
        list.removeIf(session -> session.expireDate.isBefore(LocalDateTime.now()));
        Utils.pref.edit().putString(Constants.SESSION_LIST, Utils.gson.toJson(list)).apply();

        token = Utils.pref.getString(Constants.TOKEN, null);
    }

    void initView() {
        binding.listView.setAdapter(adapter);
        binding.listView.setOnItemClickListener(this);
        binding.listView.setOnItemLongClickListener(this);

        binding.addButton.setOnClickListener((v) -> {
            //noinspection deprecation
            startActivityForResult(new Intent(this, AccountActivity.class), LOGIN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN && resultCode == RESULT_OK) {
//            Utils.showToast("添加成功", Toast.LENGTH_SHORT);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Session session = list.get(position);
        if (!session.token.equals(token)) {
            Utils.pref.edit()
                    .putString(Constants.TOKEN, session.token)
                    .putString("user", Utils.gson.toJson(session.user))
                    .apply();
            Utils.showToast("切换成功", Toast.LENGTH_SHORT);
        }
        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        PopupMenu popupMenu = new PopupMenu(this, view.findViewById(R.id.anchor), Gravity.RIGHT);
        popupMenu.getMenuInflater().inflate(R.menu.account_logout, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            Session session = list.get(position);

            if (session.token.equals(token))
                new MaterialAlertDialogBuilder(this)
                        .setMessage("要退出当前使用的账号吗？")
                        .setPositiveButton("确定", (d, w) -> logout(session.token, position))
                        .setNegativeButton("取消", null)
                        .show();
            else
                logout(session.token, position);

            return true;
        });

        popupMenu.show();
        return true;
    }

    void logout(String token, int position) {
        MyHttp.post("/devices/logout/" + token, new JSONObject(), (resp) -> {
            list.remove(position);
            Utils.pref.edit().putString(Constants.SESSION_LIST, Utils.gson.toJson(list)).apply();

            // 退出了当前账号
            if (token.equals(this.token))
                Utils.pref.edit().remove(Constants.TOKEN).remove(Constants.USER).apply();

            // 如果sessionList已经空了，用户没有切换账号的可能，那就finish
            if (list.isEmpty()) {
                finish();
            } else {
                Utils.showToast("退出成功", Toast.LENGTH_SHORT);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
