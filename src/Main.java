import loaders.OBJLoader;
import los.Raycaster;
import rasterizer.*;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

import math.Vec4;
import math.Quaternion;
import world.Mesh;
import world.UVTexture;
import world.World;

public class Main {
    private static int X = 1000;
    private static int Y = 1000;
    private static int its = 50;

    private static Mesh benchmarkMesh;
    private static Camera benchmarkCamera;
    private static MultithreadedRenderer pixelShader;
    private static World world;

    private static int[][] randomX;
    private static int[][] randomY;
    private static Raycaster.RaycastResult raycastResult = new Raycaster.RaycastResult();

    private static long lastFPSTime = System.nanoTime();
    private static int frameCount = 0;
    private static double currentFPS = 0.0;

    public static void main(String[] args) {
        StdDraw.setCanvasSize(X, Y);
        StdDraw.setXscale(0, X);
        StdDraw.setYscale(Y, 0);
        StdDraw.enableDoubleBuffering();

        TotallyLegit.init();
        pixelShader = new MultithreadedRenderer(8, X, Y);
        world = new World();

        try {
            benchmarkMesh = OBJLoader.load("models/house.obj");
            benchmarkMesh.texture = new UVTexture("models/house.png");
            world.addMesh(benchmarkMesh);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("gg");
        }

        benchmarkMesh.translateBy(new Vec4(0, -1, -2, 0));
        benchmarkMesh.setRotation(new Quaternion(1, 0, 0, 0));
        benchmarkMesh.backfaceCulling = false;

        benchmarkCamera = new Camera(X, Y);

        double last = System.nanoTime() * 1e-9;
        while (true) {
            double now = System.nanoTime() * 1e-9;
            double time = now - last;
            last = now;

            float angleX = (float) (time * 1);
            float angleY = (float) (time * 0.2);
            float angleZ = (float) (time * 0);

            Quaternion rotX = new Quaternion((float) Math.cos(angleX * 0.5), (float) Math.sin(angleX * 0.5), 0, 0);
            Quaternion rotY = new Quaternion((float) Math.cos(angleY * 0.5), 0, (float) Math.sin(angleY * 0.5), 0);
            Quaternion rotZ = new Quaternion((float) Math.cos(angleZ * 0.5), 0, 0, (float) Math.sin(angleZ * 0.5));

            Quaternion temp = new Quaternion(1, 0, 0, 0);
            rotX.mult(rotY, temp);
            temp.mult(rotZ, rotX);

            benchmarkMesh.setRotation(rotX);

            render();

            frameCount++;

            long a = System.nanoTime();
            double elapsed = (a - lastFPSTime) / 1e9;

            if (elapsed >= 1.0) {
                currentFPS = frameCount / elapsed;
                System.out.printf("FPS: %.1f  |  Triangles: %d  |  Vertices transformed: %d%n",
                        currentFPS, benchmarkMesh.tris.length, benchmarkMesh.tris.length * 3);
                frameCount = 0;
                lastFPSTime = a;
            }
        }
    }

    private static void render() {
        pixelShader.loadCamera(benchmarkCamera);
        TotallyLegit.clear();
        world.render(pixelShader);
        TotallyLegit.show();
    }

    public static void Bench() {
        StdDraw.setCanvasSize(X, Y);
        StdDraw.setXscale(0, X);
        StdDraw.setYscale(Y, 0);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenRadius(0);

        TotallyLegit.init();

        int numPoints = X * Y;

        System.out.println("Generating random coordinates...");
        randomX = new int[its][numPoints];
        randomY = new int[its][numPoints];
        Random random = new Random();
        for (int iter = 0; iter < its; iter++) {
            for (int i = 0; i < numPoints; i++) {
                randomX[iter][i] = random.nextInt(X);
                randomY[iter][i] = random.nextInt(Y);
            }
        }

        System.out.println("Warming up JVM...");
        for (int i = 0; i < 10; i++) {
            BenchFastSetRandomSet(0);
            StdDraw.clear();
        }

        long start;

        start = System.nanoTime();
        for (int i = 0; i < its; i++) {
            BenchStdDrawRandomSet(i);
            TotallyLegit.show();
            StdDraw.clear();
        }
        long seq = (System.nanoTime() - start) / its;
        System.out.printf("StdDrawRandom        %.2f ms%n", seq / 1_000_000.0);

        start = System.nanoTime();
        for (int i = 0; i < its; i++) {
            BenchFastSetRandomSet(i);
            TotallyLegit.show();
            StdDraw.clear();
        }
        long fastSet = (System.nanoTime() - start) / its;
        System.out.printf("FastSetRandom        %.2f ms%n", fastSet / 1_000_000.0);

        start = System.nanoTime();
        for (int i = 0; i < its; i++) {
            BenchFastSetSequential();
            StdDraw.show();
            StdDraw.clear();
        }
        long fastSetSeq = (System.nanoTime() - start) / its;
        System.out.printf("FastSetSequential        %.2f ms%n", fastSetSeq / 1_000_000.0);
    }

    private static void BenchStdDrawRandomSet(int iteration) {
        StdDraw.setPenColor(Color.BLACK);
        for (int i = 0; i < randomX[iteration].length; i++) {
            StdDraw.point(randomX[iteration][i], randomY[iteration][i]);
        }
    }

    private static void BenchFastSetRandomSet(int iteration) {
        int black = 0xFF000000;
        for (int i = 0; i < randomX[iteration].length; i++) {
            TotallyLegit.setRGB(randomX[iteration][i], randomY[iteration][i], black);
        }
    }

    private static void BenchFastSetSequential() {
        int black = 0xFF000000;
        int[] px = TotallyLegit.pixels;
        for (int i = 0; i < px.length; i++) {
            px[i] = black;
        }
    }
}