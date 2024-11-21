package com.cyzco.game;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.view.WindowInsets;
import androidx.activity.EdgeToEdge;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity
{
    Button orient_p, orient_l, A, lightDark;
    View rootViewPortrait, rootViewLandscape;

    @SuppressLint({"MissingInflatedId", "ResourceAsColor", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_game);

        // Get the saved theme mode from SharedPreferences (as you're already doing)
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int savedThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        // Retrieve and apply the saved orientation
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d("StartActivity", "Retrieved orientation: " + orientation);
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {setRequestedOrientation(orientation);}

        // Hide status bar and navigation bar
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null)
        {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // Enable Edge-to-Edge mode
        EdgeToEdge.enable(this);

        // Get references to buttons and views
        orient_p = findViewById(R.id.orient_at_portrait);
        orient_l = findViewById(R.id.orient_at_land);
        A = findViewById(R.id.A);
        lightDark = findViewById(R.id.lightDark);
        rootViewPortrait = findViewById(R.id.start_p);
        rootViewLandscape = findViewById(R.id.start_l);

        // Button click listeners
        lightDark.setOnClickListener(v -> toggleTheme());

        Intent intent = new Intent(StartActivity.this, MainActivity.class);

        // Handle start button clicks
        Button portraitStart = findViewById(R.id.start);
        Button landscapeStart = findViewById(R.id.start);

        portraitStart.setOnClickListener(v ->
        {
            startActivity(intent);
            overridePendingTransition(R.anim.zero_ani, R.anim.zero_ani);
        });

        landscapeStart.setOnClickListener(v ->
        {
            startActivity(intent);
            overridePendingTransition(R.anim.zero_ani, R.anim.zero_ani);
        });

        // Handle orientation change buttons
        orient_p.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            sharedPreferences.edit().putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).apply();
        });

        orient_l.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            sharedPreferences.edit().putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).apply();
        });
    }

    private void toggleTheme()
    {
        // Determine the new mode (light or dark)
        int newMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

        // Set the new theme mode
        AppCompatDelegate.setDefaultNightMode(newMode);

        // Save the selected theme mode in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme_mode", newMode).apply();

        // Recreate activity to apply changes
        recreate();
    }

}