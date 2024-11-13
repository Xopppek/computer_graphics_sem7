import org.opencv.core.Core;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Lab5 extends Lab4 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "src/results/lab5/";

    public static void main(String[] args) {
//        Базовые тесты для точек и их преобразований

//        var p = new Point3D(20, 20, 20);
//        var canvasPoint = new CanvasLab5(40, 40);
//        canvasPoint.drawPoint(new Point2D(p.getX(), p.getY()), Canvas.Color.GREEN);
//        double[][] T = {
//                {1, 0, 0, 0},
//                {0, 1, 0, 0},
//                {0, 0, 1, 0},
//                {1, 1, 1, 0.5}
//        };
//        //p = p.apply(T);
//        System.out.println("{" + p.getX() + "," + p.getY() + "," + p.getZ() + "}");
//        p = p.rotate(Math.PI / 6, new Point3D(0, 0, 1));
//        System.out.println("{" + p.getX() + "," + p.getY() + "," + p.getZ() + "}");
//        canvasPoint.drawPoint(new Point2D(p.getX(), p.getY()), Canvas.Color.RED);
//        displayImage(canvasPoint.getImage(), 5);

        var canvasParallel = new CanvasLab5(700, 700);
        var canvasPerspective = new CanvasLab5(700, 700);

        var figure = new Polyhedron(new Face[]{
                new Face(new Edge3D(new Point3D(100, 100, 0),   new Point3D(500, 100, 0)),
                         new Edge3D(new Point3D(100, 100, 0),   new Point3D(100, 500, 0))),
                new Face(new Edge3D(new Point3D(100, 100, 0),   new Point3D(200, 200, 200)),
                         new Edge3D(new Point3D(100, 100, 0),   new Point3D(500, 100, 0))),
                new Face(new Edge3D(new Point3D(100, 100, 0),   new Point3D(100, 500, 0)),
                         new Edge3D(new Point3D(100, 100, 0),   new Point3D(200, 200, 200))),
                new Face(new Edge3D(new Point3D(100, 500, 0),   new Point3D(500, 500, 0)),
                         new Edge3D(new Point3D(100, 500, 0),   new Point3D(200, 600, 200))),
                new Face(new Edge3D(new Point3D(500, 500, 0),   new Point3D(500, 100, 0)),
                         new Edge3D(new Point3D(500, 500, 0),   new Point3D(600, 600, 200))),
                new Face(new Edge3D(new Point3D(200, 200, 200), new Point3D(200, 600, 200)),
                         new Edge3D(new Point3D(200, 200, 200), new Point3D(600, 200, 200))),
        });

         figure = figure.rotate(Math.PI / 14, new Point3D(0, 0, 1));
        // figure = figure.apply(T);

        var parallelProjectionLines = figure.getParallelProjectionXY();
        for (var line : parallelProjectionLines) {
            canvasParallel.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        Imgcodecs.imwrite(savePath + "parallel.png", canvasParallel.getImage());
        displayImage(canvasParallel.getImage(), 1, "Parallel Projection");

        var perspectiveProjectionLines = figure.getPerspectiveProjectionXYpointOnZ(0.0025);
        for (var line : perspectiveProjectionLines) {
            canvasPerspective.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        Imgcodecs.imwrite(savePath + "perspective.png", canvasPerspective.getImage());
        displayImage(canvasPerspective.getImage(), 1, "Perspective Projection");

        HighGui.waitKey(0);
    }
}

class CanvasLab5 extends CanvasLab4 {
    public CanvasLab5(int width, int height) {
        super(width, height);
    }

}

class Polyhedron {
    private final Face[] faces;

    // UPD: Я поменял структуру многогранника, теперь он хранит грани, все равно надо много что проверять
    // Вообще говоря, для того чтобы иметь точно нормальный многогранник, требуется просто нереальное
    // количество проверок, включающее в себя проверку связности, что нет висячих вершин, что все ребра
    // принадлежат ровно 2-м граням, замкнутость граней и так далее. Садиться все это писать страшно.
    // Приму пока на веру, что пользователь вводит адекватный многогранник. (Параллелепипед из задания)
    public Polyhedron(Face[] faces) {
        // Face иммутабелен, поэтому можно не заморачиваться с поэлементным копированием
        this.faces = faces.clone();
    }

    public Polyhedron rotate(double angle, Point3D axis) {
        Face[] rotatedFaces = new Face[faces.length];

        for (int i = 0; i < faces.length; i++) {
            var edge1 = faces[i].getEdge1().rotate(angle, axis);
            var edge2 = faces[i].getEdge2().rotate(angle, axis);
            rotatedFaces[i] = new Face(edge1, edge2);
        }

        return new Polyhedron(rotatedFaces);
    }

    public Polyhedron apply(double[][] T){
        Face[] transformedFaces = new Face[faces.length];

        for (int i = 0; i < faces.length; i++) {
            var edge1 = faces[i].getEdge1().apply(T);
            var edge2 = faces[i].getEdge2().apply(T);
            transformedFaces[i] = new Face(edge1, edge2);
        }

        return new Polyhedron(transformedFaces);
    }

    public Point2D[][] getParallelProjectionXY() {
        // В этом простом случае даже не нужно думать о какой-либо матрице трансформации
        // достаточно просто занулить z
        var lines = new Point2D[faces.length * 4][2];
        for (int i = 0; i < faces.length; i++) {
            Edge3D[] edges = faces[i].getEdges();

            for (int j = 0; j < 4; j++) {
                lines[4 * i + j][0] = new Point2D(edges[j].getP1().getX(), edges[j].getP1().getY());
                lines[4 * i + j][1] = new Point2D(edges[j].getP2().getX(), edges[j].getP2().getY());
            }
        }

        return lines;
    }

    public Point2D[][] getPerspectiveProjectionXYpointOnZ(double k) {
        var lines = new Point2D[faces.length * 4][2];

        for (int i = 0; i < faces.length; i++) {
            Edge3D[] edges = faces[i].getEdges();

            for (int j = 0; j < 4; j++) {
                var p1 = edges[j].getP1();
                var p2 = edges[j].getP2();
                lines[4 * i + j][0] = new Point2D(p1.getX(), p1.getY())
                                                 .multiply(1 / (k * p1.getZ() + 1));
                lines[4 * i + j][1] = new Point2D(p2.getX(), p2.getY())
                                                 .multiply(1 / (k * p2.getZ() + 1));
            }
        }
        return lines;
    }
}

class Point3D {
    private final double x;
    private final double y;
    private final double z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point2D p, double z){
        this(p.getX(), p.getY(), z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Point3D add(Point3D p) {
        return new Point3D(p.getX() + getX(), p.getY() + getY(), p.getZ() + getZ());
    }

    public Point3D sub(Point3D p) {
        return new Point3D(getX() - p.getX(), getY() - p.getY(), getZ() - p.getZ());
    }

    public Point3D multiply(double scalar) {
        return new Point3D(getX() * scalar, getY() * scalar, getZ() * scalar);
    }

    public Point3D rotate(double angle, Point3D axis) {
        // На вход ожидается угол в радианах, а также точка конца радиус-вектора
        // задающего ось, вокруг которой производится вращение
        if (axis == null)
            throw new IllegalArgumentException("Вместо оси вращения пришел null");
        if (axis.getX() == 0 && axis.getY() == 0 && axis.getZ() == 0)
            throw new IllegalArgumentException("Оси не может задаваться нулевым вектором.");
        double axisVectorLength = Math.sqrt(axis.getX() * axis.getX() +
                                            axis.getY() * axis.getY() +
                                            axis.getZ() * axis.getZ());
        double nx = axis.getX() / axisVectorLength;
        double ny = axis.getY() / axisVectorLength;
        double nz = axis.getZ() / axisVectorLength;

        // Выписываем матрицу преобразования
        double[][] T = {
                {       cos(angle) + nx * nx * (1 - cos(angle)),
                        nx * ny * (1 - cos(angle)) + nz * sin(angle),
                        nx * nz * (1 - cos(angle)) - ny * sin(angle),
                        0
                },
                {
                        nx * ny * (1 - cos(angle)) - nz * sin(angle),
                        cos(angle) + ny * ny * (1 - cos(angle)),
                        ny * nz * (1 - cos(angle)) + nx * sin(angle),
                        0
                },
                {
                        nx * nz * (1 - cos(angle)) + ny * sin(angle),
                        ny * nz * (1 - cos(angle)) - nx * sin(angle),
                        cos(angle) + nz * nz * (1 - cos(angle)),
                        0
                },
                {0, 0, 0, 1}
        };

        return apply(T);
    }

    public Point3D apply(double[][] T){
        // Эта функция применяет преобразование координат
        // [X, Y, Z, H] = [x, y, z, 1]T
        // после нормализуем координаты [x`, y`, z`] = [X/H, Y/H, Z/H]
        if (T == null)
            throw new IllegalArgumentException("На вход пришел null");
        if (T.length != 4 || T[0].length != 4)
            throw new IllegalArgumentException("Матрица преобразования должна быть 4x4");

        double[] homCoords = {getX(), getY(), getZ(), 1};
        double X = 0, Y = 0, Z = 0, H = 0;
        for (int i = 0; i < T[0].length; i++) {
            X += T[i][0] * homCoords[i];
            Y += T[i][1] * homCoords[i];
            Z += T[i][2] * homCoords[i];
            H += T[i][3] * homCoords[i];
        }

        X /= H;
        Y /= H;
        Z /= H;

        return new Point3D(X, Y, Z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point3D other = (Point3D) obj;
        return Double.compare(other.getX(), getX()) == 0 &&
               Double.compare(other.getY(), getY()) == 0 &&
               Double.compare(other.getZ(), getZ()) == 0;
    }
}

class Edge3D {
    private final Point3D p1;
    private final Point3D p2;

    public Edge3D(Point3D p1, Point3D p2) {
        this.p1 = new Point3D(p1.getX(), p1.getY(), p1.getZ());
        this.p2 = new Point3D(p2.getX(), p2.getY(), p2.getZ());
    }

    public Edge3D rotate(double angle, Point3D axis) {
        var p1 = getP1().rotate(angle, axis);
        var p2 = getP2().rotate(angle, axis);
        return new Edge3D(p1, p2);
    }

    public Edge3D apply(double[][] T) {
        var p1 = getP1().apply(T);
        var p2 = getP2().apply(T);
        return new Edge3D(p1, p2);
    }

    public Point3D getP1() {
        return p1;
    }

    public Point3D getP2() {
        return p2;
    }
}

class Face {
    private final Edge3D edge1;
    private final Edge3D edge2;

    // Конструктор ожидает 2 грани, с совпадающим началом.
    // Грани упорядочены так, чтобы по ним определялся вектор нормали
    public Face(Edge3D edge1, Edge3D edge2) {
        if (edge1 == null || edge2 == null)
            throw new IllegalArgumentException("Ребра не могуть быть null");
        if (!edge1.getP1().equals(edge2.getP1()))
            throw new IllegalArgumentException("Начальные точки ребер должны совпадать");
        this.edge1 = edge1;
        this.edge2 = edge2;
    }

    public Edge3D getEdge1() {
        return edge1;
    }

    public Edge3D getEdge2() {
        return edge2;
    }

    public Edge3D[] getEdges() {
        Edge3D[] edges = new Edge3D[4];
        edges[0] = edge1;
        edges[1] = edge2;

        edges[2] = new Edge3D(edge1.getP1().add(edge2.getP2().sub(edge2.getP1())),
                              edge1.getP2().add(edge2.getP2().sub(edge2.getP1())));
        edges[3] = new Edge3D(edge2.getP1().add(edge1.getP2().sub(edge1.getP1())),
                              edge2.getP2().add(edge1.getP2().sub(edge1.getP1())));
        return edges;
    }

    public Point3D getNormal() {
        // Вернет точку, определяющую радиус-вектор, совпадающий с вектором нормали
        var r1 = edge1.getP2().sub(edge1.getP1());
        var r2 = edge2.getP2().sub(edge2.getP1());
        var n = new Point3D(r1.getY() * r2.getZ() - r1.getZ() * r2.getY(),
                            r1.getZ() * r2.getX() - r1.getX() * r2.getZ(),
                            r1.getZ() * r2.getY() - r1.getY() * r2.getX());
        double length = Math.sqrt(n.getX() * n.getX() + n.getY() * n.getY() + n.getZ() * n.getZ());
        return n.multiply(1 / length);
    }
}
