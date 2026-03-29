package com.cyzco.game;

import android.os.Bundle;
import android.content.Intent;
import android.widget.FrameLayout;
import android.view.ViewTreeObserver;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{
    private boolean isTransitionStarted = false; // Prevent transition from being triggered multiple times.
    private void fadeInAndOut(FrameLayout splashScreenView)
    {
        splashScreenView.setAlpha(0.9f); // Start semi-transparent
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
                    intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zero_ani, R.anim.zero_ani);
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTransitionStarted", isTransitionStarted);
    }
}