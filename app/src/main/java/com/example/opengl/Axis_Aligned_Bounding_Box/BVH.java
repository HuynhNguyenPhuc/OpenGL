package com.example.opengl.Axis_Aligned_Bounding_Box;

class BVHNode {
    /*
     * This class represents a node in a Bounding Volume Hierarchy
     */

    public BVHNode left;
    public BVHNode right;
    public BVHNode() {

    }
}

public class BVH {
    /*
     * This class represents a Bounding Volume Hierarchy
     */

    public BVHNode root;

    public BVH() {
        this.root = null;
    }

    public BVH(BVH other){
        this.root = other.root;
    }
}
