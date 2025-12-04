package rasterizer;

import math.Matrix4;
import math.Vec4;
import world.Mesh;
import world.Tri;
import world.UVCoord;

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

    // Clipping buffers
    private final Vec4[] clipBuffer = new Vec4[9]; // Max vertices after clipping
    private final UVCoord[] uvClipBuffer = new UVCoord[9];

    private static final float NEAR_PLANE = 0.01f; // Match camera's zNear

    public VertexShader() {
        for (int i = 0; i < clipBuffer.length; i++) {
            clipBuffer[i] = new Vec4();
            uvClipBuffer[i] = new UVCoord(0, 0, 0);
        }
    }

    public void loadCamera(Camera camera) {
        V = camera.getViewMatrix();
        P = camera.getPerspectiveMatrix();

        P.mul(V, VP);
        X = camera.getResX();
        Y = camera.getResY();
    }

    public void loadModel(Mesh mesh) {
        M = mesh.getModelMatrix();
        VP.mul(M, MVP);
        V.mul(M, MV);
    }

    /**
     * Processes a triangle with near plane clipping.
     * Returns false if triangle is completely clipped.
     */
    public void processTri(Tri tri, VertExport out) {
        // Transform to clip space (before perspective divide)
        Vec4 clipA = new Vec4();
        Vec4 clipB = new Vec4();
        Vec4 clipC = new Vec4();

        tri.a.transform(MVP, clipA);
        tri.b.transform(MVP, clipB);
        tri.c.transform(MVP, clipC);

        // Check if all vertices are behind near plane
        if (clipA.w < NEAR_PLANE && clipB.w < NEAR_PLANE && clipC.w < NEAR_PLANE) {
            // Mark as invalid
            out.aInvZ = -1;
            out.bInvZ = -1;
            out.cInvZ = -1;
            return;
        }

        // Check if all vertices are in front of near plane (no clipping needed)
        if (clipA.w >= NEAR_PLANE && clipB.w >= NEAR_PLANE && clipC.w >= NEAR_PLANE) {
            // Process normally without clipping
            processTriNormal(tri, out, clipA, clipB, clipC);
            return;
        }

        // Need to clip - mark this triangle as invalid
        // In a full implementation, you'd generate new triangles here
        // For now, just discard triangles that cross the near plane
        out.aInvZ = -1;
        out.bInvZ = -1;
        out.cInvZ = -1;
    }

    private void processTriNormal(Tri tri, VertExport out, Vec4 clipA, Vec4 clipB, Vec4 clipC) {
        // Transform view space for normal calculation
        tri.a.transform(MV, out.viewA);
        out.viewA.normalizeSelf();

        // Perspective divide and convert to screen space
        float invA = 1.0f / clipA.w;
        float ndcxA = clipA.x * invA;
        float ndcyA = clipA.y * invA;

        out.aX = (int) ((ndcxA + 1f) * 0.5f * X);
        out.aY = (int) ((ndcyA + 1f) * 0.5f * Y);
        out.aUinvZ = tri.aUV.u * invA;
        out.aVinvZ = tri.aUV.v * invA;
        out.aW = tri.aUV.w;
        out.aInvZ = invA;

        float invB = 1.0f / clipB.w;
        float ndcxB = clipB.x * invB;
        float ndcyB = clipB.y * invB;

        out.bX = (int) ((ndcxB + 1f) * 0.5f * X);
        out.bY = (int) ((ndcyB + 1f) * 0.5f * Y);
        out.bUinvZ = tri.bUV.u * invB;
        out.bVinvZ = tri.bUV.v * invB;
        out.bW = tri.bUV.w;
        out.bInvZ = invB;

        float invC = 1.0f / clipC.w;
        float ndcxC = clipC.x * invC;
        float ndcyC = clipC.y * invC;

        out.cX = (int) ((ndcxC + 1f) * 0.5f * X);
        out.cY = (int) ((ndcyC + 1f) * 0.5f * Y);
        out.cUinvZ = tri.cUV.u * invC;
        out.cVinvZ = tri.cUV.v * invC;
        out.cW = tri.cUV.w;
        out.cInvZ = invC;

        // Calculate normal in world space
        tri.a.transform(M, edgeBuffer0);
        tri.b.transform(M, edgeBuffer1);
        edgeBuffer0.sub(edgeBuffer1, edgeBuffer1);

        tri.c.transform(M, calculationBuffer);
        edgeBuffer0.subSelf(calculationBuffer);

        edgeBuffer0.cross(edgeBuffer1, out.norm);
        out.norm.normalizeSelf();
    }

    /**
     * Clips a triangle against the near plane.
     * This is a full implementation using Sutherland-Hodgman algorithm.
     */
    private int clipTriangleToNearPlane(Vec4 a, Vec4 b, Vec4 c,
                                        UVCoord uvA, UVCoord uvB, UVCoord uvC,
                                        Vec4[] outVerts, UVCoord[] outUVs) {
        // Input polygon
        Vec4[] input = {a, b, c};
        UVCoord[] inputUVs = {uvA, uvB, uvC};
        int inputCount = 3;

        int outputCount = 0;

        for (int i = 0; i < inputCount; i++) {
            Vec4 current = input[i];
            Vec4 next = input[(i + 1) % inputCount];
            UVCoord currentUV = inputUVs[i];
            UVCoord nextUV = inputUVs[(i + 1) % inputCount];

            boolean currentInside = current.w >= NEAR_PLANE;
            boolean nextInside = next.w >= NEAR_PLANE;

            if (currentInside) {
                outVerts[outputCount].x = current.x;
                outVerts[outputCount].y = current.y;
                outVerts[outputCount].z = current.z;
                outVerts[outputCount].w = current.w;
                outUVs[outputCount].u = currentUV.u;
                outUVs[outputCount].v = currentUV.v;
                outUVs[outputCount].w = currentUV.w;
                outputCount++;
            }

            if (currentInside != nextInside) {
                // Edge crosses the plane - compute intersection
                float t = (NEAR_PLANE - current.w) / (next.w - current.w);

                outVerts[outputCount].x = current.x + t * (next.x - current.x);
                outVerts[outputCount].y = current.y + t * (next.y - current.y);
                outVerts[outputCount].z = current.z + t * (next.z - current.z);
                outVerts[outputCount].w = NEAR_PLANE;

                outUVs[outputCount].u = currentUV.u + t * (nextUV.u - currentUV.u);
                outUVs[outputCount].v = currentUV.v + t * (nextUV.v - currentUV.v);
                outUVs[outputCount].w = currentUV.w + t * (nextUV.w - currentUV.w);

                outputCount++;
            }
        }

        return outputCount;
    }

    public static class VertExport {
        public int aX, aY;
        public float aUinvZ, aVinvZ, aW;
        public float aInvZ;

        public int bX, bY;
        public float bUinvZ, bVinvZ, bW;
        public float bInvZ;

        public int cX, cY;
        public float cUinvZ, cVinvZ, cW;
        public float cInvZ;

        public Vec4 norm;
        public Vec4 viewA;

        public VertExport() {
            this(new Vec4(0, 0, 0, 0), new Vec4(0, 0, 0, 0));
        }

        public VertExport(Vec4 nCross, Vec4 nViewA) {
            norm = nCross;
            viewA = nViewA;

            aX = 0;
            aY = 0;
            aUinvZ = 0;
            aVinvZ = 0;
            aInvZ = 0;

            bX = 0;
            bY = 0;
            bUinvZ = 0;
            bVinvZ = 0;
            bInvZ = 0;

            cX = 0;
            cY = 0;
            cUinvZ = 0;
            cVinvZ = 0;
            cInvZ = 0;
        }
    }
}