package net.onlyid.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.onlyid.MyApplication;
import net.onlyid.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static ObjectMapper objectMapper;
    public static SharedPreferences sharedPreferences;
    public static ProgressDialog loadingDialog;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context);
    }

    public static void showToast(String message, int duration) {
        Toast toast = Toast.makeText(MyApplication.context, message, duration);
        ViewGroup viewGroup = (ViewGroup) toast.getView();
        TextView textView = (TextView) viewGroup.getChildAt(0);
        textView.setTextSize(15);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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

    public static boolean isMobile(String s) {
        Pattern p = Pattern.compile("^1\\d{10}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static int getStatusBarHeight(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowFullscreen
        });
        boolean windowFullscreen = typedArray.getBoolean(0, false);
        typedArray.recycle();

        if (windowFullscreen) {
            return 0;
        }

        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

}
