import org.opencv.core.Core;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public class HW extends Lab5{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/hw/";

    public static void main(String[] args) {
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

        Imgcodecs.imwrite(savePath + "bspline.png", canvasBSpline.getImage());

        displayImage(canvasBSpline.getImage(), 4, "B-Spline");

        HighGui.waitKey(0);
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

}
