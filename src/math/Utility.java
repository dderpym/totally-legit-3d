package math;

public class Utility {
    /**
     * holy shit i'm a huge fucking fan of this algorithm
     * praise the lord i get to use it
     * @param x
     * @return
     */
    public static float invSqrt(float x) {
        int i = Float.floatToIntBits(x);
        // the holy number
        i = 0x5f3759df - (i >> 1);
        float y = Float.intBitsToFloat(i);

        // sneaky little newton iteration because i'm like that
        y = y * (1.5f - 0.5f * x * y * y);

        return y;
    }
}
