package rasterizer;

import math.Vec4;

public class PixelShader {
    private final Vec4 crossBuffer = new Vec4();
    private final Vec4 viewBuffer = new Vec4();
    public final VertexShader vertexShader = new VertexShader();
    private final VertexShader.VertExport verts = new VertexShader.VertExport(crossBuffer, viewBuffer);

    private final Vec4 lightVec = new Vec4(0, -1, -0.5f, 0);
    private int xmin, ymin, xmax, ymax;

    {
        lightVec.normalizeSelf();
    }

    public PixelShader(int xmin, int ymin, int xmax, int ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public void drawMesh(Mesh mesh) {
        vertexShader.loadModel(mesh);

        for (Tri tri : mesh.tris) {
            vertexShader.processTri(tri, verts);

            int minY = Math.min(Math.min(verts.aY, verts.bY), verts.cY);
            int maxY = Math.max(Math.max(verts.aY, verts.bY), verts.cY);

            if (maxY < ymin || minY >= ymax) {
                continue;
            }

            neoDrawTri();
        }

//        for (Tri tri : mesh.tris) {
//            VertexShader.processTri(tri, verts);
//
//            TotallyLegit.drawLine(verts.aX, verts.aY, verts.bX, verts.bY, black);
//            TotallyLegit.drawLine(verts.bX, verts.bY, verts.cX, verts.cY, black);
//            TotallyLegit.drawLine(verts.cX, verts.cY, verts.aX, verts.aY, black);
//        }
    }

//    private void neoDrawTri() {
//        if (verts.norm.dot(verts.viewA) <= -0.1) {
//            return;
//        }
//
//        // a has "priority" for being top. if aY = bY, a wins. if aY = cY, a wins.
//        int isAMax = (verts.aY >= verts.bY && verts.aY >= verts.cY) ? 1 : 0;
//        int isBMax = (verts.bY > verts.aY && verts.bY > verts.cY) ? 1 : 0;
//        int isCMax = 1 - isAMax - isBMax;
//
//        int isAMin = (verts.aY <= verts.bY && verts.aY <= verts.cY) ? 1 : 0;
//        int isBMin = (verts.bY < verts.aY && verts.bY < verts.cY) ? 1 : 0;
//        int isCMin = 1 - isAMin - isBMin;
//
//        int isAMid = 1 - isAMax - isAMin;
//        int isBMid = 1 - isBMax - isBMin;
//        int isCMid = 1 - isCMax - isCMin;
//
//        int maxX = isAMax * verts.aX + isBMax * verts.bX + isCMax * verts.cX;
//        int maxY = isAMax * verts.aY + isBMax * verts.bY + isCMax * verts.cY;
//
//        int midX = isAMid * verts.aX + isBMid * verts.bX + isCMid * verts.cX;
//        int midY = isAMid * verts.aY + isBMid * verts.bY + isCMid * verts.cY;
//
//        int minX = isAMin * verts.aX + isBMin * verts.bX + isCMin * verts.cX;
//        int minY = isAMin * verts.aY + isBMin * verts.bY + isCMin * verts.cY;
//
//        int dx1 = maxX - minX;
//        int dy1 = maxY - minY;
//
//        int dx2 = midX - minX;
//        int dy2 = midY - minY;
//
//        // Cross product: positive means mid is to the left of the min max vector
//        int cross = dx1 * dy2 - dy1 * dx2;
//
//        int isMaxLeft = (cross < 0) ? 1 : 0;
//        int isMidLeft = 1 - isMaxLeft;
//
//        int isMaxRight = 1 - isMaxLeft;
//        int isMidRight = 1 - isMidLeft;
//
//        int leftX = isMaxLeft * maxX + isMidLeft * midX;
//        int leftY = isMaxLeft * maxY + isMidLeft * midY;
//
//        int rightX = isMaxRight * maxX + isMidRight * midX;
//        int rightY = isMaxRight * maxY + isMidRight * midY;
//
//        float invSlopeLeft = (float) (minX - leftX) / (minY - leftY);
//        float invSlopeRight = (float) (minX - rightX) / (minY - rightY);
//
//        float leftBound = minX;
//        float rightBound = minX;
//
//        float lightLevel = Math.clamp(verts.norm.dot(lightVec), 0.1f, 1);
//
//        if (minY != midY) {
//            int yStart = Math.max(ymin, minY);
//            int yEnd = Math.min(ymax, midY);
//
//            leftBound += (yStart - minY) * invSlopeLeft;
//            rightBound += (yStart - minY) * invSlopeRight;
//
//            for (int y = yStart; y < yEnd; ++y) {
//                int xStart = Math.max(xmin, (int) leftBound - 1);
//                int xEnd = Math.min(xmax, (int) rightBound);
//
//                for (int x = xStart; x < xEnd; ++x) {
//                    draw(x, y, lightLevel);
//                }
//
//                leftBound += invSlopeLeft;
//                rightBound += invSlopeRight;
//            }
//        }
//        else {
//            leftBound = Math.min(minX, midX);
//            rightBound = Math.max(minX, midX);
//        }
//
//        invSlopeLeft = (maxX - leftBound) / (maxY - midY);
//        invSlopeRight = (maxX - rightBound) / (maxY - midY);
//
//        int yStart = Math.max(ymin, midY);
//        int yEnd = Math.min(ymax, maxY);
//
//        leftBound += (yStart - midY) * invSlopeLeft;
//        rightBound += (yStart - midY) * invSlopeRight;
//
//        for (int y = yStart; y < yEnd; ++y) {
//            int xStart = (int) Math.max(xmin, leftBound - 1);
//            int xEnd = (int) Math.min(xmax, rightBound);
//
//            for (int x = xStart; x < xEnd; ++x) {
//                draw(x, y, lightLevel);
//            }
//
//            leftBound += invSlopeLeft;
//            rightBound += invSlopeRight;
//        }
//    }

    private void neoDrawTri() {
        if (verts.norm.dot(verts.viewA) <= 0) {
            return;
        }

        // a has "priority" for being top. if aY = bY, a wins. if aY = cY, a wins.
        int isAMax = (verts.aY >= verts.bY && verts.aY >= verts.cY) ? 1 : 0;
        int isBMax = (verts.bY > verts.aY && verts.bY > verts.cY) ? 1 : 0;
        int isCMax = 1 - isAMax - isBMax;

        int isAMin = (verts.aY <= verts.bY && verts.aY <= verts.cY) ? 1 : 0;
        int isBMin = (verts.bY < verts.aY && verts.bY < verts.cY) ? 1 : 0;
        int isCMin = 1 - isAMin - isBMin;

        int isAMid = 1 - isAMax - isAMin;
        int isBMid = 1 - isBMax - isBMin;
        int isCMid = 1 - isCMax - isCMin;

        int maxX = isAMax * verts.aX + isBMax * verts.bX + isCMax * verts.cX;
        int maxY = isAMax * verts.aY + isBMax * verts.bY + isCMax * verts.cY;

        int midX = isAMid * verts.aX + isBMid * verts.bX + isCMid * verts.cX;
        int midY = isAMid * verts.aY + isBMid * verts.bY + isCMid * verts.cY;

        int minX = isAMin * verts.aX + isBMin * verts.bX + isCMin * verts.cX;
        int minY = isAMin * verts.aY + isBMin * verts.bY + isCMin * verts.cY;

        int dx1 = maxX - minX;
        int dy1 = maxY - minY;
        int dx2 = midX - minX;
        int dy2 = midY - minY;
        int cross = dx1 * dy2 - dy1 * dx2;

        int isMaxLeft = (cross < 0) ? 1 : 0;
        int isMidLeft = 1 - isMaxLeft;

        int leftX = (isMaxLeft * maxX + isMidLeft * midX);
        int leftY = (isMaxLeft * maxY + isMidLeft * midY);
        int rightX = ((1 - isMaxLeft) * maxX + (1 - isMidLeft) * midX);
        int rightY = ((1 - isMaxLeft) * maxY + (1 - isMidLeft) * midY);

        float lightLevel = Math.clamp(verts.norm.dot(lightVec), 0.1f, 1);

        if (minY != midY) {
            float invSlopeLeft = (float) (minX - leftX) / (minY - leftY);
            float invSlopeRight = (float) (minX - rightX) / (minY - rightY);

            rasterizeSegment(minX, minX, minY, midY, invSlopeLeft, invSlopeRight, lightLevel);
        }

        float leftBoundAtMid, rightBoundAtMid;
        if (minY != midY) {
            float invSlopeLeft = (float) (minX - leftX) / (minY - leftY);
            float invSlopeRight = (float) (minX - rightX) / (minY - rightY);
            leftBoundAtMid = minX + (midY - minY) * invSlopeLeft;
            rightBoundAtMid = minX + (midY - minY) * invSlopeRight;
        } else {
            leftBoundAtMid = Math.min(minX, midX);
            rightBoundAtMid = Math.max(minX, midX);
        }

        float invSlopeLeft2 = (maxX - leftBoundAtMid) / (maxY - midY);
        float invSlopeRight2 = (maxX - rightBoundAtMid) / (maxY - midY);

        rasterizeSegment(leftBoundAtMid, rightBoundAtMid, midY, maxY, invSlopeLeft2, invSlopeRight2, lightLevel);
    }

    private void rasterizeSegment(float startLeftX, float startRightX, int startY, int endY,
                                  float invSlopeLeft, float invSlopeRight,
                                  float lightLevel) {
        int yStart = Math.max(ymin, startY);
        int yEnd = Math.min(ymax, endY);

        float leftBound = startLeftX + (yStart - startY) * invSlopeLeft;
        float rightBound = startRightX + (yStart - startY) * invSlopeRight;

        for (int y = yStart; y < yEnd; ++y) {
            int xStart = Math.max(xmin, (int) leftBound - 1);
            int xEnd = Math.min(xmax, (int) rightBound);

            for (int x = xStart; x < xEnd; ++x) {
                draw(x, y, lightLevel);
            }

            leftBound += invSlopeLeft;
            rightBound += invSlopeRight;
        }
    }

    private void draw(int x, int y, float lightLevel) {
        float z = 1f/barycentric(x, y,
                verts.aX, verts.aY, verts.aZ,
                verts.bX, verts.bY, verts.bZ,
                verts.cX, verts.cY, verts.cZ
        );

        int idx = y * TotallyLegit.width + x;
        if (z < TotallyLegit.depth[idx]) {
            TotallyLegit.setRGBFast(idx, TotallyLegit.argb(255, (int) (lightLevel * 0), (int) (lightLevel * 255), (int) (lightLevel * 255)));
            TotallyLegit.setDepthFast(idx, z);
        }
    }

    private float barycentric(int x, int y,
                                     int aX, int aY, float aZ,
                                     int bX, int bY, float bZ,
                                     int cX, int cY, float cZ) {
        // Compute vectors from A
        int v0x = cX - aX;
        int v0y = cY - aY;
        int v1x = bX - aX;
        int v1y = bY - aY;
        int v2x = x - aX;
        int v2y = y - aY;

        // Compute dot products
        int dot00 = v0x * v0x + v0y * v0y;
        int dot01 = v0x * v1x + v0y * v1y;
        int dot02 = v0x * v2x + v0y * v2y;
        int dot11 = v1x * v1x + v1y * v1y;
        int dot12 = v1x * v2x + v1y * v2y;

        // Compute barycentric coordinates
        float invDenom = 1.0f / (dot00 * dot11 - dot01 * dot01);
        float w_c = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float w_b = (dot00 * dot12 - dot01 * dot02) * invDenom;
        float w_a = 1.0f - w_b - w_c;

        // Interpolate Z
        return w_a * aZ + w_b * bZ + w_c * cZ;
    }
}