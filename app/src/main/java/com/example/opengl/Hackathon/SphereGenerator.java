package com.example.opengl.Hackathon;

public class SphereGenerator {

    public static float[] generate(float[] center, float radius, int numSlices, int numStacks) {
        float[] vertices = SphereGenerator.generateVertices(center, radius, numSlices, numStacks);
        short[] indices = SphereGenerator.generateIndices(numSlices, numStacks);
        float[] result = new float[indices.length * 3];

        for (int i = 0; i < indices.length; i++) {
            int vertexIndex = indices[i] * 3;
            result[i * 3] = vertices[vertexIndex];
            result[i * 3 + 1] = vertices[vertexIndex + 1];
            result[i * 3 + 2] = vertices[vertexIndex + 2];
        }

        return result;
    }

    public static float[] generateVertices(float[] center, float radius, int numSlices, int numStacks) {
        int numVertices = (numSlices + 1) * (numStacks + 1);
        float[] vertices = new float[numVertices * 3];

        float phiStep = (float) (2 * Math.PI) / numSlices;
        float thetaStep = (float) Math.PI / numStacks;

        int index = 0;
        for (int i = 0; i <= numSlices; i++) {
            float phi = i * phiStep;
            for (int j = 0; j <= numStacks; j++) {
                float theta = j * thetaStep;

                float x = (float) (radius * Math.sin(theta) * Math.cos(phi)) + center[0];
                float y = (float) (radius * Math.sin(theta) * Math.sin(phi)) + center[1];
                float z = (float) (radius * Math.cos(theta)) + center[2];

                vertices[index++] = x;
                vertices[index++] = y;
                vertices[index++] = z;
            }
        }

        return vertices;
    }

    public static short[] generateIndices(int numSlices, int numStacks) {
        int numVertices = (numSlices + 1) * (numStacks + 1);
        int numIndices = numSlices * numStacks * 6; // Each square is made up of two triangles, each with 3 indices

        short[] indices = new short[numIndices];

        int index = 0;
        for (int i = 0; i < numSlices; i++) {
            for (int j = 0; j < numStacks; j++) {
                int vertexIndex = i * (numStacks + 1) + j;

                indices[index++] = (short) vertexIndex;
                indices[index++] = (short) (vertexIndex + numStacks + 1);
                indices[index++] = (short) (vertexIndex + 1);

                indices[index++] = (short) (vertexIndex + 1);
                indices[index++] = (short) (vertexIndex + numStacks + 1);
                indices[index++] = (short) (vertexIndex + numStacks + 2);
            }
        }

        return indices;
    }
}