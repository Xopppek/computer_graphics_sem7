import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;

public class Lab3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        var canvas = new Canvas(1024, 800);
        canvas.drawPoint(14, 50, Canvas.Color.BLACK);
        HighGui.imshow("Canvas", canvas.getImage());
        HighGui.waitKey(0);
    }
}

class Canvas{
    // now you can only create canvas with fixed sizes
    // and with BGR 8bpp for channel type
    // it can't be changed after creating
    private Mat image;

    public enum Color{
        RED(new byte[]{(byte) 255, 0, 0}),
        BLUE(new byte[]{0, 0, (byte) 255}),
        GREEN(new byte[]{0, (byte) 255, 0}),
        BLACK(new byte[]{0, 0, 0});

        private final byte[] bgr;
        Color(byte[] bgr){
            this.bgr = bgr;
        }

        public byte[] getBgr(){
            return bgr;
        }
    }

    public Canvas(int width, int height){
        // creates empty (white) canvas
        image = new Mat(height, width, CvType.CV_8UC3);
        image.setTo(new Scalar(255, 255, 255));
    }

    public Mat getImage(){
        // I'm not sure if there is any way to return object without
        // ability to change it (and you definitely shouldn't be able
        // to change original image), so I return a copy
        return image.clone();
    }

    public int getWidth(){
        return image.width();
    }

    public int getHeight(){
        return image.height();
    }

    public void drawPoint(int x, int y, Color color){
        drawPoint(x, y, color.getBgr());
    }

    public void drawPoint(int x, int y, byte[] bgr){
        image.put(y, x, bgr);
    }

}
