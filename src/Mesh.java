import math.Quaternion;
import math.Vec4;
import math.Matrix4;

public class Mesh {
    public final Tri[] tris;

    public Vec4 transform;
    public Quaternion rotation;

    private final Matrix4 modelMatrix = new Matrix4();
    private boolean dirty = true; // you dirty little bit (although i guess it's a byte)

    private final Matrix4 cache = new Matrix4();

    public Mesh(Tri[] tris) {
        this.tris = tris;
        this.transform = new Vec4(0, 0, 0, 1);
        this.rotation = new Quaternion(1, 0, 0, 0);
    }

    public Mesh(Tri[] tris, Vec4 trans, Quaternion rot) {
        this.tris = tris;
        this.transform = trans;
        this.rotation = rot;
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