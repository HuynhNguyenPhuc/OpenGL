package com.example.opengl.Hackathon;

import android.opengl.GLES30;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlaneRender extends MeshRender {
    private final PlaneMeshHelper helper;
    private float[] center = new float[3];
    private final float[] rayPosition;
    private final float[] rayDirection;

    private boolean isInsideTriangle;
    private boolean inPlane;
    private boolean isSingleIntersection;

    public PlaneRender(float[] pointA, float[] pointB, float[] pointC, float[] rayPosition, float[] rayDirection) {
        super(new float[]{pointA[0], pointA[1], pointA[2], pointB[0], pointB[1], pointB[2], pointC[0], pointC[1], pointC[2]});

        this.rayPosition = rayPosition;
        this.rayDirection = rayDirection;

        for (int i = 0; i < 3; i++) {
            this.center[i] = (pointA[i] + pointB[i] + pointC[i]) / 3.0f;
        }

        this.helper = new PlaneMeshHelper(rayPosition, rayDirection, pointA, pointB, pointC);
        this.isSingleIntersection = true;

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
        float[] intersectionPoints = helper.getIntersectionPoints();
        if (intersectionPoints == null) {
            inPlane = this.helper.checkRayInPlane();
            isSingleIntersection = false;
            return;
        }

        isInsideTriangle = this.helper.isInsideTriangle(intersectionPoints);

        mPointBuffer = ByteBuffer.allocateDirect(intersectionPoints.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPointBuffer.put(intersectionPoints);
        mPointBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        final float eyeX = this.center[0];
        final float eyeY = this.center[1];
        final float eyeZ = this.center[2] + 5.0f;

        final float lookX = this.center[0];
        final float lookY = this.center[1];
        final float lookZ = this.center[2] - 5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mRayBuffer.capacity() * mBytesPerFloat, mRayBuffer, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
        // GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPointBuffer.capacity() * mBytesPerFloat, mPointBuffer, GLES30.GL_DYNAMIC_DRAW);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

        drawPoints(10.0f);

        if (inPlane) {
            super.drawRay(10.0f, Color.WHITE, Color.RED);
        }
        else {
            super.drawRay(10.0f, Color.WHITE, Color.WHITE);
        }

        super.drawMesh(Color.YELLOW);
    }

    @Override
    protected void drawPoints(float pointSize) {
        if (!isSingleIntersection) {
            if (!inPlane) return;
            float[] intersectionPoints = this.helper.getTriangleIntersection();
            if (intersectionPoints == null) return;
            else{
                mPointBuffer = ByteBuffer.allocateDirect(intersectionPoints.length * mBytesPerFloat)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mPointBuffer.put(intersectionPoints);
                mPointBuffer.position(0);

                super.drawPoints(pointSize);
            }

            GLES30.glUniform4fv(mColorHandle, 1, Color.GREEN, 0);

            if (intersectionPoints.length == 3){
                GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
            }
            else if (intersectionPoints.length == 6){
                GLES30.glEnable(GLES30.GL_FRONT);
                GLES30.glDrawArrays(GLES30.GL_LINES, 0, mPointBuffer.capacity() / mPositionDataSize);
                GLES30.glDisable(GLES30.GL_FRONT);
            }
        }
        else {
            super.drawPoints(pointSize);

            if (isInsideTriangle) {
                GLES30.glUniform4fv(mColorHandle, 0, Color.GREEN, 0);
            } else {
                GLES30.glUniform4fv(mColorHandle, 0, Color.RED, 0);
            }

            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
        }

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}