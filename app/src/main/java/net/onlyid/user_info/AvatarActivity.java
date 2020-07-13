package net.onlyid.user_info;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import net.onlyid.Constants;
import net.onlyid.HttpUtil;
import net.onlyid.R;
import net.onlyid.Utils;
import net.onlyid.entity.User;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AvatarActivity extends AppCompatActivity {
    static final String TAG = "AvatarActivity";
    ImageView avatar;
    Uri captureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        avatar = findViewById(R.id.avatar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String userString = Utils.preferences.getString(Constants.USER, null);
        try {
            User user = Utils.objectMapper.readValue(userString, User.class);
            Glide.with(this).load(user.avatarUrl).into(avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.avatar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pick:
                pick();
                return true;
            case R.id.capture:
                capture();
                return true;
            default:
                return false;
        }
    }

    void pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    void capture() {
        try {
            File file = new File(getExternalCacheDir(), "avatar");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
            startActivityForResult(intent, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void crop(Uri uri) {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(getResources().getColor(R.color.gray));
        UCrop.of(uri, Uri.fromFile(new File(getExternalCacheDir(), "cropped-avatar")))
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);
    }

    void submit(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            File file = new File(getExternalCacheDir(), "resized-avatar");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            avatar.setImageBitmap(bitmap);

            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("file", null, RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .build();
            HttpUtil.post("img", requestBody, (Call c, String s) -> {
                HttpUtil.put("app/user/avatar", new JSONObject(s), (Call c1, String s1) -> {
                    HttpUtil.get("app/user", (Call c2, String s2) -> {
                        Utils.preferences.edit().putString(Constants.USER, s2).apply();
                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode != RESULT_OK) return;

            crop(data.getData());
        } else if (requestCode == 2) {
            if (resultCode != RESULT_OK) return;

            crop(captureUri);
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == UCrop.RESULT_ERROR) {
                UCrop.getError(data).printStackTrace();
                return;
            } else if (resultCode != RESULT_OK) return;

            submit(UCrop.getOutput(data));
        }
    }
}