import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public class Lab2 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        Mat gojoImg = Imgcodecs.imread("src/resources/lab2/gojo.png");
        Mat gojoGray = toGrayScale(gojoImg);
        HighGui.imshow("Gojo", gojoGray);
        HighGui.waitKey(0);
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

        for (int x = 0; x < img.rows(); x++){
            for (int y = 0; y < img.cols(); y++){
                img.get(x, y, bgr);
                // так как в Java нет unsigned типов, при записи значений выше 127 в byte
                // происходит переполнение. В формуле происходит неявное приведение в int
                // важно, что gray - массив byte, иначе put сработает некорректно (по тем же причинам)
                gray[0] = (byte) (0.299 * (bgr[2] & 0xFF) +
                                  0.587 * (bgr[1] & 0xFF) +
                                  0.114 * (bgr[0] & 0xFF));
                result.put(x, y, gray);
            }
        }
        return result;
    }
}
