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

        Imgcodecs.imwrite(savePath + "curves.png", canvasCurves.getImage());
        saveScaled(canvasCurves.getImage(), 3, "curves");
        displayImage(canvasCurves.getImage(), 3, "Bezier Curves");

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

    public void drawLine(Point2D p1, Point2D p2, Color color) {
        drawLine(p1, p2, color.getBgr());
    }

    public void drawLine(Point2D p1, Point2D p2, byte[] bgr) {
        drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY(), bgr);
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

    private Point2D bezierLine(Point2D p0, Point2D p1, double t){
        return p0.multiply(1.0 - t).add(p1.multiply(t));
    }

    private Point2D bezierQuadratic(Point2D p0, Point2D p1, Point2D p2, double t){
        return bezierLine(bezierLine(p0, p1, t), bezierLine(p1, p2, t), t);
    }

    private Point2D bezierCubic(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t){
        return bezierLine(bezierQuadratic(p0, p1, p2, t), bezierQuadratic(p1, p2, p3, t), t);
    }

    private double bezierDist(Point2D p){
        return Math.abs(p.getX()) + Math.abs(p.getY());
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

