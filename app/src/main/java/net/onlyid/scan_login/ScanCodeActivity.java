package net.onlyid.scan_login;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Size;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
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

import net.onlyid.R;
import net.onlyid.common.BaseActivity;
import net.onlyid.common.PermissionDialog;
import net.onlyid.common.Utils;
import net.onlyid.databinding.ActivityScanCodeBinding;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanCodeActivity extends BaseActivity {
    static final String TAG = "ScanCodeActivity";
    static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
    };

    ActivityScanCodeBinding binding;
    MultiFormatReader multiFormatReader;
    ExecutorService executorService;
    ProcessCameraProvider cameraProvider;
    Dialog permissionDialog;

    {
        multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, true);
        multiFormatReader.setHints(hints);

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backImageView.setOnClickListener((v) -> onBackPressed());

        for (String permission : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                requestPermissions(PERMISSIONS, 1);

                permissionDialog = new PermissionDialog(this, "相机权限使用说明", "用于实现扫描二维码功能");
                permissionDialog.show();

                return;
            }
        }

        startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1) return;

        permissionDialog.dismiss();

        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                Utils.showAlert(this, "没有相机权限，扫码登录不可用");
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

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(960, 1280)).build();
                imageAnalysis.setAnalyzer(executorService, analyzer);

                cameraProvider = future.get();
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
                camera.getCameraControl().setZoomRatio(1.2f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    ImageAnalysis.Analyzer analyzer = new ImageAnalysis.Analyzer() {
        @Override
        public void analyze(ImageProxy image) {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, image.getWidth(),
                    image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = multiFormatReader.decodeWithState(bitmap);

                runOnUiThread(cameraProvider::unbindAll);

                MediaPlayer mediaPlayer = MediaPlayer.create(ScanCodeActivity.this, R.raw.beep);
                mediaPlayer.setVolume(0.1f, 0.1f);
                mediaPlayer.start();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

                Intent intent = new Intent();
                intent.putExtra("scanResult", result.getText());
                setResult(RESULT_OK, intent);
                finish();
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
}
