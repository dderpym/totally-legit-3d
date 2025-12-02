package rasterizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UVTexture {
    private BufferedImage image;
    private int X, Y;

    public UVTexture(String filePath) {
        image = null;
        try {
            File file = new File(filePath);
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            System.err.println("womp womp can't read: " + e.getMessage());
            return;
        }

        if (image == null) {
            System.err.println("womp womp failed to read");
            return;
        }

        X = image.getWidth();
        Y = image.getHeight();
    }

    public int getRGBbyUV(float U, float V) {
        float uClamped = Math.max(0.0f, Math.min(Math.nextDown(1.0f), U));
        float vClamped = Math.max(0.0f, Math.min(Math.nextDown(1.0f), V));

        return image.getRGB((int) (uClamped * X), (int) (vClamped * Y));
    }
}
