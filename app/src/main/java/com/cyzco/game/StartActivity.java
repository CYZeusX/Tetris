package com.cyzco.game;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.view.WindowInsets;
import androidx.activity.EdgeToEdge;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity
{
    Button orient_p;
    Button orient_l;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d("StartActivity", "Retrieved orientation: " + orientation);
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            setRequestedOrientation(orientation);
        }

        setContentView(R.layout.start_game);
        Intent intent = new Intent(StartActivity.this, MainActivity.class);

        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null)
        {
            // Hide the status bar and navigation bar
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

            // Enable gestures for immersive experience (if needed)
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        EdgeToEdge.enable(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        orient_p = findViewById(R.id.orient_at_portrait);
        orient_l = findViewById(R.id.orient_at_land);
        Button btnP = findViewById(R.id.start);
        Button btnL = findViewById(R.id.start);

        btnP.setOnClickListener(v ->
        {
            startActivity(intent);
            overridePendingTransition(R.anim.zero_ani, R.anim.zero_ani);
        });

        btnL.setOnClickListener(v ->
        {
            startActivity(intent);
            overridePendingTransition(R.anim.zero_ani, R.anim.zero_ani);
        });

        orient_p.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            editor.apply();
        });

        orient_l.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            editor.apply();
        });
    }
}