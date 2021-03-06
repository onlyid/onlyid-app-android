package net.onlyid.scan_login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import net.onlyid.AuthorizeActivity;
import net.onlyid.R;
import net.onlyid.databinding.ActivityScanLoginBinding;
import net.onlyid.entity.Client;
import net.onlyid.util.HttpUtil;
import net.onlyid.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanLoginActivity extends AppCompatActivity {
    static final String TAG = ScanLoginActivity.class.getSimpleName();
    static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
    };
    ActivityScanLoginBinding binding;
    MultiFormatReader multiFormatReader;
    ExecutorService executorService;
    ProcessCameraProvider cameraProvider;

    {
        multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        multiFormatReader.setHints(hints);

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        for (String permission : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                return;
            }
        }

        startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 1) return;

        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                Utils.showAlertDialog(this, "你禁止了相机权限，扫码登录不可用");
                return;
            }
        }

        startCamera();
    }

    void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                imageAnalysis.setAnalyzer(executorService, analyzer);

                cameraProvider = future.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    ImageAnalysis.Analyzer analyzer = new ImageAnalysis.Analyzer() {
        @Override
        public void analyze(@NonNull ImageProxy image) {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, image.getWidth(),
                    image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = multiFormatReader.decode(bitmap);

                runOnUiThread(cameraProvider::unbindAll);

                MediaPlayer mediaPlayer = MediaPlayer.create(ScanLoginActivity.this, R.raw.beep);
                mediaPlayer.start();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);

                Log.d(TAG, "result text: " + result.getText());
                try {
                    JSONObject jsonObject = new JSONObject(result.getText());
                    String uid = jsonObject.getString("uid");
                    String clientId = jsonObject.getString("clientId");
                    if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(clientId)) {
                        throw new Exception("uid或clientId为空");
                    }

                    HttpUtil.get("app/user-client-links/" + clientId + "/check", (c, s) -> {
                        JSONObject respBody = new JSONObject(s);
                        String clientString = respBody.getString("client");
                        Client client = Utils.objectMapper.readValue(clientString, Client.class);
                        if (respBody.getBoolean("linked")) {
                            callback(ScanLoginActivity.this, uid, client, true);
                            Intent intent = new Intent(ScanLoginActivity.this, SuccessActivity.class);
                            intent.putExtra("client", client);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(ScanLoginActivity.this, AuthorizeActivity.class);
                            intent.putExtra("client", client);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    Intent intent = new Intent(ScanLoginActivity.this, IllegalQrCodeActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (NotFoundException e) {
                // do nothing
            } finally {
                multiFormatReader.reset();
                image.close();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        executorService.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    public static void callback(Activity activity, String uid, Client client, boolean result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", result);
            jsonObject.put("uid", uid);
            jsonObject.put("clientId", client.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpUtil.post("app/scan-login", jsonObject, (c, s) -> {
            activity.finish();
        });
    }
}