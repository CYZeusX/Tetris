package com.cyzco.game;

import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import android.view.WindowInsets;
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
        splashScreenView.animate()
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
        if (!isTransitionStarted)
        {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            isTransitionStarted = savedInstanceState.getBoolean("isTransitionStarted", false);
        }

        // Check current theme (light/dark) and apply it
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int mode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);

        // Set custom splash screen view
        SplashScreenView splashScreenView = new SplashScreenView(this);
        setContentView(splashScreenView);

        fadeInAndOut(splashScreenView);

        // Hide status bar and navigation bar
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null)
        {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTransitionStarted", isTransitionStarted);
    }

}
