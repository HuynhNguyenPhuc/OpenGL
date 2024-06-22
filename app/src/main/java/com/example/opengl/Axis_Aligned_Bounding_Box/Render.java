package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Render implements GLSurfaceView.Renderer {
    private static final float EPSILON = 0.01f;
    private final GLSurfaceView glSurfaceView;
    private final Handler handler = new Handler();

    protected String mode;

    protected AABB aabb1;
    protected AABB aabb2;
    protected Ray r;

    protected float current_t = 0;
    protected float v = 0.001f;
    protected float[] intersectionTime;
    protected float t;

    protected final float[] mModelMatrix = new float[16];
    protected final float[] mViewMatrix = new float[16];
    protected final float[] mProjectionMatrix = new float[16];

    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mRayBuffer;
    protected FloatBuffer mPointBuffer;

    protected final int[] mVBOHandles = new int[3];

    protected int mProgramHandle;
    protected int mPositionHandle;
    protected int mPointSizeHandle;
    protected int mColorHandle;

    protected int mModelMatrixHandle;
    protected int mViewMatrixHandle;
    protected int mProjectionMatrixHandle;

    protected final int mBytesPerFloat = 4;
    protected final int mPositionDataSize = 3;

    protected boolean mIntersect;

    public Render(GLSurfaceView glSurfaceView, String mode, Object... args) {
        this.glSurfaceView = glSurfaceView;
        this.mode = mode;
        switch (mode){
            case "mode1":
                aabb1 = (AABB) args[0];
                r = (Ray) args[1];
                break;
            case "mode2":
                aabb1 = (AABB) args[0];
                aabb2 = (AABB) args[1];
                break;
            case "mode3":
                aabb1 = (AABB) args[0];
                aabb2 = (AABB) args[1];
                r = (Ray) args[2];
                t = (float) args[3];
                break;
            case "mode4":
                break;
        }

        initBuffer();
    }

    protected void initBuffer(){
        switch (mode){
            case "mode1":
                mVertexBuffer = Buffer.getAABBBuffer(new AABB[]{aabb1});

                mRayBuffer = Buffer.getRayBuffer(new Ray[]{r});

                Point[] intersectionPoints = aabb1.getIntersectionWithRay(r);

                mPointBuffer = Buffer.getPointBuffer(intersectionPoints);
                break;
            case "mode2":
                mVertexBuffer = Buffer.getAABBBuffer(new AABB[]{aabb1, aabb2});

                mIntersect = aabb1.checkCollision(aabb2);
                break;
            case "mode3":
                mVertexBuffer = Buffer.getAABBBuffer(new AABB[]{aabb1, aabb2});

                mRayBuffer = Buffer.getRayBuffer(new Ray[]{r});

                intersectionTime = aabb2.checkSweptCollision(aabb1, r.direction, t);

                break;
        }
    }

    protected String getVertexShader() {
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

    protected String getFragmentShader() {
        return
                "precision mediump float;\n" +
                "uniform vec4 u_Color;\n" +
                "void main() {\n" +
                "    gl_FragColor = u_Color;\n" +
                "}\n";
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

    protected float[] getViewInformation(Point center1, Point center2){
        float[] result = new float[9];
        float[] center1Coordinates = center1.getCoordinates();
        float[] center2Coordinates = center2.getCoordinates();

        switch (mode){
            case "mode1":
                for (int i = 0; i < 3; i++){
                    result[i] = center1Coordinates[i] + center2Coordinates[i] + i + 2.0f;
                    result[i + 3] = center1Coordinates[i] + center2Coordinates[i] - i - 2.0f;
                }

                result[6] = 0.0f;
                result[7] = 1.0f;
                result[8] = 0.0f;
                break;
            case "mode3":
            case "mode2":
                float[] center = new float[3];
                for (int i = 0; i<3; i++){
                    center[i] = (center1Coordinates[i] + center2Coordinates[i]) / 2.0f;
                }

                Vector v = new Vector(center1Coordinates[0] - center2Coordinates[0],
                        center1Coordinates[1] - center2Coordinates[1],
                        center1Coordinates[2] - center2Coordinates[2]);

                float[] vCoordinates = v.getCoordinates();
                float[] nCoordinates = v.getNormal().getCoordinates();

                for (int i = 0; i < 3; i++) {
                    result[i] = center[i] + 5.0f * nCoordinates[i];
                    result[i + 3] = center[i] - 5.0f * nCoordinates[i];
                    result[i + 6] = vCoordinates[i];
                }
                break;
        }
        return result;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        // GLES30.glDepthFunc(GLES30.GL_LEQUAL);

        final Point center1 = aabb1.getCenter();
        final Point center2;
        if (aabb2 != null) center2 = aabb2.getCenter();
        else center2 = new Point(0.0f, 0.0f, 0.0f);

        float[] viewInformation = getViewInformation(center1, center2);

        /* Set up the view matrix */
        final float eyeX = viewInformation[0];
        final float eyeY = viewInformation[1];
        final float eyeZ = viewInformation[2];

        final float lookX = viewInformation[3];
        final float lookY = viewInformation[4];
        final float lookZ = viewInformation[5];

        final float upX = viewInformation[6];
        final float upY = viewInformation[7];
        final float upZ = viewInformation[8];

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        setupProgram();

        GLES30.glGenBuffers(3, mVBOHandles, 0);

        if (mVertexBuffer != null) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * mBytesPerFloat, mVertexBuffer, GLES30.GL_DYNAMIC_DRAW);
        }

        if (mRayBuffer != null) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mRayBuffer.capacity() * mBytesPerFloat, mRayBuffer, GLES30.GL_DYNAMIC_DRAW);
        }

        if (mPointBuffer != null) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPointBuffer.capacity() * mBytesPerFloat, mPointBuffer, GLES30.GL_DYNAMIC_DRAW);
        }
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
        final float far = 100.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);

        GLES30.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);

        draw(mode);
    }

    protected void drawPoints(float pointSize, float[] pointColor){
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPointBuffer.capacity() * mBytesPerFloat, mPointBuffer, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform1f(mPointSizeHandle, pointSize);
        GLES30.glUniform4fv(mColorHandle, 1, pointColor, 0);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointBuffer.capacity() / mPositionDataSize);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    protected void drawMesh(float[] meshColor) {
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform4fv(mColorHandle, 1, meshColor, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVertexBuffer.capacity() / mPositionDataSize);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    protected void drawRay(float sourcePointSize, float[] sourcePointColor, float[] rayColor) {
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, mPositionDataSize * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glUniform1f(mPointSizeHandle, sourcePointSize);
        GLES30.glUniform4fv(mColorHandle, 1, sourcePointColor, 0);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);

        GLES30.glUniform4fv(mColorHandle, 1, rayColor, 0);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, mRayBuffer.capacity() / mPositionDataSize);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    protected float[] getColor(float current_t, float[] intersectionTime){
        if (intersectionTime == null) return Color.RED;
        if (intersectionTime.length == 1){
            if (current_t == intersectionTime[0]) return Color.GREEN;
        }
        else if (intersectionTime.length == 2){
            if (current_t >= intersectionTime[0] && current_t <= intersectionTime[1]) return Color.GREEN;
        }
        return Color.RED;
    }

    protected void draw(String mode){
        switch (mode){
            case "mode1":
                drawRay(10.0f, Color.BLUE, Color.WHITE);
                drawPoints(10.0f, Color.GREEN);
                drawMesh(Color.YELLOW);
                break;
            case "mode2":
                if (mIntersect) drawMesh(Color.GREEN);
                else drawMesh(Color.RED);
                break;
            case "mode3":
                current_t += v;
                float[] color = getColor(current_t, intersectionTime);
                if (current_t <= t) {
                    // Draw the stationary AABB
                    Matrix.setIdentityM(mModelMatrix, 0);
                    GLES30.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);

                    mVertexBuffer = Buffer.getAABBBuffer(new AABB[]{aabb1});
                    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
                    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, 0, null, GLES30.GL_DYNAMIC_DRAW);
                    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * mBytesPerFloat, mVertexBuffer, GLES30.GL_DYNAMIC_DRAW);

                    drawMesh(color);

                    // Draw the moving AABB
                    Matrix.setIdentityM(mModelMatrix, 0);
                    Vector translation = r.direction.multiply(current_t);
                    Matrix.translateM(mModelMatrix, 0, translation.x, translation.y, translation.z);
                    GLES30.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);

                    mVertexBuffer = Buffer.getAABBBuffer(new AABB[]{aabb2});
                    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
                    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, 0, null, GLES30.GL_DYNAMIC_DRAW);
                    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * mBytesPerFloat, mVertexBuffer, GLES30.GL_DYNAMIC_DRAW);

                    drawMesh(color);

                    glSurfaceView.requestRender();
                }
                else return;
                break;
        }
    }
}
