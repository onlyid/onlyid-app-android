package net.onlyid.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.MyApplication;
import net.onlyid.R;

import java.util.regex.Pattern;

public class Utils {
    public static ObjectMapper objectMapper;
    public static final SharedPreferences pref;
    public static ProgressDialog loadingDialog;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        pref = MyApplication.context.getSharedPreferences("main", Context.MODE_PRIVATE);
    }

    public static void showToast(String text, int duration) {
        Toast.makeText(MyApplication.context, text, duration).show();
    }

    public static void showAlertDialog(Context context, String message) {
        new MaterialAlertDialogBuilder(context, R.style.MyAlertDialog)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    public static void showLoadingDialog(Context context) {
        loadingDialog = new ProgressDialog(context, R.style.LoadingDialog);
        loadingDialog.setMessage("请稍候");
        loadingDialog.show();
    }

    public static boolean isMobile(String string) {
        Pattern pattern = Pattern.compile("^1\\d{10}$");
        return pattern.matcher(string).matches();
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}
