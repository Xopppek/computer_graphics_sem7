import org.opencv.core.Core;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public class Lab4 extends Lab3{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/lab4/";

    public static void main(String[] args) {
        var canvas = new CanvasLab4(200, 200);

        Point[] points = {new Point(0, 0), new Point(80, 160),
                          new Point(160, 0), new Point(200, 160)};
        canvas.drawBezierCurveCubic(points[0], points[1], points[2], points[3], Canvas.Color.BLACK);

        Imgcodecs.imwrite(savePath + "curve.png", canvas.getImage());
        displayImage(canvas.getImage(), 3);

        HighGui.waitKey();
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
        for (double t = 0; t < 1 + 0.5 / N; t += 1 / N){
            var curPoint = bezierCubic(p0, p1, p2, p3, t);
            drawLine(prevPoint, curPoint, bgr);
            prevPoint = curPoint;
        }
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
        //return new Point((int) Math.round(scalar * x), (int) Math.round(scalar * y));
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }
}

