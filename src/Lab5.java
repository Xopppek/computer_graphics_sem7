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

        var p = new Point3D(1, 2, 3);
        double[][] T = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {1, 1, 1, 0.5}
        };
        p = p.apply(T);
        System.out.println("{" + p.getX() + "," + p.getY() + "," + p.getZ() + "}");

        p = p.rotate(Math.PI, new Point3D(0, 1, 0));

        System.out.println("{" + p.getX() + "," + p.getY() + "," + p.getZ() + "}");

        var canvasParallel = new CanvasLab5(160, 160);
        var canvasPerspective = new CanvasLab5(160, 160);
        var figure = new Polyhedron(new Edge3D[]{
                new Edge3D(new Point3D( 25, 25, 0), new Point3D( 25,125, 0)),
                new Edge3D(new Point3D( 25,125, 0), new Point3D(125,125, 0)),
                new Edge3D(new Point3D(125,125, 0), new Point3D(125, 25, 0)),
                new Edge3D(new Point3D(125, 25, 0), new Point3D( 25, 25, 0)),

                new Edge3D(new Point3D(25,  25, 0), new Point3D( 50, 50, 200)),
                new Edge3D(new Point3D(25, 125, 0), new Point3D( 50,150, 200)),
                new Edge3D(new Point3D(125,125, 0), new Point3D(150,150, 200)),
                new Edge3D(new Point3D(125, 25, 0), new Point3D(150, 50, 200)),

                new Edge3D(new Point3D( 50, 50, 200), new Point3D( 50,150, 200)),
                new Edge3D(new Point3D( 50,150, 200), new Point3D(150,150, 200)),
                new Edge3D(new Point3D(150,150, 200), new Point3D(150, 50, 200)),
                new Edge3D(new Point3D(150, 50, 200), new Point3D( 50, 50, 200)),
        });

        var parallelProjectionLines = figure.getParallelProjectionXY();
        for (var line : parallelProjectionLines) {
            canvasParallel.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        Imgcodecs.imwrite(savePath + "parallel.png", canvasParallel.getImage());
        displayImage(canvasParallel.getImage(), 3, "Parallel Projection");

        var perspectiveProjectionLines = figure.getPerspectiveProjectionXYpointOnZ(0.0025);
        for (var line : perspectiveProjectionLines) {
            canvasPerspective.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        Imgcodecs.imwrite(savePath + "perspective.png", canvasPerspective.getImage());
        displayImage(canvasPerspective.getImage(), 3, "Perspective Projection");

        HighGui.waitKey(0);
    }
}

class CanvasLab5 extends CanvasLab4 {
    public CanvasLab5(int width, int height) {
        super(width, height);
    }

}

class Polyhedron {
    private final Edge3D[] edges;

    // Вообще говоря, для того чтобы иметь точно нормальный многогранник, требуется просто нереальное
    // количество проверок, включающее в себя проверку связности, что нет висячих вершин, что все ребра
    // принадлежат ровно 2-м граням, замкнутость граней и так далее. Садиться все это писать страшно.
    // Приму пока на веру, что пользователь вводит адекватный многогранник. (Параллелепипед из задания)
    public Polyhedron(Edge3D[] edges) {
        // Edge3D иммутабелен, поэтому можно не заморачиваться с поэлементным копированием
        this.edges = edges.clone();
    }

    public Point2D[][] getParallelProjectionXY() {
        // В этом простом случае даже не нужно думать о какой-либо матрице трансформации
        // достаточно просто занулить z
        var points = new Point2D[edges.length][2];
        for (int i = 0; i < edges.length; i++) {
            var p1 = edges[i].getP1();
            var p2 = edges[i].getP2();
            points[i][0] = new Point2D(p1.getX(), p1.getY());
            points[i][1] = new Point2D(p2.getX(), p2.getY());
        }

        return points;
    }

    public Point2D[][] getPerspectiveProjectionXYpointOnZ(double k) {
        var points = new Point2D[edges.length][2];
        for (int i = 0; i < edges.length; i++) {
            var p1 = edges[i].getP1();
            var p2 = edges[i].getP2();
            points[i][0] = new Point2D(p1.getX(), p1.getY()).multiply(1 / (k * p1.getZ() + 1));
            points[i][1] = new Point2D(p2.getX(), p2.getY()).multiply(1 / (k * p2.getZ() + 1));
        }

        return points;
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
                {cos(angle) + nx * nx * (1 - cos(angle)),
                        nx * ny * (1 - cos(angle) + nz * sin(angle)),
                        nx * nz * (1 - cos(angle) - ny * sin(angle)),
                        0
                },
                {
                        nx * ny * (1 - cos(angle) - nz * sin(angle)),
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
}

class Edge3D {
    private final Point3D p1;
    private final Point3D p2;

    public Edge3D(Point3D p1, Point3D p2) {
        this.p1 = new Point3D(p1.getX(), p1.getY(), p1.getZ());
        this.p2 = new Point3D(p2.getX(), p2.getY(), p2.getZ());
    }

    public Point3D getP1() {
        return p1;
    }

    public Point3D getP2() {
        return p2;
    }
}
