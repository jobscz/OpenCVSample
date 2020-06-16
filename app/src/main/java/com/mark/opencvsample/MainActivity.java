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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
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

    ImageView ivSource, ivPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivSource = findViewById(R.id.iv_source);
        ivPreview = findViewById(R.id.iv_preview);

        ivSource.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.test));


        Mat source = null;
        try {
            source = Utils.loadResource(getApplicationContext(), R.drawable.test);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Mat gray = gray(source);
        //Mat sharpen = sharpen(gray);


        //Mat gray = gray(source);
        // Mat dst = brightness(source, 1.2, 30);
        //Mat dst = sharpen(gray);

//        Mat source2 = null;
//        try {
//            source2 = Utils.loadResource(getApplicationContext(), R.drawable.test_copy);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Mat dst = overlay(source2, source, 0.8);


        //Mat dst = blur(source, 51, 1);


        //Mat dst = gaussianBlur(source, new Size(33, 33), 33, 33);
        //Mat dst = medianBlur(source, 55);

        Mat dst = bilateralFilter(source, 5, 100, 3);


//        //模糊
//        Mat kernel = new Mat(3, 3, CvType.CV_16SC1);
//        kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
//        Mat dst = new Mat();
//        Imgproc.filter2D(source, dst, -1, kernel, new Point(-1, -1), 0);

        Mat dstMat = new Mat();
        Imgproc.cvtColor(dst, dstMat, Imgproc.COLOR_BGR2RGB);
        Bitmap dstBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstMat, dstBitmap);
        ivPreview.setImageBitmap(dstBitmap);
    }

    /**
     * 均值模糊
     *
     * @param src
     * @param xRadius 为正数且是奇数
     * @param yRadius 为正数且是奇数
     * @return
     */
    private Mat blur(Mat src, double xRadius, double yRadius) {
        Imgproc.blur(src, src, new Size(xRadius, yRadius), new Point(-1, -1));
        return src;
    }

    /**
     * 高斯模糊
     *
     * @param src
     * @param size   中 x,y 均为正数且是奇数
     * @param sigmaX
     * @param sigmaY
     * @return
     */
    private Mat gaussianBlur(Mat src, Size size, double sigmaX, double sigmaY) {
        Imgproc.GaussianBlur(src, src, size, sigmaX, sigmaY);
        return src;
    }

    /**
     * 中值模糊
     *
     * @param src
     * @param kSize 大于1且为奇数
     * @return
     */
    private Mat medianBlur(Mat src, int kSize) {
        Imgproc.medianBlur(src, src, kSize);
        return src;
    }

    /**
     * 双边模糊(磨皮效果)
     *
     * @param src
     * @param d          计算的半径，半径之内的像素都会被纳入计算，如果为-1，则根据sigmaSpace 参数取值
     * @param sigmaColor 决定多少差值内的像素会被计算
     * @param sigmaSpace 如果d的值大于0，则无效，否则根据它计算d值
     * @return
     */
    private Mat bilateralFilter(Mat src, int d, double sigmaColor, double sigmaSpace) {
        Mat dst = new Mat(src.size(), src.type());
        Imgproc.bilateralFilter(src, dst, d, sigmaColor, sigmaSpace);
        return dst;
    }


    /**
     * 图像重叠
     *
     * @param src1
     * @param src2
     * @param alpha
     * @return
     */
    private Mat overlay(Mat src1, Mat src2, double alpha) {
        Mat dst = new Mat(src1.size(), src1.type());
        Core.addWeighted(src1, 1 - alpha, src2, alpha, 0.0, dst);
        return dst;
    }


    /**
     * 锐化
     */
    private Mat sharpen(Mat src) {

        //自定义图像的卷积核
        Mat sharpenKernel = new Mat(3, 3, CvType.CV_16SC1);
        sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
        Imgproc.filter2D(src, src, src.depth(), sharpenKernel);

        return src;
    }


    /**
     * 灰度化
     */
    private Mat gray(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        return src;
    }


    /***
     * 亮度
     * alpha 大小决定对比度 大于0
     * beta  决定亮度
     * 使用opencv实现
     */
    private Mat brightness(Mat src, double alpha, double beta) {
        Mat dst = Mat.zeros(src.size(), src.type());
        try {
            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);
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
                        double[] gray = src.get(i, j);
                        double d_gray = gray[0] * alpha + beta;
                        dst.put(i, j, d_gray);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dst;
    }


    /**
     * 亮度
     * 使用安卓原生Bitmap实现
     * 数据精确度不够 导致某些像素变脏了 无法实现
     *
     * @param sys
     */
    private void brightness(boolean sys) {

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