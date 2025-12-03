package world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UVTexture {
    private final int[] pixels;
    private BufferedImage image;
    private int X, Y;
    private float Xf, Yf;

    public UVTexture(String filePath) {
        try {
            File file = new File(filePath);
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            System.err.println("womp womp can't read: " + e.getMessage());
            throw new RuntimeException();
        }

        X = image.getWidth();
        Xf = (float) X;
        Y = image.getHeight();
        Yf = (float) Y;
        pixels = image.getRGB(0, 0, X, Y, null, 0, X);
    }

    public final int getRGBbyUV(float u, float v) {
        int tx = (int)(u * Xf + 0.5f)  % X;
        int ty = (int)((1.0f - v) * Yf + 0.5f) % X;
        if (tx < 0) tx += X;
        if (ty < 0) ty += Y;
        return pixels[ty * X + tx];
    }
}
