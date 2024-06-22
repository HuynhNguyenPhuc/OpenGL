package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.opengl.R;

public class Mode4Activity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode4);

        String mode = getIntent().getStringExtra("mode");

        findViewById(R.id.submit_button).setOnClickListener(v -> {
            mGLView = new MyGLSurfaceView(this, mode);

            setContentView(mGLView);
        });

        findViewById(R.id.return_button).setOnClickListener(v -> {
            finish();
        });
    }
}