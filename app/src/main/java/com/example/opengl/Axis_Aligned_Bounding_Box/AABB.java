package com.example.opengl.Axis_Aligned_Bounding_Box;

public class AABB {
    /*
     * This class represents an axis-aligned bounding box.
     */
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

    public Point getCenter() {
        return new Point(
                (min.x + max.x) / 2,
                (min.y + max.y) / 2,
                (min.z + max.z) / 2
        );
    }

    float getSurfaceArea() {
        return 2.0f * ((max.x - min.x) * (max.y - min.y) +
                (max.x - min.x) * (max.z - min.z) +
                (max.y - min.y) * (max.z - min.z));
    }

    public float[] getMesh() {
        float[][] vertices = {
                {min.x, min.y, min.z},
                {max.x, min.y, min.z},
                {max.x, max.y, min.z},
                {min.x, max.y, min.z},
                {min.x, min.y, max.z},
                {max.x, min.y, max.z},
                {max.x, max.y, max.z},
                {min.x, max.y, max.z},
        };

        int[][] triangles = {
                {0, 1, 2}, {0, 2, 3},
                {4, 5, 6}, {4, 6, 7},
                {0, 3, 7}, {0, 7, 4},
                {1, 2, 6}, {1, 6, 5},
                {3, 2, 6}, {3, 6, 7},
                {0, 1, 5}, {0, 5, 4},
        };

        float[] mesh = new float[triangles.length * 3 * 3];
        int index = 0;
        for (int[] triangle: triangles) {
            for (int vertexIndex : triangle) {
                float[] vertex = vertices[vertexIndex];
                mesh[index++] = vertex[0];
                mesh[index++] = vertex[1];
                mesh[index++] = vertex[2];
            }
        }

        return mesh;
    }

    public float[] getLineMesh() {
        float[][] vertices = {
                {min.x, min.y, min.z},
                {max.x, min.y, min.z},
                {max.x, max.y, min.z},
                {min.x, max.y, min.z},
                {min.x, min.y, max.z},
                {max.x, min.y, max.z},
                {max.x, max.y, max.z},
                {min.x, max.y, max.z},
        };

        int[][] lines = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        float[] mesh = new float[lines.length * 2 * 3];
        int index = 0;
        for (int[] line : lines) {
            for (int vertexIndex : line) {
                float[] vertex = vertices[vertexIndex];
                mesh[index++] = vertex[0];
                mesh[index++] = vertex[1];
                mesh[index++] = vertex[2];
            }
        }

        return mesh;
    }

    AABB expand(AABB other){
        return new AABB(
            new Point(
                Math.min(min.x, other.min.x),
                Math.min(min.y, other.min.y),
                Math.min(min.z, other.min.z)
            ),
            new Point(
                Math.max(max.x, other.max.x),
                Math.max(max.y, other.max.y),
                Math.max(max.z, other.max.z)
            )
        );
    }

    boolean checkIntersectionWithRay(Ray r) {
        float tMinByX, tMaxByX;
        if (r.direction.x == 0) {
            if (r.origin.x < min.x || r.origin.x > max.x) return false;
            tMinByX = Float.NEGATIVE_INFINITY;
            tMaxByX = Float.POSITIVE_INFINITY;
        } else {
            tMinByX = (min.x - r.origin.x) / r.direction.x;
            tMaxByX = (max.x - r.origin.x) / r.direction.x;
            if (r.direction.x < 0) { float temp = tMinByX; tMinByX = tMaxByX; tMaxByX = temp; }
        }

        float tMinByY, tMaxByY;
        if (r.direction.y == 0) {
            if (r.origin.y < min.y || r.origin.y > max.y) return false;
            tMinByY = Float.NEGATIVE_INFINITY;
            tMaxByY = Float.POSITIVE_INFINITY;
        } else {
            tMinByY = (min.y - r.origin.y) / r.direction.y;
            tMaxByY = (max.y - r.origin.y) / r.direction.y;
            if (r.direction.y < 0) { float temp = tMinByY; tMinByY = tMaxByY; tMaxByY = temp; }
        }

        float tMinByZ, tMaxByZ;
        if (r.direction.z == 0) {
            if (r.origin.z < min.z || r.origin.z > max.z) return false;
            tMinByZ = Float.NEGATIVE_INFINITY;
            tMaxByZ = Float.POSITIVE_INFINITY;
        } else {
            tMinByZ = (min.z - r.origin.z) / r.direction.z;
            tMaxByZ = (max.z - r.origin.z) / r.direction.z;
            if (r.direction.z < 0) { float temp = tMinByZ; tMinByZ = tMaxByZ; tMaxByZ = temp; }
        }

        float tMin = Math.max(Math.max(tMinByX, tMinByY), tMinByZ);
        float tMax = Math.min(Math.min(tMaxByX, tMaxByY), tMaxByZ);

        if (tMax < 0 || tMax < tMin) return false;

        return true;
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

    @Override
    public String toString() {
        return "AABB{" +
                "min = " + min.toString() +
                ", max = " + max.toString() +
                '}';
    }
}
