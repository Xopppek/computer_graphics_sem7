import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

        var canvasParallel = new CanvasLab5(900, 600);
        var canvasPerspective = new CanvasLab5(900, 600);
        var canvasParallelDel = new CanvasLab5(900, 600);
        var canvasPerspectiveDel = new CanvasLab5(900, 600);

        var figure = new Polyhedron(new Face[]{
                new Face(new Point3D(100, 100, 0),   new Point3D(500, 100, 0),
                         new Point3D(100, 500, 0),   new Point3D(500, 500, 0)),
                new Face(new Point3D(100, 100, 0),   new Point3D(200, 200, 500),
                         new Point3D(500, 100, 0),   new Point3D(600, 200, 500)),
                new Face(new Point3D(100, 100, 0),   new Point3D(100, 500, 0),
                         new Point3D(200, 200, 500), new Point3D(200, 600, 500)),
                new Face(new Point3D(100, 500, 0),   new Point3D(500, 500, 0),
                         new Point3D(200, 600, 500), new Point3D(600, 600, 500)),
                new Face(new Point3D(500, 500, 0),   new Point3D(500, 100, 0),
                         new Point3D(600, 600, 500), new Point3D(600, 200, 500)),
                new Face(new Point3D(200, 200, 500), new Point3D(200, 600, 500),
                         new Point3D(600, 200, 500), new Point3D(600, 600, 500)),
        });

        figure = figure.rotate(Math.PI / 3, new Point3D(1, 1.6, 0));
        // figure = figure.apply(T);

        var parallelProjectionLines = figure.getParallelProjectionXY();
        for (var line : parallelProjectionLines) {
            canvasParallel.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        Imgcodecs.imwrite(savePath + "parallel.png", canvasParallel.getImage());
        displayImage(canvasParallel.getImage(), 1, "Parallel Projection");

        var perspectiveProjectionLines = figure.getPerspectiveProjectionXYpointOnZ(-0.0006);
        for (var line : perspectiveProjectionLines) {
            canvasPerspective.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }

        var parallelDelLines = figure.getParallelProjectionXY(new Point3D(0, 0, -1));
        for (var line : parallelDelLines) {
            canvasParallelDel.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }
        displayImage(canvasParallelDel.getImage(), 1, "Parallel with deleted edges");
        Imgcodecs.imwrite(savePath + "parallel_deleted_edges.png", canvasParallelDel.getImage());

        var perspectiveDelLines = figure.getPerspectiveProjectionXYpointOnZ(-0.0006, new Point3D(0, 0, -1));
        for (var line : perspectiveDelLines) {
            canvasPerspectiveDel.drawLine(line[0], line[1], Canvas.Color.BLACK);
        }
        displayImage(canvasPerspectiveDel.getImage(), 1, "Perspective with deleted edges");
        Imgcodecs.imwrite(savePath + "perspective_deleted_edges.png", canvasPerspectiveDel.getImage());


        Imgcodecs.imwrite(savePath + "perspective.png", canvasPerspective.getImage());
        displayImage(canvasPerspective.getImage(), 1, "Perspective Projection");


        try {
            createParallelGif(savePath + "parallel_rotation.gif", 120, 20, figure);
            createPerspectiveGif(savePath + "perspective_rotation.gif", 120, 20, figure);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HighGui.waitKey(0);
    }


    public static void createParallelGif(String filePath, int frameCount, int delay, Polyhedron figure) throws Exception {
        ImageWriter gifWriter = getGifWriter();
        ImageWriteParam params = gifWriter.getDefaultWriteParam();
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(filePath));
        gifWriter.setOutput(output);

        // Настройка цикла анимации
        gifWriter.prepareWriteSequence(null);

        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / (frameCount / 2.0)) {
            var canv = new CanvasLab5(900, 600);
            var figureDraw = figure.rotate(angle, new Point3D(1, 2, 0));
            var parallelDelLines = figureDraw.getParallelProjectionXY(new Point3D(0, 0, -1));
            for (var line : parallelDelLines) {
                canv.drawLine(line[0], line[1], Canvas.Color.BLACK);
            }
            Mat matFrame = canv.getImage();
            BufferedImage bufferedImage = matToBufferedImage(matFrame);

            IIOMetadata metadata = gifWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bufferedImage), params);
            configureMetadata(metadata, delay);

            gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metadata), params);
        }

        gifWriter.endWriteSequence();
        output.close();
    }

    public static void createPerspectiveGif(String filePath, int frameCount, int delay, Polyhedron figure) throws Exception {
        ImageWriter gifWriter = getGifWriter();
        ImageWriteParam params = gifWriter.getDefaultWriteParam();
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(filePath));
        gifWriter.setOutput(output);

        // Настройка цикла анимации
        gifWriter.prepareWriteSequence(null);

        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / (frameCount / 2.0)) {
            var canv = new CanvasLab5(900, 600);
            var figureDraw = figure.rotate(angle, new Point3D(1, 2, 0));
            var parallelDelLines = figureDraw.getPerspectiveProjectionXYpointOnZ(-0.0006, new Point3D(0, 0, -1));
            for (var line : parallelDelLines) {
                canv.drawLine(line[0], line[1], Canvas.Color.BLACK);
            }
            Mat matFrame = canv.getImage();
            BufferedImage bufferedImage = matToBufferedImage(matFrame);

            IIOMetadata metadata = gifWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bufferedImage), params);
            configureMetadata(metadata, delay);

            gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metadata), params);
        }

        gifWriter.endWriteSequence();
        output.close();
    }

    private static ImageWriter getGifWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) throw new IllegalStateException("GIF writer not found!");
        return writers.next();
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    private static void configureMetadata(IIOMetadata metadata, int delay) throws Exception {
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10)); // Конвертируем задержку в сотые доли секунды
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode applicationExtensions = new IIOMetadataNode("ApplicationExtensions");
        IIOMetadataNode applicationExtension = new IIOMetadataNode("ApplicationExtension");
        applicationExtension.setAttribute("applicationID", "NETSCAPE");
        applicationExtension.setAttribute("authenticationCode", "2.0");

        byte[] loopContinuously = {0x1, 0x0, 0x0};
        applicationExtension.setUserObject(loopContinuously);
        applicationExtensions.appendChild(applicationExtension);
        root.appendChild(applicationExtensions);

        metadata.setFromTree(metaFormatName, root);
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
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
            rotatedFaces[i] = faces[i].rotate(angle, axis);
        }

        return new Polyhedron(rotatedFaces);
    }

    public Polyhedron apply(double[][] T){
        Face[] transformedFaces = new Face[faces.length];

        for (int i = 0; i < faces.length; i++) {
            transformedFaces[i] = faces[i].apply(T);
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

    public Point2D[][] getParallelProjectionXY(Point3D viewer){
        if (viewer == null)
            throw new IllegalArgumentException("Вместо вектора направления наблюдение пришел null");
        if (viewer.equals(new Point3D(0, 0, 0)))
            throw new IllegalArgumentException("Вектор направления наблюдения не может быть нулевым");

        var lines = new Point2D[faces.length * 4][2];
        for (int i = 0; i < faces.length; i++) {
            Edge3D[] edges = faces[i].getEdges();
            Point3D n = faces[i].getNormal();

            if (n.dot(viewer) > 0)
                for (int j = 0; j < 4; j++) {
                    lines[4 * i + j][0] = new Point2D(edges[j].getP1().getX(), edges[j].getP1().getY());
                    lines[4 * i + j][1] = new Point2D(edges[j].getP2().getX(), edges[j].getP2().getY());
                }
            else
                for (int j = 0; j < 4; j++) {
                    // заглушки
                    lines[4 * i + j][0] = new Point2D(-1, -1);
                    lines[4 * i + j][1] = new Point2D(-1, -1);
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

    public Point2D[][] getPerspectiveProjectionXYpointOnZ(double k, Point3D viewer) {
        if (viewer == null)
            throw new IllegalArgumentException("Вместо вектора направления наблюдение пришел null");
        if (viewer.equals(new Point3D(0, 0, 0)))
            throw new IllegalArgumentException("Вектор направления наблюдения не может быть нулевым");

        var lines = new Point2D[faces.length * 4][2];

        double[][] T = {
                { 1, 0, 0, 0},
                { 0, 1, 0, 0},
                { 0, 0, 1, k},
                { 0, 0, 0, 1}
        };

        for (int i = 0; i < faces.length; i++) {
            Edge3D[] edges = faces[i].getEdges();
            Point3D n = faces[i].apply(T).getNormal();

            if (n.dot(viewer) > 0)
                for (int j = 0; j < 4; j++) {
                    var p1 = edges[j].getP1();
                    var p2 = edges[j].getP2();
                    lines[4 * i + j][0] = new Point2D(p1.getX(), p1.getY())
                            .multiply(1 / (k * p1.getZ() + 1));
                    lines[4 * i + j][1] = new Point2D(p2.getX(), p2.getY())
                            .multiply(1 / (k * p2.getZ() + 1));
                }
            else
                for (int j = 0; j < 4; j++) {
                    lines[4 * i + j][0] = new Point2D(-1, -1);
                    lines[4 * i + j][1] = new Point2D(-1, -1);
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

    public double dot(Point3D p) {
        return getX() * p.getX() + getY() * p.getY() + getZ() * p.getZ();
    }

    public Point3D rotate(double angle, Point3D axis) {
        // На вход ожидается угол в радианах, а также точка конца радиус-вектора
        // задающего ось, вокруг которой производится вращение
        if (axis == null)
            throw new IllegalArgumentException("Вместо оси вращения пришел null");
        if (axis.getX() == 0 && axis.getY() == 0 && axis.getZ() == 0)
            throw new IllegalArgumentException("Ось не может задаваться нулевым вектором");
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
    private final Point3D p1;
    private final Point3D p2;
    private final Point3D p3;
    private final Point3D p4;

    // Конструктор ожидает 4 точки.
    // Точки должны быть расположены так, чтобы по первым трем можно было
    // посчитать нормаль как векторное произведение [p2 - p1, p3 - p1]
    public Face(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        if (p1 == null || p2 == null || p3 == null || p4 == null)
            throw new IllegalArgumentException("Вершины быть null");

        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    public Edge3D[] getEdges() {
        return new Edge3D[]{
                new Edge3D(p1, p2),
                new Edge3D(p1, p3),
                new Edge3D(p3, p4),
                new Edge3D(p2, p4),
        };
    }

    public Point3D[] getPoints() {
        return new Point3D[] {p1, p2, p3, p4};
    }

    public Point3D getNormal() {
        // Вернет точку, определяющую радиус-вектор, совпадающий с вектором нормали
        var r1 = p2.sub(p1);
        var r2 = p3.sub(p1);
        var n = new Point3D(r1.getY() * r2.getZ() - r1.getZ() * r2.getY(),
                            r1.getZ() * r2.getX() - r1.getX() * r2.getZ(),
                            r1.getX() * r2.getY() - r1.getY() * r2.getX());
        double length = Math.sqrt(n.getX() * n.getX() + n.getY() * n.getY() + n.getZ() * n.getZ());
        return n.multiply(1 / length);
    }

    public Face apply(double[][] T){
        if (T == null)
            throw new IllegalArgumentException("На вход пришел null");
        if (T.length != 4 || T[0].length != 4)
            throw new IllegalArgumentException("Матрица преобразования должна быть 4x4");

        return new Face(p1.apply(T), p2.apply(T), p3.apply(T), p4.apply(T));
    }

    public Face rotate(double angle, Point3D axis) {
        return new Face(p1.rotate(angle, axis), p2.rotate(angle, axis),
                        p3.rotate(angle, axis), p4.rotate(angle, axis));
    }
}
