package com.example.opengl.MeshRender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CapsuleRender implements GLSurfaceView.Renderer {
    private final Context mActivityContext;
    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];
    private final float[] mMVNormalMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private float[] lightPosition = new float[3];

    private final FloatBuffer mVertexBuffer;
    private final Material mMaterial;
    private final FloatBuffer mOffsetBuffer;

    private final int numInstances = 5;
    private final float[] offsets = new float [3 * numInstances];
    private int mOffsetHandle;

    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mMVNormalMatrixHandle;
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

    private final int numTriangles;
    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mNormalDataSize = 3;
    private final int mTextureDataSize = 2;
    private final int mOffsetDataSize = 3;

    public CapsuleRender(Context context) {
        this.mActivityContext = context;

        List<Float> vertexArray = new VertexLoader().load(context, "capsule.obj");

        numTriangles = vertexArray.size() / (mPositionDataSize + mNormalDataSize + mTextureDataSize);

        mVertexBuffer = ByteBuffer.allocateDirect(vertexArray.size() * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        for (float value: vertexArray) {
            mVertexBuffer.put(value);
        }

        mVertexBuffer.position(0);

        for (int i = 0; i < numInstances; i++){
            offsets[3 * i] =  4 * (float) i - 2 * numInstances;
            offsets[3 * i + 1] = 0.0f;
            offsets[3 * i + 2] = 0.0f;
        }

        mOffsetBuffer = ByteBuffer.allocateDirect(offsets.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mOffsetBuffer.put(offsets);
        mOffsetBuffer.position(0);

        vertexArray.clear();

        System.gc();
        System.runFinalization();

        mMaterial = new MaterialLoader().load(context, "capsule.mtl");
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

    private String getVertexShader(){
        return
                "uniform mat4 u_MVPMatrix;\n" +
                "uniform mat4 u_MVMatrix;\n" +
                "uniform mat4 u_MVNormalMatrix;\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec3 a_Normal;\n" +
                "attribute vec2 a_TexCoord;\n" +
                "attribute vec3 a_Offset;\n" +
                "varying vec3 v_Position;\n" +
                "varying vec3 v_Normal;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "   vec4 offsetPosition = a_Position + vec4(a_Offset, 0.0);\n" +
                "   v_Position = (u_MVMatrix * offsetPosition).xyz;\n" +
                "   v_Normal = (u_MVNormalMatrix * vec4(a_Normal, 0.0)).xyz;\n" +
                "   v_TexCoord = a_TexCoord;\n" +
                "   gl_Position = u_MVPMatrix *offsetPosition;\n" +
                "}\n";
    }

    private String getFragmentShader(){
        return
                "precision mediump float;\n" +
                "uniform vec3 kA;\n" +
                "uniform vec3 kD;\n" +
                "uniform vec3 kS;\n" +
                "uniform float nS;\n" +
                "uniform vec3 u_LightPosition;\n" +
                "uniform sampler2D u_Texture;\n" +
                "varying vec3 v_Position;\n" +
                "varying vec3 v_Normal;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "    vec3 N = normalize(v_Normal);\n" +
                "    vec3 L = normalize(u_LightPosition - v_Position);\n" +
                "    vec3 V = normalize(-v_Position);\n" +
                "    float d_component = max(dot(N, L), 0.1);\n" +
                "    vec3 H = normalize(L + V);\n" +
                // "    vec3 R = normalize(reflect(-L, N));\n" +
                // "    float s_component = pow(max(dot(R, V), 0.1), nS);\n" +
                "    float s_component = pow(max(2.0 * dot(N, H) * dot(N, H)- 1.0, 0.1), nS);\n" +
                "    float sourceDistance = distance(u_LightPosition, v_Position);\n" +
                "    float attenuation = 1.0 / (1.0 + 0.25 * pow(sourceDistance, 2.0));\n" +
                "    vec3 color = kA + kD * d_component * attenuation + kS * s_component;\n" +
                "    gl_FragColor = texture2D(u_Texture, v_TexCoord) * vec4(color, 1.0) ;\n" +
                "}\n";
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
        final float eyeZ = 10.0f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        /* Set up the light position */
        final float lightX =  0.0f;
        final float lightY =  1.0f;
        final float lightZ =  0.0f;

        lightPosition = new float [] {lightX, lightY, lightZ};

        final String vertexShader = getVertexShader();
        int vertexShaderHandle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertexShaderHandle, vertexShader);
        GLES30.glCompileShader(vertexShaderHandle);

        final String fragmentShader = getFragmentShader();
        int fragmentShaderHandle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES30.glCompileShader(fragmentShaderHandle);

        int programHandle = GLES30.glCreateProgram();
        GLES30.glAttachShader(programHandle, vertexShaderHandle);
        GLES30.glAttachShader(programHandle, fragmentShaderHandle);
        GLES30.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES30.glBindAttribLocation(programHandle, 1, "a_TexCoord");
        GLES30.glBindAttribLocation(programHandle, 2, "a_Normal");
        GLES30.glBindAttribLocation(programHandle, 3, "a_Offset");
        GLES30.glLinkProgram(programHandle);

        mPositionHandle = GLES30.glGetAttribLocation(programHandle, "a_Position");
        mTextureCoordinateHandle = GLES30.glGetAttribLocation(programHandle, "a_TexCoord");
        mNormalHandle = GLES30.glGetAttribLocation(programHandle, "a_Normal");
        mOffsetHandle = GLES30.glGetAttribLocation(programHandle, "a_Offset");

        mMVPMatrixHandle = GLES30.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES30.glGetUniformLocation(programHandle, "u_MVMatrix");
        mMVNormalMatrixHandle = GLES30.glGetUniformLocation(programHandle, "u_MVNormalMatrix");
        mLightPositionHandle = GLES30.glGetUniformLocation(programHandle, "u_LightPosition");
        mTextureUniformHandle = GLES30.glGetUniformLocation(programHandle, "u_Texture");
        mKAHandle = GLES30.glGetUniformLocation(programHandle, "kA");
        mKDHandle = GLES30.glGetUniformLocation(programHandle, "kD");
        mKSHandle = GLES30.glGetUniformLocation(programHandle, "kS");
        mNSHandle = GLES30.glGetUniformLocation(programHandle, "nS");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureHandle);
        GLES30.glUniform1i(mTextureUniformHandle, 0);
        mTextureHandle = loadTexture(mActivityContext, R.drawable.capsule);

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


        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0, 1.0f, 0);

        drawVertices(mVertexBuffer, mOffsetBuffer);
    }

    private void drawVertices(final FloatBuffer vertexBuffer, final FloatBuffer offsetBuffer) {
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize)* mBytesPerFloat, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.position(mPositionDataSize);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, mTextureDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize) * mBytesPerFloat, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);

        vertexBuffer.position(mPositionDataSize + mTextureDataSize);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false, (mPositionDataSize + mNormalDataSize + mTextureDataSize) * mBytesPerFloat, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mNormalHandle);

        offsetBuffer.position(0);
        GLES30.glVertexAttribPointer(mOffsetHandle, mOffsetDataSize, GLES30.GL_FLOAT, false, mOffsetDataSize * mBytesPerFloat, offsetBuffer);
        GLES30.glEnableVertexAttribArray(mOffsetHandle);
        GLES30.glVertexAttribDivisor(mOffsetHandle, 1);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mMVNormalMatrix, 0, mMVMatrix, 0);

        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVNormalMatrixHandle, 1, true, mMVNormalMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES30.glUniform3fv(mLightPositionHandle, 1, lightPosition, 0);

        GLES30.glUniform3fv(mKAHandle, 1, mMaterial.getAmbient(), 0);
        GLES30.glUniform3fv(mKDHandle, 1, mMaterial.getDiffuse(), 0);
        GLES30.glUniform3fv(mKSHandle, 1, mMaterial.getSpecular(), 0);
        GLES30.glUniform1f(mNSHandle, mMaterial.getShininess());

        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, numTriangles, numInstances);
    }
}