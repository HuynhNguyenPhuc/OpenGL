package com.example.Axis_Aligned_Bounding_Box;

public interface SplitStrategy {
    int strategy(Object... args);
}

class MedianStrategy implements SplitStrategy {
    @Override
    public int strategy(Object... args) {
        int start = (int) args[0];
        int end = (int) args[1];
        return start + (end - start) / 2;
    }
}

class RandomStrategy implements SplitStrategy {
    @Override
    public int strategy(Object... args) {
        int start = (int) args[0];
        int end = (int) args[1];
        return start + (int) (Math.random() * (end - start));
    }
}

class SurfaceAreaHeuristicStrategy implements SplitStrategy {
    @Override
    public int strategy(Object... args) {
        AABB[] aabbs = (AABB[]) args[0];
        int start = (int) args[1];
        int end = (int) args[2];

        int n = end - start;
        float[] sahCosts = new float[n - 1];
        AABB combinedAABB = AABB.expand(aabbs, start, end);

        float totalSurfaceArea = combinedAABB.getSurfaceArea();

        for (int i = start + 1; i < end; i++) {
            AABB leftAABB = AABB.expand(aabbs, start, i);
            AABB rightAABB = AABB.expand(aabbs, i, end);
            float leftSurfaceArea = leftAABB.getSurfaceArea();
            float rightSurfaceArea = rightAABB.getSurfaceArea();
            sahCosts[i - start - 1] = (i - start) * (leftSurfaceArea / totalSurfaceArea) +
                    (end - i) * (rightSurfaceArea / totalSurfaceArea);
        }

        float minCost = Float.POSITIVE_INFINITY;
        int minIndex = start + 1;
        for (int i = start + 1; i < end; i++) {
            if (sahCosts[i - start - 1] < minCost) {
                minCost = sahCosts[i - start - 1];
                minIndex = i;
            }
        }

        return minIndex;
    }
}
