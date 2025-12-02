import math.Vec4;
import rasterizer.Mesh;
import rasterizer.Tri;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class STLLoader {

    public static Mesh load(String filename) throws IOException {
        File file = new File(filename);
        byte[] allBytes = Files.readAllBytes(file.toPath());

        String header = new String(allBytes, 0, Math.min(256, allBytes.length), StandardCharsets.UTF_8);
        if (header.startsWith("solid ")) {
            return loadAscii(header, allBytes);
        } else {
            return loadBinary(allBytes);
        }
    }

    private static Mesh loadBinary(byte[] data) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        buf.position(80);
        int triangleCount = buf.getInt();

        ArrayList<Tri> tris = new ArrayList<>(triangleCount);

        for (int i = 0; i < triangleCount; i++) {
            buf.getFloat(); buf.getFloat(); buf.getFloat();

            Vec4 a = new Vec4(buf.getFloat(), buf.getFloat(), buf.getFloat(), 1f);
            Vec4 b = new Vec4(buf.getFloat(), buf.getFloat(), buf.getFloat(), 1f);
            Vec4 c = new Vec4(buf.getFloat(), buf.getFloat(), buf.getFloat(), 1f);

            tris.add(new Tri(a, b, c));

            buf.getShort();
        }

        return new Mesh(tris.toArray(new Tri[0]));
    }

    private static Mesh loadAscii(String headerLine, byte[] data) throws IOException {
        String content = new String(data, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");

        ArrayList<Tri> tris = new ArrayList<>();

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
