import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class Lab3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        var canvas = new Canvas(120, 120);
        //canvas.drawPoint(14, 50, Canvas.Color.BLACK);
        canvas.drawLine(10, 10, 80, 10, Canvas.Color.GREEN);
        canvas.drawLine(80, 10, 10, 90, Canvas.Color.BLUE);
        canvas.drawLine(10, 90, 80, 10, Canvas.Color.BLACK);
        canvas.drawLine(50, 20, 90, 4, Canvas.Color.RED);
        var poly = new Polygon(12, 13, 50, 70, 110, 40, 30, 110);
        System.out.println(poly.getVertexNum());
        //System.out.println(Arrays.toString(poly.getPointCoords(1)));
        canvas.drawPolygon(poly, Canvas.Color.BLUE);


        // before drawing a window I resize image, so I can see something on my monitor
        // Only unchanged pictures will go to result files
        Mat picture = canvas.getImage();
        Mat resizedPicture = new Mat();
        Imgproc.resize(picture, resizedPicture, new Size(picture.cols() * 4,
                       picture.height() * 4), 0, 0, Imgproc.INTER_NEAREST);
        HighGui.imshow("Canvas", resizedPicture);
        HighGui.waitKey(0);
    }
}
class Polygon{
    private final int[] xCoords;
    private final int[] yCoords;

    public Polygon(int... coords){
        // arguments should be pairs of integers like (x1, y1, x2, y2, ...)
        if (coords.length % 2 != 0)
            throw new IllegalArgumentException("Все координаты должны иметь пары");
        xCoords = new int[coords.length / 2];
        yCoords = new int[coords.length / 2];
        for (int i = 0; i < coords.length; i++){
            if (i % 2 == 0)
                xCoords[i / 2] = coords[i];
            else
                yCoords[i / 2] = coords[i];
        }
    }

    public Polygon(int[] xCoords, int[] yCoords){
        // I use clone so it is not possible to change arrays after creating Polygon
        this.xCoords = xCoords.clone();
        this.yCoords = yCoords.clone();
    }

    public int getVertexNum(){
        return xCoords.length;
    }

    public int[] getPointCoords(int index){
        if (index >= getVertexNum())
            throw new IllegalArgumentException("Индекс превышает количество вершин");
        return new int[]{xCoords[index], yCoords[index]};
    }
}

class Canvas{
    // now you can only create canvas with fixed sizes
    // and with BGR 8bpp for channel type
    // it can't be changed after creating
    private Mat image;

    public enum Color{
        RED  (new byte[]{         0,          0, (byte) 255}),
        BLUE (new byte[]{(byte) 255,          0,          0}),
        GREEN(new byte[]{         0, (byte) 255,          0}),
        BLACK(new byte[]{         0,          0,          0});

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

    public void drawLine(int x1, int y1, int x2, int y2, Color color){
        drawLine(x1, y1, x2, y2, color.getBgr());
    }

    public void drawLine(int x1, int y1, int x2, int y2, byte[] bgr){
        int x = x1, y = y1;
        int dx = x2 - x1, dy = y2 - y1;
        int e, i;
        int ix = Integer.compare(dx, 0);
        int iy = Integer.compare(dy, 0);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx >= dy) {
            e = 2 * dy - dx;
            if (iy >= 0) {
                for (i = 0; i <= dx; i++) {
                    drawPoint(x, y, bgr);
                    if (e >= 0) {
                        y += iy;
                        e -= 2 * dx;
                    }
                    x += ix;
                    e += dy * 2;
                }
            } else {
                for (i = 0; i <= dx; i++) {
                    drawPoint(x, y, bgr);
                    if (e > 0) {
                        y += iy;
                        e -= 2 * dx;
                    }
                    x += ix;
                    e += dy * 2;
                }
            }
        } else {
            e = 2 * dx - dy;
            if (ix >= 0) {
                for (i = 0; i <= dy; i++) {
                    drawPoint(x, y, bgr);
                    if (e >= 0) {
                        x += ix;
                        e -= 2 * dy;
                    }
                    y += iy;
                    e += dx * 2;
                }
            } else {
                for (i = 0; i <= dy; i++) {
                    drawPoint(x, y, bgr);
                    if (e > 0) {
                        x += ix;
                        e -= 2 * dy;
                    }
                    y += iy;
                    e += dx * 2;
                }
            }
        }
    }

    public void drawPolygon(Polygon poly, Color color){
        drawPolygon(poly, color.getBgr());
    }

    public void drawPolygon(Polygon poly, byte[] bgr){
        int vertexNum = poly.getVertexNum();

        if (vertexNum == 0)
            return;
        if (vertexNum == 1){
            drawPoint(poly.getPointCoords(0)[0], poly.getPointCoords(0)[1], bgr);
            return;
        }
        if (vertexNum == 2){
            int[] coords1 = poly.getPointCoords(0);
            int[] coords2 = poly.getPointCoords(1);
            drawLine(coords1[0], coords1[1], coords2[0], coords2[1], bgr);
            return;
        }

        int[] coordsPrev = poly.getPointCoords(0);
        int[] coordsNext;
        for (int i = 1; i < vertexNum; i++){
            coordsNext = poly.getPointCoords(i);
            drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
            coordsPrev = coordsNext;
        }
        coordsNext = poly.getPointCoords(0);
        drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
    }
}