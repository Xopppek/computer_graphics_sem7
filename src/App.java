import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class App {
    public static void main(String[] args) throws Exception {
        File resultFile = new File("src/results/result.png");
        BufferedImage gojo = null;
        BufferedImage geometry = null;
        BufferedImage circleMask = null;
        BufferedImage gojoMask = null;
        BufferedImage result = null;
        BufferedImage birdImg = null;
        BufferedImage flagImg = null;
        BufferedImage flagMask = null;
        
        try {
            gojo =  ImageIO.read(new File("src/resources/gojo.png"));
            geometry = ImageIO.read(new File("src/resources/img2.png")); 
            circleMask =  ImageIO.read(new File("src/resources/circle_mask.png")); 
            gojoMask = ImageIO.read(new File("src/resources/mask2.jpg"));
            birdImg = ImageIO.read(new File("src/resources/bird.jpg"));
            flagImg = ImageIO.read(new File("src/resources/flag.jpg"));
            flagMask = ImageIO.read(new File("src/resources/gradient.png"));
        } 
        catch (IOException e) { e.printStackTrace(System.out); }
        //result = composeImages(birdImg, flagImg, flagMask);

        for (BlendingType type : BlendingType.values()){
            result = composeImages(birdImg, flagImg, flagMask, type);
            ImageIO.write(result, "png", new File("src/results/result_" + type + ".png"));
            //displayImage(result);
        }

        result = composeImages(gojo, geometry, gojoMask);
        ImageIO.write(result, "png", resultFile);
        //displayImage(result);

        result = applyTransparencyMask(toGrayScale(gojo), circleMask);
        ImageIO.write(result, "png", new File("src/results/halftone.png"));
        displayImage(result);
    }

    public static void displayImage(BufferedImage img){
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        frame.setSize(img.getWidth(), img.getHeight());
        label.setIcon(new ImageIcon(img));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static boolean areSameSize(BufferedImage img1, BufferedImage img2){
        return (img1.getWidth() == img2.getWidth()) && (img1.getHeight() == img2.getHeight());
    }

    public static BufferedImage composeImages(BufferedImage botImg, BufferedImage topImg, double alpha)
                                              throws IllegalArgumentException{
        return composeImages(botImg, topImg, getSimpleTransparencyMask(botImg, 1), getSimpleTransparencyMask(topImg, alpha), BlendingType.Normal);
    }

    public static BufferedImage composeImages(BufferedImage botImg, BufferedImage topImg, BufferedImage alphaImg)
                                              throws IllegalArgumentException{
        return composeImages(botImg, topImg, getSimpleTransparencyMask(botImg, 1), alphaImg, BlendingType.Normal);
    }

    public static BufferedImage composeImages(BufferedImage botImg, BufferedImage topImg, BufferedImage alphaImg, BlendingType blendingType)
                                              throws IllegalArgumentException{
        return composeImages(botImg, topImg, getSimpleTransparencyMask(botImg, 1), alphaImg, blendingType);
    }

    public enum BlendingType {
        Normal,
        Multiply,
        Screen,
        Darken,
        Lighten,
        Difference,
        ColorDodge,
        ColorBurn,
        SoftLight,
    }

    public static BufferedImage composeImages(BufferedImage botImg, BufferedImage topImg, BufferedImage botAlphaImg, 
                                              BufferedImage topAlphaImg, BlendingType blendingType)
                                              throws IllegalArgumentException{
        return switch (blendingType) {
            case Normal -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> b);
            case Multiply -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> (int) (a * b / 255.0));
            case Screen -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, 
                                        (a, b) -> (int) ((1 - (1 - a / 255.0) * (1 - b / 255.0)) * 255));
            case Darken -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> Math.min(a, b));
            case Lighten -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> Math.max(a, b));
            case Difference -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> Math.abs(a - b));
            case ColorDodge -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, 
                                            (a, b) -> (int) (b < 255 ? Math.min(1, a * 1.0 / (255 - b)) * 255 : 255));
            case ColorBurn -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, 
                                           (a, b) -> (int) (b > 0 ? 255 * (1 - Math.min(1, (255 - a) * 1.0 / b)) : 0));
            case SoftLight -> composeImages(botImg, topImg, botAlphaImg, topAlphaImg, (a, b) -> blendSoftLight(a, b));
            default -> null;
        };
    }

    private static BufferedImage composeImages(BufferedImage botImg, BufferedImage topImg, BufferedImage botAlphaImg, 
                                               BufferedImage topAlphaImg, BiFunction<Integer, Integer, Integer> blendFunction)
                                               throws IllegalArgumentException{           
        if (!areSameSize(botImg, topImg) || !areSameSize(topImg, botAlphaImg) || !areSameSize(botAlphaImg, topAlphaImg))
            throw new IllegalArgumentException("Images must be the same size");

        BufferedImage composedImg = new BufferedImage(botImg.getWidth(), botImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < composedImg.getHeight(); y++){
            for (int x = 0; x < composedImg.getWidth(); x++){
                int botRGB = botImg.getRGB(x, y);
                int[] botColors = getColorsFromRGB(botRGB);

                int topRGB = topImg.getRGB(x, y);
                int[] topColors = getColorsFromRGB(topRGB);

                int[] composedColors = new int[3];
                for (int i = 0; i < 3; i++){
                    double botAlpha = (botAlphaImg.getRGB(x, y) & 0x000000ff) / 255.0;
                    double topAlpha = (topAlphaImg.getRGB(x, y) & 0x000000ff) / 255.0;
                    composedColors[i] = (int) ((1 - topAlpha) * botAlpha * botColors[i] + 
                                               (1 - botAlpha) * topAlpha * topColors[i] +
                                               botAlpha * topAlpha * blendFunction.apply(botColors[i], topColors[i]));
                }

                int composedRGB = (255 << 24) | composedColors[2] << 16 | composedColors[1] << 8 | composedColors[0];
                composedImg.setRGB(x, y, composedRGB);
            }
        }
        
        return composedImg;
    }

    private static int[] getColorsFromRGB(int rgb){
        int[] colors = new int[3];
        colors[2] =  ((rgb >> 16) & 0xFF);
        colors[1] = ((rgb >> 8) & 0xFF);
        colors[0] = (rgb & 0xFF);
        return colors;
    }

    public static BufferedImage applyTransparencyMask(BufferedImage img, BufferedImage mask) throws IllegalArgumentException{
        
        if (!areSameSize(img, mask))
            throw new IllegalArgumentException("Image and mask must be the same size");

        BufferedImage maskedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                int rgb = img.getRGB(x, y);
                maskedImg.setRGB(x, y, rgb & 0x00ffffff | (mask.getRGB(x, y) << 24));
            }
        }

        return maskedImg;
    }

    public static BufferedImage toGrayScale(BufferedImage img){

        BufferedImage grayImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                int rgb = img.getRGB(x, y);
                int r =  ((rgb >> 16) & 0xFF);
                int g = ((rgb >> 8) & 0xFF);
                int b = (rgb & 0xFF);
                //rgb = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                rgb = (int) (r + g + b) / 3;
                rgb = (255 << 24) | (rgb << 16) | (rgb << 8) | rgb;
                grayImg.setRGB(x, y, rgb);
            } 
        }

        return grayImg;
    }

    private static BufferedImage getSimpleTransparencyMask(BufferedImage img, double alpha){

        BufferedImage mask = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < mask.getHeight(); y++){
            for (int x = 0; x < mask.getWidth(); x++){
                int c = (int) (alpha * 255);
                mask.setRGB(x, y, 255 << 24 | c << 16 | c << 8 | c);
            }
        }

        return mask;
    }

    private static double blendSoftLightD(double x){
        if (x > 0.25)
            return Math.sqrt(x);
        return ((16 * x - 12) * x + 4) * x;
    }
    private static Integer blendSoftLight(Integer a, Integer b){
        double cb = a / 255.0;
        double cs = b / 255.0;
        if (cs > 0.5)
            return (int) (cb + (2 * cs - 1) * (blendSoftLightD(cb) - cb));
        return (int) (cb - (1 - 2 * cs) * cb * (1 - cb));
    }
}
