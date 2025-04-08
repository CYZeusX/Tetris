package com.cyzco.game;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.ViewTreeObserver;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{
    private boolean isTransitionStarted = false; // Prevent transition from being triggered multiple times.
    private void fadeInAndOut(FrameLayout splashScreenView)
    {

        splashScreenView.setAlpha(0.9f); // Start invisible
        splashScreenView
                .animate()
                .alpha(1f) // Fade-in animation
                .setDuration(1000) // 1 second duration
                .withEndAction(() ->
                {
                    // Start fade-out after fade-in completes.
                    splashScreenView.animate()
                            .alpha(0f) // Fade-out animation
                            .setDuration(500) // 1 second duration
                            .withEndAction(() -> startNextActivity(splashScreenView)) // Transition after fade-out
                            .start();
                })
                .start();
    }

    private void startNextActivity(FrameLayout splashScreenView)
    {
        // Only trigger transition once
        if (isTransitionStarted)
            return;

        isTransitionStarted = true; // Set flag to true to avoid double transition.

        // Add a listener to wait for the animation to complete
        splashScreenView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            @Override
            public boolean onPreDraw()
            {
                // Make sure the animation has ended before starting the next activity
                if (isTransitionStarted)
                {
                    // Remove the listener
                    splashScreenView.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Start the next activity after a slight delay for smoothness
                    Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // No transition animation
                    finish(); // Finish the splash activity
                }
                return true; // Continue drawing the view
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d("MainActivity", "Retrieved orientation: " + orientation);
        setRequestedOrientation(orientation);

        if (savedInstanceState != null)
            isTransitionStarted = savedInstanceState.getBoolean("isTransitionStarted", false);

        // Get the saved theme mode from SharedPreferences (as you're already doing)
        int savedThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        // Set custom splash screen view
        SplashScreenView splashScreenView = new SplashScreenView(this);
        setContentView(splashScreenView);

        fadeInAndOut(splashScreenView);

        // Hide the status bar and navigation bar
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null)
        {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTransitionStarted", isTransitionStarted);
    }
}