import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

import math.Vec4;
import math.Quaternion;
import math.Matrix4;

public class Main {
    private static int X = 1000;
    private static int Y = 1000;
    private static int its = 50;

    private static Mesh benchmarkMesh;
    private static Camera benchmarkCamera;

    private static final Matrix4 VPMatrix = new Matrix4();
    private static final Matrix4 MVPMatrix = new Matrix4();

    private static int[][] randomX;
    private static int[][] randomY;

    private static long lastFPSTime = System.nanoTime();
    private static int frameCount = 0;
    private static double currentFPS = 0.0;

    public static void main(String[] args) {
        StdDraw.setCanvasSize(X, Y);
        StdDraw.setXscale(0, X);
        StdDraw.setYscale(Y, 0);
        StdDraw.enableDoubleBuffering();
        TotallyLegit.init();

        try {
            benchmarkMesh = STLLoader.loadSTL("models/Suzanne.stl");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("gg");
        }
        benchmarkMesh.translateBy(new Vec4(0, 0, -10, 0));
        benchmarkMesh.rotation = new Quaternion(0.577f, 0.577f, 0.577f, 0);

        benchmarkCamera = new Camera(X, Y);
        double startTime = System.nanoTime() * 1e-9;

        int i = 0;
        double last = System.nanoTime() * 1e-9;
        while (true) {
            double now = System.nanoTime() * 1e-9;
            double time = now - last;
            last = now;

            TotallyLegit.clear();

            float angleX = (float) (time * 0.7);
            float angleY = (float) (time * 1.1);
            float angleZ = (float) (time * 0.4);

            Quaternion rotX = new Quaternion((float) Math.cos(angleX * 0.5), (float) Math.sin(angleX * 0.5), 0, 0);
            Quaternion rotY = new Quaternion((float) Math.cos(angleY * 0.5), 0, (float) Math.sin(angleY * 0.5), 0);
            Quaternion rotZ = new Quaternion((float) Math.cos(angleZ * 0.5), 0, 0, (float) Math.sin(angleZ * 0.5));

            Quaternion temp = new Quaternion(1, 0, 0, 0);
            rotX.mult(rotY, temp);
            temp.mult(rotZ, rotX);

            rotX.normalizeSelf();
            benchmarkMesh.setRotation(rotX);

            wireframeRender();

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

    private static Mesh createCubeMesh() {
        Tri[] tris = {
                // Front
                new Tri(new Vec4(-1, 1,-1,1), new Vec4( 1, 1,-1,1), new Vec4( 1,-1,-1,1)),
                new Tri(new Vec4(-1, 1,-1,1), new Vec4( 1,-1,-1,1), new Vec4(-1,-1,-1,1)),
                // Back
                new Tri(new Vec4(-1, 1, 1,1), new Vec4( 1,-1, 1,1), new Vec4( 1, 1, 1,1)),
                new Tri(new Vec4(-1, 1, 1,1), new Vec4(-1,-1, 1,1), new Vec4( 1,-1, 1,1)),
                // Left
                new Tri(new Vec4(-1, 1,-1,1), new Vec4(-1,-1,-1,1), new Vec4(-1,-1, 1,1)),
                new Tri(new Vec4(-1, 1,-1,1), new Vec4(-1,-1, 1,1), new Vec4(-1, 1, 1,1)),
                // Right
                new Tri(new Vec4( 1, 1,-1,1), new Vec4( 1,-1, 1,1), new Vec4( 1, 1, 1,1)),
                new Tri(new Vec4( 1, 1,-1,1), new Vec4( 1,-1,-1,1), new Vec4( 1,-1, 1,1)),
                // Top
                new Tri(new Vec4(-1, 1, 1,1), new Vec4( 1, 1, 1,1), new Vec4( 1, 1,-1,1)),
                new Tri(new Vec4(-1, 1, 1,1), new Vec4( 1, 1,-1,1), new Vec4(-1, 1,-1,1)),
                // Bottom
                new Tri(new Vec4(-1,-1,-1,1), new Vec4( 1,-1, 1,1), new Vec4( 1,-1,-1,1)),
                new Tri(new Vec4(-1,-1,-1,1), new Vec4(-1,-1, 1,1), new Vec4( 1,-1, 1,1)),
        };
        return new Mesh(tris);
    }

    private static void wireframeRender() {
        Matrix4 M = benchmarkMesh.getModelMatrix();
        Matrix4 V = benchmarkCamera.getViewMatrix();
        Matrix4 P = benchmarkCamera.getPerspectiveMatrix();

        P.mul(V, VPMatrix);
        VPMatrix.mul(M, MVPMatrix);

        Vec4 a = new Vec4();
        Vec4 b = new Vec4();
        Vec4 c = new Vec4();

        TotallyLegit.clear();

        for (Tri tri : benchmarkMesh.tris) {
            tri.a.transformInto(MVPMatrix, a);
            tri.b.transformInto(MVPMatrix, b);
            tri.c.transformInto(MVPMatrix, c);

            float invA = 1.0f / a.t;
            float invB = 1.0f / b.t;
            float invC = 1.0f / c.t;

            float ndcAx = a.x * invA;
            float ndcAy = a.y * invA;
            float ndcBx = b.x * invB;
            float ndcBy = b.y * invB;
            float ndcCx = c.x * invC;
            float ndcCy = c.y * invC;

            int sxA = (int) ((ndcAx + 1f) * 0.5f * X);
            int syA = (int) ((1f - ndcAy) * 0.5f * Y);   // Y is flipped in StdDraw
            int sxB = (int) ((ndcBx + 1f) * 0.5f * X);
            int syB = (int) ((1f - ndcBy) * 0.5f * Y);
            int sxC = (int) ((ndcCx + 1f) * 0.5f * X);
            int syC = (int) ((1f - ndcCy) * 0.5f * Y);

            TotallyLegit.graphics.drawLine(sxA, syA, sxB, syB);
            TotallyLegit.graphics.drawLine(sxB, syB, sxC, syC);
            TotallyLegit.graphics.drawLine(sxC, syC, sxA, syA);
        }

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

        // Pre-generate random coordinates for all iterations
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
            TotallyLegit.setRGBFast(randomX[iteration][i], randomY[iteration][i], black);
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