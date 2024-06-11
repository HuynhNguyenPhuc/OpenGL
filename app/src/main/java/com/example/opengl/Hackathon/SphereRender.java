package com.example.opengl.Hackathon;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SphereRender extends MeshRender {
    private final float[] center;
    private final float radius;
    private final float[] rayPosition;
    private final float[] rayDirection;

    public SphereRender(float[] rayPosition, float[] rayDirection, float[] center, float radius) {
        super(SphereGenerator.generate(center, radius, 180, 180));
        this.center = center;
        this.radius = radius;
        this.rayPosition = rayPosition;
        this.rayDirection = rayDirection;

        setupRayBuffer();
        setupIntersectionPointBuffer();
    }

    private void setupRayBuffer() {
        float[] rayVertices = {
                rayPosition[0], rayPosition[1], rayPosition[2],
                rayPosition[0] + rayDirection[0] * 1000, rayPosition[1] + rayDirection[1] * 1000, rayPosition[2] + rayDirection[2] * 1000
        };

        mRayBuffer = ByteBuffer.allocateDirect(rayVertices.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mRayBuffer.put(rayVertices);
        mRayBuffer.position(0);
    }

    private void setupIntersectionPointBuffer() {
        List<float[]> intersectionPoints = new SphereMeshHelper(rayPosition, rayDirection, center, radius).getIntersectionPoints();
        float[] pointVertices = new float[intersectionPoints.size() * 3];

        for (int i = 0; i < intersectionPoints.size(); i++) {
            pointVertices[i * 3] = intersectionPoints.get(i)[0];
            pointVertices[i * 3 + 1] = intersectionPoints.get(i)[1];
            pointVertices[i * 3 + 2] = intersectionPoints.get(i)[2];
        }

        mPointBuffer = ByteBuffer.allocateDirect(pointVertices.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPointBuffer.put(pointVertices);
        mPointBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        final float eyeX = center[0];
        final float eyeY = center[1];
        final float eyeZ = -3.0f * radius + center[2];

        final float lookX = center[0];
        final float lookY = center[1];
        final float lookZ = 3.0f * radius + center[2];

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

        // GLES30.glEnable(GLES30.GL_CULL_FACE);
        super.drawRay(10.0f, Color.BLUE, Color.RED);
        // GLES30.glDisable(GLES30.GL_CULL_FACE);

        // GLES30.glEnable(GLES30.GL_FRONT_FACE);
        drawPoints(10.0f);
        // GLES30.glDisable(GLES30.GL_FRONT_FACE);

        // GLES30.glEnable(GLES30.GL_CULL_FACE);
        super.drawMesh(Color.BLACK);
        // GLES30.glDisable(GLES30.GL_CULL_FACE);
    }

    @Override
    protected void drawPoints(float pointSize) {
        if (mPointBuffer.capacity() == 0) {
            return;
        }

        super.drawPoints(pointSize);

        GLES30.glUniform4fv(mColorHandle, 0, Color.WHITE, 0);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointBuffer.capacity() / mPositionDataSize);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}