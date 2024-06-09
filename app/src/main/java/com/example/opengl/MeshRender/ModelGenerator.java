package com.example.opengl.MeshRender;

import android.opengl.Matrix;
public class ModelGenerator {
    public static float[] generate(int numInstances){
        float[] result = new float[numInstances * 16];
        for(int i = 0; i < numInstances; i++) {
            float[] modelMatrix = new float[16];

            /* Perform operation to generate model matrix */
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, 0, 2 * ((float)i) - 1.0f * numInstances, 0);

            /* Copy model matrix to result */
            System.arraycopy(modelMatrix, 0, result, i * 16 + 0, 16);
        }
        return result;
    }
}
