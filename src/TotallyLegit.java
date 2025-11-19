import edu.princeton.cs.algs4.StdDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * StdDraw does not let me access the BufferedImage, so I pull it out with reflections
 * for go much fast!
**/
public class TotallyLegit {
    public static int[] pixels;
    public static int width, height;
    public static int clearColor;

    public static BufferedImage image;
    public static Graphics2D graphics;

    public static JFrame frame;
    public static Canvas canvas;
    public static BufferStrategy bufferStrategy;


    public static void init() {
        try {
            Field bufImg = StdDraw.class.getDeclaredField("offscreenImage");
            bufImg.setAccessible(true);
            image = (BufferedImage) bufImg.get(null);

            pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            width = image.getWidth();
            height = image.getHeight();
            clearColor = argb(255, 255, 255, 255);

            Field graphik = StdDraw.class.getDeclaredField("offscreen");
            graphik.setAccessible(true);
            graphics = (Graphics2D) graphik.get(null);

            Field frameField = StdDraw.class.getDeclaredField("frame");
            frameField.setAccessible(true);
            frame = (JFrame) frameField.get(null);

            canvas = new Canvas();
            canvas.setPreferredSize(new Dimension(width, height));
            canvas.setIgnoreRepaint(true);
            frame.setIgnoreRepaint(true);

            frame.getContentPane().removeAll();
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(canvas, BorderLayout.CENTER);
            frame.pack();

            canvas.createBufferStrategy(2);
            bufferStrategy = canvas.getBufferStrategy();

            frame.setVisible(true);
        }
        catch (Exception e) {
            System.err.println("Reflection failed: Could not rip out StdDraw guts. It's joever.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void setRGBFast(int x, int y, int argb) {
        pixels[getPixelLocation(x, y)] = argb;
    }

    /*
    Yes, seriously.
     */
    public static void clear() {
        Arrays.fill(pixels, clearColor);
    }

    /*
     Unironically StdDraw show was so slow it capped me at 300 fps. Can you imagine being capped at a mere 300 fps?
     */
    public static void show() {
        Graphics g = bufferStrategy.getDrawGraphics();
        // This uses the BufferStrategy's OWN back buffer directly → no copy, no SunGraphics2D
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        bufferStrategy.show();
    }

    public static int getPixelLocation(int x, int y) {
        return y * width + x;
    }

    public static int argb(int a, int r, int g, int b) {
       return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void drawLine(int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int[] pixels = TotallyLegit.pixels;
        int width = TotallyLegit.width;

        // Simple bounds check (optional but safe)
        if (x0 < 0 || x0 >= width || y0 < 0 || y0 >= height ||
                x1 < 0 || x1 >= width || y1 < 0 || y1 >= height) {
        }

        while (true) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                pixels[y0 * width + x0] = color;
            }

            if (x0 == x1 && y0 == y1) break;

            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}