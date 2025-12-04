package rasterizer;

import math.Quaternion;
import math.Vec4;
import math.Matrix4;

public class Camera {
    public Vec4 transform;
    public Quaternion rotation;

    private int X, Y;
    private float fov;
    private float zNear;

    private final Matrix4 viewMatrix = new Matrix4();
    private final Matrix4 perspectiveMatrix = new Matrix4();

    public Camera(int nX, int nY) {
        this(nX, nY, new Vec4(0, 0, 0, 1), new Quaternion(1, 0, 0, 0));
    }

    public Camera(int nX, int nY, Vec4 trans, Quaternion rot) {
        transform = trans;
        rotation = rot;

        X = nX;
        Y = nY;
        fov = 90.0f;
        zNear = 0.01f;

        rewritePerspective();
    }

    public Matrix4 getViewMatrix() {
        return Matrix4.writeView(transform, rotation, viewMatrix);
    }

    public void moveTo(Vec4 vec) {
        this.transform.x = vec.x;
        this.transform.y = vec.y;
        this.transform.z = vec.z;
    }

    public void setRotation(Quaternion q) {
        rotation.w = q.w;
        rotation.i = q.i;
        rotation.j = q.j;
        rotation.k = q.k;
    }

    public void rotateBy(Quaternion delta) {
        rotation.multSelf(delta);
        rotation.normalizeSelf();
    }

    public void translateBy(Vec4 vec) {
        this.transform.addSelf(vec);
    }

    public Matrix4 getPerspectiveMatrix() {
        return perspectiveMatrix;
    }

    public void setFOV(float newFov) {
        fov = newFov;
        rewritePerspective();
    }

    public void setResolution(int newX, int newY) {
        X = newX;
        Y = newY;
        rewritePerspective();
    }

    public int getResX() {
        return X;
    }
    public int getResY() {
        return Y;
    }

    public void setZNear(float newZNear) {
        zNear = newZNear;
        rewritePerspective();
    }

    private void rewritePerspective() {
        Matrix4.writePerspectiveInfinite(fov, (float)X/Y, zNear, perspectiveMatrix);
    }

    public Vec4 getForwardDirection() {
        // Rotation applied to canonical forward vector (0,0,-1) because in most RH systems -Z is forward
        Quaternion q = rotation;
        Vec4 forward = new Vec4(0, 0, -1, 0);

        // Rotate forward by camera rotation:  q * v * q conjugate
        Quaternion v = new Quaternion(0, forward.x, forward.y, forward.z);
        Quaternion qConj = new Quaternion(q.w, -q.i, -q.j, -q.k);

        Quaternion temp = new Quaternion(1, 0, 0, 0);
        q.mult(v, temp);
        temp.mult(qConj, temp);

        return new Vec4(temp.i, temp.j, temp.k, 0);
    }
}
