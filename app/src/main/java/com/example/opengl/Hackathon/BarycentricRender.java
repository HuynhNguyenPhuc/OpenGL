package com.example.opengl.Hackathon;

import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BarycentricRender extends MeshRender {
    private final float[] pointA;
    private final float[] pointB;
    private final float[] pointC;
    private final float[] pointP;
    private final float[] normalA;
    private final float[] normalB;
    private final float[] normalC;

    private float[] center = new float[3];

    private boolean isInsideTriangle;

    public BarycentricRender(float[] pointA, float[] pointB, float[] pointC, float[] pointP, float[] normalA, float[] normalB, float[] normalC) {
        super(new float[]{pointA[0], pointA[1], pointA[2], pointB[0], pointB[1], pointB[2], pointC[0], pointC[1], pointC[2]});

        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
        this.pointP = pointP;
        this.normalA = normalA;
        this.normalB = normalB;
        this.normalC = normalC;

        setupRayBuffer();
    }

    private void setupRayBuffer() {
        PlaneMeshHelper helper = new PlaneMeshHelper(new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f}, pointA, pointB, pointC);
        float[] bCoordinates = helper.getBarycentricCoordinates(pointP);
        float[] normalP = new float[3];

        for (int i = 0; i < 3; i++) {
            center[i] = (pointA[i] + pointB[i] + pointC[i]) / 3;
            normalP[i] = normalA[i] * bCoordinates[0] + normalB[i] * bCoordinates[1] + normalC[i] * bCoordinates[2];
        }

        float[] rayVertices = {
                pointP[0], pointP[1], pointP[2],
                pointP[0] + normalP[0], pointP[1] + normalP[1], pointP[2] + normalP[2],
                pointA[0], pointA[1], pointA[2],
                pointA[0] + normalA[0], pointA[1] + normalA[1], pointA[2] + normalA[2],
                pointB[0], pointB[1], pointB[2],
                pointB[0] + normalB[0], pointB[1] + normalB[1], pointB[2] + normalB[2],
                pointC[0], pointC[1], pointC[2],
                pointC[0] + normalC[0], pointC[1] + normalC[1], pointC[2] + normalC[2]
        };

        mRayBuffer = ByteBuffer.allocateDirect(rayVertices.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mRayBuffer.put(rayVertices);
        mRayBuffer.position(0);

        mPointBuffer = ByteBuffer.allocateDirect(3 * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPointBuffer.put(pointP);
        mPointBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        final float eyeX = this.center[0];
        final float eyeY = this.center[1];
        final float eyeZ = this.center[2] - 5.0f;
        final float lookX = this.center[0];
        final float lookY = this.center[1];
        final float lookZ = this.center[2] + 5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mRayBuffer.capacity() * mBytesPerFloat, mRayBuffer, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPointBuffer.capacity() * mBytesPerFloat, mPointBuffer, GLES30.GL_DYNAMIC_DRAW);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

        // GLES30.glEnable(GLES30.GL_FRONT_FACE);
        super.drawRay(10.0f, Color.BLUE, Color.RED);
        // GLES30.glDisable(GLES30.GL_FRONT_FACE);

        // GLES30.glEnable(GLES30.GL_CULL_FACE);
        drawPoints(10.0f);
        // GLES30.glDisable(GLES30.GL_CULL_FACE);

        drawMesh(Color.YELLOW);
    }

    @Override
    protected void drawPoints(float pointSize) {
        if (mPointBuffer.capacity() == 0) {
            return;
        }

        super.drawPoints(pointSize);

        GLES30.glUniform4fv(mColorHandle, 0, Color.BLUE, 0);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}