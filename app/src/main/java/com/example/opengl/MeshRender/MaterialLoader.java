package com.example.opengl.MeshRender;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Material {
    private float[] kA;
    private float[] kD;
    private float[] kS;
    private float nS;

    Material(float[] kA, float[] kD, float[] kS, float nS){
        this.kA = kA;
        this.kD = kD;
        this.kS = kS;
        this.nS = nS;
    }

    public float[] getAmbient() {
        return kA;
    }

    public float[] getDiffuse() {
        return kD;
    }

    public float[] getSpecular() {
        return kS;
    }

    public float getShininess() {
        return nS;
    }
}

public class MaterialLoader {
    public Material load(Context context, String fileName) {
        float[] kA = new float[3];
        float[] kD = new float[3];
        float[] kS = new float[3];
        float nS = 0;

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                switch (parts[0]) {
                    case "Ka":
                        for (int i = 0; i < 3; i++) {
                            kA[i] = Float.parseFloat(parts[i + 1]);
                        }
                        break;
                    case "Kd":
                        for (int i = 0; i < 3; i++) {
                            kD[i] = Float.parseFloat(parts[i + 1]);
                        }
                        break;
                    case "Ks":
                        for (int i = 0; i < 3; i++) {
                            kS[i] = Float.parseFloat(parts[i + 1]);
                        }
                        break;
                    case "Ns":
                        nS = Float.parseFloat(parts[1]);
                        break;
                    default:
                        break;
                }
            }

            reader.close();
            inputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return new Material(kA, kD, kS, nS);
    }
}
