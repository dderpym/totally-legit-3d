# TotallyLegit3D - A (hopefully) fast software rasterizer for Java, hijacking Princeton Algorithms StdDraw

This was made as a submission for Project 3 of  UC Berkeley's CS61B.

If, for some godforsaken reason, you would like to run this code on your own machine, you may compile it with the following command:
As this was a school project, I've created this using the basic IntelliJ setup they provide, so this process is quite janky.

```
cd src
javac -cp ".:/path/to/algs4.jar" *.java math/*.java
```
in that directory, run 
```
java -cp ".:/path/to/algs4.jar" src/Main
```
```
```

You should see a small cube spinning in the middle of a 1000x1000 canvas.

The less awkward way of running it is to use IntelliJ Idea and link the library directory.
To my recollection it should not require anything other than StdDraw in algs4 (Princeton algorithms library), however I have not tried with an empty repo.
You can find algs4.jar here: https://algs4.cs.princeton.edu/code/
If that is insufficient, you can hunt down the CS61B standard library I used at https://fa25.datastructur.es

This code should be easily adaptable to work with just about any display tool as long as you modify TotallyLegit correctly.
