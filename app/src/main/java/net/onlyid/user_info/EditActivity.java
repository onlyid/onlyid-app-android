package net.onlyid.user_info;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import net.onlyid.R;

public class EditActivity extends AppCompatActivity {
    static final String TYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
    }
}