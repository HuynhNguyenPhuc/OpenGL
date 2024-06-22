package com.example.opengl.Axis_Aligned_Bounding_Box;

class BVHNode {
    /*
     * This class represents a node in a Bounding Volume Hierarchy
     */
    public AABB aabb;
    public BVHNode left;
    public BVHNode right;
    public Object object; // The object this leaf node contains (for simplicity, assuming object can be any type)

    public BVHNode() {
        this.left = null;
        this.right = null;
        this.aabb = null;
        this.object = null;
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

    public BVH(BVH other) {
        this.root = other.root;
    }

    // Build the BVH from a list of objects
    public void build(Object[] objects, AABB[] aabbs) {
        this.root = buildRecursive(objects, aabbs, 0, objects.length);
    }

    private BVHNode buildRecursive(Object[] objects, AABB[] aabbs, int start, int end) {
        if (start == end) return null;

        BVHNode node = new BVHNode();

        // Compute the bounding box for this node
        AABB nodeBox = aabbs[start];
        for (int i = start + 1; i < end; i++) {
            nodeBox = combine(nodeBox, aabbs[i]);
        }
        node.aabb = nodeBox;

        if (end - start == 1) {
            node.object = objects[start];
            return node;
        }

        // Determine split axis and sort objects
        int splitAxis = determineSplitAxis(nodeBox);
        sort(objects, aabbs, start, end, splitAxis);

        int mid = (start + end) / 2;
        node.left = buildRecursive(objects, aabbs, start, mid);
        node.right = buildRecursive(objects, aabbs, mid, end);

        return node;
    }

    private int determineSplitAxis(AABB box) {
        float xLength = box.max.x - box.min.x;
        float yLength = box.max.y - box.min.y;
        float zLength = box.max.z - box.min.z;

        if (xLength > yLength && xLength > zLength) return 0;
        if (yLength > zLength) return 1;
        return 2;
    }

    private void sort(Object[] objects, AABB[] aabbs, int start, int end, int axis) {
        java.util.Arrays.sort(aabbs, start, end, (a, b) -> {
            float aCenter, bCenter;
            switch (axis) {
                case 0: aCenter = (a.min.x + a.max.x) / 2; bCenter = (b.min.x + b.max.x) / 2; break;
                case 1: aCenter = (a.min.y + a.max.y) / 2; bCenter = (b.min.y + b.max.y) / 2; break;
                default: aCenter = (a.min.z + a.max.z) / 2; bCenter = (b.min.z + b.max.z) / 2; break;
            }
            return Float.compare(aCenter, bCenter);
        });

        java.util.Arrays.sort(objects, start, end, (a, b) -> {
            AABB aBox = aabbs[start];
            AABB bBox = aabbs[end - 1];
            float aCenter, bCenter;
            switch (axis) {
                case 0: aCenter = (aBox.min.x + aBox.max.x) / 2; bCenter = (bBox.min.x + bBox.max.x) / 2; break;
                case 1: aCenter = (aBox.min.y + aBox.max.y) / 2; bCenter = (bBox.min.y + bBox.max.y) / 2; break;
                default: aCenter = (aBox.min.z + aBox.max.z) / 2; bCenter = (bBox.min.z + bBox.max.z) / 2; break;
            }
            return Float.compare(aCenter, bCenter);
        });
    }

    private AABB combine(AABB a, AABB b) {
        return new AABB(
                new Point(Math.min(a.min.x, b.min.x), Math.min(a.min.y, b.min.y), Math.min(a.min.z, b.min.z)),
                new Point(Math.max(a.max.x, b.max.x), Math.max(a.max.y, b.max.y), Math.max(a.max.z, b.max.z))
        );
    }

    public boolean intersects(AABB box) {
        return intersectsRecursive(this.root, box);
    }

    private boolean intersectsRecursive(BVHNode node, AABB box) {
        if (node == null) return false;
        if (!node.aabb.checkCollision(box)) return false;
        if (node.object != null) return true; // Assuming any leaf node means intersection

        return intersectsRecursive(node.left, box) || intersectsRecursive(node.right, box);
    }
}