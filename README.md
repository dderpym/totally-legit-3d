# TotallyLegit3D - A (hopefully) fast software rasterizer for Java, hijacking Princeton Algorithms StdDraw.

This was made as a part of a submission for Project 3 of  UC Berkeley's CS61B.

TotallyLegit3D is a 3D software rasterizer that runs in real time on Java swing. It runs entirely on the CPU and is optimized as such. Further, it only uses the Java standard libraries and the Princeton StdDraw library.

In order to achieve reasonable efficiency when drawing to the screen, TotallyLegit3D uses reflections to take out private components of StdDraw, which would otherwise be too slow to render in real time. By doing so, rasterizer.TotallyLegit achieves a ~50x speedup in random pixel writes (235 ms down to 4.5ms for 1 million random writes), and approximately a 2-3x speedup in time to show the image on screen. 

Currently, it is able to wireframe render Suzanne (2904 verts) at 600 fps on my Ryzen 7 5825u, reaching upwards of 900 FPS on my friends Apple M3 chip. Raster is currently WIP.

The princeton algs4 library is required to run this code. You can find algs4.jar here: https://algs4.cs.princeton.edu/code/

As this was a school project, I've created this similar to the rest of the projects in the same class. Thus, the build tooling is quite lacking. On Linux, you may run from the project root:

```
cd src
javac -cp ".:/path/to/algs4.jar" *.java math/*.java
```
To execute, in the src directory, run 
```
java -cp ".:/path/to/algs4.jar" src/Main
```
By default it is configured to display Suzanne (included in the repository).
