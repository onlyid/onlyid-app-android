package onlyid.app;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Utils {
    public static ObjectMapper objectMapper;
    public static SharedPreferences preferences;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context);
    }
}
