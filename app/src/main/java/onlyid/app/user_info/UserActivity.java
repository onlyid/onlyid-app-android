package onlyid.app.user_info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import onlyid.app.Constants;
import onlyid.app.R;
import onlyid.app.Utils;
import onlyid.app.entity.User;

public class UserActivity extends AppCompatActivity {
    static final String TAG = "UserActivity";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView imageView = findViewById(R.id.image_view);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String s = Utils.preferences.getString(Constants.USER, null);
        try {
            if (s != null) user = Utils.objectMapper.readValue(s, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Glide.with(this).load(user.avatarUrl).into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}