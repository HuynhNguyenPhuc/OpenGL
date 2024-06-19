package com.example.opengl.Axis_Aligned_Bounding_Box;

public class AABB {
    public final Point min;
    public final Point max;

    AABB(Point min, Point max){
        this.min = min;
        this.max = max;
    }

    AABB(float[] min, float[] max){
        this.min = new Point(min);
        this.max = new Point(max);
    }

    Point[] getIntersectionWithRay(Ray r) {
        float tMinByX, tMaxByX;
        if (r.direction.x == 0) {
            if (r.origin.x < min.x || r.origin.x > max.x) return null;
            tMinByX = Float.NEGATIVE_INFINITY;
            tMaxByX = Float.POSITIVE_INFINITY;
        } else {
            tMinByX = (min.x - r.origin.x) / r.direction.x;
            tMaxByX = (max.x - r.origin.x) / r.direction.x;
            if (r.direction.x < 0) { float temp = tMinByX; tMinByX = tMaxByX; tMaxByX = temp; }
        }

        float tMinByY, tMaxByY;
        if (r.direction.y == 0) {
            if (r.origin.y < min.y || r.origin.y > max.y) return null;
            tMinByY = Float.NEGATIVE_INFINITY;
            tMaxByY = Float.POSITIVE_INFINITY;
        } else {
            tMinByY = (min.y - r.origin.y) / r.direction.y;
            tMaxByY = (max.y - r.origin.y) / r.direction.y;
            if (r.direction.y < 0) { float temp = tMinByY; tMinByY = tMaxByY; tMaxByY = temp; }
        }

        float tMinByZ, tMaxByZ;
        if (r.direction.z == 0) {
            if (r.origin.z < min.z || r.origin.z > max.z) return null;
            tMinByZ = Float.NEGATIVE_INFINITY;
            tMaxByZ = Float.POSITIVE_INFINITY;
        } else {
            tMinByZ = (min.z - r.origin.z) / r.direction.z;
            tMaxByZ = (max.z - r.origin.z) / r.direction.z;
            if (r.direction.z < 0) { float temp = tMinByZ; tMinByZ = tMaxByZ; tMaxByZ = temp; }
        }

        float tMin = Math.max(Math.max(tMinByX, tMinByY), tMinByZ);
        float tMax = Math.min(Math.min(tMaxByX, tMaxByY), tMaxByZ);

        if (tMax < 0 || tMax < tMin) return null;

        Point[] points;
        if (tMin < 0 || tMin == tMax) {
            points = new Point[1];
            points[0] = new Point(r.origin.x + tMax * r.direction.x, r.origin.y + tMax * r.direction.y, r.origin.z + tMax * r.direction.z);
        }
        else {
            points = new Point[2];
            points[0] = new Point(r.origin.x + tMin * r.direction.x, r.origin.y + tMin * r.direction.y, r.origin.z + tMin * r.direction.z);
            points[1] = new Point(r.origin.x + tMax * r.direction.x, r.origin.y + tMax * r.direction.y, r.origin.z + tMax * r.direction.z);
        }
        return points;
    }

    boolean checkCollision(AABB other){
        boolean x = min.x > other.max.x || other.min.x > max.x;
        if (x) return false;
        boolean y = min.y > other.max.y || other.min.y > max.y;
        if (y) return false;
        boolean z = min.z > other.max.z || other.min.z > max.z;
        return !z;
    }

    float[] checkSweptCollision(AABB other, Vector direction, float t){
        float tMinByX, tMaxByX;
        if (direction.x == 0) {
            if (max.x < other.min.x || other.max.x < min.x) return null;
            tMinByX = Float.NEGATIVE_INFINITY;
            tMaxByX = Float.POSITIVE_INFINITY;
        } else {
            tMinByX = (other.min.x - max.x) / direction.x;
            tMaxByX = (other.max.x - min.x) / direction.x;
            if (direction.x < 0) { float temp = tMinByX; tMinByX = tMaxByX; tMaxByX = temp; }
        }

        float tMinByY, tMaxByY;
        if (direction.y == 0) {
            if (max.y < other.min.y || other.max.y < min.y) return null;
            tMinByY = Float.NEGATIVE_INFINITY;
            tMaxByY = Float.POSITIVE_INFINITY;
        } else {
            tMinByY = (other.min.y - max.y) / direction.y;
            tMaxByY = (other.max.y - min.y) / direction.y;
            if (direction.y < 0) { float temp = tMinByY; tMinByY = tMaxByY; tMaxByY = temp; }
        }

        float tMinByZ, tMaxByZ;
        if (direction.z == 0) {
            if (max.z < other.min.z || other.max.z < min.z) return null;
            tMinByZ = Float.NEGATIVE_INFINITY;
            tMaxByZ = Float.POSITIVE_INFINITY;
        } else {
            tMinByZ = (other.min.z - max.z) / direction.z;
            tMaxByZ = (other.max.z - min.z) / direction.z;
            if (direction.z < 0) { float temp = tMinByZ; tMinByZ = tMaxByZ; tMaxByZ = temp; }
        }

        float tMin = Math.max(Math.max(tMinByX, tMinByY), tMinByZ);
        float tMax = Math.min(Math.min(tMaxByX, tMaxByY), tMaxByZ);

        if (tMax < 0 || tMin > t || tMax < tMin) return null;
        if (tMax > t) tMax = t;
        if (tMin < 0) tMin = 0;
        if (tMin == tMax) return new float[]{tMin};
        return new float[] {tMin, tMax};
    }
}
