package net.onlyid.switch_account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import net.onlyid.MainActivity;
import net.onlyid.MyApplication;
import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.Constants;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityDeleteAccountBinding;
import net.onlyid.entity.Session;
import net.onlyid.entity.User;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DeleteAccountActivity extends BaseActivity {
    ActivityDeleteAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        initView();
    }

    void initView() {
        User user = MyApplication.getCurrentUser();

        int radius = Utils.dp2px(this, 5);
        Glide.with(this).load(user.avatar)
                .transform(new RoundedCornersTransformation(radius, 0))
                .into(binding.avatarImageView);
        binding.nicknameTextView.setText(user.nickname);
        binding.accountTextView.setText(TextUtils.isEmpty(user.email) ? user.mobile : user.email);

        binding.submitButton.setOnClickListener((v) -> submit());
    }

    void submit() {
        if (!binding.checkBox.isChecked()) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.checkbox_required);
            ((View) binding.checkBox.getParent()).startAnimation(animation);
            return;
        }

        MyHttp.delete("/user", resp -> {
            // 维护sessionList
            String sessionListString = Utils.pref.getString(Constants.SESSION_LIST, null);
            List<Session> list = Utils.gson.fromJson(sessionListString, new TypeToken<List<Session>>() {});
            String token = Utils.pref.getString(Constants.TOKEN, null);

            for (int i = 0; i < list.size(); i++) {
                Session session  = list.get(i);
                if (session.token.equals(token)) {
                    list.remove(i);
                    Utils.pref.edit().putString(Constants.SESSION_LIST, Utils.gson.toJson(list)).apply();
                    break;
                }
            }

            // 清除当前登录用户
            Utils.pref.edit().remove(Constants.TOKEN).remove(Constants.USER).apply();

            Utils.showToast("注销成功", Toast.LENGTH_SHORT);

            Intent intent = new Intent(this, MainActivity.class);
            // 两个flag都要设置，这样不管target是在栈顶还是在栈下面，都会调起onNewIntent
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // 这里不需要触发登录，因为由切换账号页回去，自然就会调起syncUserInfo
            startActivity(intent);
        });
    }
}
