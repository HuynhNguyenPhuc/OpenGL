package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.opengl.R;

public class Mode4Activity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode4);

        String mode = getIntent().getStringExtra("mode");

        EditText rayOriginX = findViewById(R.id.ray_origin_x);
        EditText rayOriginY = findViewById(R.id.ray_origin_y);
        EditText rayOriginZ = findViewById(R.id.ray_origin_z);
        EditText rayDirectionX = findViewById(R.id.ray_direction_x);
        EditText rayDirectionY = findViewById(R.id.ray_direction_y);
        EditText rayDirectionZ = findViewById(R.id.ray_direction_z);
        EditText numInstances = findViewById(R.id.num_instances);

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> {
            float originX = Float.parseFloat(rayOriginX.getText().toString());
            float originY = Float.parseFloat(rayOriginY.getText().toString());
            float originZ = Float.parseFloat(rayOriginZ.getText().toString());
            float directionX = Float.parseFloat(rayDirectionX.getText().toString());
            float directionY = Float.parseFloat(rayDirectionY.getText().toString());
            float directionZ = Float.parseFloat(rayDirectionZ.getText().toString());
            int numOfInstances = Integer.parseInt(numInstances.getText().toString());

            float[] rayOrigin = {originX, originY, originZ};
            float[] rayDirection = {directionX, directionY, directionZ};

            mGLView = new MyGLSurfaceView(this, mode, new Ray(new Point(rayOrigin), new Vector(rayDirection)), numOfInstances);

            setContentView(mGLView);
        });

        Button returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(v -> {
            finish();
        });
    }
}