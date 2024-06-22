package com.example.opengl.Axis_Aligned_Bounding_Box;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.opengl.R;

public class ViewActivity extends Activity implements View.OnClickListener {
    private String selectedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize mode selection buttons
        Button mode1Button = findViewById(R.id.button_mode1);
        Button mode2Button = findViewById(R.id.button_mode2);
        Button mode3Button = findViewById(R.id.button_mode3);
        Button mode4Button = findViewById(R.id.button_mode4);

        // Set click listeners for mode buttons
        mode1Button.setOnClickListener(this);
        mode2Button.setOnClickListener(this);
        mode3Button.setOnClickListener(this);
        mode4Button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        // Handle mode button clicks
        if (buttonId == R.id.button_mode1){
            selectedMode = "mode1";
        }
        else if (buttonId == R.id.button_mode2){
            selectedMode = "mode2";
        }
        else if (buttonId == R.id.button_mode3){
            selectedMode = "mode3";
        }
        else if (buttonId == R.id.button_mode4){
            selectedMode = "mode4";
        }
        navigateToModeActivity(getModeClass(selectedMode));
    }

    private Class<?> getModeClass(String mode) {
        switch (mode) {
            case "mode1":
                return Mode1Activity.class;
            case "mode2":
                return Mode2Activity.class;
            case "mode3":
                return Mode3Activity.class;
            case "mode4":
                return Mode4Activity.class;
            default:
                return null;
        }
    }

    private void navigateToModeActivity(Class<?> activityClass) {
        if (selectedMode == null || selectedMode.isEmpty()) {
            Toast.makeText(ViewActivity.this, "Please select a mode", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ViewActivity.this, activityClass);
        intent.putExtra("mode", selectedMode);
        startActivity(intent);
    }
}