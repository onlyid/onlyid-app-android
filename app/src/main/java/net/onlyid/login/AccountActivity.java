package net.onlyid.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.PatternsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.MyHttp;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityAccountBinding;
import net.onlyid.databinding.DialogPrivacyBinding;
import net.onlyid.entity.Entity1;

public class AccountActivity extends BaseActivity {
    static final String TAG = "AccountActivity";
    ActivityAccountBinding binding;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setElevation(0);
        actionBar.setIcon(R.drawable.ic_logo_colored);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(" 唯ID");

        initView();
    }

    void initView() {
        SpannableString ss = new SpannableString("阅读并同意唯ID 服务协议、隐私政策");
        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TermsActivity.start(AccountActivity.this, "terms");
            }
        };
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TermsActivity.start(AccountActivity.this, "privacy");
            }
        };
        ss.setSpan(termsSpan, 9, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacySpan, 14, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.textView.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textView.setText(ss);
        binding.submitButton.setOnClickListener((v) -> validateFields());
        editText = binding.accountInput.getEditText();

        if (Utils.pref.getBoolean("showPrivacy", true)) showPrivacyDialog();
    }

    void showPrivacyDialog() {
        DialogPrivacyBinding binding1 = DialogPrivacyBinding.inflate(getLayoutInflater());

        SpannableString ss = new SpannableString("唯ID非常重视对个人信息的保护，为便于你理解《隐私政策》，现简要说明如下：");
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TermsActivity.start(AccountActivity.this, "privacy");
            }
        };
        ss.setSpan(span, 22, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding1.textView1.setMovementMethod(LinkMovementMethod.getInstance());
        binding1.textView1.setText(ss);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(binding1.getRoot())
                .setPositiveButton("同意，开始使用", (d, w) ->
                        Utils.pref.edit().putBoolean("showPrivacy", false).apply())
                .setNeutralButton("不同意，退出", (d, w) -> finish())
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setLetterSpacing(0);
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setLetterSpacing(0);
        neutralButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
    }

    void validateFields() {
        String account = editText.getText().toString();

        if (TextUtils.isEmpty(account)) {
            Utils.showAlert(this, "请填写手机号/邮箱");
            return;
        }

        if (account.contains("@")) {
            if (!PatternsCompat.EMAIL_ADDRESS.matcher(account).matches()) {
                Utils.showAlert(this, "邮箱格式不正确");
                return;
            }
        } else {
            if (!Utils.isMobile(account)) {
                Utils.showAlert(this, "手机号格式不正确");
                return;
            }
        }

        if (!binding.checkBox.isChecked()) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.checkbox_required);
            ((View) binding.checkBox.getParent()).startAnimation(animation);
            return;
        }

        submit();
    }

    void submit() {
        String account = editText.getText().toString();
        MyHttp.get("/auth/check-account?account=" + account, resp -> {
            Intent intent;
            if (TextUtils.isEmpty(resp)) {
                intent = new Intent(this, SignUpActivity.class);
                intent.putExtra("account", account);
            } else {
                Entity1 user = Utils.gson.fromJson(resp, Entity1.class);
                user.account = account;
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra("user", user);
            }
            //noinspection deprecation
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }
}
