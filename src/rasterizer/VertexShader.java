package rasterizer;

import math.Matrix4;
import math.Vec4;

public class VertexShader {
    private static int X, Y;
    private static final Matrix4 VPMatrix = new Matrix4();
    private static Matrix4 MMatrix;
    private static final Matrix4 MVPMatrix = new Matrix4();

    private static final Vec4 transformBuffer = new Vec4();
    private static final Vec4 edgeBuffer0 = new Vec4();
    private static final Vec4 edgeBuffer1 = new Vec4();
    private static final Vec4 edgeBuffer2 = new Vec4();

    /**
     * Loads the camera (VP matrices) into the vertex shader.
     * Will not update MVP if it already has been calculated.
     * @param camera - The camera to load view and perspectives
     */
    public static void loadCamera(Camera camera) {
        Matrix4 V = camera.getViewMatrix();
        Matrix4 P = camera.getPerspectiveMatrix();

        P.mul(V, VPMatrix);
        X = camera.getResX();
        Y = camera.getResY();
    }

    /**
     * This loads the model into the vertex shader.
     * Only call *after* loading the camera.
     * @param mesh - The mesh to load the model matrix
     */
    public static void loadModel(Mesh mesh) {
        MMatrix = mesh.getModelMatrix();
        VPMatrix.mul(MMatrix, MVPMatrix);
    }

    /**
     * Processes a triangle into a xy pixel coordinates, assuming camera and model are the most recently loaded.
     * @param tri - Triangle to process.
     * @param out - array to write output into. format: Ax, Ay, Bx, By, Cx, Cy. Required capacity of 6.
     */
    public static void processTri(Tri tri, VertExport out) {
        tri.a.transform(MVPMatrix, transformBuffer);

        float invA = 1.0f / transformBuffer.t;
        float ndcxA = transformBuffer.x * invA;
        float ndcyA = transformBuffer.y * invA;

        out.aX = (int) ((ndcxA + 1f) * 0.5f * X);
        out.aY = (int) ((1f - ndcyA) * 0.5f * Y); // flip y cause i guess i gotta
        out.aZ = invA;

        tri.b.transform(MVPMatrix, transformBuffer);

        float invB = 1.0f / transformBuffer.t;
        float ndcxB = transformBuffer.x * invB;
        float ndcyB = transformBuffer.y * invB;

        out.bX = (int) ((ndcxB + 1f) * 0.5f * X);
        out.bY = (int) ((1f - ndcyB) * 0.5f * Y);
        out.bZ = invB;

        tri.c.transform(MVPMatrix, transformBuffer);

        float invC = 1.0f / transformBuffer.t;
        float ndcxC = transformBuffer.x * invC;
        float ndcyC = transformBuffer.y * invC;

        out.cX = (int) ((ndcxC + 1f) * 0.5f * X);
        out.cY = (int) ((1f - ndcyC) * 0.5f * Y);
        out.cZ = invC;

        tri.a.transform(MMatrix, edgeBuffer0);
        tri.b.transform(MMatrix, edgeBuffer1);
        edgeBuffer0.sub(edgeBuffer1, edgeBuffer1);

        tri.c.transform(MMatrix, edgeBuffer2);
        edgeBuffer0.subSelf(edgeBuffer2);

        edgeBuffer0.normalizeSelf();
        edgeBuffer1.normalizeSelf();

        edgeBuffer0.cross(edgeBuffer1, out.norm);
    }

    public static class VertExport {
        public int aX, aY;
        public float aZ;

        public int bX, bY;
        public float bZ;

        public int cX, cY;
        public float cZ;

        public Vec4 norm;

        public VertExport(Vec4 nCross) {
            norm = nCross;

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
