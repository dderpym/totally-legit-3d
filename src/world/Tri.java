package world;

import math.Vec4;

import static world.UVCoord.DUMMY_UV;

/**
 * public fields because i think they're faster (unproven but my vibes say yes)
 * Can take out vector class and put into this if later needed for SIMD stuff
 */
public class Tri {
    public Vec4 a, b, c;
    public UVCoord aUV, bUV, cUV;

    public Tri(Vec4 a, Vec4 b, Vec4 c) {
        this(a, DUMMY_UV, b, DUMMY_UV, c, DUMMY_UV);
    }

    public Tri(Vec4 a, UVCoord aUV, Vec4 b, UVCoord bUV, Vec4 c, UVCoord cUV) {
        this.a = a;
        this.b = b;
        this.c = c;

        this.aUV = aUV;
        this.bUV = bUV;
        this.cUV = cUV;
    }
}
