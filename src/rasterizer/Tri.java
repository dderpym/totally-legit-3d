package rasterizer;

import math.Vec4;
/**
 * public fields because i think they're faster (unproven but my vibes say yes)
 * Can take out vector class and put into this if later needed for SIMD stuff
 */
public class Tri {
    public Vec4 a, b, c;

    public Tri(Vec4 a, Vec4 b, Vec4 c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
