package com.example.opengl.MeshRender;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VertexLoader {

    private List<float[]> vertices = new ArrayList<>();
    private List<float[]>normals = new ArrayList<>();
    private List<float[]>textures = new ArrayList<>();
    private List<int[][]>faces = new ArrayList<>();
    private final int VERTEX_SIZE = 3;
    private final int TEXTURE_SIZE = 2;
    private final int NORMAL_SIZE = 3;

    public List<Float> load(Context context, String filename) {
        List<Float> vertexArray = new ArrayList<>();

        try {
            InputStream inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                switch (parts[0]) {
                    case "v":
                        processVertex(parts);
                        break;
                    case "f":
                        processFace(parts);
                        break;
                    case "vt":
                        processTexture(parts);
                        break;
                    case "vn":
                        processNormal(parts);
                        break;
                    default:
                        break;
                }
            }

            vertexArray = getVertexArray();

            vertices.clear();
            normals.clear();
            textures.clear();
            faces.clear();

            vertices = null;
            normals = null;
            textures = null;
            faces = null;

            System.gc();
            System.runFinalization();

            reader.close();
            inputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return vertexArray;
    }

    private void processVertex(String[] parts) {
        float[] point = new float[VERTEX_SIZE];
        for (int i = 1; i < VERTEX_SIZE + 1; i++) {
            point[i-1] = Float.parseFloat(parts[i]);
        }
        vertices.add(point);
    }

    private void processTexture(String[] parts){
        float[] texture = new float[TEXTURE_SIZE];
        for (int i = 1; i < TEXTURE_SIZE + 1; i++) {
            texture[i-1] = Float.parseFloat(parts[i]);
        }
        textures.add(texture);
    }

    private void processNormal(String[] parts){
        float[] normal = new float[NORMAL_SIZE];
        for (int i = 1; i < NORMAL_SIZE + 1; i++) {
            normal[i-1] = Float.parseFloat(parts[i]);
        }
        normals.add(normal);
    }
    private void processFace(String[] parts) {
        int[][] indices = new int[parts.length - 1][];
        for (int i = 0; i < indices.length; i++) {
            String[] value = parts[i + 1].split("/");
            if (value[1].isEmpty()) {
                indices[i] = new int[]{(Integer.parseInt(value[0]) - 1), -1, (Integer.parseInt(value[2]) - 1)};
            }
            else {
                indices[i] = new int[]{(Integer.parseInt(value[0]) - 1), (Integer.parseInt(value[1]) - 1), (Integer.parseInt(value[2]) - 1)};
            }
        }
        faces.add(indices);
    }
    private List<Float> getVertexArray(){
        List<Float> vertexArray = new ArrayList<>();
        for (int[][] face : faces) {
            for (int i = 1; i < face.length - 1; i++) {
                for (int j = 0; j < 3; j++) {
                    int di = (j == 0)? -i: j - 1;
                    for (float value : vertices.get(face[i + di][0])) {
                        vertexArray.add(value);
                    }
                    if (face[i + di][1] == -1){
                        for (int k = 0; k < TEXTURE_SIZE; k++){
                            vertexArray.add(0.0f);
                        }
                    }
                    else {
                        for (float value : textures.get(face[i + di][1])) {
                            vertexArray.add(value);
                        }
                    }
                    for (float value : normals.get(face[i + di][2])) {
                        vertexArray.add(value);
                    }
                }
            }
        }
        return vertexArray;
    };
}