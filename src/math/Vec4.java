package math;

public final class Vec4 {
    public float x, y, z, w;

    public Vec4() {
    }

    /**
     * @param x - x coordinate (do you really need this doc comment?)
     * @param y - y coordinate
     * @param z - z coordinate
     * @param w - t coordinate, 1 if pos and 0 if vector
     */
    public Vec4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void add(Vec4 o, Vec4 out) {
        out.x = x + o.x;
        out.y = y + o.y;
        out.z = z + o.z;
        out.w = w + o.w;
    }

    public void addSelf(Vec4 o) {
        add(o, this);
    }

    public void sub(Vec4 o, Vec4 out) {
        out.x = x - o.x;
        out.y = y - o.y;
        out.z = z - o.z;
        out.w = w - o.w;
    }

    public void subSelf(Vec4 o) {
        sub(o, this);
    }

    public void transform(Matrix4 m, Vec4 out) {
        final float xx = x, yy = y, zz = z, ww = w;

        out.x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03 * ww;
        out.y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13 * ww;
        out.z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23 * ww;
        out.w = m.m30 * xx + m.m31 * yy + m.m32 * zz + m.m33 * ww;
    }

    public void transformSelf(Matrix4 m) {
        final float xx = x, yy = y, zz = z, ww = w;

        x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03 * ww;
        y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13 * ww;
        z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23 * ww;
        w = m.m30 * xx + m.m31 * yy + m.m32 * zz + m.m33 * ww;
    }

    public void transformAffine(Matrix4 m, Vec4 out) {
        final float xx = x, yy = y, zz = z;

        out.x = m.m00 * xx + m.m01 * yy + m.m02 * zz + m.m03;
        out.y = m.m10 * xx + m.m11 * yy + m.m12 * zz + m.m13;
        out.z = m.m20 * xx + m.m21 * yy + m.m22 * zz + m.m23;
        out.w = 1.0f;
    }

    public float dot(Vec4 o) {
        return x * o.x + y * o.y + z * o.z + w * o.w;
    }

    /**
     * You may recall the cross product only works in three dimensions. This is correct.
     * We assume that both inputs are vectors (t is 0)
     * @param o other vector fr
     * @param out vector to write result to
     */
    public void cross(Vec4 o, Vec4 out) {
        out.x = y*o.z - z*o.y;
        out.y = z*o.x - x*o.z;
        out.z = x*o.y - y*o.x;
        out.w = 0;
    }

    public void normalizeSelf() {
        float invMag = 1f /(float) Math.sqrt(x*x+y*y+z*z+ w * w);
        x = x*invMag;
        y = y*invMag;
        z = z*invMag;
        w = w *invMag;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}