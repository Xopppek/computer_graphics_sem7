import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HW extends Lab5{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/hw/";
    static String loadPath = "src/resources/hw/";

    public static void main(String[] args) {

        var polygon1 = new PolygonHW(0, 10, 40, 140, 160, 40);
        var polygon2 = new PolygonHW(120, 120, 120, 10, 50, 10, 50, 120);

        var canvasIntersect = new CanvasHW(160, 150);
        canvasIntersect.drawPolygon(polygon1, Canvas.Color.RED);
        canvasIntersect.drawPolygon(polygon2, Canvas.Color.BLUE);
        canvasIntersect.drawPolygon(polygon1.intersect(polygon2), Canvas.Color.GREEN);

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
        canvasBSpline.drawBSplineCurve(points, Canvas.Color.BLACK);

        for (var point : points) {
            canvasBSpline.drawPoint(point, Canvas.Color.RED);
        }

        var canvasYHist = getHistY(gojoImg, 1000, 600);
        var canvasCbHist = getHistCb(gojoImg, 1000, 600);
        var canvasCrHist = getHistCr(gojoImg, 1000, 600);

        Imgcodecs.imwrite(savePath + "intersect.png", canvasIntersect.getImage());
        Imgcodecs.imwrite(savePath + "bspline.png", canvasBSpline.getImage());
        Imgcodecs.imwrite(savePath + "gojo_y_hist.png", canvasYHist.getImage());
        Imgcodecs.imwrite(savePath + "gojo_cb_hist.png", canvasCbHist.getImage());
        Imgcodecs.imwrite(savePath + "gojo_cr_hist.png", canvasCrHist.getImage());

        displayImage(canvasIntersect.getImage(), 4, "Intersection");
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
                counts[(int) ((bgr[0] & 0xFF) * 0.114 + (bgr[1] & 0xFF) * 0.587 + (bgr[2] & 0xFF) * 0.299)]++;
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
                double Y = ((bgr[0] & 0xFF) * 0.114 + (bgr[1] & 0xFF) * 0.587 + (bgr[2] & 0xFF) * 0.299);
                counts[(int) (0.5 * ((bgr[0] & 0xFF) - Y) / (1 - 0.114) + 128)]++;
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
                double Y = ((bgr[0] & 0xFF) * 0.114 + (bgr[1] & 0xFF) * 0.587 + (bgr[2] & 0xFF) * 0.299);
                counts[(int) (0.5 * ((bgr[2] & 0xFF) - Y) / (1 - 0.299) + 128)]++;
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

    protected Point2D bSplineCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t) {
        return p0.multiply(Math.pow(1 - t, 3) / 6)
                 .add(p1.multiply((3 * Math.pow(t, 3) - 6 * Math.pow(t, 2) + 4) / 6))
                 .add(p2.multiply((-3 * Math.pow(t, 3) + 3 * Math.pow(t, 2) + 3 * t + 1) / 6))
                 .add(p3.multiply(Math.pow(t, 3) / 6));
    }

    public void drawBSplineCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, Color color) {
        drawBSplineCurveCubic(p0, p1, p2, p3, color.getBgr());
    }

    public void drawBSplineCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, byte[] bgr) {
        double H = Math.max(bezierDist(p0.add(p2.multiply(-2)).add(p3)),
                bezierDist(p0.add(p2.multiply(-2)).add(p3)));
        double N = 1.0 + Math.sqrt(3 * H);

        var prevPoint = bSplineCurveCubic(p0, p1, p2, p3, 0);
        for (double t = 0; t < 1; t += 1 / N){
            var curPoint = bSplineCurveCubic(p0, p1, p2, p3, t);
            drawLine(prevPoint, curPoint, bgr);
            prevPoint = curPoint;
        }
        drawLine(prevPoint, bSplineCurveCubic(p0, p1, p2, p3, 1), bgr);
    }

    public void drawBSplineCurve(Point2D[] points, Color color) {
        drawBSplineCurve(points, color.getBgr());
    }

    public void drawBSplineCurve(Point2D[] points, byte[] bgr) {
        if (points.length <= 3)
            throw new IllegalArgumentException("Количество точек должно быть больше 3");

        int m = points.length;

        drawBSplineCurveCubic(points[0], points[0], points[0], points[1], bgr);
        drawBSplineCurveCubic(points[0], points[0], points[1], points[2], bgr);
        for (int i = 3; i < m; i++) {
            drawBSplineCurveCubic(points[i - 3], points[i - 2], points[i - 1], points[i], bgr);
        }
        drawBSplineCurveCubic(points[m - 3], points[m - 2], points[m - 1], points[m - 1], bgr);
        drawBSplineCurveCubic(points[m - 2], points[m - 1], points[m - 1], points[m - 1], bgr);
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

class PolygonHW extends Polygon {
    public PolygonHW(int[] xCoords, int[] yCoords) {
        super(xCoords, yCoords);
    }

    public PolygonHW(int... coords){
        super(coords);
    }

    public PolygonHW getPolygonHW(List<Point2D> points) {
        int[] xCoords = new int[points.size()];
        int[] yCoords = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xCoords[i] = (int) Math.round(points.get(i).getX());
            yCoords[i] = (int) Math.round(points.get(i).getY());
        }
        return new PolygonHW(xCoords, yCoords);
    }

    public List<Point2D> getVertices() {
        List<Point2D> vertices = new ArrayList<>();
        for (int i = 0; i < this.getVertexNum(); i++) {
            vertices.add(new Point2D(getVertexCoords(i)[0], getVertexCoords(i)[1]));
        }
        return vertices;
    }

    public PolygonHW intersect(PolygonHW other) {
        if (other == null)
            throw new IllegalArgumentException("Второй полигон null");
        if (!isClockWiseOriented(other))
            other = switchOrientation(other);
        var thisPoly = this;
        if (!isClockWiseOriented(thisPoly))
            thisPoly = switchOrientation(thisPoly);

        List<Point2D> result = new ArrayList<>(this.getVertices());

        for (int i = 0; i < other.getVertexNum(); i++) {
            var curLine = new Line2D(other.getVertexCoords(i), other.getVertexCoords((i+1)%other.getVertexNum()));
            var curPoly = getPolygonHW(result);

            result.clear();

            for (int j = 0; j < curPoly.getVertexNum(); j++) {
                var p1 = new Point2D(curPoly.getVertexCoords(j)[0], curPoly.getVertexCoords(j)[1]);
                var p2 = new Point2D(curPoly.getVertexCoords((j + 1) % curPoly.getVertexNum())[0],
                                     curPoly.getVertexCoords((j + 1) % curPoly.getVertexNum())[1]);
                Point2D[] clipped = clipLineByLine(p1, p2, curLine);
                if (clipped != null)
                    result.addAll(Arrays.asList(clipped));
            }
        }

        return getPolygonHW(result);
    }

    public Point2D[] clipLineByLine(Point2D p1, Point2D p2, Line2D line) {
        double t1 = 0, t2 = 1, t;
        double sx = p2.getX() - p1.getX(), sy = p2.getY() - p1.getY();

        double nx = line.getY2() - line.getY1();
        double ny = line.getX1() - line.getX2();
        double denom = nx * sx + ny * sy;
        double num = nx * (p1.getX() - line.getX1()) +
                     ny * (p1.getY() - line.getY1());
        if (denom != 0) {
            t = -num / denom;
            if (denom > 0) {
                if (t > t1)
                    t1 = t;
            }
            else {
                if (t < t2)
                    t2 = t;
            }
        } else {
            if (Polygon.pointSegmentClassify(line.getX1(), line.getY1(),
                                             line.getX2(), line.getY2(),
                                             p1.getX(), p1.getY()) == Polygon.CLPointType.LEFT)
                return null;
        }

        if (t1 <= t2) {
            Point2D p1Cut = p1.add(p2.sub(p1).multiply(t1));
            Point2D p2Cut = p1.add(p2.sub(p1).multiply(t2));
            if (Double.compare(p1.getX(), p1Cut.getX()) == 0 && Double.compare(p1.getY(), p1Cut.getY()) == 0) {
                return new Point2D[]{p2Cut};
            } else if (Double.compare(p2.getX(), p2Cut.getX()) == 0 && Double.compare(p2.getY(), p2Cut.getY()) == 0) {
                return new Point2D[]{p1Cut, p2Cut};
            }
        }
        return null;
    }

    private static PolygonHW switchOrientation(Polygon polygon){
        int n = polygon.getVertexNum();

        int[] xCoords = new int[n];
        int[] yCoords = new int[n];

        for (int i = 0; i < n; i++){
            xCoords[i] = polygon.getVertexCoords(n - 1 - i)[0];
            yCoords[i] = polygon.getVertexCoords(n - 1 - i)[1];
        }

        return new PolygonHW(xCoords, yCoords);
    }

    private static boolean isClockWiseOriented(Polygon polygon){
        int n = polygon.getVertexNum();

        boolean hasPositiveRotation = false;

        for (int i = 0; i < n; i++){
            int[] a = polygon.getVertexCoords(i);
            int[] b = polygon.getVertexCoords((i + 1) % n);
            int[] c = polygon.getVertexCoords((i + 2) % n);
            int abx = b[0] - a[0];
            int aby = b[1] - a[1];
            int bcx = c[0] - b[0];
            int bcy = c[1] - b[1];
            int product = abx * bcy - aby * bcx;
            if (product > 0)
                hasPositiveRotation = true;
            if (hasPositiveRotation)
                return false;
        }
        return true;
    }
}

class Line2D {
    private final Point2D p1;
    private final Point2D p2;

    public Line2D(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Line2D(int[] p1Coords, int[] p2Coords) {
        p1 = new Point2D(p1Coords[0], p1Coords[1]);
        p2 = new Point2D(p2Coords[0], p2Coords[1]);
    }

    public Point2D getP1() {
        return p1;
    }

    public Point2D getP2() {
        return p2;
    }

    public double getX1() {
        return p1.getX();
    }

    public double getY1() {
        return p1.getY();
    }

    public double getX2() {
        return p2.getX();
    }

    public double getY2() {
        return p2.getY();
    }
}
