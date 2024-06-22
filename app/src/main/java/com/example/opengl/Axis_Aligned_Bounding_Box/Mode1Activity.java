package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.opengl.R;

public class Mode1Activity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode1);

        String mode = getIntent().getStringExtra("mode");

        EditText minXInput = findViewById(R.id.aabb_min_x);
        EditText minYInput = findViewById(R.id.aabb_min_y);
        EditText minZInput = findViewById(R.id.aabb_min_z);

        EditText maxXInput = findViewById(R.id.aabb_max_x);
        EditText maxYInput = findViewById(R.id.aabb_max_y);
        EditText maxZInput = findViewById(R.id.aabb_max_z);

        EditText rayOriginXInput = findViewById(R.id.ray_origin_x);
        EditText rayOriginYInput = findViewById(R.id.ray_origin_y);
        EditText rayOriginZInput = findViewById(R.id.ray_origin_z);

        EditText rayDirectionXInput = findViewById(R.id.ray_direction_x);
        EditText rayDirectionYInput = findViewById(R.id.ray_direction_y);
        EditText rayDirectionZInput = findViewById(R.id.ray_direction_z);

        findViewById(R.id.submit_button).setOnClickListener(v -> {
            float minX = Float.parseFloat(minXInput.getText().toString());
            float minY = Float.parseFloat(minYInput.getText().toString());
            float minZ = Float.parseFloat(minZInput.getText().toString());

            float maxX = Float.parseFloat(maxXInput.getText().toString());
            float maxY = Float.parseFloat(maxYInput.getText().toString());
            float maxZ = Float.parseFloat(maxZInput.getText().toString());

            float rayOriginX = Float.parseFloat(rayOriginXInput.getText().toString());
            float rayOriginY = Float.parseFloat(rayOriginYInput.getText().toString());
            float rayOriginZ = Float.parseFloat(rayOriginZInput.getText().toString());

            float rayDirectionX = Float.parseFloat(rayDirectionXInput.getText().toString());
            float rayDirectionY = Float.parseFloat(rayDirectionYInput.getText().toString());
            float rayDirectionZ = Float.parseFloat(rayDirectionZInput.getText().toString());

            AABB aabb = new AABB(new Point(minX, minY, minZ), new Point(maxX, maxY, maxZ));
            Ray ray = new Ray(new Point(rayOriginX, rayOriginY, rayOriginZ), new Vector(rayDirectionX, rayDirectionY, rayDirectionZ));

            mGLView = new MyGLSurfaceView(this, mode, aabb, ray);

            setContentView(mGLView);
        });

        findViewById(R.id.return_button).setOnClickListener(v -> {
            finish();
        });
    }
}
