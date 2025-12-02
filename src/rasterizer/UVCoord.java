package rasterizer;

public class UVCoord {
    public static UVCoord DUMMY_UV = new UVCoord(0.0f, 0.0f, 0.0f);

    public float u;
    public float v;
    public float w;

    public UVCoord(float u, float v, float w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }
}
