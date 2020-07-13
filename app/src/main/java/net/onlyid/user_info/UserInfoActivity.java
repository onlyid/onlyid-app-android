package net.onlyid.user_info;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.Utils;
import net.onlyid.entity.User;

public class UserInfoActivity extends AppCompatActivity {
    static final String TAG = "UserActivity";
    ImageView avatar;
    TextView nickname, mobile, email, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        avatar = findViewById(R.id.avatar);
        nickname = findViewById(R.id.nickname);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        gender = findViewById(R.id.gender);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * 放resume是因为从
     */
    @Override
    protected void onResume() {
        super.onResume();

        String userString = Utils.preferences.getString(Constants.USER, null);
        try {
            User user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatarUrl).into(avatar);
            nickname.setText(user.nickname);
            mobile.setText(user.mobile);
            email.setText(user.email);
            gender.setText(user.gender == null ? "-" : user.gender.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    public void avatar(View v) {
        startActivity(new Intent(this, AvatarActivity.class));
    }

    public void nickname(View v) {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(UserEditActivity.TYPE, "nickname");
        startActivity(intent);
    }

    public void mobile(View v) {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(UserEditActivity.TYPE, "mobile");
        startActivity(intent);
    }

    public void email(View v) {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(UserEditActivity.TYPE, "email");
        startActivity(intent);
    }

    public void gender(View v) {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(UserEditActivity.TYPE, "gender");
        startActivity(intent);
    }
}