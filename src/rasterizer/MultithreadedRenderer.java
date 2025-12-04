package rasterizer;

import world.Mesh;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MultithreadedRenderer {
    private final Thread[] workers;
    private final PixelShader[] shaders;
    private final int numThreads;

    private final CyclicBarrier startBarrier;
    private final CyclicBarrier midDraw;
    private final CyclicBarrier endBarrier;

    private volatile Mesh currentMesh;
    private volatile Camera currentCamera;
    private volatile boolean loadingCamera = false;
    private volatile boolean running = true;

    private VertexShader[] vertexShadersPool;
    private VertexShader.VertExport[] vertExportPool;
    private int vertExportPoolSize;
    private static final int INITIAL_POOL_SIZE = 10000;

    public MultithreadedRenderer(int X, int Y) {
        this(Runtime.getRuntime().availableProcessors(), X, Y);
    }

    public MultithreadedRenderer(int numThreads, int X, int Y) {
        this.numThreads = numThreads;

        // Barriers include +1 for main thread
        startBarrier = new CyclicBarrier(numThreads + 1);
        midDraw = new CyclicBarrier(numThreads + 1);
        endBarrier = new CyclicBarrier(numThreads + 1);

        shaders = new PixelShader[numThreads];
        vertexShadersPool = new VertexShader[numThreads];
        workers = new Thread[numThreads];

        vertExportPool = new VertexShader.VertExport[INITIAL_POOL_SIZE];
        vertExportPoolSize = vertExportPool.length;

        for (int i = 0; i < vertExportPoolSize; ++i) {
            vertExportPool[i] = new VertexShader.VertExport();
        }

        int rowsPerThread = Y / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int yMin = i * rowsPerThread;
            int yMax = (i == numThreads - 1) ? Y : (i + 1) * rowsPerThread;

            shaders[i] = new PixelShader(0, yMin, X, yMax);
            vertexShadersPool[i] = new VertexShader();

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
                    vertexShadersPool[threadIndex].loadCamera(currentCamera);
                } else {
                    vertexShadersPool[threadIndex].loadModel(currentMesh);
                    int totalTris = currentMesh.tris.length;
                    int trisPerThread = totalTris / numThreads;
                    int startIdx = threadIndex * trisPerThread;
                    int endIdx;

                    if (threadIndex == numThreads - 1) {
                        // Last thread gets the base amount PLUS all the leftover remainder
                        endIdx = totalTris;
                    } else {
                        // All other threads get just the base amount
                        endIdx = (threadIndex + 1) * trisPerThread;
                    }

                    if (startIdx >= totalTris) {
                        startIdx = totalTris;
                        endIdx = totalTris;
                    }

                    for (int i = startIdx; i < endIdx; ++i) {
                        vertexShadersPool[threadIndex].processTri(currentMesh.tris[i], vertExportPool[i]);
                    }

                    midDraw.await();

                    for (int i = 0; i < currentMesh.tris.length; ++i) {
                        shader.drawVerts(vertExportPool[i], currentMesh.texture, currentMesh.backfaceCulling);
                    }
                }

                endBarrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void renderMesh(Mesh mesh) {
        currentMesh = mesh;
        loadingCamera = false;

        if (mesh.tris.length > vertExportPoolSize) {
            VertexShader.VertExport[] past = vertExportPool;
            vertExportPool = new VertexShader.VertExport[mesh.tris.length];
            System.arraycopy(past, 0, vertExportPool, 0, vertExportPoolSize);
            for (int i = vertExportPoolSize; i < vertExportPool.length; ++i) {
                vertExportPool[i] = new VertexShader.VertExport();
            }
            vertExportPoolSize = vertExportPool.length;
        }

        try {
            startBarrier.await();  // Should process vertices
            midDraw.await();       // Start rasterizing
            endBarrier.await();    // Wait for completion :/
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