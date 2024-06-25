package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    private Render mRenderer;
    private LaurelRender mLaurelRenderer;

    public MyGLSurfaceView(Context context, String mode, Object... args) {
        super(context);

        setEGLContextClientVersion(3);

        if (!mode.equals("mode4")) {
            mRenderer = new Render(this, mode, args);
            setRenderer(mRenderer);
        }
        else {
            mLaurelRenderer = new LaurelRender(context, args);
            setRenderer(mLaurelRenderer);
        }
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}