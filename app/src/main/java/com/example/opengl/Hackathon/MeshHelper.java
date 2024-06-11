package com.example.opengl.Hackathon;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//class Point{
//    private float x;
//    private float y;
//    private float z;
//
//    public Point(float x, float y, float z){
//        this.x = x;
//        this.y = y;
//        this.z = z;
//    }
//}
//
//class Vector{
//    private float x;
//    private float y;
//    private float z;
//
//    public Vector(float x, float y, float z){
//        this.x = x;
//        this.y = y;
//        this.z = z;
//    }
//
//    private float dotProduct(Vector v1, Vector v2){
//        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
//    }
//
//    private Vector crossProduct(Vector a, Vector b){
//        float x = a.y * b.z - a.z * b.y;
//        float y = a.z * b.x - a.x * b.z;
//        float z = a.x * b.y - a.y * b.x;
//        return new Vector(x, y, z);
//    }
//
//    private float s(Vector a, Vector b) {
//        Vector cross = crossProduct(a, b);
//        float magnitude = (float) Math.sqrt(dotProduct(cross, cross));
//        return magnitude / 2.0f;
//    }
//}

public class MeshHelper {
    protected float[] rayPosition;
    protected float[] rayDirection;

    public MeshHelper(float[] rayPosition, float[] rayDirection) {
        this.rayPosition = rayPosition;
        this.rayDirection = rayDirection;
    }

    protected float dotProduct(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    protected float[] crossProduct(float[] a, float[] b) {
        float[] result = new float[3];
        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];
        return result;
    }

    protected float s(float[] a, float[] b) {
        float[] cross = crossProduct(a, b);
        float magnitude = (float) Math.sqrt(dotProduct(cross, cross));
        return magnitude / 2.0f;
    }

    protected float det2(float a00, float a01, float a10, float a11){
        return a00*a11 - a01*a10;
    }
}

class SphereMeshHelper extends MeshHelper {
    private final float[] center;
    private final float radius;

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
    private final float[] a;
    private final float[] b;
    private final float[] c;

    private float[] normal;

    private float[] ab;
    private float[] ac;
    private float[] bc;

    private float[] intersectionPoint;

    public PlaneMeshHelper(float[] rayPosition, float[] rayDirection, float[] a, float[] b, float[] c) {
        super(rayPosition, rayDirection);
        this.a = a;
        this.b = b;
        this.c = c;
        this.intersectionPoint = null;

        ab = new float[]{b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        ac = new float[]{c[0] - a[0], c[1] - a[1], c[2] - a[2]};
        bc = new float[]{c[0] - b[0], c[1] - b[1], c[2] - b[2]};

        this.normal = new float[]{
                ab[1] * ac[2] - ab[2] * ac[1],
                ab[2] * ac[0] - ab[0] * ac[2],
                ab[0] * ac[1] - ab[1] * ac[0]
        };
    }

    public float[] getIntersectionPoints() {
        float dotProduct = rayDirection[0] * normal[0] + rayDirection[1] * normal[1] + rayDirection[2] * normal[2];

        float distance = (normal[0] * (a[0] - rayPosition[0]) +
                normal[1] * (a[1] - rayPosition[1]) +
                normal[2] * (a[2] - rayPosition[2])) / dotProduct;

        if (Math.abs(dotProduct) < 1e-6) return null;

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

        return barycentricCoordinates[0] >= 0 && barycentricCoordinates[1] >= 0 && barycentricCoordinates[2] >= 0 &&
                barycentricCoordinates[0] <= 1 && barycentricCoordinates[1] <= 1 && barycentricCoordinates[2] <= 1 &&
                (barycentricCoordinates[0] + barycentricCoordinates[1] + barycentricCoordinates[2]- 1.0f < 1e-6);
    }

    public float[] getTriangleIntersection(){
        float[] intersectionAB = findVectorIntersection(rayPosition, rayDirection, a, ab);
        float[] intersectionBC = findVectorIntersection(rayPosition, rayDirection, b, bc);
        float[] intersectionAC = findVectorIntersection(rayPosition, rayDirection, a, ac);

        if (intersectionAB != null && intersectionAB.length == 6) return intersectionAB;
        if (intersectionBC != null && intersectionBC.length == 6) return intersectionBC;
        if (intersectionAC != null && intersectionAC.length == 6) return intersectionAC;


        if(intersectionBC != null && intersectionAC != null) {
            if (intersectionBC[0] != intersectionAC[0] || intersectionBC[1] != intersectionAC[1] || intersectionBC[2] != intersectionAC[2]) {
                float[] finalIntersection = new float[]{intersectionBC[0], intersectionBC[1], intersectionBC[2], intersectionAC[0], intersectionAC[1], intersectionAC[2]};
                return finalIntersection;
            } else {
                if (intersectionAB != null) {
                    float[] finalIntersection = new float[]{intersectionAB[0], intersectionAB[1], intersectionAB[2], intersectionAC[0], intersectionAC[1], intersectionAC[2]};
                    return finalIntersection;
                }
            }
        }


        if (intersectionAB != null && intersectionAC != null) {
            if (intersectionAB[0] != intersectionAC[0] || intersectionAB[1] != intersectionAC[1] || intersectionAB[2] != intersectionAC[2]) {
                float[] finalIntersection = new float[]{intersectionAB[0], intersectionAB[1], intersectionAB[2], intersectionAC[0], intersectionAC[1], intersectionAC[2]};
                return finalIntersection;
            } else {
                if (intersectionBC != null) {
                    float[] finalIntersection = new float[]{intersectionBC[0], intersectionBC[1], intersectionBC[2], intersectionAC[0], intersectionAC[1], intersectionAC[2]};
                    return finalIntersection;
                }
            }
        }

        if (intersectionAB != null && intersectionBC != null){
            if (intersectionAB[0] != intersectionBC[0] || intersectionAB[1] != intersectionBC[1] || intersectionAB[2] != intersectionBC[2]) {
                float[] finalIntersection = new float[]{intersectionAB[0], intersectionAB[1], intersectionAB[2], intersectionBC[0], intersectionBC[1], intersectionBC[2]};
                return finalIntersection;
            }
            else{
                if (intersectionAC != null) {
                    float[] finalIntersection = new float[]{intersectionAC[0], intersectionAC[1], intersectionAC[2], intersectionBC[0], intersectionBC[1], intersectionBC[2]};
                    return finalIntersection;
                }
            }
        }

        if (intersectionAB != null) return intersectionAB;
        if (intersectionBC != null) return intersectionBC;
        if (intersectionAC != null) return intersectionAC;

        return null;
    }

    private boolean checkPointInPlane(float[] point) {
        float[] vector = new float[]{this.a[0] - point[0], this.a[1] - point[1], this.a[2] - point[2]};
        if (Math.abs(dotProduct(vector, this.normal)) < 1e-6) return true;
        return false;
    }

    public boolean checkRayInPlane(){
        float[] rayPoint = new float[]{rayPosition[0] + rayDirection[0], rayPosition[1] + rayDirection[1], rayPosition[2] + rayDirection[2]};
        return checkPointInPlane(rayPoint) && checkPointInPlane(rayPosition);
    }

    private float[] findVectorIntersection(float[] raySource, float[] rayDirection, float[] point, float[] direction) {
        float det = det2(rayDirection[0], -direction[0], rayDirection[1] + rayDirection[2], -direction[1] - direction[2]);
        float detX = det2(point[0] - raySource[0], -direction[0], point[1] + point[2] - (raySource[1] + raySource[2]), -direction[1] - direction[2]);
        float detY = det2(rayDirection[0], point[0] - raySource[0], rayDirection[1] + rayDirection[2], point[1] + point[2] - (raySource[1] + raySource[2]));

        if (det != 0) {
            float t1 = detX / det;
            float t2 = detY / det;

            if (t1 < 0 || t2 < 0 || t2 > 1) return null;

            float x = point[0] + t2 * direction[0];
            float y = point[1] + t2 * direction[1];
            float z = point[2] + t2 * direction[2];

            if (z != raySource[2] + t1 * rayDirection[2]) return null;

            return new float[]{x, y, z};
        } else {
            if (detX != 0 || detY != 0) return null;

            boolean checkA = false;
            boolean checkB = false;
            boolean checkO = false;

            float t;

            // Check A in ray direction
            t = ((point[0] + point[1] + point[2]) - (raySource[0] + raySource[1] + raySource[2])) / (rayDirection[0] + rayDirection[1] + rayDirection[2]);

            if (raySource[0] + t * rayDirection[0] == point[0] && raySource[1] + t * rayDirection[1] == point[1] && raySource[2] + t * rayDirection[2] == point[2] && t >= 0)
                checkA = true;

            // Check B in ray direction
            t = ((point[0] + direction[0] + point[1] + direction[1] + point[2] + direction[2]) - (raySource[0] + raySource[1] + raySource[2])) / (rayDirection[0] + rayDirection[1] + rayDirection[2]);

            if (raySource[0] + t * rayDirection[0] == point[0] + direction[0] && raySource[1] + t * rayDirection[1] == point[1] + direction[1] && raySource[2] + t * rayDirection[2] == point[2] + direction[2] && t >= 0)
                checkB = true;

            // Check O in AB
            t = ((raySource[0] + raySource[1] + raySource[2]) - (point[0] + point[1] + point[2])) / (direction[0] + direction[1] + direction[2]);

            if (point[0] + t * direction[0] == raySource[0] && point[1] + t * direction[1] == raySource[1] && point[2] + t * direction[2] == raySource[2] && t >= 0 && t <= 1)
                checkO = true;



            if (checkA && checkB) {
                return new float[]{point[0], point[1], point[2], point[0] + direction[0], point[1] + direction[1], point[2] + direction[2]};
            }
            if (checkA && checkO) return new float[]{point[0], point[1], point[2], raySource[0], raySource[1], raySource[2]};

            if (checkB && checkO)
                return new float[]{point[0] + direction[0], point[1] + direction[1], point[2] + direction[2], raySource[0], raySource[1], raySource[2]};
        }
        return null;
    }
}