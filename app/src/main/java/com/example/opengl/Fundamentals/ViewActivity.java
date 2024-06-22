package com.example.opengl.Fundamentals;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.opengl.R;

public class ViewActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;
    private LaurelRender mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_num_instances);

        EditText instanceCountInput = findViewById(R.id.instanceCountInput);
        Button updateButton = findViewById(R.id.updateButton);
        FrameLayout glSurfaceContainer = findViewById(R.id.glSurfaceContainer);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);

        mRenderer = new LaurelRender(this, mGLSurfaceView);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glSurfaceContainer.addView(mGLSurfaceView);

        updateButton.setOnClickListener(v -> {
            String inputText = instanceCountInput.getText().toString();
            if (!inputText.isEmpty()) {
                int instanceCount = Integer.parseInt(inputText);
                mRenderer.setNumInstances(instanceCount);
                mGLSurfaceView.requestRender();
            }
        });
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