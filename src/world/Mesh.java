package world;

import math.Quaternion;
import math.Vec4;
import math.Matrix4;
import rasterizer.VertexShader;

public class Mesh {
    private static final UVTexture defaultTexture = new UVTexture("models/default_texture.png");

    public final Tri[] tris;
    public final VertexShader.VertExport[] vertExports;

    public boolean backfaceCulling = false;

    public Vec4 transform;
    public Quaternion rotation;
    public UVTexture texture;

    private final Matrix4 modelMatrix = new Matrix4();
    private boolean dirty = true;

    private final Matrix4 cache = new Matrix4();

    public Mesh(Tri[] tris) {
        this(tris, new Vec4(0, 0, 0, 1), new Quaternion(1, 0, 0, 0), defaultTexture);
    }

    public Mesh(Tri[] tris, UVTexture texture) {
        this(tris, new Vec4(0, 0, 0, 1), new Quaternion(1, 0, 0, 0), texture);
    }

    public Mesh(Tri[] tris, Vec4 trans, Quaternion rot, UVTexture texture) {
        this.tris = tris;
        this.transform = trans;
        this.rotation = rot;
        this.vertExports = new VertexShader.VertExport[tris.length];
        this.texture = texture;
    }

    public Matrix4 getModelMatrix() {
        if (dirty) {
            Matrix4.writeTranslation(transform, modelMatrix);
            Matrix4.writeRotation(rotation, cache);
            modelMatrix.mulSelf(cache);
            dirty = false;
        }
        return modelMatrix;
    }

    public void moveTo(Vec4 vec) {
        this.transform.x = vec.x;
        this.transform.y = vec.y;
        this.transform.z = vec.z;
        dirty = true;
    }

    public void setRotation(Quaternion q) {
        rotation.w = q.w;
        rotation.i = q.i;
        rotation.j = q.j;
        rotation.k = q.k;
        dirty = true;
    }

    public void rotateBy(Quaternion delta) {
        rotation.multSelf(delta);
        rotation.normalizeSelf();
        dirty = true;
    }

    public void translateBy(Vec4 vec) {
        this.transform.addSelf(vec);
        dirty = true;
    }
}