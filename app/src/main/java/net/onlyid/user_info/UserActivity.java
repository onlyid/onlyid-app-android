package net.onlyid.user_info;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.onlyid.Constants;
import net.onlyid.R;
import net.onlyid.Utils;
import net.onlyid.entity.User;

public class UserActivity extends AppCompatActivity {
    static final String TAG = "UserActivity";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ImageView avatar = findViewById(R.id.avatar);
        TextView nickname = findViewById(R.id.nickname);
        TextView mobile = findViewById(R.id.mobile);
        TextView email = findViewById(R.id.email);
        TextView gender = findViewById(R.id.gender);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String s = Utils.preferences.getString(Constants.USER, null);
        try {
            user = Utils.objectMapper.readValue(s, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Glide.with(this).load(user.avatarUrl).into(avatar);
        nickname.setText(user.nickname);
        mobile.setText(user.mobile);
        email.setText(user.email);
        gender.setText(user.gender == null ? "-" : user.gender.toString());
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

    public void avatar(View v) {
        startActivity(new Intent(this, AvatarActivity.class));
    }

    public void nickname(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.TYPE, "nickname");
        startActivity(intent);
    }

    public void mobile(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.TYPE, "mobile");
        startActivity(intent);
    }

    public void email(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.TYPE, "email");
        startActivity(intent);
    }

    public void gender(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.TYPE, "gender");
        startActivity(intent);
    }
}