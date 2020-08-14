package net.onlyid.scan_login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import net.onlyid.Utils;
import net.onlyid.databinding.ActivityScanLoginBinding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScanLoginActivity extends AppCompatActivity {
    static final String TAG = "ScanLoginActivity";
    static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
    };
    ActivityScanLoginBinding binding;
    CameraManager cameraManager;
    CameraCharacteristics characteristics;
    CameraDevice cameraDevice;
    ImageReader imageReader;
    CameraCaptureSession captureSession;
    MultiFormatReader multiFormatReader;
    HandlerThread decodeThread = new HandlerThread("decode");
    Handler decodeHandler;

    {
        multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hints);

        decodeThread.start();
        decodeHandler = new Handler(decodeThread.getLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        for (String permission : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                return;
            }
        }

        binding.viewFinder.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    openCamera();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
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

        try {
            openCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    void openCamera() throws CameraAccessException {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        // 找到一个兼容的后置摄像头
        String cameraId = null;
        for (String id : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
            int[] capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            List<Integer> capabilityList = Arrays.stream(capabilities).boxed().collect(Collectors.toList());
            if (!capabilityList.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE))
                continue;

            if (characteristics.get(CameraCharacteristics.LENS_FACING) != CameraMetadata.LENS_FACING_BACK)
                continue;

            cameraId = id;
        }
        Log.d(TAG, "cameraId: " + cameraId);

        // 打开摄像头
        characteristics = cameraManager.getCameraCharacteristics(cameraId);
        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG, "onOpened, camera: " + camera);
                cameraDevice = camera;
                try {
                    createCaptureSession();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d(TAG, "onDisconnected: " + camera);
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.e(TAG, "onError: " + error);
            }
        }, null);
    }

    /**
     * 新建拍摄会话
     */
    void createCaptureSession() throws CameraAccessException {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        Size size = Arrays.stream(sizes)
                // 过滤掉2000以上的，不知为什么，太高分别率识别不到
                .filter((s) -> s.getWidth() < 2000 && s.getHeight() < 2000)
                // 取最大的
                .max((a, b) -> a.getHeight() * a.getWidth() - b.getHeight() * b.getWidth())
                .get();
        Log.d(TAG, "sizes: " + Arrays.toString(sizes));
        Log.d(TAG, "size: " + size);

        imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener((reader) -> {
            Image image = reader.acquireLatestImage();
            if (image == null) return;

            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, image.getWidth(),
                    image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = multiFormatReader.decode(bitmap);
                String s = result.getText();
                Log.d(TAG, "result: " + s);

                captureSession.close();
            } catch (NotFoundException e) {
                // do nothing
            } finally {
                multiFormatReader.reset();
            }

            image.close();
        }, decodeHandler);

        List<Surface> outputs = new ArrayList<>();
        outputs.add(imageReader.getSurface());
        outputs.add(binding.viewFinder.getHolder().getSurface());
        cameraDevice.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                captureSession = session;
                try {
                    startPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(TAG, "onConfigureFailed: " + session);
            }
        }, null);
    }

    /**
     * 开始预览
     */
    void startPreview() throws CameraAccessException {
        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(binding.viewFinder.getHolder().getSurface());
        builder.addTarget(imageReader.getSurface());
        captureSession.setRepeatingRequest(builder.build(), null, null);
    }
}