package math;

public final class Vec4 {
    public float x, y, z, t;

    public Vec4() { }

    /**
     * @param x - x coordinate (do you really need this doc comment?)
     * @param y - y coordinate
     * @param z - z coordinate
     * @param t - t coordinate, 1 if pos and 0 if vector
     */
    public Vec4(float x, float y, float z, float t) {
        this.x = x; this.y = y; this.z = z; this.t = t;
    }

    public void addSelf(Vec4 o) {
        this.x += o.x;
        this.y += o.y;
        this.z += o.z;
    }

    public void transformInto(Matrix4 m, Vec4 out) {
        final float xx = x, yy = y, zz = z, ww = t;

        out.x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03 * ww;
        out.y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13 * ww;
        out.z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23 * ww;
        out.t = m.m30 * xx + m.m31 * yy + m.m32 * zz + m.m33 * ww;
    }

    public void transformSelf(Matrix4 m) {
        final float xx = x, yy = y, zz = z, ww = t;

        x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03 * ww;
        y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13 * ww;
        z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23 * ww;
        t = m.m30 * xx + m.m31 * yy + m.m32 * zz + m.m33 * ww;
    }

    public void transformAffine(Matrix4 m, Vec4 out) {
        final float xx = x, yy = y, zz = z;

        out.x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03;
        out.y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13;
        out.z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23;
        out.t = 1.0f;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f, %.3f)", x, y, z, t);
    }
}