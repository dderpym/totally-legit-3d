# TotallyLegit3D — A Fast CPU Software Rasterizer in Java
TotallyLegit3D is a real-time 3D software rasterizer built entirely within the constraints of UC Berkeley's CS61B standard library (StdDraw). TotallyLegit implements its own high performance linear algebra and quaternion library to accomplish this task. It achieves competitive performance for a software rasterizer.

## Features
- Custom vertex processing, raster, depth buffering, and color output.
- Supports STL and OBJ meshes in configurable render scenes.
- Fully supports diffuse UV textures on obj models.
- Barycentric raster pipeline with backface culling.
- Clipping algorithm (Sutherland-Hodgman) to improve visual fidelity of objects that clip into the near plane.

## Performance
- Implements direct buffer access patterns via Java reflection API, bypassing standard draw call overhead and achieving a ~50× performance multiplier in pixel throughput (~50× faster).
- Custom (hacked) buffering that reduces screen-blit time by 2–3× (over StdDraw).
- Zero in flight allocation architecture, bypassing GC pressure.
- Multithreaded raster and vertex stage.

_Using the Java reflection API to obtain access to the private BufferedImage under StdDraw_
<img width="786" height="115" alt="image" src="https://github.com/user-attachments/assets/cffb6a15-80d7-49e2-9eab-4f2ffd55497b" />

## Benchmarks
Textured Benchmarks:
House (478 triangles) (textured, no backface culling): ~200 FPS (Ryzen 7, 5825U, 8 threads)
![House rendered](images/house.png)
Suzanne (968 triangles) (textured): ~230 FPS (Ryzen 7 5825U, 7 threads)

Legacy benchmarks:
Suzanne (968 triangles): ~350 FPS (Ryzen 7 5825U, 7 threads)
![Suzanne rendered](images/suzanne.png)

My friend with an M3 (which I suspect is faster due to much faster memory bandwidth) told me he achieved these results:

Suzanne (968 triangles): ~800 FPS (Apple Silicon M3, 4 threads)

Dragon (37986 triangles): 300 FPS (Apple Silicon M3, 4 threads)

## Planned Features
- Implement tile based rendering and use the more optimized parallel streams java library for better cache locality and whatever multithreading advantages java streams will hopefully bring as a big industrial (optimized?) library.
- Clean up all the public variables to adhere better to standard OOP principles. (we'll see if that affects performance).

## Multithreading Status
- Raster Stage: Multithreaded in horizontal strips. Tile rendering with a small cacheable array for better cache locality is planned.
- Vertex Stage: Fully multithreaded.

## Development Constraints
- No GPU access (no OpenGL, Vulkan, DirectX, Metal).
- Limited to CS61B standard library (StdDraw via Java Swing).
- All rendering, rasterization, and buffer management implemented from scratch.
- No external graphics or math libraries.

## Running and building
As this was a school project, I've created this similar to the rest of the projects in the same class. Thus, the build tooling is quite lacking.
I will eventually add proper tooling to this, however, in the meantime there are two options to build and run this:

### Option 1: IntelliJ (recommended)
Open the project in IntelliJ and add the CS61B `algs4.jar` library manually. The exact setup on my machine is a subet of the steps [https://fa25.datastructur.es/homeworks/hw01/#task-5-cloning-repositories](here). 

### Option 2: Command Line
Refer to the cloning repository part of the above step to get algs4.jar.
From the project root:
```
cd src
javac -cp ".:/path/to/algs4.jar" *.java math/*.java
```
To execute, in the src directory, run 
```
java -cp ".:/path/to/algs4.jar" src/Main
```

By default it is configured to display a house (included in the repository). You may modify the settings in Main.java.
