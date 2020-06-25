package com.mark.opencvsample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCVHelper {

    /**
     * 均值模糊
     *
     * @param src
     * @param xRadius 为正数且是奇数
     * @param yRadius 为正数且是奇数
     * @return
     */
    public static Mat blur(Mat src, double xRadius, double yRadius) {
        Mat dst = new Mat(src.size(), src.type());
        Imgproc.blur(src, dst, new Size(xRadius, yRadius), new Point(-1, -1));
        return dst;
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
    public static Mat gaussianBlur(Mat src, Size size, double sigmaX, double sigmaY) {
        Mat dst = new Mat(src.size(), src.type());
        Imgproc.GaussianBlur(src, dst, size, sigmaX, sigmaY);
        return dst;
    }

    /**
     * 中值模糊
     *
     * @param src
     * @param kSize 大于1且为奇数
     * @return
     */
    public static Mat medianBlur(Mat src, int kSize) {
        Mat dst = new Mat(src.size(), src.type());
        Imgproc.medianBlur(src, dst, kSize);
        return dst;
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
    public static Mat bilateralFilter(Mat src, int d, double sigmaColor, double sigmaSpace) {
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
    public static Mat overlay(Mat src1, Mat src2, double alpha) {
        Mat dst = new Mat(src1.size(), src1.type());
        Core.addWeighted(src1, 1 - alpha, src2, alpha, 0.0, dst);
        return dst;
    }


    /**
     * 锐化
     */
    public static Mat sharpen(Mat src) {
        Mat dst = new Mat();
        //自定义图像的卷积核
        Mat sharpenKernel = new Mat(3, 3, CvType.CV_16SC1);
        sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
        Imgproc.filter2D(src, dst, src.depth(), sharpenKernel);
        return dst;

//        Mat usm = new Mat();
//        Mat gaussian = gaussianBlur(src, new Size(0, 0), 25, 25);
//        Core.addWeighted(src, 1.5, gaussian, -0.5, 0, usm);
//        return usm;
    }


    /**
     * 灰度化
     */
    public static Mat gray(Mat src) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        return dst;
    }


    public static Mat contrastAndBrightness(Mat src, double alpha, double beta, boolean fast) throws Throwable {
        Mat blank = Mat.zeros(src.size(), src.type());
        Mat dst = new Mat(src.size(), src.type());
        Core.addWeighted(src, alpha, blank, 1 - alpha, beta, dst);
        return dst;
    }


    /***
     * 亮度
     * alpha 大小决定对比度 大于0
     * beta  决定亮度
     * 使用opencv实现
     */
    @Deprecated
    public static Mat contrastAndBrightness(Mat src, double alpha, double beta) throws Throwable {
        Mat dst = Mat.zeros(src.size(), src.type());
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
        return dst;
    }


    /**
     * 亮度
     * 使用安卓原生Bitmap实现
     * 数据精确度不够 导致某些像素变脏了 无法实现
     *
     * @param bitmap
     */
    @Deprecated
    public static Bitmap contrastAndBrightness(Bitmap bitmap) throws Throwable {

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
        return copy;
    }


    private void test() {
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

        //Mat dst = OpenCVHelper.bilateralFilter(this.source, 5, 100, 3);


//        //模糊
//        Mat kernel = new Mat(3, 3, CvType.CV_16SC1);
//        kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
//        Mat dst = new Mat();
//        Imgproc.filter2D(source, dst, -1, kernel, new Point(-1, -1), 0);

    }

}
