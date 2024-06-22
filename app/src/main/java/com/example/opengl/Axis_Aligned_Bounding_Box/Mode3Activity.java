package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.opengl.R;

public class Mode3Activity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode3);

        String mode = getIntent().getStringExtra("mode");

        // AABB 1 inputs
        EditText aabb1MinXInput = findViewById(R.id.aabb1_min_x);
        EditText aabb1MinYInput = findViewById(R.id.aabb1_min_y);
        EditText aabb1MinZInput = findViewById(R.id.aabb1_min_z);

        EditText aabb1MaxXInput = findViewById(R.id.aabb1_max_x);
        EditText aabb1MaxYInput = findViewById(R.id.aabb1_max_y);
        EditText aabb1MaxZInput = findViewById(R.id.aabb1_max_z);

        // AABB 2 inputs
        EditText aabb2MinXInput = findViewById(R.id.aabb2_min_x);
        EditText aabb2MinYInput = findViewById(R.id.aabb2_min_y);
        EditText aabb2MinZInput = findViewById(R.id.aabb2_min_z);

        EditText aabb2MaxXInput = findViewById(R.id.aabb2_max_x);
        EditText aabb2MaxYInput = findViewById(R.id.aabb2_max_y);
        EditText aabb2MaxZInput = findViewById(R.id.aabb2_max_z);

        // Direction vector inputs
        EditText directionXInput = findViewById(R.id.direction_x);
        EditText directionYInput = findViewById(R.id.direction_y);
        EditText directionZInput = findViewById(R.id.direction_z);

        // Time input
        EditText timeInput = findViewById(R.id.time);

        findViewById(R.id.submit_button).setOnClickListener(v -> {
            // AABB 1 values
            float aabb1MinX = Float.parseFloat(aabb1MinXInput.getText().toString());
            float aabb1MinY = Float.parseFloat(aabb1MinYInput.getText().toString());
            float aabb1MinZ = Float.parseFloat(aabb1MinZInput.getText().toString());

            float aabb1MaxX = Float.parseFloat(aabb1MaxXInput.getText().toString());
            float aabb1MaxY = Float.parseFloat(aabb1MaxYInput.getText().toString());
            float aabb1MaxZ = Float.parseFloat(aabb1MaxZInput.getText().toString());

            // AABB 2 values
            float aabb2MinX = Float.parseFloat(aabb2MinXInput.getText().toString());
            float aabb2MinY = Float.parseFloat(aabb2MinYInput.getText().toString());
            float aabb2MinZ = Float.parseFloat(aabb2MinZInput.getText().toString());

            float aabb2MaxX = Float.parseFloat(aabb2MaxXInput.getText().toString());
            float aabb2MaxY = Float.parseFloat(aabb2MaxYInput.getText().toString());
            float aabb2MaxZ = Float.parseFloat(aabb2MaxZInput.getText().toString());

            // Direction vector values
            float directionX = Float.parseFloat(directionXInput.getText().toString());
            float directionY = Float.parseFloat(directionYInput.getText().toString());
            float directionZ = Float.parseFloat(directionZInput.getText().toString());

            // Time value
            float time = Float.parseFloat(timeInput.getText().toString());

            AABB aabb1 = new AABB(new Point(aabb1MinX, aabb1MinY, aabb1MinZ), new Point(aabb1MaxX, aabb1MaxY, aabb1MaxZ));
            AABB aabb2 = new AABB(new Point(aabb2MinX, aabb2MinY, aabb2MinZ), new Point(aabb2MaxX, aabb2MaxY, aabb2MaxZ));
            Vector direction = new Vector(directionX, directionY, directionZ);
            Ray r = new Ray(aabb1.getCenter(), direction);

            mGLView = new MyGLSurfaceView(this, mode, aabb1, aabb2, r, time);

            setContentView(mGLView);
        });

        findViewById(R.id.return_button).setOnClickListener(v -> {
            finish();
        });
    }
}