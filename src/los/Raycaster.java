package los;

import math.Matrix4;
import math.Vec4;
import world.Mesh;
import world.Tri;

import java.util.List;

public final class Raycaster {

    public static final class RaycastResult {
        public boolean clear;
        public boolean hit;
        public Vec4 where;      // world-space hit point (you own this Vec4)
        public Mesh mesh;
        public float distance;  // actual distance to hit or to target
        public float t;         // [0,1] along ray

        public RaycastResult() {
            clear = true;
            hit = false;
            where = new Vec4();
            mesh = null;
            distance = 0f;
            t = 1f;
        }

        @Override
        public String toString() {
            return clear ? "CLEAR" : ("BLOCKED by " + mesh + " at " + where + " (t=" + t + ")");
        }
    }

    // Reusable temps — never allocated after class load
    private static final Vec4 dir    = new Vec4();
    private static final Vec4 v0     = new Vec4();
    private static final Vec4 v1     = new Vec4();
    private static final Vec4 v2     = new Vec4();
    private static final Vec4 e1     = new Vec4();
    private static final Vec4 e2     = new Vec4();
    private static final Vec4 h      = new Vec4();
    private static final Vec4 s      = new Vec4();
    private static final Vec4 q      = new Vec4();
    private static final Vec4 temp   = new Vec4(); // for hit point calc

    /**
     * Performs line-of-sight check.
     * Writes result into `out` (reuse your own RaycastResult to avoid GC).
     */
    public static void lineOfSight(Vec4 from, Vec4 to, List<Mesh> worldMeshes, float maxRange, RaycastResult out) {
        // Compute direction and length
        to.sub(from, dir);
        float fullDistance = dir.magnitude();
        if (fullDistance > maxRange) {
            out.clear = false;
            return;
        }
        if (fullDistance < 0.001f) {
            return; // same point
        }

        dir.normalizeSelf();

        float closestT = Float.MAX_VALUE;
        Mesh closestMesh = null;
        Vec4 closestHit = out.where;

        for (Mesh mesh : worldMeshes) {
            if (mesh == null) continue;
            Matrix4 M = mesh.getModelMatrix();

            for (Tri tri : mesh.tris) {
                // Transform vertices to world space
                tri.a.transform(M, v0);
                tri.b.transform(M, v1);
                tri.c.transform(M, v2);

                // Edges from v0
                v1.sub(v0, e1);
                v2.sub(v0, e2);

                // Möller–Trumbore
                dir.cross(e2, h);
                float det = e1.dot(h);
                if (det > -0.00001f && det < 0.00001f) continue; // parallel

                float invDet = 1f / det;
                from.sub(v0, s);                    // vector from v0 to ray origin

                float u = invDet * s.dot(h);
                if (u < 0f || u > 1f) continue;

                s.cross(e1, q);
                float v = invDet * dir.dot(q);
                if (v < 0f || u + v > 1f) continue;

                float t = invDet * e2.dot(q);
                if (t > 0.05f && t < closestT && t < fullDistance * 0.99f) {
                    closestT = t;
                    closestMesh = mesh;

                    // Compute world-space hit point: from + dir * t
                    dir.scale(t, temp);
                    from.add(temp, closestHit); // creates new Vec4 — but we reuse below
                }
            }
        }

        if (closestMesh != null) {
            out.clear = false;
            out.hit = true;
            out.mesh = closestMesh;
            out.distance = closestT;
            out.t = closestT / fullDistance;
            out.where = closestHit; // you now own this Vec4
        } else {
            out.clear = true;
            out.distance = fullDistance;
            out.t = 1f;
        }
    }
}