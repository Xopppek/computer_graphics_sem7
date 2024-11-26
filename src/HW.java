import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.Arrays;

public class HW extends Lab5{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/hw/";
    static String loadPath = "src/resources/hw/";

    public static void main(String[] args) {
        Mat gojoImg = Imgcodecs.imread(loadPath + "gojo.png");

        Point2D[] points = {
                new Point2D(10, 10),
                new Point2D(20, 5),
                new Point2D(40, 60),
                new Point2D(80, 40),
                new Point2D(120, 70),
                new Point2D(150, 31),
        };

        var canvasBSpline = new CanvasHW(160, 100);
        canvasBSpline.drawBSplineBezierCurve(points, Canvas.Color.BLACK);

        for (var point : points) {
            canvasBSpline.drawPoint(point, Canvas.Color.RED);
        }

        var canvasYHist = getHistY(gojoImg, 1000, 600);
        var canvasCbHist = getHistCb(gojoImg, 1000, 600);
        var canvasCrHist = getHistCr(gojoImg, 1000, 600);

        Imgcodecs.imwrite(savePath + "bspline.png", canvasBSpline.getImage());
        Imgcodecs.imwrite(savePath + "gojo_y_hist.png", canvasYHist.getImage());
        Imgcodecs.imwrite(savePath + "gojo_cb_hist.png", canvasCbHist.getImage());
        Imgcodecs.imwrite(savePath + "gojo_cr_hist.png", canvasCrHist.getImage());


        displayImage(canvasBSpline.getImage(), 4, "B-Spline");
        displayImage(canvasYHist.getImage(), 1, "Y-Hist");
        displayImage(canvasCbHist.getImage(), 1, "Cb-Hist");
        displayImage(canvasCrHist.getImage(), 1, "Cr-Hist");

        HighGui.waitKey(0);
    }

    public static CanvasHW getHistY(Mat image, int width, int height) {
        var canvas = new CanvasHW(width, height);
        var counts = new int[256];
        var bgr = new byte[3];
        for (int x = 0; x < image.width(); x++) {
            for (int y = 0; y < image.height(); y++) {
                image.get(y, x, bgr);
                counts[(int) ((bgr[0] + 128) * 0.114 + (bgr[1] + 128) * 0.587 + (bgr[2] + 128) * 0.299)]++;
            }
        }
        canvas.drawHist(counts);
        return canvas;
    }

    public static CanvasHW getHistCb(Mat image, int width, int height) {
        var canvas = new CanvasHW(width, height);
        var counts = new int[256];
        var bgr = new byte[3];
        for (int x = 0; x < image.width(); x++) {
            for (int y = 0; y < image.height(); y++) {
                image.get(y, x, bgr);
                double Y = ((bgr[0] + 128) * 0.114 + (bgr[1] + 128) * 0.587 + (bgr[2] + 128) * 0.299);
                counts[(int) (0.5 * (bgr[0] + 128 - Y) / (1 - 0.114) + 128)]++;
            }
        }
        canvas.drawHist(counts);
        return canvas;
    }

    public static CanvasHW getHistCr(Mat image, int width, int height) {
        var canvas = new CanvasHW(width, height);
        var counts = new int[256];
        var bgr = new byte[3];
        for (int x = 0; x < image.width(); x++) {
            for (int y = 0; y < image.height(); y++) {
                image.get(y, x, bgr);
                double Y = ((bgr[0] + 128) * 0.114 + (bgr[1] + 128) * 0.587 + (bgr[2] + 128) * 0.299);
                counts[(int) (0.5 * (bgr[2] + 128 - Y) / (1 - 0.299) + 128)]++;
            }
        }
        canvas.drawHist(counts);
        return canvas;
    }
}


class CanvasHW extends CanvasLab5 {
    public CanvasHW(int width, int height) {
        super(width, height);
    }

    protected Point2D bSplineBezierCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t) {
        return p0.multiply(Math.pow(1 - t, 3) / 6)
                 .add(p1.multiply((3 * Math.pow(t, 3) - 6 * Math.pow(t, 2) + 4) / 6))
                 .add(p2.multiply((-3 * Math.pow(t, 3) + 3 * Math.pow(t, 2) + 3 * t + 1) / 6))
                 .add(p3.multiply(Math.pow(t, 3) / 6));
    }

    public void drawBSplineBezierCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, Color color) {
        drawBSplineBezierCurveCubic(p0, p1, p2, p3, color.getBgr());
    }

    public void drawBSplineBezierCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, byte[] bgr) {
        double H = Math.max(bezierDist(p0.add(p2.multiply(-2)).add(p3)),
                bezierDist(p0.add(p2.multiply(-2)).add(p3)));
        double N = 1.0 + Math.sqrt(3 * H);

        var prevPoint = bSplineBezierCurveCubic(p0, p1, p2, p3, 0);
        for (double t = 0; t < 1; t += 1 / N){
            var curPoint = bSplineBezierCurveCubic(p0, p1, p2, p3, t);
            drawLine(prevPoint, curPoint, bgr);
            prevPoint = curPoint;
        }
        drawLine(prevPoint, bSplineBezierCurveCubic(p0, p1, p2, p3, 1), bgr);
    }

    public void drawBSplineBezierCurve(Point2D[] points, Color color) {
        drawBSplineBezierCurve(points, color.getBgr());
    }

    public void drawBSplineBezierCurve(Point2D[] points, byte[] bgr) {
        if (points.length <= 3)
            throw new IllegalArgumentException("Количество точек должно быть больше 3");

        int m = points.length;

        drawBSplineBezierCurveCubic(points[0], points[0], points[0], points[1], bgr);
        drawBSplineBezierCurveCubic(points[0], points[0], points[1], points[2], bgr);
        for (int i = 3; i < m; i++) {
            drawBSplineBezierCurveCubic(points[i - 3], points[i - 2], points[i - 1], points[i], bgr);
        }
        drawBSplineBezierCurveCubic(points[m - 3], points[m - 2], points[m - 1], points[m - 1], bgr);
        drawBSplineBezierCurveCubic(points[m - 2], points[m - 1], points[m - 1], points[m - 1], bgr);
    }

    public void drawHist(int[] values) {
        if (values == null || values.length <= 1)
            throw new IllegalArgumentException();

        double dx = getWidth() * 1.0 / values.length;
        double dy = getHeight() * 1.0 / Arrays.stream(values).max().orElse(1);

        for (int i = 0; i < values.length; i++) {
            var column = new Polygon((int) Math.round((i + 1) * dx), getHeight() - 1,
                                     (int) Math.round(i * dx), getHeight() - 1,
                                     (int) Math.round(i * dx), (int) Math.round(getHeight() - values[i] * dy),
                                     (int) Math.round((i + 1) * dx), (int) Math.round(getHeight() - values[i] * dy));
            fillPolygonEOMode(column, new byte[] {(byte) (i * 255.0 / values.length),
                                                  (byte) (255 - i * 255.0 / values.length),
                                                  0});
            drawPolygon(column, Color.BLACK);
        }
    }
}
