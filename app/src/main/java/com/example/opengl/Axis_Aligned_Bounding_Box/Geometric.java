package com.example.opengl.Axis_Aligned_Bounding_Box;

public abstract class Geometric{

}

class Point extends Geometric{
    public final float x;
    public final float y;
    public final float z;

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(float[] coordinates){
        this.x = coordinates[0];
        this.y = coordinates[1];
        this.z = coordinates[2];
    }

    public Point add(Vector vector){
        return new Point(
            x + vector.x,
            y + vector.y,
            z + vector.z
        );
    }

    public Point subtract(Point point){
        return new Point(
            x - point.x,
            y - point.y,
            z - point.z
        );
    }

    public Point subtract(Vector vector){
        return new Point(
            x - vector.x,
            y - vector.y,
            z - vector.z
        );
    }

    public float[] getCoordinates(){ return new float[]{x, y, z}; }

    public Point getCenter(Point point){
        return new Point(
            (x + point.x) / 2,
            (y + point.y) / 2,
            (z + point.z) / 2
        );
    }

    public float[] getMesh(){
        return new float[]{x, y, z};
    }

    @Override
    public String toString() {
        return "Point coordinates: (" + x + ", " + y + ", " + z + ")";
    }
}

class Vector extends Geometric{
    public final float x;
    public final float y;
    public final float z;

    public Vector(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(float[] coordinates){
        this.x = coordinates[0];
        this.y = coordinates[1];
        this.z = coordinates[2];
    }

    public float magnitude(){
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float[] getCoordinates(){ return new float[]{x, y, z}; }

    public Vector normalize(){
        float magnitude = this.magnitude();
        return new Vector(
            x / magnitude,
            y / magnitude,
            z / magnitude
        ).normalize();
    }

    public Vector getNormal(){
        return new Vector(
                2.0f / (x + (float) 1e-6),
                -1.0f / (y + (float) 1e-6),
                -1.0f / (z + (float) 1e-6)
        );
    }

    public Vector negate(){
        return new Vector(
            -x,
            -y,
            -z
        );
    }

    public Vector add(Vector vector){
        return new Vector(
            x + vector.x,
            y + vector.y,
            z + vector.z
        );
    }

    public Vector subtract(Vector vector){
        return new Vector(
            x - vector.x,
            y - vector.y,
            z - vector.z
        );
    }

    public Vector multiply(float scalar){
        return new Vector(
            x * scalar,
            y * scalar,
            z * scalar
        );
    }

    public Vector crossProduct(Vector vector){
        return new Vector(
            y * vector.z - z * vector.y,
            z * vector.x - x * vector.z,
            x * vector.y - y * vector.x
        );
    }

    public float dotProduct(Vector vector){
        return x * vector.x + y * vector.y + z * vector.z;
    }

    @Override
    public String toString() {
        return "Vector coordinates: (" + x + ", " + y + ", " + z + ")";
    }
}

class Ray extends Geometric{
    public final Point origin;
    public final Vector direction;

    public Ray(Point origin, Vector direction){
        this.origin = origin;
        this.direction = direction;
    }

    public Point at(float t){
        return new Point(
            origin.x + direction.x * t,
            origin.y + direction.y * t,
            origin.z + direction.z * t
        );
    }

    public float[] getMesh(){
        return new float[]{origin.x, origin.y, origin.z, origin.x + direction.x * 1000.0f, origin.y + direction.y * 1000.0f, origin.z + direction.z * 1000.0f};
    }

    @Override
    public String toString() {
        return "Ray origin: " + origin + ", direction: " + direction + "\n" + "Ray direction: (" + direction.x + ", " + direction.y + ", " + direction.z + ")";
    }
}
