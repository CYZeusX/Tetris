package com.cyzco.game;

import android.graphics.Color;
import android.view.Gravity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("CustomSplashScreen")
public final class SplashScreenView extends FrameLayout
{
    public SplashScreenView(@NonNull Context context)
    {
        super(context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int savedThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        int backgroundColor = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ?
                Color.parseColor("#0F0F0F") : Color.parseColor("#D4D3D7");
        setBackgroundColor(backgroundColor);

        // Add an ImageView for the app icon
        ImageView appIcon = new ImageView(context);
        LayoutParams iconParams = new LayoutParams(800, 800); // Size of the icon in dp
        iconParams.gravity = Gravity.CENTER;
        appIcon.setImageResource(R.drawable.app_icon); // Replace with your drawable
        appIcon.setLayoutParams(iconParams);
        addView(appIcon);
    }
}
