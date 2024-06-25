package com.example.opengl.Axis_Aligned_Bounding_Box;

public class Color {
    public static final float[] RED = {1.0f, 0.0f, 0.0f, 1.0f};
    public static final float[] GREEN = {0.0f, 1.0f, 0.0f, 1.0f};
    public static final float[] BLUE = {0.0f, 0.0f, 1.0f, 1.0f};
    public static final float[] YELLOW = {1.0f, 1.0f, 0.0f, 1.0f};
    public static final float[] CYAN = {0.0f, 1.0f, 1.0f, 1.0f};
    public static final float[] MAGENTA = {1.0f, 0.0f, 1.0f, 1.0f};
    public static final float[] WHITE = {1.0f, 1.0f, 1.0f, 1.0f};
    public static final float[] BLACK = {0.0f, 0.0f, 0.0f, 1.0f};

    public static float[][] mapToColors(boolean[] boolArray) {
        float[][] colorArray = new float[boolArray.length][4];

        for (int i = 0; i < boolArray.length; i++) {
            if (boolArray[i]) {
                colorArray[i] = Color.GREEN;
            } else {
                colorArray[i] = Color.RED;
            }
        }

        return colorArray;
    }
}
