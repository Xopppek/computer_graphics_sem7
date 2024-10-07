import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Arrays;

public class Lab2 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        Mat gojoImg = Imgcodecs.imread("src/resources/lab2/gojo.png");
        Mat cubeImg = toGrayScale(Imgcodecs.imread("src/resources/lab2/cube.png"));

        Mat gojoDithered = floydSteinbergDithering(gojoImg, 1);
        HighGui.imshow("Gojo", gojoDithered);

        //Mat cubeDithered = floydSteinbergDithering(cubeImg, 1);
        //HighGui.imshow("cube", cubeDithered);

        for (int i = 1; i < 5; i++){
            // изображение в градации серого
            Mat curCubeImg = floydSteinbergDithering(cubeImg, i);
            Imgcodecs.imwrite("src/results/lab2/floyd_cube_" + i + "bpp.png", curCubeImg);
            curCubeImg = floydSteinbergDithering(cubeImg, i, true);
            Imgcodecs.imwrite("src/results/lab2/floyd_dirchange_cube_" + i + "bpp.png", curCubeImg);

            // цветное изображение
            Mat curGojoImg = floydSteinbergDithering(gojoImg, i);
            Imgcodecs.imwrite("src/results/lab2/floyd_gojo_" + i + "bpp.png", curGojoImg);
            curGojoImg = floydSteinbergDithering(gojoImg, i, true);
            Imgcodecs.imwrite("src/results/lab2/floyd_dirchange_gojo_" + i + "bpp.png", curGojoImg);
        }

        HighGui.waitKey(0);
    }

    private static byte[] getUniformPalette(int n){
        int colorsCount = (int) Math.pow(2, n);
        int step = 255 / (colorsCount - 1);

        var colors = new byte[colorsCount];
        for (int i = 0; i < colorsCount; i++){
            colors[i] = (byte) (i * step);
        }
        // нужно быть осторожным, так как byte принимает отрицательные значения
        return colors;
    }

    private static byte getClosestColorInPalette(byte value, byte[] palette){
        byte result = palette[0];
        int minDiff = Math.abs((value & 0xFF) - (result & 0xFF));
        for (var color : palette){
            int curDiff = Math.abs((color & 0xFF) - (value & 0xFF));
            if (curDiff < minDiff){
                minDiff = curDiff;
                result = color;
            }
        }
        return result;
    }

    public static Mat floydSteinbergDithering(Mat img, int n){
        return floydSteinbergDithering(img, n, false);
    }

    public static Mat floydSteinbergDithering(Mat img, int n, boolean isChangingDirection){
        // реализован только для равномерной палитры
        if (img == null)
            throw new IllegalArgumentException("На вход пришел null");
        if (n > 8)
            throw new IllegalArgumentException("n должно быть меньше 8");
        if (n == 8)
            return img;

        var result = img.clone();
        var palette = getUniformPalette(n);

        // деление на 16 будет уже внутри работы с пикселями побитовым сдвигом
        var ditheringMatrix = new int[][]{
                {0, 0, 7},
                {3, 5, 1},
        };

        // чтобы функция была универсальной, но при этом не портила альфа канал
        int colorChannelsCount = Math.min(img.channels(), 3);
        var pixel = new byte[img.channels()];
        var errs = new int[colorChannelsCount];

        for (int y = 0; y < img.rows(); y++){
            int x_end, x_start, dithMatEnd, dithMatStart, dithMatStep;
            if (y % 2 == 0 & isChangingDirection){
                x_end = -1;
                x_start = img.cols() - 1;
                dithMatEnd = -1;
                dithMatStart = ditheringMatrix[0].length - 1;
                dithMatStep = -1;
            }else{
                x_end = img.cols();
                x_start = 0;
                dithMatEnd = ditheringMatrix[0].length;
                dithMatStart = 0;
                dithMatStep = 1;
            }
            for (int x = x_start; x < x_end; x++){
                result.get(y, x, pixel);
                for (int i = 0; i < colorChannelsCount; i++){
                    int oldValue = pixel[i] & 0xFF;
                    pixel[i] = getClosestColorInPalette(pixel[i], palette);
                    result.put(y, x, pixel);
                    errs[i] = oldValue - (pixel[i] & 0xFF);
                }

                for (int i = 0; i < ditheringMatrix.length; i++){
                    for (int j = dithMatStart; j < dithMatEnd; j += dithMatStep){
                        int y_ = y + i;
                        int x_ = x + j - 1;
                        if (y_ < img.rows() && x_ >= 0 && x_ < img.cols()){
                            var curPixel = new byte[img.channels()];
                            result.get(y_, x_, curPixel);
                            for (int k = 0; k < colorChannelsCount; k++){
                                curPixel[k] = (byte) Math.min(Math.max((curPixel[k] & 0xFF) + ((errs[k] * ditheringMatrix[i][j]) >> 4), 0), 255);
                            }
                            result.put(y_, x_, curPixel);
                        }
                    }
                }
            }
        }

        return result;
    }

    public static Mat toGrayScale(Mat img){
        // на вход должно прийти либо 3, либо 4 канальное 8bpp изображение
        // альфа канал, при его наличии, просто игнорируется
        if (img == null || !(img.type() == CvType.CV_8UC3 || img.type() == CvType.CV_8UC4))
            throw new IllegalArgumentException("На вход пришло не 3 или 4 канальное 8bpp изображение");
        // CV_8UC1 - один канал unsigned char (8bpp)
        var result = new Mat(img.size(), CvType.CV_8UC1);
        // в OpenCV изображения хранятся в BGR
        var bgr = new byte[img.channels()];
        var gray = new byte[1];

        for (int x = 0; x < img.cols(); x++){
            for (int y = 0; y < img.rows(); y++){
                img.get(y, x, bgr);
                // так как в Java нет unsigned типов, при записи значений выше 127 в byte
                // происходит переполнение. В формуле происходит неявное приведение в int
                // важно, что gray - массив byte, иначе put сработает некорректно (по тем же причинам)
                gray[0] = (byte) (0.299 * (bgr[2] & 0xFF) +
                                  0.587 * (bgr[1] & 0xFF) +
                                  0.114 * (bgr[0] & 0xFF));
                result.put(y, x, gray);
            }
        }
        return result;
    }
}
