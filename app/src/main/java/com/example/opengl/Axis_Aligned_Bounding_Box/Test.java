package com.example.opengl.Axis_Aligned_Bounding_Box;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Test {
//    public static void main(String[] args) {
//        Test test = new Test();
//    }

    private void readTestcase(String filename){
        try {
            InputStream inputStream = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {

            }

            reader.close();
            inputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void testIntersectionWithRay(Point min, Point max, Point origin, Vector direction) {
        AABB aabb = new AABB(min, max);
        Ray ray = new Ray(origin, direction);
        Point[] intersectionPoints = aabb.getIntersectionWithRay(ray);

        if (intersectionPoints != null) {
            System.out.println("Intersection Points:");
            for (Point point : intersectionPoints) {
                System.out.println(point);
            }
        } else {
            System.out.println("No intersection.");
        }
    }

    public void testCollision(Point min1, Point max1, Point min2, Point max2) {
        AABB aabb1 = new AABB(min1, max1);
        AABB aabb2 = new AABB(min2, max2);

        boolean collision = aabb1.checkCollision(aabb2);

        if (collision) {
            System.out.println("AABBs collide.");
        } else {
            System.out.println("AABBs do not collide.");
        }
    }

    public void testMotionlessVsMovement(Point min1, Point max1, Point min2, Point max2, Vector direction, float t) {
        AABB aabb1 = new AABB(min1, max1);
        AABB aabb2 = new AABB(min2, max2);

        float[] collisionTimes = aabb1.checkSweptCollision(aabb2, direction, t);

        if (collisionTimes != null) {
            System.out.println("Collision times:");
            for (float time : collisionTimes) {
                System.out.println(time);
            }
        } else {
            System.out.println("No collision detected within the given time frame.");
        }
    }
}
