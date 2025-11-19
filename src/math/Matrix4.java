package math;

import java.text.DecimalFormat;

public final class Matrix4 {
    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    public Matrix4() {
        m00 = m11 = m22 = m33 = 1f;
    }

    public void set(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Matrix4 mul(Matrix4 r, Matrix4 out) {
        final float r00 = r.m00, r01 = r.m01, r02 = r.m02, r03 = r.m03;
        final float r10 = r.m10, r11 = r.m11, r12 = r.m12, r13 = r.m13;
        final float r20 = r.m20, r21 = r.m21, r22 = r.m22, r23 = r.m23;
        final float r30 = r.m30, r31 = r.m31, r32 = r.m32, r33 = r.m33;

        out.set(m00 * r00 + m01 * r10 + m02 * r20 + m03 * r30, m00 * r01 + m01 * r11 + m02 * r21 + m03 * r31, m00 * r02 + m01 * r12 + m02 * r22 + m03 * r32, m00 * r03 + m01 * r13 + m02 * r23 + m03 * r33,

                m10 * r00 + m11 * r10 + m12 * r20 + m13 * r30, m10 * r01 + m11 * r11 + m12 * r21 + m13 * r31, m10 * r02 + m11 * r12 + m12 * r22 + m13 * r32, m10 * r03 + m11 * r13 + m12 * r23 + m13 * r33,

                m20 * r00 + m21 * r10 + m22 * r20 + m23 * r30, m20 * r01 + m21 * r11 + m22 * r21 + m23 * r31, m20 * r02 + m21 * r12 + m22 * r22 + m23 * r32, m20 * r03 + m21 * r13 + m22 * r23 + m23 * r33,

                m30 * r00 + m31 * r10 + m32 * r20 + m33 * r30, m30 * r01 + m31 * r11 + m32 * r21 + m33 * r31, m30 * r02 + m31 * r12 + m32 * r22 + m33 * r32, m30 * r03 + m31 * r13 + m32 * r23 + m33 * r33);
        return out;
    }

    public Matrix4 mulSelf(Matrix4 r) {
        final float r00 = r.m00, r01 = r.m01, r02 = r.m02, r03 = r.m03;
        final float r10 = r.m10, r11 = r.m11, r12 = r.m12, r13 = r.m13;
        final float r20 = r.m20, r21 = r.m21, r22 = r.m22, r23 = r.m23;
        final float r30 = r.m30, r31 = r.m31, r32 = r.m32, r33 = r.m33;

        this.set(m00 * r00 + m01 * r10 + m02 * r20 + m03 * r30, m00 * r01 + m01 * r11 + m02 * r21 + m03 * r31, m00 * r02 + m01 * r12 + m02 * r22 + m03 * r32, m00 * r03 + m01 * r13 + m02 * r23 + m03 * r33,

                m10 * r00 + m11 * r10 + m12 * r20 + m13 * r30, m10 * r01 + m11 * r11 + m12 * r21 + m13 * r31, m10 * r02 + m11 * r12 + m12 * r22 + m13 * r32, m10 * r03 + m11 * r13 + m12 * r23 + m13 * r33,

                m20 * r00 + m21 * r10 + m22 * r20 + m23 * r30, m20 * r01 + m21 * r11 + m22 * r21 + m23 * r31, m20 * r02 + m21 * r12 + m22 * r22 + m23 * r32, m20 * r03 + m21 * r13 + m22 * r23 + m23 * r33,

                m30 * r00 + m31 * r10 + m32 * r20 + m33 * r30, m30 * r01 + m31 * r11 + m32 * r21 + m33 * r31, m30 * r02 + m31 * r12 + m32 * r22 + m33 * r32, m30 * r03 + m31 * r13 + m32 * r23 + m33 * r33);
        return this;
    }

    public static Matrix4 newTranslation(Vec4 offset) {
        Matrix4 m = new Matrix4();
        writeTranslation(offset, m);
        return m;
    }

    public static Matrix4 writeTranslation(Vec4 offset, Matrix4 out) {
        out.m03 = offset.x;
        out.m13 = offset.y;
        out.m23 = offset.z;
        return out;
    }

    /**
     * @param q quaternion representing orientation
     * @return Rotation matrix (takes input in oriented space and rotates it back to world space)
     */
    public static Matrix4 newRotation(Quaternion q) {
        Matrix4 m = new Matrix4();
        writeRotation(q, m);
        return m;
    }

    public static Matrix4 writeRotation(Quaternion q, Matrix4 out) {

        float x = q.i, y = q.j, z = q.k, w = q.w;
        float x2 = x + x, y2 = y + y, z2 = z + z;
        float xx = x * x2, xy = x * y2, xz = x * z2;
        float yy = y * y2, yz = y * z2, zz = z * z2;
        float wx = w * x2, wy = w * y2, wz = w * z2;

        out.m00 = 1.0f - (yy + zz);
        out.m01 = xy - wz;
        out.m02 = xz + wy;

        out.m10 = xy + wz;
        out.m11 = 1.0f - (xx + zz);
        out.m12 = yz - wx;

        out.m20 = xz - wy;
        out.m21 = yz + wx;
        out.m22 = 1.0f - (xx + yy);
        return out;
    }

    /**
     * This calculates the inverse of a model matrix. In other words, it takes from world space to model space.
     * This is useful in the camera. I want a separate method for this, inlining all the math to avoid having to write
     * a general purpose inverse matrix method (hard) and avoid floating point divisions as much as possible.
     * Like all other write methods, it writes into a preassigned matrix rather than allocating a new one
     * for performance (although really this happens once a frame, so it's not strictly necessary.
     *
     * @param pos - Position of the viewer
     * @param rot - Rotation of the viewer
     * @param out - Matrix to write into
     */
    public static Matrix4 writeView(Vec4 pos, Quaternion rot, Matrix4 out) {
        float x = -rot.i;
        float y = -rot.j;
        float z = -rot.k;
        float w = rot.w;

        float x2 = x + x, y2 = y + y, z2 = z + z;
        float xx = x * x2, xy = x * y2, xz = x * z2;
        float yy = y * y2, yz = y * z2, zz = z * z2;
        float wx = w * x2, wy = w * y2, wz = w * z2;

        out.m00 = 1.0f - (yy + zz);
        out.m01 = xy - wz;
        out.m02 = xz + wy;

        out.m10 = xy + wz;
        out.m11 = 1.0f - (xx + zz);
        out.m12 = yz - wx;

        out.m20 = xz - wy;
        out.m21 = yz + wx;
        out.m22 = 1.0f - (xx + yy);

        float tx = pos.x;
        float ty = pos.y;
        float tz = pos.z;

        out.m03 = -(out.m00 * tx + out.m01 * ty + out.m02 * tz);
        out.m13 = -(out.m10 * tx + out.m11 * ty + out.m12 * tz);
        out.m23 = -(out.m20 * tx + out.m21 * ty + out.m22 * tz);

        out.m30 = out.m31 = out.m32 = 0.0f;
        out.m33 = 1.0f;
        return out;
    }

    /**
     * This calculates the perspective matrix. This projects things from model space onto a plane zNear
     * in front of the model. Uses treats zFar as infinite using calculus/analysis bullshit so that's cool.
     * Performance here is not really necessary.
     */
    public static Matrix4 writePerspectiveInfinite(float fovYDegrees, float aspect, float zNear, Matrix4 out) {
        float f = 1.0f / (float) Math.tan(fovYDegrees * 0.5f * Math.PI / 180.0f);

        out.m00 = f / aspect;
        out.m01 = 0;
        out.m02 = 0;
        out.m03 = 0;
        out.m10 = 0;
        out.m11 = f;
        out.m12 = 0;
        out.m13 = 0;
        out.m20 = 0;
        out.m21 = 0;
        out.m22 = -1.0f;
        out.m23 = -2.0f * zNear;
        out.m30 = 0;
        out.m31 = 0;
        out.m32 = -1.0f;
        out.m33 = 0;
        return out;
    }

    public String toString() {
        // DecimalFormat helps align floating-point numbers nicely.
        // Adjust the format string as needed (e.g., "0.00" for fewer decimals)
        DecimalFormat df = new DecimalFormat("0.0000");

        // Use StringBuilder for efficient string concatenation in Java

        return "\n" + "[" + df.format(m00) + ", " + df.format(m01) + ", " + df.format(m02) + ", " + df.format(m03) + "]\n" +

                "[" + df.format(m10) + ", " + df.format(m11) + ", " + df.format(m12) + ", " + df.format(m13) + "]\n" +

                "[" + df.format(m20) + ", " + df.format(m21) + ", " + df.format(m22) + ", " + df.format(m23) + "]\n" +

                "[" + df.format(m30) + ", " + df.format(m31) + ", " + df.format(m32) + ", " + df.format(m33) + "]";
    }
}