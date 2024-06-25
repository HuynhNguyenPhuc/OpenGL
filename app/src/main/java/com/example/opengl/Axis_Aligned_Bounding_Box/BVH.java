package com.example.opengl.Axis_Aligned_Bounding_Box;

import java.util.Arrays;

class BVHNode {
    public AABB aabb;
    public int id;
    public BVHNode left;
    public BVHNode right;

    public BVHNode(){
        this.aabb = null;
        this.id = -1;
        this.left = null;
        this.right = null;
    }

    public BVHNode(AABB aabb, int id) {
        this.aabb = aabb;
        this.id = id;
        this.left = null;
        this.right = null;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}


public class BVH {
    /*
     * This class represents a Bounding Volume Hierarchy
     */
    public BVHNode root;
    public int numAABBs;

    public BVH() {
        this.root = null;
        this.numAABBs = 0;
    }

    public BVH(BVH other) {
        this.root = other.root;
        this.numAABBs = other.numAABBs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100000);
        toString(sb, root, 0);
        return sb.toString();
    }

    private void toString(StringBuilder sb, BVHNode node, int depth) {
        if (node == null) {
            return;
        }
        char[] indent = new char[depth * 2];
        Arrays.fill(indent, ' ');
        sb.append(indent);
        if (node.isLeaf()) {
            sb.append("Leaf Node - ").append(node.aabb.toString()).append("\n");
        } else {
            sb.append("Internal Node - ").append(node.aabb.toString()).append("\n");
            toString(sb, node.left, depth + 1);
            toString(sb, node.right, depth + 1);
        }
    }

    public void insert(AABB aabb, int id) {
        numAABBs++;
        if (root == null) {
            root = new BVHNode(aabb, id);
        } else {
            insertRecursive(root, aabb, id);
        }
    }

    private void insertRecursive(BVHNode node, AABB aabb, int id) {
        if (node.isLeaf()) {
            node.left = new BVHNode(node.aabb, node.id);
            node.right = new BVHNode(aabb, id);
            node.aabb = node.aabb.expand(aabb);
        } else {
            boolean traversalLeft = false;

            if (node.left.aabb.checkCollision(aabb) && !node.right.aabb.checkCollision(aabb)) traversalLeft = true;
            else if (node.right.aabb.checkCollision(aabb) && !node.left.aabb.checkCollision(aabb)) traversalLeft = false;
            else {
                float leftVolumeIncrease = node.left.aabb.expand(aabb).getSurfaceArea() - node.left.aabb.getSurfaceArea();
                float rightVolumeIncrease = node.right.aabb.expand(aabb).getSurfaceArea() - node.right.aabb.getSurfaceArea();

                traversalLeft = leftVolumeIncrease < rightVolumeIncrease;
            }

            if (traversalLeft){
                insertRecursive(node.left, aabb, id);
                node.left.aabb = node.left.aabb.expand(aabb);
            } else {
                insertRecursive(node.right, aabb, id);
                node.right.aabb = node.right.aabb.expand(aabb);
            }
        }
    }

    public boolean[] checkIntersectionWithRay(Ray r) {
        boolean[] intersections = new boolean[numAABBs];
        checkIntersectionWithRayRecursive(root, r, intersections);
        return intersections;
    }

    private void checkIntersectionWithRayRecursive(BVHNode node, Ray r, boolean[] intersections) {
        if (node == null) return;
        if (node.aabb.checkIntersectionWithRay(r)) {
            if (node.isLeaf()) {
                intersections[node.id] = true;
            } else {
                checkIntersectionWithRayRecursive(node.left, r, intersections);
                checkIntersectionWithRayRecursive(node.right, r, intersections);
            }
        }
    }
}