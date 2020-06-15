package com.mark.opencvsample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "static initializer: OpenCVLoader error");
        } else {
            Log.e(TAG, "static initializer: OpenCVLoader success");
        }
    }

    ImageView ivPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivPreview = findViewById(R.id.iv_preview);

        light();
    }


    /***
     * 亮度
     * alpha 大小决定对比度 大于0
     * beta  决定亮度
     * 使用opencv实现
     */
    private void light() {
        try {
            Mat src = Utils.loadResource(getApplicationContext(), R.drawable.test, Imgcodecs.IMREAD_UNCHANGED);

            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);

            Mat dst = Mat.zeros(src.size(), src.type());

            double alpha = 1.05, beta = 6;

            int cols = src.cols();
            int rows = src.rows();
            int channels = src.channels();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (channels == 3) {
                        double[] bgr = src.get(i, j);
                        double b = bgr[0];
                        double g = bgr[1];
                        double r = bgr[2];
                        double d_b = b * alpha + beta;
                        double d_g = g * alpha + beta;
                        double d_r = r * alpha + beta;
                        dst.put(i, j, d_b, d_g, d_r);
                    } else if (channels == 1) {

                    } else if (channels == 4) {

                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(dst, bitmap);
            ivPreview.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 亮度
     * 使用安卓原生Bitmap实现
     * 数据精确度不够 导致某些像素变脏了 无法实现
     * @param sys
     */
    private void light(boolean sys) {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

        Bitmap copy = bitmap.copy(Bitmap.Config.RGB_565, true);

        int width = copy.getWidth();
        int height = copy.getHeight();
        float alpha = 1.0f;
        int beta = 10;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = copy.getPixel(i, j);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);

                int dstRed = (int) (red * alpha) + beta;
                int dstGreen = (int) (green * alpha) + beta;
                int dstBlue = (int) (blue * alpha) + beta;

                int dstColor = Color.rgb(dstRed, dstGreen, dstBlue);
                copy.setPixel(i, j, dstColor);
            }
        }
        ivPreview.setImageBitmap(copy);


    }
}