package com.example.opengl.Hackathon;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.opengl.R;

public class ViewActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;
    private PlaneRender mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_num_instances);

        FrameLayout glSurfaceContainer = findViewById(R.id.glSurfaceContainer);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);

        mRenderer = new PlaneRender(new float[]{0.0f, 0.0f, 0.0f}, new float[]{3.0f, 0.0f, 0.0f}, new float[]{0.0f, 3.0f, 0.0f}, new float[]{0.0f, 0.0f, -2.0f}, new float[]{1.0f, 1.0f, 1.0f});
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glSurfaceContainer.addView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}