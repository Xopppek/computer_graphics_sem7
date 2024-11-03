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

        Point[] curve1 = {new Point(0, 0), new Point(80, 160),
                          new Point(160, 0), new Point(200, 160)};
        Point[] curve2 = {new Point(160, 200), new Point(0, 160),
                          new Point(40, 0), new Point(200, 160)};
        Point[] curve3 = {new Point(120, 120), new Point(0, 0),
                          new Point(0, 200), new Point(120, 80)};
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

    public void drawLine(Point p1, Point p2, Color color) {
        drawLine(p1, p2, color.getBgr());
    }

    public void drawLine(Point p1, Point p2, byte[] bgr) {
        drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY(), bgr);
    }

    public void drawBezierCurveCubic(Point p0, Point p1, Point p2, Point p3, Color color) {
        drawBezierCurveCubic(p0, p1, p2, p3, color.getBgr());
    }

    public void drawBezierCurveCubic(Point p0, Point p1, Point p2, Point p3, byte[] bgr) {
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

    private Point bezierLine(Point p0, Point p1, double t){
        return p0.multiply(1.0 - t).add(p1.multiply(t));
    }

    private Point bezierQuadratic(Point p0, Point p1, Point p2, double t){
        return bezierLine(bezierLine(p0, p1, t), bezierLine(p1, p2, t), t);
    }

    private Point bezierCubic(Point p0, Point p1, Point p2, Point p3, double t){
        return bezierLine(bezierQuadratic(p0, p1, p2, t), bezierQuadratic(p1, p2, p3, t), t);
    }

    private double bezierDist(Point p){
        return Math.abs(p.getX()) + Math.abs(p.getY());
    }
}

class Point{
    // Возможно стоило добавить этот класс еще в предыдущей лабораторной
    // в этой без него стало совсем уж неудобно
    // после экспериментов я пришел к выводу, что лучше всего хранить координаты в действительных числах
    // пробовал в целых, но набегала слишком большая ошибка при построении кривых
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point add(Point p){
        return new Point(x + p.x, y + p.y);
    }

    public Point multiply(double scalar){
        return new Point(scalar * x, scalar * y);
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }
}

