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
        fov = 50.0f;
        zNear = 0.69f;

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
        // Get the rotation matrix and extract the forward direction from it
        Matrix4 rotMatrix = Matrix4.newRotation(rotation);

        // In camera/view space, forward is (0, 0, -1)
        // Transform it by the rotation to get world space forward
        Vec4 forward = new Vec4(0, 0, -1, 0);
        forward.transformSelf(rotMatrix);
        forward.normalizeSelf();

        return forward;
    }
}
