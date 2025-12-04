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
     * Writes up to two output triangles: primary -> out, secondary (if non-null) -> out2
     */
    public void processTri(Tri tri, VertExport out, VertExport out2) {
        // Transform to clip space (before perspective divide)
        Vec4 clipA = new Vec4();
        Vec4 clipB = new Vec4();
        Vec4 clipC = new Vec4();

        tri.a.transform(MVP, clipA);
        tri.b.transform(MVP, clipB);
        tri.c.transform(MVP, clipC);

        // Compute triangle normal (world/model space) once (used for all generated tris)
        // This is cheaper than recomputing per-clipped-triangle and preserves correct lighting
        tri.a.transform(M, edgeBuffer0);
        tri.b.transform(M, edgeBuffer1);
        edgeBuffer1.sub(edgeBuffer0, edgeBuffer1); // edgeBuffer1 = b - a

        tri.c.transform(M, calculationBuffer);
        calculationBuffer.sub(edgeBuffer0, calculationBuffer); // calculationBuffer = c - a

        edgeBuffer1.cross(calculationBuffer, transformBuffer); // transformBuffer = (b-a) x (c-a)
        transformBuffer.normalizeSelf(); // normalized normal in model/world space

        // compute viewA once (used for view-related shading if needed)
        tri.a.transform(MV, edgeBuffer0);
        edgeBuffer0.normalizeSelf();

        // Quick reject: all vertices are behind near
        if (clipA.w < NEAR_PLANE && clipB.w < NEAR_PLANE && clipC.w < NEAR_PLANE) {
            out.aInvZ = out.bInvZ = out.cInvZ = -1;
            if (out2 != null) out2.aInvZ = out2.bInvZ = out2.cInvZ = -1;
            return;
        }

        // Quick accept: all vertices in front
        if (clipA.w >= NEAR_PLANE && clipB.w >= NEAR_PLANE && clipC.w >= NEAR_PLANE) {
            // Process normally without clipping - write into primary
            processTriNormal(tri, out, clipA, clipB, clipC, transformBuffer, edgeBuffer0);
            if (out2 != null) {
                out2.aInvZ = out2.bInvZ = out2.cInvZ = -1; // no second triangle
            }
            return;
        }

        // Need to clip - use Sutherland-Hodgman (implemented below)
        int outCount = clipTriangleToNearPlane(clipA, clipB, clipC,
                tri.aUV, tri.bUV, tri.cUV,
                clipBuffer, uvClipBuffer);

        if (outCount < 3) {
            // Fully clipped
            out.aInvZ = out.bInvZ = out.cInvZ = -1;
            if (out2 != null) out2.aInvZ = out2.bInvZ = out2.cInvZ = -1;
            return;
        }

        // Triangulate fan: (0, i, i+1) for i=1..outCount-2
        // There can be at most 4 vertices -> at most 2 triangles.
        int written = 0;
        int totalTris = outCount - 2;

        for (int i = 1; i <= outCount - 2; ++i) {
            Vec4 c0 = clipBuffer[0];
            Vec4 c1 = clipBuffer[i];
            Vec4 c2 = clipBuffer[i + 1];

            UVCoord u0 = uvClipBuffer[0];
            UVCoord u1 = uvClipBuffer[i];
            UVCoord u2 = uvClipBuffer[i + 1];

            if (written == 0) {
                processTriClipped(c0, c1, c2, u0, u1, u2, transformBuffer, edgeBuffer0, out);
            } else if (written == 1 && out2 != null) {
                processTriClipped(c0, c1, c2, u0, u1, u2, transformBuffer, edgeBuffer0, out2);
            }

            written++;
            // If more than two triangles would be generated (shouldn't for near-plane clipping), skip extras
            if (written >= 2) break;
        }

        // If we produced only one triangle, mark the second as invalid
        if (written == 1 && out2 != null) {
            out2.aInvZ = out2.bInvZ = out2.cInvZ = -1;
        }
    }

    private void processTriNormal(Tri tri, VertExport out, Vec4 clipA, Vec4 clipB, Vec4 clipC, Vec4 normalWorld, Vec4 viewA) {
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

        // Use precomputed world normal
        out.norm.x = normalWorld.x;
        out.norm.y = normalWorld.y;
        out.norm.z = normalWorld.z;
        out.norm.w = normalWorld.w;
        out.norm.normalizeSelf();
    }

    private void processTriClipped(Vec4 clipA, Vec4 clipB, Vec4 clipC,
                                   UVCoord uvA, UVCoord uvB, UVCoord uvC,
                                   Vec4 normalWorld, Vec4 viewA, VertExport out) {
        // We can't recompute a proper per-triangle 'viewA' from clipped points cheaply, so reuse the provided viewA
        out.viewA.x = viewA.x; out.viewA.y = viewA.y; out.viewA.z = viewA.z; out.viewA.w = viewA.w;

        // Perspective divide and convert to screen space
        float invA = 1.0f / clipA.w;
        float ndcxA = clipA.x * invA;
        float ndcyA = clipA.y * invA;

        out.aX = (int) ((ndcxA + 1f) * 0.5f * X);
        out.aY = (int) ((ndcyA + 1f) * 0.5f * Y);
        out.aUinvZ = uvA.u * invA;
        out.aVinvZ = uvA.v * invA;
        out.aW = uvA.w;
        out.aInvZ = invA;

        float invB = 1.0f / clipB.w;
        float ndcxB = clipB.x * invB;
        float ndcyB = clipB.y * invB;

        out.bX = (int) ((ndcxB + 1f) * 0.5f * X);
        out.bY = (int) ((ndcyB + 1f) * 0.5f * Y);
        out.bUinvZ = uvB.u * invB;
        out.bVinvZ = uvB.v * invB;
        out.bW = uvB.w;
        out.bInvZ = invB;

        float invC = 1.0f / clipC.w;
        float ndcxC = clipC.x * invC;
        float ndcyC = clipC.y * invC;

        out.cX = (int) ((ndcxC + 1f) * 0.5f * X);
        out.cY = (int) ((ndcyC + 1f) * 0.5f * Y);
        out.cUinvZ = uvC.u * invC;
        out.cVinvZ = uvC.v * invC;
        out.cW = uvC.w;
        out.cInvZ = invC;

        // Use precomputed world normal
        out.norm.x = normalWorld.x;
        out.norm.y = normalWorld.y;
        out.norm.z = normalWorld.z;
        out.norm.w = normalWorld.w;
        out.norm.normalizeSelf();
    }

    /**
     * Clips a triangle against the near plane.
     * This is a full implementation using Sutherland-Hodgman algorithm.
     * Writes output vertices into outVerts and outUVs. Returns the vertex count.
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

        public Vec4 norm = new Vec4();
        public Vec4 viewA = new Vec4();

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