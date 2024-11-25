import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Lab4 extends Lab3{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/lab4/";

    public static void main(String[] args) {
        var canvasCurves = new CanvasLab4(200, 200);

        Point2D[] curve1 = {new Point2D(0, 0), new Point2D(80, 160),
                          new Point2D(160, 0), new Point2D(200, 160)};
        Point2D[] curve2 = {new Point2D(160, 200), new Point2D(0, 160),
                          new Point2D(40, 0), new Point2D(200, 160)};
        Point2D[] curve3 = {new Point2D(120, 120), new Point2D(0, 0),
                          new Point2D(0, 200), new Point2D(120, 80)};
        canvasCurves.drawBezierCurveCubic(curve1[0], curve1[1], curve1[2], curve1[3], Canvas.Color.BLACK);
        canvasCurves.drawBezierCurveCubic(curve2[0], curve2[1], curve2[2], curve2[3], Canvas.Color.BLUE);
        canvasCurves.drawBezierCurveCubic(curve3[0], curve3[1], curve3[2], curve3[3], Canvas.Color.GREEN);

        var canvasCurve1 = new CanvasLab4(200, 200);
        var canvasCurve2 = new CanvasLab4(200, 200);
        var canvasCurve3 = new CanvasLab4(200, 200);
        canvasCurve1.drawBezierCurveCubic(curve1[0], curve1[1], curve1[2], curve1[3], Canvas.Color.BLACK);
        canvasCurve2.drawBezierCurveCubic(curve2[0], curve2[1], curve2[2], curve2[3], Canvas.Color.BLUE);
        canvasCurve3.drawBezierCurveCubic(curve3[0], curve3[1], curve3[2], curve3[3], Canvas.Color.GREEN);
        for (int i = 1; i < 4; i++){
            canvasCurve1.drawLine(curve1[i-1], curve1[i], Canvas.Color.RED);
            canvasCurve2.drawLine(curve2[i-1], curve2[i], Canvas.Color.RED);
            canvasCurve3.drawLine(curve3[i-1], curve3[i], Canvas.Color.RED);
        }
        saveScaled(canvasCurve1.getImage(), 3, "curve1_with_lines");
        saveScaled(canvasCurve2.getImage(), 3, "curve2_with_lines");
        saveScaled(canvasCurve3.getImage(), 3, "curve3_with_lines");

        var canvasCutting = new CanvasLab4(200, 200);
        // var polygon = new Polygon(0, 10, 40, 140, 160, 40); // clockwise
        var polygon = new Polygon(0, 10, 160, 40, 40, 140); // not clockwise
        canvasCutting.drawPolygon(polygon, Canvas.Color.BLACK);
        Point2D[][] lines = {
                {new Point2D(0, 0),     new Point2D(80, 160) },
                {new Point2D(30, 170),  new Point2D(160, 190)},
                {new Point2D(100, 59),  new Point2D(200, 40) },
                {new Point2D(200, 200), new Point2D(150, 150)},
                {new Point2D(30, 30),   new Point2D(60, 60)  },
                {new Point2D(160, 40),  new Point2D(40, 140) },
                {new Point2D(0, 5),     new Point2D(160, 35) },
        };

        for (var line : lines){
            canvasCutting.drawLine(line[0], line[1], Canvas.Color.RED);
            canvasCutting.drawCyrusBeckClippedLine(line[0], line[1], polygon, Canvas.Color.GREEN);
        }

        Imgcodecs.imwrite(savePath + "curves.png", canvasCurves.getImage());
        saveScaled(canvasCurves.getImage(), 3, "curves");
        Imgcodecs.imwrite(savePath + "cutting.png", canvasCutting.getImage());
        saveScaled(canvasCutting.getImage(), 3, "cutting");

        displayImage(canvasCurves.getImage(), 3, "Bezier Curves");
        displayImage(canvasCutting.getImage(), 3, "Cutting Lines");

        HighGui.waitKey();
    }

    public static void saveScaled(Mat image, double scalingFactor, String title) {
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(image.cols() * scalingFactor,
                        image.height() * scalingFactor),
                0, 0, Imgproc.INTER_NEAREST);
        Imgcodecs.imwrite(savePath + title + "_scaled.png", resizedImage);
    }
}

class CanvasLab4 extends Canvas{
    // Для удобства наследуемся от класса холста из 3 лабораторной
    public CanvasLab4(int width, int height) {
        super(width, height);
    }

    public void drawPoint(Point2D p, Color color) {
        drawPoint(p, color.getBgr());
    }

    public void drawPoint(Point2D p, byte[] bgr) {
        drawPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()), bgr);
    }

    public void drawLine(Point2D p1, Point2D p2, Color color) {
        drawLine(p1, p2, color.getBgr());
    }

    public void drawLine(Point2D p1, Point2D p2, byte[] bgr) {
        drawLine((int) Math.round(p1.getX()), (int) Math.round(p1.getY()),
                 (int) Math.round(p2.getX()), (int) Math.round(p2.getY()), bgr);
    }

    public void drawBezierCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, Color color) {
        drawBezierCurveCubic(p0, p1, p2, p3, color.getBgr());
    }

    public void drawBezierCurveCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, byte[] bgr) {
        double H = Math.max(bezierDist(p0.add(p2.multiply(-2)).add(p3)),
                            bezierDist(p0.add(p2.multiply(-2)).add(p3)));
        double N = 1.0 + Math.sqrt(3 * H);

        var prevPoint = p0;
        for (double t = 0; t < 1; t += 1 / N){
            var curPoint = bezierCubic(p0, p1, p2, p3, t);
            drawLine(prevPoint, curPoint, bgr);
            prevPoint = curPoint;
        }
        drawLine(prevPoint, p3, bgr);
    }

    protected Point2D bezierLine(Point2D p0, Point2D p1, double t){
        return p0.multiply(1.0 - t).add(p1.multiply(t));
    }

    protected Point2D bezierQuadratic(Point2D p0, Point2D p1, Point2D p2, double t){
        return bezierLine(bezierLine(p0, p1, t), bezierLine(p1, p2, t), t);
    }

    protected Point2D bezierCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t){
        return bezierLine(bezierQuadratic(p0, p1, p2, t), bezierQuadratic(p1, p2, p3, t), t);
    }

    protected double bezierDist(Point2D p){
        return Math.abs(p.getX()) + Math.abs(p.getY());
    }

    public void drawCyrusBeckClippedLine(Point2D p1, Point2D p2, Polygon polygon, Color color){
        drawCyrusBeckClippedLine(p1, p2, polygon, color.getBgr());
    }

    public void drawCyrusBeckClippedLine(Point2D p1, Point2D p2, Polygon polygon, byte[] bgr){
        if (!isClockWiseOriented(polygon))
            polygon = switchOrientation(polygon);
        if (!polygon.isConvex())
            throw new IllegalArgumentException("Полигон должен быть выпуклым");

        int n = polygon.getVertexNum();
        double t1 = 0, t2 = 1, t;
        double sx = p2.getX() - p1.getX(), sy = p2.getY() - p1.getY();
        for (int i = 0; i < n; i++) {
            double nx = polygon.getVertexCoords((i+1)%n)[1] - polygon.getVertexCoords(i)[1];
            double ny = polygon.getVertexCoords(i)[0] - polygon.getVertexCoords((i+1)%n)[0];
            double denom = nx * sx + ny * sy;
            double num = nx * (p1.getX() - polygon.getVertexCoords(i)[0]) +
                         ny * (p1.getY() - polygon.getVertexCoords(i)[1]);
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
                if (Polygon.pointSegmentClassify(polygon.getVertexCoords(i)[0], polygon.getVertexCoords(i)[1],
                            polygon.getVertexCoords((i+1)%n)[0], polygon.getVertexCoords((i+1)%n)[1],
                            p1.getX(), p1.getY()) == Polygon.CLPointType.LEFT)
                    return;
            }
            if (t1 > t2)
                return;
        }
        if (t1 <= t2) {
            Point2D p1Cut = p1.add(p2.sub(p1).multiply(t1));
            Point2D p2Cut = p1.add(p2.sub(p1).multiply(t2));
            drawLine(p1Cut, p2Cut, bgr);
        }
    }

    private static Polygon switchOrientation(Polygon polygon){
        int n = polygon.getVertexNum();

        int[] xCoords = new int[n];
        int[] yCoords = new int[n];

        for (int i = 0; i < n; i++){
            xCoords[i] = polygon.getVertexCoords(n - 1 - i)[0];
            yCoords[i] = polygon.getVertexCoords(n - 1 - i)[1];
        }

        return new Polygon(xCoords, yCoords);
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

class Point2D {
    // Возможно стоило добавить этот класс еще в предыдущей лабораторной
    // в этой без него стало совсем уж неудобно
    // после экспериментов я пришел к выводу, что лучше всего хранить координаты в действительных числах
    // пробовал в целых, но набегала слишком большая ошибка при построении кривых
    private final double x;
    private final double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point2D add(Point2D p){
        return new Point2D(x + p.x, y + p.y);
    }

    public Point2D sub(Point2D p){
        return new Point2D(x - p.x, y - p.y);
    }

    public Point2D multiply(double scalar){
        return new Point2D(scalar * x, scalar * y);
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }
}

