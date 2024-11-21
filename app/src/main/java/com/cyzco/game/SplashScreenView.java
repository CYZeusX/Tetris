package com.cyzco.game;

import android.view.Gravity;
import android.graphics.Color;
import android.content.Context;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import android.annotation.SuppressLint;

@SuppressLint("CustomSplashScreen")
public final class SplashScreenView extends FrameLayout {
    public SplashScreenView(@NonNull Context context) {
        super(context);

        // Set the background color (dark mode, light mode, etc.)
        setBackgroundColor(getResources().getColor(R.color.mainBackgroundLight));

        // Add an ImageView for the app icon
        ImageView appIcon = new ImageView(context);
        LayoutParams iconParams = new LayoutParams(800, 800); // Size of the icon in dp
        iconParams.gravity = Gravity.CENTER;
        appIcon.setImageResource(R.drawable.app_icon); // Replace with your drawable
        appIcon.setLayoutParams(iconParams);

        // Add a TextView for a loading message (optional)
        TextView loadingText = new TextView(context);
        loadingText.setText("Loading...");
        loadingText.setTextColor(Color.WHITE);
        loadingText.setTextSize(18);
        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        textParams.bottomMargin = 100; // Margin from bottom in dp
        loadingText.setLayoutParams(textParams);

        // Add the views to the SplashScreenView
        addView(appIcon);
        addView(loadingText);

        fadeInAnimation(this);
    }

    private void fadeInAnimation(FrameLayout splashScreenView)
    {
        // Fade-in animation for the entire view
        splashScreenView.setAlpha(0f); // Start invisible
        splashScreenView.animate()
                .alpha(1f) // End fully visible
                .setDuration(1000) // Duration in 1/1000 seconds
                .start();
    }
}
