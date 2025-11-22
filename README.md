# TotallyLegit3D — A Fast CPU Software Rasterizer in Java

TotallyLegit3D is a real-time software rasterizer written entirely in Java with only standard libraries for UC Berkeley’s CS61B Project 3. It implements a full miniature graphics pipeline using only the Java Standard Library and Princeton’s StdDraw library — no OpenGL, Vulkan, or GPU acceleration.

Despite relying on CPU-only rendering, the engine achieves reasonable performance under fairly high poly counts.

## Real-Time CPU Rendering
- Custom vertex processing, face assembly, rasterization, depth buffering, and color output
- Supports STL meshes and configurable render scenes.
- Barycentric rasterization pipeline with backface culling.
## Performance
- Uses Java reflection to access private StdDraw buffers, reducing 1 million random-pixel-write time from 235 ms → 4.5 ms (~50× faster)
- Internal buffering that reduces screen-blit time by 2–3×
- Multithreaded raster stage (and planned vertex stage!)
## Benchmarks 
Suzanne (968 triangles): ~350 FPS (Ryzen 7 5825U, 7 threads)
![Suzanne rendered](images/suzanne.png)
Suzanne (968 triangles): ~800 FPS (Apple Silicon M3, 4 threads)

Dragon (37986 triangles): ~300 FPS (Apple Silicon M3, 4 threads)
## Multithreading Status
- Raster Stage: Fully multithreaded
- Vertex Stage: Currently executed per-thread before raster; refactoring in progress

As this was a school project, I've created this similar to the rest of the projects in the same class. Thus, the build tooling is quite lacking.
I will eventually add proper tooling to this, however, in the meantime:

### Option 1: IntelliJ (recommended)
Open the project in IntelliJ and add the CS61B `algs4.jar` library manually.

### Option 2: Command Line
From the project root:
```
cd src
javac -cp ".:/path/to/algs4.jar" *.java math/*.java
```
To execute, in the src directory, run 
```
java -cp ".:/path/to/algs4.jar" src/Main
```

By default it is configured to display Suzanne (included in the repository). You may modify the settings in Main.java.
