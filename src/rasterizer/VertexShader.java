package rasterizer;

import math.Matrix4;
import math.Vec4;

public class VertexShader {
    private int X, Y;

    private Matrix4 M;
    private Matrix4 V;
    private Matrix4 P;

    private final Matrix4 VP = new Matrix4();
    private final Matrix4 MV = new Matrix4();
    private final Matrix4 MVP = new Matrix4();

    private final Vec4 transformBuffer = new Vec4();
    private final Vec4 edgeBuffer0 = new Vec4();
    private final Vec4 edgeBuffer1 = new Vec4();
    private final Vec4 calculationBuffer = new Vec4();

    /**
     * Loads the camera (VP matrices) into the vertex shader.
     * Will not update MVP if it already has been calculated.
     * @param camera - The camera to load view and perspectives
     */
    public void loadCamera(Camera camera) {
        V = camera.getViewMatrix();
        P = camera.getPerspectiveMatrix();

        P.mul(V, VP);
        X = camera.getResX();
        Y = camera.getResY();
    }

    /**
     * This loads the model into the vertex shader.
     * Only call *after* loading the camera.
     * @param mesh - The mesh to load the model matrix
     */
    public void loadModel(Mesh mesh) {
        M = mesh.getModelMatrix();
        VP.mul(M, MVP);
        V.mul(M, MV);
    }

    /**
     * Processes a triangle into a xy pixel coordinates, assuming camera and model are the most recently loaded.
     * @param tri - Triangle to process.
     * @param out - array to write output into. format: Ax, Ay, Bx, By, Cx, Cy. Required capacity of 6.
     */
    public void processTri(Tri tri, VertExport out) {
        tri.a.transform(MV, out.viewA);
        out.viewA.normalizeSelf();

        tri.a.transform(MVP, transformBuffer);

        float invA = 1.0f / transformBuffer.t;
        float ndcxA = transformBuffer.x * invA;
        float ndcyA = transformBuffer.y * invA;

        out.aX = (int) ((ndcxA + 1f) * 0.5f * X);
        out.aY = (int) ((1f - ndcyA) * 0.5f * Y); // flip y cause i guess i gotta
        out.aZ = invA;

        tri.b.transform(MVP, transformBuffer);

        float invB = 1.0f / transformBuffer.t;
        float ndcxB = transformBuffer.x * invB;
        float ndcyB = transformBuffer.y * invB;

        out.bX = (int) ((ndcxB + 1f) * 0.5f * X);
        out.bY = (int) ((1f - ndcyB) * 0.5f * Y);
        out.bZ = invB;

        tri.c.transform(MVP, transformBuffer);

        float invC = 1.0f / transformBuffer.t;
        float ndcxC = transformBuffer.x * invC;
        float ndcyC = transformBuffer.y * invC;

        out.cX = (int) ((ndcxC + 1f) * 0.5f * X);
        out.cY = (int) ((1f - ndcyC) * 0.5f * Y);
        out.cZ = invC;

        tri.a.transform(M, edgeBuffer0);
        tri.b.transform(M, edgeBuffer1);
        edgeBuffer0.sub(edgeBuffer1, edgeBuffer1);

        tri.c.transform(M, calculationBuffer);
        edgeBuffer0.subSelf(calculationBuffer);

        edgeBuffer0.cross(edgeBuffer1, out.norm);
        out.norm.normalizeSelf();
    }

    public static class VertExport {
        public int aX, aY;
        public float aZ;

        public int bX, bY;
        public float bZ;

        public int cX, cY;
        public float cZ;

        public Vec4 norm;
        public Vec4 viewA;

        public VertExport(Vec4 nCross, Vec4 nViewA) {
            norm = nCross;
            viewA = nViewA;

            aX = 0;
            aY = 0;
            aZ = 0;

            bX = 0;
            bY = 0;
            bZ = 0;

            cX = 0;
            cY = 0;
            cZ = 0;
        }
    }
}
