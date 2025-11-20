package rasterizer;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class PixelShaderMultithreader {
    private final Thread[] workers;
    private final PixelShader[] shaders;
    private final int numThreads;

    private final CyclicBarrier startBarrier;
    private final CyclicBarrier endBarrier;

    private volatile Mesh currentMesh;
    private volatile Camera currentCamera;
    private volatile boolean loadingCamera = false;
    private volatile boolean running = true;

    public PixelShaderMultithreader(int X, int Y) {
        this(Runtime.getRuntime().availableProcessors(), X, Y);
    }

    public PixelShaderMultithreader(int numThreads, int X, int Y) {
        this.numThreads = numThreads;

        // Barriers include +1 for main thread
        startBarrier = new CyclicBarrier(numThreads + 1);
        endBarrier = new CyclicBarrier(numThreads + 1);

        shaders = new PixelShader[numThreads];
        workers = new Thread[numThreads];

        int rowsPerThread = Y / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int yMin = i * rowsPerThread;
            int yMax = (i == numThreads - 1) ? Y : (i + 1) * rowsPerThread;
            shaders[i] = new PixelShader(0, yMin, X, yMax);

            final int threadIndex = i;
            workers[i] = new Thread(() -> workerLoop(threadIndex), "RenderWorker-" + i);
            workers[i].start();
        }
    }

    private void workerLoop(int threadIndex) {
        PixelShader shader = shaders[threadIndex];

        while (running) {
            try {
                startBarrier.await();

                if (!running) break;

                if (loadingCamera) {
                    shader.vertexShader.loadCamera(currentCamera);
                } else {
                    shader.drawMesh(currentMesh);
                }

                endBarrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void renderFrame(Mesh mesh) {
        currentMesh = mesh;
        loadingCamera = false;

        try {
            startBarrier.await();  // Release workers
            endBarrier.await();    // Wait for completion
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void loadCamera(Camera camera) {
        currentCamera = camera;
        loadingCamera = true;

        try {
            startBarrier.await();  // Release workers
            endBarrier.await();    // Wait for completion
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void cleanup() {
        running = false;
        try {
            startBarrier.await();  // Wake up workers so they can exit
        } catch (Exception e) {
            // Ignore
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}