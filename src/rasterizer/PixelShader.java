package rasterizer;

import math.Vec4;

public class PixelShader {
    private static final int red = TotallyLegit.argb(255, 255, 0, 0);
    private static final int black = TotallyLegit.argb(255, 0, 0, 0);

    private static final Span span = new Span();  // reused forever, no allocation
    private static final Vec4 crossBuffer = new Vec4();
    private static final Vec4 viewBuffer = new Vec4();
    private static final VertexShader.VertExport verts = new VertexShader.VertExport(crossBuffer, viewBuffer);

    private static final Vec4 lightVec = new Vec4(0, 1, -0.5f, 0);
    {
        lightVec.normalizeSelf();
    }

    private static final class Span {
        int left  = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;

        void update(int x) {
            if (x < left)  left  = x;
            if (x > right) right = x;
        }

        void reset() {
            left  = Integer.MAX_VALUE;
            right = Integer.MIN_VALUE;
        }

        boolean isValid() {
            return left <= right;
        }
    }

    public static void drawMesh(Mesh mesh) {
        VertexShader.loadModel(mesh);

        for (Tri tri : mesh.tris) {
            VertexShader.processTri(tri, verts);
            drawTri();
        }
    }

    private static void drawTri() {
        // backface culling
        if (verts.norm.dot(verts.viewA) <= -0.1) return;
        int ax = verts.aX, ay = verts.aY;
        int bx = verts.bX, by = verts.bY;
        int cx = verts.cX, cy = verts.cY;
        float az = verts.aZ, bz = verts.bZ, cz = verts.cZ;

        float oneOverArea = 1.0f / edge(ax, ay, bx, by, cx, cy);

        int minY = Math.max(0,   Math.min(ay, Math.min(by, cy)));
        int maxY = Math.min(TotallyLegit.height - 1, Math.max(ay, Math.max(by, cy)));

        float lightLevel = Math.clamp(verts.norm.dot(lightVec), 0.1f, 1);

        for (int y = minY; y <= maxY; y++) {
            span.reset();

            intersectEdge(ax, ay, bx, by, y, span);
            intersectEdge(bx, by, cx, cy, y, span);
            intersectEdge(cx, cy, ax, ay, y, span);

            if (!span.isValid()) continue;

            int left  = Math.max(0, span.left);
            int right = Math.min(TotallyLegit.width - 1, span.right);
            if (left > right) continue;

            for (int x = left - 1; x <= right + 1; x++) {
                float px = x;
                float py = y;

                float wA = edge(px, py, bx, by, cx, cy) * oneOverArea;
                float wB = edge(px, py, cx, cy, ax, ay) * oneOverArea;
                float wC = 1.0f - wA - wB;

                if (wA >= 0 && wB >= 0 && wC >= 0) {
                    float z = wA / az + wB / bz + wC / cz;

                    int idx = y * TotallyLegit.width + x;
                    if (z < TotallyLegit.depth[idx]) {
                        TotallyLegit.setRGBFast(idx, TotallyLegit.argb(255, (int) (lightLevel * 255), (int) (lightLevel * 255), (int) (lightLevel*255)));
                        TotallyLegit.setDepthFast(idx, z);
                    }
                }
            }
        }
    }

    private static void intersectEdge(int x0, int y0, int x1, int y1, int scanY, Span span) {
        if (y0 == y1) {
            span.update(x0);
            return;
        }

        if (y0 > y1) {
            int tx = x0; x0 = x1; x1 = tx;
            int ty = y0; y0 = y1; y1 = ty;
        }

        if (scanY < y0 || scanY >= y1) return;

        int dy = y1 - y0;
        int dx = x1 - x0;

        int x = x0 + (dx * (scanY - y0) + dy - 1) / dy;

        span.update(x);
    }

    private static float edge(int x0, int y0, int x1, int y1, int x2, int y2) {
        return (float)(x1 - x0) * (y2 - y0) - (float)(y1 - y0) * (x2 - x0);
    }
    private static float edge(float x0, float y0, float x1, float y1, float x2, float y2) {
        return (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0);
    }
}