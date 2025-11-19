package math;

/**
 * yeah, i did steal these formulas off wikipedia
 * no, i do not understand them
 * does it matter?
 */
public class Quaternion {
    public float w, i, j, k;

    public Quaternion(float w, float i, float j, float k) {
        this.w = w;
        this.i = i;
        this.j = j;
        this.k = k;
    }

    public void mult(Quaternion other, Quaternion out) {
        out.w = this.w * other.w - this.i * other.i - this.j * other.j - this.k * other.k;
        out.i = this.w * other.i + this.i * other.w + this.j * other.k - this.k * other.j;
        out.j = this.w * other.j - this.i * other.k + this.j * other.w + this.k * other.i;
        out.k = this.w * other.k + this.i * other.j - this.j * other.i + this.k * other.w;
    }

    public void multSelf(Quaternion other) {
        float newW = this.w * other.w - this.i * other.i - this.j * other.j - this.k * other.k;
        float newX = this.w * other.i + this.i * other.w + this.j * other.k - this.k * other.j;
        float newY = this.w * other.j - this.i * other.k + this.j * other.w + this.k * other.i;
        float newZ = this.w * other.k + this.i * other.j - this.j * other.i + this.k * other.w;

        this.w = newW;
        this.i = newX;
        this.j = newY;
        this.k = newZ;
    }

    public void normalizeSelf() {
        float sq = w * w + i * i + j * j + k * k;

        if (sq < 1e-10f) {
            w = 1.0f;
            i = j = k = 0.0f;
            return;
        }

        // float invMag = Utility.invSqrt(sq);
        float invMag = (float) (1/Math.sqrt(sq));

        w *= invMag;
        i *= invMag;
        j *= invMag;
        k *= invMag;
    }

    public String toString() {
        return String.format("%3f + %3fi + %3fj + %2fk", w, i, j, k);
    }
}
