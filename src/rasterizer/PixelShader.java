package rasterizer;

import math.Vec4;
import world.UVTexture;

public class PixelShader {
    private final BarycentricWeights weights = new BarycentricWeights(0, 0, 0);
    private final Vec4 lightVec = new Vec4(0, -1, -0.2f, 0);
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

    public void drawVerts(VertexShader.VertExport verts, UVTexture texture, boolean backfaceCulling) {
            int minY = Math.min(Math.min(verts.aY, verts.bY), verts.cY);
            int maxY = Math.max(Math.max(verts.aY, verts.bY), verts.cY);

            if (maxY < ymin || minY >= ymax) {
                return;
            }
            if (verts.aInvZ <= 0 || verts.bInvZ <= 0 || verts.cInvZ <= 0) {
                return;
            }

            drawVertsPriv(verts, texture, backfaceCulling);
    }

    private void drawVertsPriv(VertexShader.VertExport verts, UVTexture texture, boolean backfaceCulling) {
        if (backfaceCulling && verts.norm.dot(verts.viewA) <= 0) {
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

        float dot = verts.norm.dot(lightVec);
        dot = backfaceCulling ? dot : Math.abs(dot);
        float lightLevel = Math.clamp(dot, 0.1f, 1);

        if (minY != midY) {
            float invSlopeLeft = (float) (minX - leftX) / (minY - leftY);
            float invSlopeRight = (float) (minX - rightX) / (minY - rightY);

            rasterizeSegment(verts, minX, minX, minY, midY, invSlopeLeft, invSlopeRight, lightLevel, texture);
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

        rasterizeSegment(verts, leftBoundAtMid, rightBoundAtMid, midY, maxY, invSlopeLeft2, invSlopeRight2, lightLevel, texture);
    }

    private void rasterizeSegment(VertexShader.VertExport verts, float startLeftX, float startRightX, int startY, int endY,
                                  float invSlopeLeft, float invSlopeRight,
                                  float lightLevel, UVTexture texture) {
        int yStart = Math.max(ymin, startY);
        int yEnd = Math.min(ymax, endY);

        float leftBound = startLeftX + (yStart - startY) * invSlopeLeft;
        float rightBound = startRightX + (yStart - startY) * invSlopeRight;

        for (int y = yStart; y < yEnd; ++y) {
            int xStart = Math.max(xmin, (int) leftBound);
            int xEnd = Math.min(xmax, (int) Math.ceil(rightBound));

            int span = xEnd - xStart;
            if (span <= 0) {
                // Nothing to draw on this scanline, skip remaining calculations
                leftBound += invSlopeLeft;
                rightBound += invSlopeRight;
                continue;
            }

            getBarycentricWeights(xStart, y,
                    verts.aX, verts.aY,
                    verts.bX, verts.bY,
                    verts.cX, verts.cY);

            float invZ = weightedAverage(verts.aInvZ, weights.w_a, verts.bInvZ, weights.w_b, verts.cInvZ, weights.w_c);
            float UinvZ = weightedAverage(verts.aUinvZ, weights.w_a, verts.bUinvZ, weights.w_b, verts.cUinvZ, weights.w_c);
            float VinvZ = weightedAverage(verts.aVinvZ, weights.w_a, verts.bVinvZ, weights.w_b, verts.cVinvZ, weights.w_c);

            getBarycentricWeights(xEnd - 1, y,
                    verts.aX, verts.aY,
                    verts.bX, verts.bY,
                    verts.cX, verts.cY);

            float invZEnd = weightedAverage(verts.aInvZ, weights.w_a, verts.bInvZ, weights.w_b, verts.cInvZ, weights.w_c);
            float UinvZEnd = weightedAverage(verts.aUinvZ, weights.w_a, verts.bUinvZ, weights.w_b, verts.cUinvZ, weights.w_c);
            float VinvZEnd = weightedAverage(verts.aVinvZ, weights.w_a, verts.bVinvZ, weights.w_b, verts.cVinvZ, weights.w_c);

            float invZInc = (invZEnd-invZ)/(xEnd-xStart);
            float UinvZInc = (UinvZEnd-UinvZ)/(xEnd-xStart);
            float VinvZInc = (VinvZEnd-VinvZ)/(xEnd-xStart);

            for (int x = xStart; x < xEnd; ++x) {
                draw(x, y, UinvZ, VinvZ, invZ, lightLevel, texture);
                invZ += invZInc;
                UinvZ += UinvZInc;
                VinvZ += VinvZInc;
            }

            leftBound += invSlopeLeft;
            rightBound += invSlopeRight;
        }
    }

    public class BarycentricWeights {
        public float w_a;
        public float w_b;
        public float w_c;

        public BarycentricWeights(float wa, float wb, float wc) {
            this.w_a = wa;
            this.w_b = wb;
            this.w_c = wc;
        }

        @Override
        public String toString() {
            return "w_a: " + w_a + " w_b: " + w_b +" w_c: " + w_c;
        }
    }

    private void getBarycentricWeights(int x, int y, int ax, int ay, int bx, int by, int cx, int cy) {
        long area = (long)(bx - ax) * (cy - ay) - (long)(cx - ax) * (by - ay);
        if (area == 0) {
            weights.w_a = weights.w_b = weights.w_c = 0;
            return;
        }

        long a = (long)(bx - x) * (cy - y) - (long)(cx - x) * (by - y);
        long b = (long)(cx - x) * (ay - y) - (long)(ax - x) * (cy - y);
        long c = (long)(ax - x) * (by - y) - (long)(bx - x) * (ay - y);

        weights.w_a = (float)a / area;
        weights.w_b = (float)b / area;
        weights.w_c = (float)c / area;
    }

    private float weightedAverage(float a, float aW, float b, float bW, float c, float cW) {
        return a*aW + b*bW + c*cW;
    }

    private void draw(int x, int y, float UinvZ, float VinvZ, float invZ, float lightLevel, UVTexture texture) {
        int idx = TotallyLegit.getPixelLocation(x, y);
        if (invZ > TotallyLegit.depth[idx]) {
            TotallyLegit.setRGBFast(idx, dimARGB(texture.getRGBbyUV(UinvZ/invZ, VinvZ/invZ), lightLevel));
            TotallyLegit.setDepthFast(idx, invZ);
        }
    }

    public static int dimARGB(int argb, float factor) {
        int r = (int) ((((argb >> 16) & 0xFF) * factor) + 0.5f); // +0.5 for rounding
        int g = (int) ((((argb >> 8) & 0xFF) * factor) + 0.5f);
        int b = (int) (((argb & 0xFF) * factor) + 0.5f);

        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    class ARGBDimmer {
        private static final int[] MUL_TABLE = new int[256 * 256];
        {
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    MUL_TABLE[(j << 8) | i] = (i * j) >> 8;  // fixed-point multiply
                }
            }
        }

        public static int dimARGB(int argb, float factor) {
            int f = (int)(factor * 255.0f + 0.5f);
            if (f >= 255) return argb;
            if (f <= 0)   return argb & 0xFF000000;

            int r = (argb >> 16) & 0xFF;
            int g = (argb >>  8) & 0xFF;
            int b =  argb        & 0xFF;

            r = MUL_TABLE[(f << 8) | r];
            g = MUL_TABLE[(f << 8) | g];
            b = MUL_TABLE[(f << 8) | b];

            return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }
}