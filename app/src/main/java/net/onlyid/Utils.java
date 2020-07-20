package net.onlyid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        new AlertDialog.Builder(context, R.style.MyAlertDialog)
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
}
