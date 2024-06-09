package com.example.opengl.Hackathon;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MeshHelper {
    protected float[] rayPosition;
    protected float[] rayDirection;

    public MeshHelper(float[] rayPosition, float[] rayDirection) {
        this.rayPosition = rayPosition;
        this.rayDirection = rayDirection;
    }
}

class SphereMeshHelper extends MeshHelper {
    private float[] center;
    private float radius;

    public SphereMeshHelper(float[] rayPosition, float[] rayDirection, float[] center, float radius) {
        super(rayPosition, rayDirection);
        this.center = center;
        this.radius = radius;
    }

    public List<float[]> getIntersectionPoints() {
        List<float[]>intersectionPoints = new ArrayList<>();
        float a = 0.0f; float b = 0.0f; float c = 0.0f;
        for (int i = 0; i < 3; i++){
            a += rayDirection[i] * rayDirection[i];
            b += 2.0f * (rayPosition[i] - center[i]) * rayDirection[i];
            c += (rayPosition[i] - center[i]) * (rayPosition[i] - center[i]);
        }
        c -= radius * radius;
        float[] t = new float[2];
        float discriminant = b * b - 4.0f * a * c;
        if (discriminant < 0) {
            return intersectionPoints;
        }
        t[0] = (-1.0f * b + (float)Math.sqrt(discriminant)) / (2.0f * a);
        t[1] = (-1.0f * b - (float)Math.sqrt(discriminant)) / (2.0f * a);
        for (int i = 0; i < 2; i++){
            Log.d("MeshHelper", "t[" + i + "] = " + t[i]);
            if (t[i] > 0) {
                float[] intersectionPoint = new float[3];
                for (int j = 0; j < 3; j++) {
                    intersectionPoint[j] = rayPosition[j] + t[i] * rayDirection[j];
                }
                intersectionPoints.add(intersectionPoint);
            }
        }
        return intersectionPoints;
    }

    public boolean isInsideSphere(float[] p) {
        float distance = 0.0f;
        for (int i = 0; i < 3; i++) {
            distance += (p[i] - center[i]) * (p[i] - center[i]);
        }
        return distance <= radius;
    }
}

class PlaneMeshHelper extends MeshHelper {
    private float[] a;
    private float[] b;
    private float[] c;
    private float[] intersectionPoint;

    public PlaneMeshHelper(float[] rayPosition, float[] rayDirection, float[] a, float[] b, float[] c) {
        super(rayPosition, rayDirection);
        this.a = a;
        this.b = b;
        this.c = c;
        this.intersectionPoint = null;
    }

    public float[] getIntersectionPoints() {
        float[] ab = new float[]{b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        float[] ac = new float[]{c[0] - a[0], c[1] - a[1], c[2] - a[2]};
        float[] normal = new float[]{
                ab[1] * ac[2] - ab[2] * ac[1],
                ab[2] * ac[0] - ab[0] * ac[2],
                ab[0] * ac[1] - ab[1] * ac[0]
        };

        float dotProduct = rayDirection[0] * normal[0] + rayDirection[1] * normal[1] + rayDirection[2] * normal[2];

        if (Math.abs(dotProduct) < 1e-6) return null;

        float distance = (normal[0] * (a[0] - rayPosition[0]) +
                normal[1] * (a[1] - rayPosition[1]) +
                normal[2] * (a[2] - rayPosition[2])) / dotProduct;

        if (distance < 0) return null;

        this.intersectionPoint = new float[]{
                rayPosition[0] + rayDirection[0] * distance,
                rayPosition[1] + rayDirection[1] * distance,
                rayPosition[2] + rayDirection[2] * distance
        };

        return intersectionPoint;
    }

    public float[] getBarycentricCoordinates(float[] p) {
        float[] v0 = new float[]{a[0] - p[0], a[1] - p[1], a[2] - p[2]};
        float[] v1 = new float[]{b[0] - p[0], b[1] - p[1], b[2] - p[2]};
        float[] v2 = new float[]{c[0] - p[0], c[1] - p[1], c[2] - p[2]};

        float[] ab = new float[]{b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        float[] ac = new float[]{c[0] - a[0], c[1] - a[1], c[2] - a[2]};

        float sABC = s(ab, ac);
        float sPAB = s(v0, v1);
        float sPBC = s(v1, v2);
        float sPCA = s(v2, v0);

        return new float[]{sPAB / sABC, sPBC / sABC, sPCA / sABC};
    }

    public boolean isInsideTriangle(float[] p) {
        if (intersectionPoint == null) return false;
        float[] barycentricCoordinates = getBarycentricCoordinates(p);
        Log.d("MeshHelper", "barycentricCoordinates: " + barycentricCoordinates[0] + ", " + barycentricCoordinates[1] + ", " + barycentricCoordinates[2]);
        return barycentricCoordinates[0] <= 1 && barycentricCoordinates[1] <= 1 && barycentricCoordinates[2] <= 1 &&
                (barycentricCoordinates[0] + barycentricCoordinates[1] + barycentricCoordinates[2]- 1.0f < 1e-6);
    }

    private float dotProduct(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    private float[] crossProduct(float[] a, float[] b) {
        float[] result = new float[3];
        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];
        return result;
    }

    private float s(float[] a, float[] b) {
        float[] cross = crossProduct(a, b);
        float magnitude = (float) Math.sqrt(dotProduct(cross, cross));
        return magnitude / 2.0f;
    }
}