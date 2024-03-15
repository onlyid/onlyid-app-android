package net.onlyid.common;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import net.onlyid.MyApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Utils {
    static final String TAG = "Utils";
    public static final Gson gson;
    public static ObjectMapper objectMapper;
    public static final SharedPreferences pref;
    static Dialog loadingDialog;

    static {
        JsonDeserializer<LocalDateTime> localDateTimeDeserializer =
                (json, type, context) -> LocalDateTime.parse(json.getAsString());
        JsonDeserializer<LocalDate> localDateDeserializer =
                (json, type, context) -> LocalDate.parse(json.getAsString());
        JsonSerializer<LocalDateTime> localDateTimeSerializer =
                (src, type, context) -> new JsonPrimitive(src.format(Constants.DATE_TIME_FORMATTER));
        JsonSerializer<LocalDate> localDateSerializer =
                (src, type, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));

        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer)
                .registerTypeAdapter(LocalDate.class, localDateDeserializer)
                .registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer)
                .registerTypeAdapter(LocalDate.class, localDateSerializer)
                .create();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        pref = MyApplication.context.getSharedPreferences("main", Context.MODE_PRIVATE);
    }

    public static void showToast(String text, int duration) {
        Toast.makeText(MyApplication.context, text, duration).show();
    }

    public static void showAlert(Context context, String message) {
        new MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    public static void showLoading(Context context) {
        loadingDialog = new LoadingDialog(context);
        loadingDialog.show();
    }

    public static void hideLoading() {
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    public static boolean isMobile(String string) {
        Pattern pattern = Pattern.compile("^1\\d{10}$");
        return pattern.matcher(string).matches();
    }

    public static int dp2px(Context context, int dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float pxValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        return (int) (pxValue + 0.5f);
    }
}
