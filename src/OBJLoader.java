import math.Vec4;
import rasterizer.Mesh;
import rasterizer.Tri;
import rasterizer.UVCoord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import static rasterizer.UVCoord.DUMMY_UV;

public class OBJLoader {
    public static Mesh load(String filename) throws IOException {
        File file = new File(filename);
        byte[] allBytes = Files.readAllBytes(file.toPath());

        String header = new String(allBytes, 0, Math.min(256, allBytes.length), StandardCharsets.UTF_8);
        return loadAscii(header, allBytes);

    }

    private static int objParseInt(String s) {
        return s.isEmpty() ? -1 : Integer.parseInt(s) - 1;
    }

    private static Mesh loadAscii(String headerLine, byte[] data) throws IOException {
        String content = new String(data, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");

        // these are basically made immutable in the code, although it's not actually enforced.
        // TODO: maybe look into making them enforcably immutable?.
        ArrayList<Vec4> vectors = new ArrayList<>();
        ArrayList<UVCoord> uvCoordinates = new ArrayList<>();
        ArrayList<Tri> tris = new ArrayList<>();

        for (String line : lines) {
            String[] things = line.split("\\s+");
            if (things.length < 1) continue;
            switch(things[0]) {
                case "v":
                    float x = Float.parseFloat(things[1]);
                    float y = Float.parseFloat(things[2]);
                    float z = Float.parseFloat(things[3]);
                    float w = 1.0f; // we can write code to parse this, but I don't really see the value.
                    vectors.add(new Vec4(x, y, z, w));
                    break;
                case "vt":
                    float u = Float.parseFloat(things[1]);
                    float v = 0.0f;
                    if (things.length >= 3) {
                        v = Float.parseFloat(things[2]);
                    }
                    float w1 = 0.0f;
                    if (things.length >= 4) {
                        w1 = Float.parseFloat(things[3]);
                    }
                    uvCoordinates.add(new UVCoord(u, v, w1));
                    break;
                case "f":
                    int[][] verts = Arrays.stream(things, 1, things.length)
                            .map(p -> Arrays.stream(p.split("/")).mapToInt(OBJLoader::objParseInt).toArray())
                            .toArray(int[][]::new);
                    int v0 = verts[0][0];
                    int vt0Ind = (verts[0].length >= 2) ? verts[0][1] : -1;
                    UVCoord vt0 = vt0Ind >= 0 ? uvCoordinates.get(vt0Ind) : DUMMY_UV;
                    // int vn0 = (verts[0].length >= 3) ? verts[0][2] : -1;

                    for (int i = 1; i < verts.length - 1; ++i) {
                        int v1 = verts[i][0];
                        int vt1Ind = (verts[i].length >= 2) ? verts[i][1] : -1;
                        UVCoord vt1 = vt1Ind >= 0 ? uvCoordinates.get(vt1Ind) : DUMMY_UV;
                        int v2 = verts[i+1][0];
                        int vt2Ind = (verts[i+1].length >= 2) ? verts[i+1][1] : -1;
                        UVCoord vt2 = vt2Ind >= 0 ? uvCoordinates.get(vt2Ind) : DUMMY_UV;

                        tris.add(new Tri(
                                vectors.get(v0), vt0,
                                vectors.get(v1), vt1,
                                vectors.get(v2), vt2
                        ));
                    }
                    break;
//                case "vn":
//                    We can't actually process vertex normals right now since we're just flat shading
//                    and I honestly can't be bothered to figure out how to store it
//                    break;
//                case "vp":
//                    These specify non polygon parametric curves and I don't wanna deal with that shit
//                    break;
//                case "l":
//                    Also free form geometry specification (at least I think it is).
//                    break;
            }
        }


        Vec4[] verts = new Vec4[3];
        int idx = 0;

        for (String line : lines) {
            String trimmed = line.trim().toLowerCase();
            if (trimmed.startsWith("vertex")) {
                String[] parts = trimmed.split("\\s+");
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                float z = Float.parseFloat(parts[3]);
                verts[idx++] = new Vec4(x, y, z, 1f);

                if (idx == 3) {
                    tris.add(new Tri(verts[0], verts[1], verts[2]));
                    idx = 0;
                }
            }
        }

        return new Mesh(tris.toArray(new Tri[0]));
    }
}
