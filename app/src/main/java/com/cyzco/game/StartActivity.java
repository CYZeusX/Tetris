package com.cyzco.game;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.Button;
import android.graphics.Color;
import android.content.Intent;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class StartActivity extends AppCompatActivity
{
    Button orient_inPortrait, orient_inLandscape, A, B, X, Y, l1, r1, lightDark;
    public RelativeLayout joystickKnob, joystickBase, joystick_pad;
    View rootViewPortrait, rootViewLandscape;

    @SuppressLint({"MissingInflatedId", "ResourceAsColor", "ResourceType", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_game);

        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setRequestedOrientation(orientation);

        // Get the saved theme mode from SharedPreferences
        int savedThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        // Hide the status bar and navigation bar
        hideSystemBars();

        // Get references to buttons and views
        orient_inPortrait = findViewById(R.id.orient_at_portrait);
        orient_inLandscape = findViewById(R.id.orient_at_land);
        A = findViewById(R.id.A);
        B = findViewById(R.id.B);
        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        l1 = findViewById(R.id.l1);
        r1 = findViewById(R.id.r1);
        joystick_pad = findViewById(R.id.joystick_controlArea);
        joystickKnob = findViewById(R.id.joystick_knob);
        joystickBase = findViewById(R.id.joystick_base);
        lightDark = findViewById(R.id.lightDark);
        rootViewPortrait = findViewById(R.id.start_p);
        rootViewLandscape = findViewById(R.id.start_l);

        // Button click listeners
        lightDark.setOnClickListener(v -> toggleTheme());

        Intent intent = new Intent(StartActivity.this, MainActivity.class);

        // Handle start button clicks
        Button portraitStart = findViewById(R.id.start);
        Button landscapeStart = findViewById(R.id.start);

        portraitStart.setOnClickListener(v -> fadeEntering(intent));

        landscapeStart.setOnClickListener(v -> fadeEntering(intent));

        buttonOnTouch(A, "A", intent);
        buttonOnTouch(B, "B", null);
        buttonOnTouch(X, "X", null);
        buttonOnTouch(Y, "Y", null);
        buttonOnTouch(l1, "l1", null);
        buttonOnTouch(r1, "r1", null);

        joystick_pad.setOnTouchListener((v, event)
                -> handleJoystickTouch(event, joystick_pad, joystickKnob));

        // Handle orientation change buttons
        orient_inPortrait.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            sharedPreferences.edit().putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).apply();
        });

        orient_inLandscape.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            sharedPreferences.edit().putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).apply();
        });

        senseControllerConnection();
    }

    public void toggleTheme()
    {
        // Determine the new mode (light or dark)
        int newMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES ?
                AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

        // Set the new theme mode
        AppCompatDelegate.setDefaultNightMode(newMode);

        // Save the selected theme mode in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme_mode", newMode).apply();

        // Recreate activity to apply changes
        recreate();
    }

    private void hideSystemBars()
    {
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

    /* --- Button functions --- */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_A)
        {
            fadeEntering(new Intent(StartActivity.this, MainActivity.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private void buttonOnTouch(Button button, String action, Intent intent)
    {
        button.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    button.setElevation(0f);
                    button.setTextColor(Color.WHITE);
                    if (action.equals("l1") || action.equals("r1")) {
                        button.setBackground(getResources().getDrawable(R.drawable.shoulder_background_pressed, getTheme()));
                        break;
                    }
                    button.setBackground(getResources().getDrawable(R.drawable.button_right_pressed, getTheme()));
                    break;

                case MotionEvent.ACTION_UP:
                    if (intent != null)
                        fadeEntering(intent);
                    button.setElevation(15f);
                    button.setTextColor(com.google.android.material.R.attr.colorOnSecondary);
                    if (action.equals("l1") || action.equals("r1")) {
                        button.setBackground(getResources().getDrawable(R.drawable.shoulder_background, getTheme()));
                        break;
                    }
                    button.setBackground(getResources().getDrawable(R.drawable.button_right, getTheme()));
                    break;
            }
            return true;
        });
    }

    private boolean handleJoystickTouch(MotionEvent event, RelativeLayout joystick, View joystickHandle)
    {
        final float centerX = joystick.getWidth() / 2f;
        final float centerY = joystick.getHeight() / 2f;
        final float maxRadius = Math.min(centerX, centerY) / 2.5f;
        //knob switch radius

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            {
                final float deltaX = event.getX() - centerX;
                final float deltaY = event.getY() - centerY;
                final float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                final float clampedDistance = Math.min(distance, maxRadius);

                // Normalize values
                final float normalizedX = distance == 0 ? 0 : (deltaX / maxRadius) * (clampedDistance / distance);
                final float normalizedY = distance == 0 ? 0 : (deltaY / maxRadius) * (clampedDistance / distance);

                // Apply dead zone
                final float adjustedNormalizedX = Math.abs(normalizedX) < 0 ? 0 : normalizedX;
                final float adjustedNormalizedY = Math.abs(normalizedY) < 0 ? 0 : normalizedY;

                // Animate Knob movement
                final float handleX = centerX + adjustedNormalizedX * maxRadius;
                final float handleY = centerY + adjustedNormalizedY * maxRadius;
                joystickHandle.setX(handleX - joystickHandle.getWidth() / 2f);
                joystickHandle.setY(handleY - joystickHandle.getHeight() / 2f);

                // Animate Knob rotation
                joystickHandle.setRotationX(-adjustedNormalizedY * 35);
                joystickHandle.setRotationY(adjustedNormalizedX * 35);

                break;
            }

            case MotionEvent.ACTION_UP: {
                joystickHandle.setX(centerX - joystickHandle.getWidth() / 2f);
                joystickHandle.setY(centerY - joystickHandle.getHeight() / 2f);

                joystickHandle.setRotationX(0);
                joystickHandle.setRotationY(0);

                break;
            }

            default:
                break;
        }
        return true;
    }

    private void isGamePadConnected()
    {
        boolean isConnected;
        int visibility = View.VISIBLE;

        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds)
        {
            InputDevice device = InputDevice.getDevice(deviceId);
            assert device != null;
            isConnected = (device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;

            visibility = isConnected ? View.GONE : View.VISIBLE;
        }

        joystick_pad.setVisibility(visibility);
        joystickBase.setVisibility(visibility);
        joystickKnob.setVisibility(visibility);
        A.setVisibility(visibility);
        B.setVisibility(visibility);
        X.setVisibility(visibility);
        Y.setVisibility(visibility);
        l1.setVisibility(visibility);
        r1.setVisibility(visibility);
    }

    private void senseControllerConnection()
    {
        final Handler handler = new Handler();

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                isGamePadConnected();
                handler.postDelayed(this, 1000/240);
            }
        };

        handler.post(runnable);
    }

    private void fadeEntering(Intent intent)
    {
        startActivity(intent);
        overridePendingTransition(R.anim.enter_fade, R.anim.exit_fade);
    }
}