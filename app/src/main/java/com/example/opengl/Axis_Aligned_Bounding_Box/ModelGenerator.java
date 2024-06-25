package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.opengl.Matrix;
import java.util.Random;

public class ModelGenerator {
    public static float[] generate(int numInstances) {
        float[] result = new float[numInstances * 16];
        Random random = new Random();

        for (int i = 0; i < numInstances; i++) {
            float[] modelMatrix = new float[16];
            Matrix.setIdentityM(modelMatrix, 0);

            float translateX = random.nextFloat() * 10.0f - 5.0f;
            float translateY = random.nextFloat() * 10.0f - 5.0f;
            float translateZ = random.nextFloat() * 10.0f - 5.0f;
            Matrix.translateM(modelMatrix, 0, translateX, translateY, translateZ);

            // float scaleX = random.nextFloat() * 1.0f + 0.5f;
            // float scaleY = random.nextFloat() * 1.0f + 0.5f;
            // float scaleZ = random.nextFloat() * 1.0f + 0.5f;
            // Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, scaleZ);

            System.arraycopy(modelMatrix, 0, result, i * 16, 16);
        }
        return result;
    }
}