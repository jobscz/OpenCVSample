package com.mark.opencvsample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mark.opencvsample.utils.ContentResolverHelper;
import com.mark.opencvsample.utils.OpenCVHelper;
import com.mark.opencvsample.utils.PermissionHelper;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int DEFAULT_IMAGE_RES_ID = R.drawable.test;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "static initializer: OpenCVLoader error");
        } else {
            Log.e(TAG, "static initializer: OpenCVLoader success");
        }
    }

    ImageView ivSource, ivPreview;
    SeekBar sbBrightness, sbContrast;

    double alpha, beta;
    Mat source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        ivSource = findViewById(R.id.iv_source);
        ivPreview = findViewById(R.id.iv_preview);
        sbBrightness = findViewById(R.id.sb_brightness);
        sbContrast = findViewById(R.id.sb_contrast);

    }

    private void initData() {
        ivSource.setImageBitmap(BitmapFactory.decodeResource(getResources(), DEFAULT_IMAGE_RES_ID));
        try {
            source = Utils.loadResource(this, DEFAULT_IMAGE_RES_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sbContrast.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                alpha = seekBar.getProgress() * 0.01;
                try {
                    Mat src = OpenCVHelper.contrastAndBrightness(source, alpha, beta, true);
                    display(OpenCVHelper.sharpen(src));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

        sbBrightness.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                beta = seekBar.getProgress();
                try {
                    Mat src = OpenCVHelper.contrastAndBrightness(source, alpha, beta, true);
                    display(OpenCVHelper.sharpen(src));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_sharpen:
                Mat sharpen = OpenCVHelper.sharpen(source);
                display(sharpen);
                break;
            case R.id.bt_gray:
                Mat gray = OpenCVHelper.gray(source);
                display(gray);
                break;
            case R.id.bt_blur:
                Mat blur = OpenCVHelper.blur(source, 51, 1);
                display(blur);
                break;
            case R.id.bt_system_camera:
                openSystemCamera();
                break;
            case R.id.bt_album:
                openAlbum(view);
                break;

            case R.id.bt_custom_camera:
                startActivity(new Intent(this,CameraActivity.class));
                break;

        }
    }

    private void openSystemCamera() {
        List<String> deniedPermissions = PermissionHelper.checkPermissions(this, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (deniedPermissions.isEmpty()) {
            realOpenSystemCamera();
        } else {
            PermissionHelper.requestPermissions(this, CAMERA_PERMISSION_REQUEST_CODE,
                    deniedPermissions.toArray(new String[deniedPermissions.size()]));
        }
    }

    Uri captureImageUri;

    private void realOpenSystemCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String imageName = "CV_" + new SimpleDateFormat("yyyyMMdd_hh_MM_ss").format(System.currentTimeMillis()) + ".jpg";
        try {
            captureImageUri = ContentResolverHelper.buildImageUri(imageName);
        } catch (Throwable throwable) {
            Toast.makeText(this, "realOpenSystemCamera " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
    }


    private void openAlbum(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }


    private void display(Mat src) {
        if (src.channels() != 1) {
            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2RGBA);
        }
        Bitmap dstBitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, dstBitmap);
        ivPreview.setImageBitmap(dstBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            Toast.makeText(this, "onActivityResult data == null", Toast.LENGTH_SHORT).show();
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "onActivityResult resultCode != Activity.RESULT_OK", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            handlePickImage(data);
        }
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            handleCaptureImage();
        }
    }

    private void handleCaptureImage() {
        try {
            Bitmap bitmap = ContentResolverHelper.readBitmap(captureImageUri);
            ivSource.setImageBitmap(bitmap);
            Mat dst = new Mat();
            Utils.bitmapToMat(bitmap, dst);
            source = dst;
        } catch (Throwable throwable) {
            Toast.makeText(this, "handleCaptureImage " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePickImage(Intent data) {
        try {
            Bitmap bitmap = ContentResolverHelper.readBitmap(data.getData());
            ivSource.setImageBitmap(bitmap);
            Mat dst = new Mat();
            Utils.bitmapToMat(bitmap, dst);
            source = dst;
        } catch (Throwable throwable) {
            Toast.makeText(this, "handlePickImage " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions.length == 2) {
                realOpenSystemCamera();
            }else {
                Toast.makeText(this, "权限被拒绝" + permissions, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SimpleSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

}