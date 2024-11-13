import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.function.BiFunction;

public class Lab3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static int canvasId = 0;
    static String savePath = "src/results/lab3/";

    public static void main(String[] args) {
        var canvasLines = new Canvas(120, 120);
        canvasLines.drawLine(10, 80, 20, 15, Canvas.Color.BLACK);
        canvasLines.drawLine(20 + 2, 15, 10 + 2, 80, Canvas.Color.BLACK);
        canvasLines.drawLine(30, 80, 70, 90, Canvas.Color.BLUE);
        canvasLines.drawLine(70, 90 + 2, 30, 80 + 2, Canvas.Color.BLUE);
        canvasLines.drawLine(50, 50, 100, 100, Canvas.Color.RED);
        canvasLines.drawLine(100 + 2, 100, 50 + 2, 50, Canvas.Color.RED);
        canvasLines.drawLine(50, 20, 90, 40, Canvas.Color.GREEN);
        canvasLines.drawLine(90, 40 + 2, 50, 20 + 2, Canvas.Color.GREEN);

        displayImage(canvasLines.getImage(), 4, "Lines");
        Imgcodecs.imwrite(savePath + "lines.png", canvasLines.getImage());

        var canvasPolygon = new Canvas(120, 120);
        var starPolygon = new Polygon(10, 40, 110, 40, 30, 110, 60, 10, 90, 110);
        canvasPolygon.drawPolygon(starPolygon, Canvas.Color.BLUE);
        displayImage(canvasPolygon.getImage(), 4, "Star");
        Imgcodecs.imwrite(savePath + "star.png", canvasPolygon.getImage());
        System.out.println("Star self intersections " + starPolygon.hasSelfIntersection());
        System.out.println("Star is convex " + starPolygon.isConvex());

        var trianglePolygon = new Polygon(10, 10, 100, 80, 50, 30);
        System.out.println("Triangle self intersections " + trianglePolygon.hasSelfIntersection());
        System.out.println("Triangle is convex " + trianglePolygon.isConvex());

        var canvasFilledEO = new Canvas(120, 120);
        canvasFilledEO.drawPolygon(starPolygon, Canvas.Color.BLACK);
        canvasFilledEO.fillPolygonEOMode(starPolygon, Canvas.Color.GREEN);
        displayImage(canvasFilledEO.getImage(), 4, "Filled EO");
        Imgcodecs.imwrite(savePath + "filledEO.png", canvasFilledEO.getImage());

        var canvasFilledNZW = new Canvas(120, 120);
        canvasFilledNZW.drawPolygon(starPolygon, Canvas.Color.BLACK);
        canvasFilledNZW.fillPolygonNZWMode(starPolygon, Canvas.Color.GREEN);
        displayImage(canvasFilledNZW.getImage(), 4, "Filled NZW");
        Imgcodecs.imwrite(savePath + "filledNZW.png", canvasFilledNZW.getImage());

        HighGui.waitKey(0);
    }

    public static void displayImage(Mat image, int scalingFactor){
        displayImage(image, scalingFactor, null);
    }

    public static void displayImage(Mat image, int scalingFactor, String winname){
        // before drawing a window I resize image, so I can see something on my monitor
        // Only unchanged pictures will go to result files
        if (winname == null){
            winname = getWinname();
        }
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(image.cols() * scalingFactor,
                                                     image.height() * scalingFactor),
                                                     0, 0, Imgproc.INTER_NEAREST);
        HighGui.imshow(winname, resizedImage);
    }

    private static String getWinname(){
        return "Canvas" + ++canvasId;
    }
}

class Polygon{
    private final int[] xCoords;
    private final int[] yCoords;
    private final boolean hasSelfIntersections;
    private final boolean isConvex;

    public Polygon(int[] xCoords, int[] yCoords){
        // I use clone so it is not possible to change arrays after creating Polygon
        this.xCoords = xCoords.clone();
        this.yCoords = yCoords.clone();
        hasSelfIntersections = checkSelfIntersections();
        isConvex = checkIfConvex();
    }

    public Polygon(int... coords){
        // arguments should be pairs of integers like (x1, y1, x2, y2, ...)
        if (coords.length % 2 != 0)
            throw new IllegalArgumentException("Все координаты должны иметь пары");
        xCoords = new int[coords.length / 2];
        yCoords = new int[coords.length / 2];
        for (int i = 0; i < coords.length; i++) {
            if (i % 2 == 0)
                xCoords[i / 2] = coords[i];
            else
                yCoords[i / 2] = coords[i];
        }
        hasSelfIntersections = checkSelfIntersections();
        isConvex = checkIfConvex();
    }

    public int getVertexNum(){
        return xCoords.length;
    }

    public int[] getVertexCoords(int index){
        if (index >= getVertexNum())
            throw new IllegalArgumentException("Индекс превышает количество вершин");
        return new int[]{xCoords[index], yCoords[index]};
    }

    public int getMaxX(){
        if (getVertexNum() == 0)
            throw new IllegalStateException("В полигоне нет ни одной точки");
        return Arrays.stream(xCoords).max().getAsInt();
    }

    public int getMinX(){
        if (getVertexNum() == 0)
            throw new IllegalStateException("В полигоне нет ни одной точки");
        return Arrays.stream(xCoords).min().getAsInt();
    }

    public int getMaxY(){
        if (getVertexNum() == 0)
            throw new IllegalStateException("В полигоне нет ни одной точки");
        return Arrays.stream(yCoords).max().getAsInt();
    }

    public int getMinY(){
        if (getVertexNum() == 0)
            throw new IllegalStateException("В полигоне нет ни одной точки");
        return Arrays.stream(yCoords).min().getAsInt();
    }

    public boolean isConvex(){
        return isConvex;
    }

    public boolean hasSelfIntersection(){
        return hasSelfIntersections;
    }

    protected enum CLPointType{
        LEFT,
        RIGHT,
        BEYOND,
        BEHIND,
        BETWEEN,
        ORIGIN,
        DESTINATION,
    }

    protected static CLPointType pointSegmentClassify(double x1, double y1,
                                                    double x2, double y2,
                                                    double x, double y){
        double ax = x2 - x1;
        double ay = y2 - y1;
        double bx = x - x1;
        double by = y - y1;
        double s = ax * by - bx * ay;
        if (s > 0)
            return CLPointType.LEFT;
        if (s < 0)
            return CLPointType.RIGHT;
        if ((ax * bx < 0) || (ay * by < 0))
            return CLPointType.BEHIND;
        if ((ax * ax + ay * ay) < (bx * bx + by * by))
            return CLPointType.BEYOND;
        if (x1 == x && y1 == y)
            return CLPointType.ORIGIN;
        if (x2 == x && y2 == y)
            return CLPointType.DESTINATION;
        return CLPointType.BETWEEN;
    }

    private enum IntersectType{
        SAME,
        PARALLEL,
        SKEW,
        SKEW_CROSS,
        SKEW_NO_CROSS,
    }

    private static IntersectType intersectSegmentLine(double ax, double ay,
                                                      double bx, double by,
                                                      double cx, double cy,
                                                      double dx, double dy,
                                                      double[] t){
        if (t.length != 1)
            throw new IllegalArgumentException("t[] должен быть массивом из одного элемента");
        double nx = dy - cy;
        double ny = cx - dx;
        CLPointType clPointType;
        double denom = nx * (bx - ax) + ny * (by - ay);
        if (denom == 0){
            clPointType = pointSegmentClassify(cx, cy, dx, dy, ax, ay);
            if (clPointType == CLPointType.LEFT || clPointType == CLPointType.RIGHT)
                return IntersectType.PARALLEL;
            else
                return IntersectType.SAME;
        }
        double num = nx * (ax - cx) + ny * (ay - cy);
        t[0] = -num/denom;
        return IntersectType.SKEW;
    }

    private static IntersectType intersectSegmentSegment(double ax, double ay,
                                                         double bx, double by,
                                                         double cx, double cy,
                                                         double dx, double dy){
        double[] tab = new double[1], tcd = new double[1];
        IntersectType intersectType = intersectSegmentLine(ax, ay, bx, by, cx, cy, dx, dy, tab);
        if (intersectType == IntersectType.SAME || intersectType == IntersectType.PARALLEL)
            return intersectType;
        if ((tab[0] < 0) || (tab[0] > 1))
            return IntersectType.SKEW_NO_CROSS;
        intersectSegmentLine(cx, cy, dx, dy, ax, ay, bx, by, tcd);
        if ((tcd[0] < 0) || (tcd[0] > 1))
            return IntersectType.SKEW_NO_CROSS;
        return IntersectType.SKEW_CROSS;
    }

    private enum EType {
        TOUCHING,
        CROSS_LEFT,
        CROSS_RIGHT,
        INESSENTIAL,
    }

    private static EType edgeRayClassify(double ax, double ay, double bx, double by, double rayStartX, double rayStartY){
        switch (pointSegmentClassify(ax, ay, bx, by, rayStartX, rayStartY)) {
            case LEFT:
                if (rayStartY > ay && rayStartY <= by)
                    return EType.CROSS_LEFT;
                return EType.INESSENTIAL;
            case RIGHT:
                if (rayStartY > by && rayStartY <= ay)
                    return EType.CROSS_RIGHT;
                return EType.INESSENTIAL;
            case BETWEEN:
            case ORIGIN:
            case DESTINATION:
                return EType.TOUCHING;
            default:
                return EType.INESSENTIAL;
        }
    }

    public boolean isPointInPolygonEOMode(double x, double y){
        int n = getVertexNum();
        int param = 0;
        for (int i = 0; i < n; i++){
            int[] aCoords = getVertexCoords(i);
            int[] bCoords = getVertexCoords((i + 1) % n);
            switch (edgeRayClassify(aCoords[0], aCoords[1], bCoords[0], bCoords[1], x, y)){
                case TOUCHING:
                    return true;
                case CROSS_LEFT:
                case CROSS_RIGHT:
                    param = 1 - param;
            }
        }
        return param == 1;
    }

    public boolean isPointInPolygonNZWMode(double x, double y){
        int n = getVertexNum();
        int param = 0;
        for (int i = 0; i < n; i++){
            int[] aCoords = getVertexCoords(i);
            int[] bCoords = getVertexCoords((i + 1) % n);
            switch (edgeRayClassify(aCoords[0], aCoords[1], bCoords[0], bCoords[1], x, y)){
                case TOUCHING:
                    return true;
                case CROSS_LEFT:
                    param++;
                    break;
                case CROSS_RIGHT:
                    param--;
                    break;
            }
        }
        return param != 0;
    }

    private boolean checkSelfIntersections(){
        int n = getVertexNum();
        if (n < 4)
            return false;

        for (int i = 0; i < n; i++){
            int[] a = getVertexCoords(i);
            int[] b = getVertexCoords((i + 1) % n);

            for (int j = i + 2; j < n; j++){
                if (i == 0 && j == n - 1)
                    continue;
                int[] c = getVertexCoords(j);
                int[] d = getVertexCoords((j + 1) % n);
                IntersectType intersectType = intersectSegmentSegment(a[0], a[1],
                                                                      b[0], b[1],
                                                                      c[0], c[1],
                                                                      d[0], d[1]);
                if (intersectType == IntersectType.SKEW_CROSS)
                    return true;
            }
        }

        return false;
    }

    private boolean checkIfConvex(){
        int n = getVertexNum();
        if (n < 3)
            return true;

        if (checkSelfIntersections())
            return false;

        boolean hasPositiveRotation = false;
        boolean hasNegativeRotation = false;

        for (int i = 0; i < n; i++){
            // a, b, c -- 3 vertices in a row
            // going around the polygon every rotation should have the same orientation
            // either like clock or unlike. So going through all possible pairs of sides
            // we can check if polygon is convex
            int[] a = getVertexCoords(i);
            int[] b = getVertexCoords((i + 1) % n);
            int[] c = getVertexCoords((i + 2) % n);
            int abx = b[0] - a[0];
            int aby = b[1] - a[1];
            int bcx = c[0] - b[0];
            int bcy = c[1] - b[1];
            int product = abx * bcy - aby * bcx;
            if (product > 0)
                hasPositiveRotation = true;
            if (product < 0)
                hasNegativeRotation = true;
            if (hasNegativeRotation && hasPositiveRotation)
                return false;
        }
        return true;
    }
}

class Canvas{
    // now you can only create canvas with fixed sizes
    // and with BGR 8bpp for channel type
    // it can't be changed after creating
    private final Mat image;

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
        if (x >= 0 && y >= 0)
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
            drawPoint(poly.getVertexCoords(0)[0], poly.getVertexCoords(0)[1], bgr);
            return;
        }
        if (vertexNum == 2){
            int[] coords1 = poly.getVertexCoords(0);
            int[] coords2 = poly.getVertexCoords(1);
            drawLine(coords1[0], coords1[1], coords2[0], coords2[1], bgr);
            return;
        }

        int[] coordsPrev = poly.getVertexCoords(0);
        int[] coordsNext;
        for (int i = 1; i < vertexNum; i++){
            coordsNext = poly.getVertexCoords(i);
            drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
            coordsPrev = coordsNext;
        }
        coordsNext = poly.getVertexCoords(0);
        drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
    }

    public void fillPolygonEOMode(Polygon poly, Color color){
        fillPolygonEOMode(poly, color.getBgr());
    }

    public void fillPolygonEOMode(Polygon poly, byte[] bgr){
        fillPolygon(poly, bgr, poly::isPointInPolygonEOMode);
    }

    public void fillPolygonNZWMode(Polygon poly, Color color){
        fillPolygonNZWMode(poly, color.getBgr());
    }

    public void fillPolygonNZWMode(Polygon poly, byte[] bgr){
        fillPolygon(poly, bgr, poly::isPointInPolygonNZWMode);
    }

    private void fillPolygon(Polygon poly, byte[] bgr, BiFunction<Integer, Integer, Boolean> isInPolygonFunc){
        int minX = poly.getMinX();
        int minY = poly.getMinY();
        int maxX = poly.getMaxX();
        int maxY = poly.getMaxY();

        for (int x = minX; x <= maxX; x++){
            for (int y = minY; y <= maxY; y++){
                if (isInPolygonFunc.apply(x, y))
                    image.put(y, x, bgr);
            }
        }
    }
}
