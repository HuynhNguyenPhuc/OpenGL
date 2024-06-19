package com.example.opengl.Fundamentals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.opengl.R;

public class LaurelRender implements GLSurfaceView.Renderer {
    private final Context mActivityContext;
    private GLSurfaceView mGLSurfaceView;
    private float[] mModelMatrix;
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private float[] lightPosition;

    private FloatBuffer mVertexBuffer;
    private Material mMaterial;
    private int[] mVBOHandles = new int [2];

    private int numInstances = 5;

    private int mModelMatrixHandle;
    private int mViewMatrixHandle;
    private int mProjectionMatrixHandle;
    private int mPositionHandle;
    private int mTextureHandle;
    private int mNormalHandle;
    private int mTextureCoordinateHandle;
    private int mLightPositionHandle;
    private int mTextureUniformHandle;
    private int mKAHandle;
    private int mKDHandle;
    private int mKSHandle;
    private int mNSHandle;

    private int numPoints;
    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mNormalDataSize = 3;
    private final int mTextureDataSize = 2;

    public LaurelRender(Context context, GLSurfaceView glSurfaceView) {
        this.mActivityContext = context;
        this.mGLSurfaceView = glSurfaceView;

        List<Float> vertexArray = new VertexLoader().load(mActivityContext, "laurel.obj");
        numPoints = vertexArray.size() / (mPositionDataSize + mNormalDataSize + mTextureDataSize);
        mVertexBuffer = ByteBuffer.allocateDirect(vertexArray.size() * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        for (float value: vertexArray) {
            mVertexBuffer.put(value);
        }
        mVertexBuffer.position(0);

        mMaterial = new MaterialLoader().load(mActivityContext, "laurel.mtl");

        vertexArray.clear();
        System.gc();
        System.runFinalization();
    }

    public void setNumInstances(int numInstances) {
        this.numInstances = numInstances;
        mGLSurfaceView.requestRender();
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            // Set wrapping
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);

            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Use culling to remove back faces.
        GLES30.glEnable(GLES30.GL_CULL_FACE);

        // Enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        /* Set up the view matrix */
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 9.0f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -9.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        /* Set up the light position */
        final float lightX =  0.0f;
        final float lightY =  0.0f;
        final float lightZ =  -2.0f;

        lightPosition = new float [] {lightX, lightY, lightZ};

        final String vertexShader = ShaderProgram.getVertexShader();
        int vertexShaderHandle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertexShaderHandle, vertexShader);
        GLES30.glCompileShader(vertexShaderHandle);

        final String fragmentShader = ShaderProgram.getFragmentShader();
        int fragmentShaderHandle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES30.glCompileShader(fragmentShaderHandle);

        int programHandle = GLES30.glCreateProgram();
        GLES30.glAttachShader(programHandle, vertexShaderHandle);
        GLES30.glAttachShader(programHandle, fragmentShaderHandle);
        GLES30.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES30.glBindAttribLocation(programHandle, 1, "a_TexCoord");
        GLES30.glBindAttribLocation(programHandle, 2, "a_Normal");
        GLES30.glBindAttribLocation(programHandle, 3, "a_ModelMatrix");
        GLES30.glLinkProgram(programHandle);

        mPositionHandle = GLES30.glGetAttribLocation(programHandle, "a_Position");
        mTextureCoordinateHandle = GLES30.glGetAttribLocation(programHandle, "a_TexCoord");
        mNormalHandle = GLES30.glGetAttribLocation(programHandle, "a_Normal");
        mModelMatrixHandle = GLES30.glGetAttribLocation(programHandle, "a_ModelMatrix");

        mViewMatrixHandle = GLES30.glGetUniformLocation(programHandle, "u_ViewMatrix");
        mProjectionMatrixHandle = GLES30.glGetUniformLocation(programHandle, "u_ProjectionMatrix");
        mLightPositionHandle = GLES30.glGetUniformLocation(programHandle, "u_LightPosition");
        mTextureUniformHandle = GLES30.glGetUniformLocation(programHandle, "u_Texture");
        mKAHandle = GLES30.glGetUniformLocation(programHandle, "kA");
        mKDHandle = GLES30.glGetUniformLocation(programHandle, "kD");
        mKSHandle = GLES30.glGetUniformLocation(programHandle, "kS");
        mNSHandle = GLES30.glGetUniformLocation(programHandle, "nS");

        GLES30.glGenBuffers(2, mVBOHandles, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * mBytesPerFloat, mVertexBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 16 * mBytesPerFloat, null, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureHandle);
        GLES30.glUniform1i(mTextureUniformHandle, 0);
        mTextureHandle = loadTexture(mActivityContext, R.drawable.laurel);

        GLES30.glUseProgram(programHandle);
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

        mModelMatrix = ModelGenerator.generate(numInstances);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);

        ByteBuffer mappedByteBuffer = (ByteBuffer) GLES30.glMapBufferRange(
                GLES30.GL_ARRAY_BUFFER, 0, numInstances * 16 * mBytesPerFloat,
                GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT);

        mappedByteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer mappedFloatBuffer = mappedByteBuffer.asFloatBuffer();
        mappedFloatBuffer.put(mModelMatrix);
        GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        drawVertices();
    }

    private void drawVertices() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize) * mBytesPerFloat, 0);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, mTextureDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize) * mBytesPerFloat, mPositionDataSize * mBytesPerFloat);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[0]);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize) * mBytesPerFloat, (mPositionDataSize + mTextureDataSize) * mBytesPerFloat);
        GLES30.glEnableVertexAttribArray(mNormalHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOHandles[1]);
        for (int i = 0; i < 4; i++) {
            int attribLocation = mModelMatrixHandle + i;
            int offset = i * 4 * mBytesPerFloat;
            GLES30.glVertexAttribPointer(attribLocation, 4, GLES30.GL_FLOAT, false, 16 * mBytesPerFloat, offset);
            GLES30.glEnableVertexAttribArray(attribLocation);
            GLES30.glVertexAttribDivisor(attribLocation, 1);
        }

        GLES30.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);
        GLES30.glUniform3fv(mLightPositionHandle, 1, lightPosition, 0);

        GLES30.glUniform3fv(mKAHandle, 1, mMaterial.getAmbient(), 0);
        GLES30.glUniform3fv(mKDHandle, 1, mMaterial.getDiffuse(), 0);
        GLES30.glUniform3fv(mKSHandle, 1, mMaterial.getSpecular(), 0);
        GLES30.glUniform1f(mNSHandle, mMaterial.getShininess());

        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, numPoints, numInstances);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mNormalHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES30.glDisableVertexAttribArray(mModelMatrixHandle);
    }
}