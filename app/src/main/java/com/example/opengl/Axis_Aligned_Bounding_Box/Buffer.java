package com.example.opengl.Axis_Aligned_Bounding_Box;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Buffer {
    public static FloatBuffer getAABBBuffer(AABB[] aabbs){
        AABB aabb = aabbs[0];
        float[] mesh = aabb.getMesh();
        FloatBuffer buffer = ByteBuffer.allocateDirect(mesh.length * aabbs.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        buffer.put(mesh);
        for (int i = 1; i < aabbs.length; i++) {
            aabb = aabbs[i];
            buffer.put(aabb.getMesh());
        }
        buffer.position(0);
        return buffer;
    }

    public static FloatBuffer getRayBuffer(Ray[] rays){
        Ray ray = rays[0];
        float[] mesh = ray.getMesh();
        FloatBuffer buffer = ByteBuffer.allocateDirect(mesh.length * rays.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        buffer.put(mesh);
        for (int i = 1; i < rays.length; i++) {
            ray = rays[i];
            buffer.put(ray.getMesh());
        }
        buffer.position(0);
        return buffer;
    }

    public static FloatBuffer getPointBuffer(Point[] points){
        if (points == null) return ByteBuffer.allocateDirect(0).asFloatBuffer();
        Point point = points[0];
        float[] mesh = point.getMesh();
        FloatBuffer buffer = ByteBuffer.allocateDirect(mesh.length * points.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        buffer.put(mesh);
        for (int i = 1; i < points.length; i++) {
            point = points[i];
            buffer.put(point.getMesh());
        }
        buffer.position(0);
        return buffer;
    }
}
