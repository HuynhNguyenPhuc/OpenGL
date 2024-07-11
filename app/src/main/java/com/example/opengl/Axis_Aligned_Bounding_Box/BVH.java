package com.example.opengl.Axis_Aligned_Bounding_Box;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent a node of the Bounding Volume Hierarchy tree
 */
class BVHNode {
    public AABB aabb;
    public int id;
    public BVHNode left;
    public BVHNode right;

    /**
     * @param aabb: The bounding volume
     * @param id: The index of this node (-1 for internal node,
     *          non-negative integer for leaf node)
     */
    public BVHNode(AABB aabb, int id) {
        this.aabb = aabb;
        this.id = id;
        this.left = null;
        this.right = null;
    }

    /**
     * Check if a node is leaf node or not
     */
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

    /**
     * Return the string represents the structure of the BVH tree
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100000);
        toString(sb, root, 0);
        return sb.toString();
    }

    /**
     * Recursively print the structure of the BVH tree
     * @param sb: The string builder
     * @param node: The current node we are considering
     * @param depth: The current depth of this node
     */
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

    /**
     * Pair class for helping sort AABBs but not shuffle the index of each AABB
     */
    class Pair{
        public AABB aabb;
        public int id;
        public Pair(AABB aabb, int id){
            this.aabb = aabb;
            this.id = id;
        }
    }

    /**
     * Sorting the list of AABBs along the axis 0, 1 or 2
     * @param aabbs: List of AABBs
     * @param start: Start index
     * @param end: End index
     * @param axis: Axis value (0 for x, 1 for y, 2 for z)
     * @param indices: The array for keeping track of the indices
     */
    private void sortAABBs(AABB[] aabbs, int start, int end, int axis, int[] indices) {
        Pair[] pairs = new Pair[end - start];
        for (int i = start; i < end; i++) {
            pairs[i - start] = new Pair(aabbs[i], indices[i]);
        }

        Arrays.sort(pairs, (p1, p2) -> Float.compare(p1.aabb.getCenter().getCoordinates()[axis], p2.aabb.getCenter().getCoordinates()[axis]));

        for (int i = start; i < end; i++) {
            aabbs[i] = pairs[i - start].aabb;
            indices[i] = pairs[i - start].id;
        }
    }

    /**
     * Build the BVH tree from the list of AABBs
     * @param aabbs: List of AABBs
     */
    public void build(AABB[] aabbs){
        numAABBs = aabbs.length;
        int[] indices = new int[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            indices[i] = i;
        }
        root = buildRecursive(aabbs, 0, aabbs.length, indices);
    }

    /**
     * Recursively build the BVH tree
     * @param aabbs: List of AABBs
     * @param start: Start index
     * @param end: End index
     * @param indices: Index of each AABB
     * @return: The node of the BVH tree which is built from the AABBs
     */
    private BVHNode buildRecursive(AABB[] aabbs, int start, int end, int[] indices){
        if (start >= end) return null;
        if (end - start == 1) return new BVHNode(aabbs[start], indices[start]);

        /* Expand the bounding volume of the AABBs */
        AABB boundingVolume = AABB.expand(aabbs, 0, aabbs.length);

        /* Get the longest extent axis to split */
        int axis = boundingVolume.getLongestAxis();

        /* Sort the AABBs based on the longest extent axis */
        sortAABBs(aabbs, start, end, axis, indices);

        /* Surface area heuristic strategy */
        int splitIndex = strategy("sah", aabbs, start, end);

        /* Continue to build form left and right part */
        BVHNode leftChild = buildRecursive(aabbs, start, splitIndex, indices);
        BVHNode rightChild = buildRecursive(aabbs, splitIndex, end, indices);

        BVHNode node = new BVHNode(boundingVolume, -1);
        node.left = leftChild;
        node.right = rightChild;

        return node;
    }

    /**
     * Get the split index based on the split strategy
     * @param mode The strategy you want to split: median, random or surface area heuristic
     * @param args The necessary arguments for this strategy
     * @return The value of the split index
     */
    private int strategy(String mode, Object... args){
        Map<String, SplitStrategy> strategy = new HashMap<>();
        strategy.put("median", new MedianStrategy());
        strategy.put("random", new RandomStrategy());
        strategy.put("sah", new SurfaceAreaHeuristicStrategy());

        SplitStrategy helper = strategy.get(mode);
        if (helper == null) return -1;
        return helper.strategy(args);
    }

    /**
     * Insert a new AABB into the BVH tree
     * @param aabb: The new axis-aligned bounding box
     * @param id: The index of this AABB
     */
    public void insert(AABB aabb, int id) {
        numAABBs++;
        if (root == null) {
            root = new BVHNode(aabb, id);
        } else {
            insertRecursive(root, aabb, id);
        }
    }

    /**
     * Recursively insert a new AABB into the BVH tree
     * @param node: The current node we are considering
     * @param aabb: The new axis-aligned bounding box
     * @param id: The index of this aabb
     */
    private void insertRecursive(BVHNode node, AABB aabb, int id) {
        if (node.isLeaf()) {
            node.left = new BVHNode(node.aabb, node.id);
            node.right = new BVHNode(aabb, id);
            node.aabb = node.aabb.expand(aabb);
            node.id = -1;
        } else {
            /* Calculate the increase in volume if we add into the left and right child */
            float leftVolumeIncrease = node.left.aabb.expand(aabb).getSurfaceArea() - node.left.aabb.getSurfaceArea();
            float rightVolumeIncrease = node.right.aabb.expand(aabb).getSurfaceArea() - node.right.aabb.getSurfaceArea();

            /* Insert into the branch which has less increase */
            if (leftVolumeIncrease < rightVolumeIncrease){
                insertRecursive(node.left, aabb, id);
                node.aabb = node.aabb.expand(node.left.aabb);
            } else {
                insertRecursive(node.right, aabb, id);
                node.aabb = node.aabb.expand(node.right.aabb);
            }
        }
    }

    /**
     * Check if a ray intersects with any of the AABBs in the BVH tree
     * @param r: The ray we are checking
     * @return: A boolean array with the same length as the number of AABBs
     */
    public boolean[] checkIntersectWithRay(Ray r) {
        boolean[] intersections = new boolean[numAABBs];
        checkIntersectWithRayRecursive(root, r, intersections);
        return intersections;
    }

    /**
     * Recursively check if a ray intersects with any of the AABBs in the BVH tree
     * @param node: The current node we are considering
     * @param r: The ray we are checking
     * @param intersections: A boolean array with the same length as the number of AABBs
     */
    private void checkIntersectWithRayRecursive(BVHNode node, Ray r, boolean[] intersections) {
        if (node == null) return;
        if (node.aabb.checkIntersectWithRay(r)) {
            if (node.isLeaf()) {
                intersections[node.id] = true;
            } else {
                checkIntersectWithRayRecursive(node.left, r, intersections);
                checkIntersectWithRayRecursive(node.right, r, intersections);
            }
        }
    }

    /**
     * Get list of AABBs which intersect with the ray
     * @param r: The ray we are considering
     * @return: List of AABBs
     */
    public int[] getIntersectWithRay(Ray r) {
        DynamicArray result = new DynamicArray(10);
        int count = getIntersectWithRayRecursive(root, r, result, 0);
        return Arrays.copyOf(result.array, count);
    }

    /**
     * Recursively get list of AABBs which intersect with the ray
     * @param node: The current node we are considering
     * @param r: The ray we are checking
     * @param array: The dynamic array for tracking the result
     * @param index: The current number of AABBs which intersect with the ray
     */
    private int getIntersectWithRayRecursive(BVHNode node, Ray r, DynamicArray array, int index) {
        if (node == null) return index;
        if (node.aabb.checkIntersectWithRay(r)) {
            if (node.isLeaf()) {
                if (array.overflow(index)) {
                    array.resize();
                }
                array.array[index++] = node.id;
            } else {
                index = getIntersectWithRayRecursive(node.left, r, array, index);
                index = getIntersectWithRayRecursive(node.right, r, array, index);
            }
        }
        return index;
    }
}
