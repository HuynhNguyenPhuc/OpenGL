package com.example.opengl.Hackathon;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SurfaceRender implements GLSurfaceView.Renderer {
    private PlaneMeshHelper helper;
    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private final float[] pointA;
    private final float[] pointB;
    private final float[] pointC;

    private final float[] rayPosition;
    private final float[] rayDirection;

    private float[] mColor = new float[4];

    private final FloatBuffer mVertexBuffer;
    private FloatBuffer mRayBuffer;
    private FloatBuffer mPointBuffer;

    private final int[] mVBOHandles = new int[3];

    private int mProgramHandle;

    private int mPositionHandle;
    private int mPointSizeHandle;
    private int mColorHandle;

    private int mModelMatrixHandle;
    private int mViewMatrixHandle;
    private int mProjectionMatrixHandle;

    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;

    public SurfaceRender(float[] pointA, float[] pointB, float[] pointC, float[] rayPosition, float[] rayDirection) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
        this.rayPosition = rayPosition;
        this.rayDirection = rayDirection;

        float[] vertexAttributeArray = new float[]{
                pointA[0], pointA[1], pointA[2],
                pointB[0], pointB[1], pointB[2],
                pointC[0], pointC[1], pointC[2]
        };

        mVertexBuffer = ByteBuffer.allocateDirect(vertexAttributeArray.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(vertexAttributeArray);
        mVertexBuffer.position(0);

        setupRayBuffer();
        setupIntersectionPointBuffer();
    }

    private void setupRayBuffer() {
        float[] rayVertices = {
                rayPosition[0], rayPosition[1], rayPosition[2],
                rayPosition[0] + rayDirection[0] * 10, rayPosition[1] + rayDirection[1] * 10, rayPosition[2] + rayDirection[2] * 10
        };

        mRayBuffer = ByteBuffer.allocateDirect(rayVertices.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mRayBuffer.put(rayVertices);
        mRayBuffer.position(0);
    }

    private void setupIntersectionPointBuffer() {
        this.helper = new PlaneMeshHelper(rayPosition, rayDirection, pointA, pointB, pointC);
        float[] intersectionPoints = helper.getIntersectionPoints();
        if (intersectionPoints == null) {
            mPointBuffer = ByteBuffer.allocateDirect(0)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            return;
        }

        mPointBuffer = ByteBuffer.allocateDirect(intersectionPoints.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPointBuffer.put(intersectionPoints);
        mPointBuffer.position(0);
    }

    private String getVertexShader() {
        return
                "uniform mat4 u_ModelMatrix;\n" +
                        "uniform mat4 u_ViewMatrix;\n" +
                        "uniform mat4 u_ProjectionMatrix;\n" +
                        "uniform float u_PointSize;\n" +
                        "attribute vec4 a_Position;\n" +
                        "void main() {\n" +
                        "   mat4 u_MVPMatrix = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix;\n" +
                        "   gl_Position = u_MVPMatrix * a_Position;\n" +
                        "   gl_PointSize = u_PointSize;\n" +
                        "}\n";
    }


    private String getFragmentShader() {
        return
                "precision mediump float;\n" +
                        "uniform vec4 u_Color;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = u_Color;\n" +
                        "}\n";
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -5.0f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        setupProgram();

        GLES30.glGenBuffers(3, mVBOHandles, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * mBytesPerFloat, mVertexBuffer, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mRayBuffer.capacity() * mBytesPerFloat, mRayBuffer, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPointBuffer.capacity() * mBytesPerFloat, mPointBuffer, GLES30.GL_DYNAMIC_DRAW);
    }

    private void setupProgram() {
        final String vertexShader = getVertexShader();
        int vertexShaderHandle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertexShaderHandle, vertexShader);
        GLES30.glCompileShader(vertexShaderHandle);

        final String fragmentShader = getFragmentShader();
        int fragmentShaderHandle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES30.glCompileShader(fragmentShaderHandle);

        mProgramHandle = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgramHandle, vertexShaderHandle);
        GLES30.glAttachShader(mProgramHandle, fragmentShaderHandle);
        GLES30.glBindAttribLocation(mProgramHandle, 0, "a_Position");
        GLES30.glLinkProgram(mProgramHandle);

        mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        mPointSizeHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_PointSize");
        mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_Color");
        mModelMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_ModelMatrix");
        mViewMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_ViewMatrix");
        mProjectionMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_ProjectionMatrix");

        GLES30.glUseProgram(mProgramHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        // long time = SystemClock.uptimeMillis() % 10000L;
        // float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        Matrix.setIdentityM(mModelMatrix, 0);
        // Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        mColor = new float[]{0.5f, 0.5f, 0.0f, 1.0f};

        GLES30.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);

        drawVertices();
        drawRay();
        drawIntersectionPoints();
    }

    private void drawVertices() {
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform4f(mColorHandle, mColor[0], mColor[1], mColor[2], mColor[3]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    private void drawRay() {
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform4f(mColorHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    private void drawIntersectionPoints() {
        if (mPointBuffer.capacity() == 0) {
            return;
        }
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform1f(mPointSizeHandle, 10.0f);

        float[] p = new float[]{mPointBuffer.get(0), mPointBuffer.get(1), mPointBuffer.get(2)};
        if (this.helper.isInsideTriangle(p)) {
            GLES30.glUniform4f(mColorHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        else{
            GLES30.glUniform4f(mColorHandle, 0.0f, 1.0f, 0.0f, 1.0f);
        }

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointBuffer.capacity() / mPositionDataSize);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}