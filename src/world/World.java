package world;

import math.Vec4;
import rasterizer.MultithreadedRenderer;

import java.util.ArrayList;
import java.util.List;

public class World {
    public final ArrayList<Mesh> meshes = new ArrayList<>();
    public final ArrayList<Tri> tris = new ArrayList<>();

    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
        tris.addAll(List.of(mesh.tris));
    }

    public void render(MultithreadedRenderer renderer) {
        meshes.forEach(renderer::renderMesh);
    }
}
