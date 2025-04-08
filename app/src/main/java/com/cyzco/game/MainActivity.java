package com.cyzco.game;

import java.util.Map;

import android.os.Build;
import android.util.Log;
import java.util.HashMap;
import android.os.Bundle;
import java.util.Objects;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.Button;
import android.os.Parcelable;
import android.graphics.Color;
import android.widget.TextView;
import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidx.activity.EdgeToEdge;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity
{
    Button A, X, Y, l1, r1, orientPortrait, orientLandscape, pause, lightDark, setting;
    RelativeLayout pad_mid, upDown, dpad;
    Vibrator vibrator;
    TextView scores, lines;
    SurfaceView monitor;
    TetrisGame tetrisGame = new TetrisGame(this);
    SettingsDialogFragment settingsFragment = new SettingsDialogFragment();
    PauseDialogFragment pauseFragment = new PauseDialogFragment();

    @SuppressLint({"MissingInflatedId", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d("MainActivity", "Retrieved orientation: " + orientation);
        setRequestedOrientation(orientation);

        TetrisGame tetrisGame;
        setContentView(R.layout.activity_main);

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

        // Initialize views and game logic
        A = findViewById(R.id.A);
        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        l1 = findViewById(R.id.l1);
        r1 = findViewById(R.id.r1);
        dpad = findViewById(R.id.dpad);
        pad_mid = findViewById(R.id.pad_mid);
        upDown = findViewById(R.id.up_down);
        orientPortrait = findViewById(R.id.orient_at_portrait);
        orientLandscape = findViewById(R.id.orient_at_land);
        pause = findViewById(R.id.pause);
        setting = findViewById(R.id.setting);
        lightDark = findViewById(R.id.lightDark);
        scores = findViewById(R.id.scores);
        lines = findViewById(R.id.lines);
        monitor = findViewById(R.id.mon);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tetrisGame = new TetrisGame(this);
        tetrisGame.initEffects(getResources());
        startGameLoop();
        setButtonListeners();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        //??
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the game state
        int linesCleared = savedInstanceState.getInt("linesCleared", 0);
        int scoreGained = savedInstanceState.getInt("scoreGained", 0);
        Parcelable gameState = savedInstanceState.getParcelable("gameState");

        tetrisGame.setLinesCleared(linesCleared);
        tetrisGame.setScoreGained(scoreGained);

        // Ensure the game is rendered correctly
        tetrisGame.renderGame(monitor);
    }   //??

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Handle the configuration change (e.g., update UI if necessary)
        tetrisGame.renderGame(monitor); // Ensure the game is re-rendered
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Resume rendering or related operations on SurfaceView
        // Ensure the SurfaceView has been recreated before using it
        SurfaceView surfaceView = findViewById(R.id.mon);
        if (surfaceView != null) {
            // Initialize or resume rendering logic
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            setRequestedOrientation(orientation);
    }

    @Override
    public void onBackPressed()
    {
        // Save the current orientation
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int currentOrientation = getResources().getConfiguration().orientation;
        editor.putInt("orientation", currentOrientation == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        editor.apply();
        super.onBackPressed();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation)
    {
        super.setRequestedOrientation(requestedOrientation);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("orientation", requestedOrientation);
        editor.apply();
        Log.d("MainActivity", "Orientation set to: " + requestedOrientation);
    }

    //control actions for each buttons
    @SuppressLint("ClickableViewAccessibility")
    private void setButtonListeners()
    {
        AtomicReference<SharedPreferences> sharedPreferences = new AtomicReference<>(getSharedPreferences("AppPreferences", MODE_PRIVATE));
        SharedPreferences.Editor editor = sharedPreferences.get().edit();

        orientPortrait.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).apply();
        });

        orientLandscape.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).apply();
        });

        pause.setOnClickListener(v ->
        {
            tetrisGame.togglePause();
            pauseFragment.show(getSupportFragmentManager(), "pauseDialog");
        });

        setting.setOnClickListener(v ->
        {
            tetrisGame.togglePause();
            settingsFragment.show(getSupportFragmentManager(), "settingsDialog");
        });

        // Set up d-pad buttons
        setupContinuousMovement();

        // Drop the block immediately
        buttonOnTouch(A, "A");

        // Rotate the block anti-clockwise
        buttonOnTouch(X, "X");

        // Rotate the block clockwise
        buttonOnTouch(Y, "Y");

        // Rotate the block anti-clockwise
        buttonOnTouch(l1, "l1");

        // Rotate the block clockwise
        buttonOnTouch(r1, "r1");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonOnTouch(Button button, String action)
    {
        button.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    button.setElevation(0f);
                    tetrisGame.getAction(action);
                    setVibrateAndRender();
                    button.setTextColor(Color.WHITE);
                    break;

                case MotionEvent.ACTION_UP:
                    button.setElevation(15f);
                    button.setTextColor(com.google.android.material.R.attr.colorOnSecondary);
                    break;
            }
            return true;
        });
    }

    // performs control actions for left buttons
    private void doMovementAction(String action)
    {
        setMovementAction(action);
        tetrisGame.renderGame(monitor);
    }

    // setup action logics for left control buttons
    private void setMovementAction(String action)
    {
        tetrisGame.getAction(action);
    }

    private final Map<String, Boolean> keyState = new HashMap<>();

    // Create a new Handler for this button
    Handler movementHandler = new Handler();

    // Tracks which direction is active
    final String[] currentDirection = {null};
    Runnable movementRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentDirection[0] != null) {
                doMovementAction(currentDirection[0]); // Use current direction
                movementHandler.postDelayed(movementRunnable, 100); // Repeat
            }
        }
    };

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        String action = switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT -> "a";
            case KeyEvent.KEYCODE_DPAD_RIGHT -> "d";
            case KeyEvent.KEYCODE_DPAD_DOWN -> "s";
            default -> null;
        };

        if (action != null && (keyState.get(action) == null || Boolean.FALSE.equals(keyState.get(action))))
        {
            Handler movementHandler = new Handler();

            keyState.put(action, true);
            doMovementAction(action);
            movementHandler.postDelayed(() -> doMovementAction(action), 100);
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        String action = switch (keyCode)
        {
            case KeyEvent.KEYCODE_BUTTON_A -> "A";
            case KeyEvent.KEYCODE_BUTTON_X -> "X";
            case KeyEvent.KEYCODE_BUTTON_Y -> "Y";
            case KeyEvent.KEYCODE_BUTTON_L1 -> "l1";
            case KeyEvent.KEYCODE_BUTTON_R1 -> "r1";
            default -> null;
        };

        if (action != null && (keyState.get(action) == null || Boolean.FALSE.equals(keyState.get(action))))
        {
            keyState.put(action, true);
            tetrisGame.getAction(action);
            setVibrateAndRender();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        String action = switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A -> "A";
            case KeyEvent.KEYCODE_BUTTON_X -> "X";
            case KeyEvent.KEYCODE_BUTTON_Y -> "Y";
            case KeyEvent.KEYCODE_BUTTON_L1 -> "l1";
            case KeyEvent.KEYCODE_BUTTON_R1 -> "r1";
            case KeyEvent.KEYCODE_DPAD_LEFT -> "a";
            case KeyEvent.KEYCODE_DPAD_RIGHT -> "d";
            case KeyEvent.KEYCODE_DPAD_DOWN -> "s";
            default -> null;
        };

        if (action != null) {
            keyState.put(action, false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // feature is added to plan -> to see if exclusive UI layout is possible
    private boolean isGamepadConnected()
    {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds)
        {
            InputDevice device = InputDevice.getDevice(deviceId);
            assert device != null;
            if ((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                return true;
        }
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
        {
            // Joystick movement
            float leftStickX = event.getAxisValue(MotionEvent.AXIS_X);
            float leftStickY = event.getAxisValue(MotionEvent.AXIS_Y);
            float rightStickX = event.getAxisValue(MotionEvent.AXIS_Z);
            float rightStickY = event.getAxisValue(MotionEvent.AXIS_RZ);

            // Process joystick input
            processJoystickInput(leftStickX, leftStickY, rightStickX, rightStickY);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private void processJoystickInput(float lx, float ly, float rx, float ry)
    {
        // Handle joystick input
        Log.d("Gamepad", "Left Stick X: " + lx + " Y: " + ly);
        Log.d("Gamepad", "Right Stick X: " + rx + " Y: " + ry);
    }

    private void setVibrateAndRender()
    {
        tetrisGame.setVibrator(new long[]{0, 8}, new int[]{0, 80});
        tetrisGame.renderGame(monitor);
    }

    @SuppressLint({"ClickableViewAccessibility", "ResourceAsColor"})
    private void setupContinuousMovement()
    {
        // Create a new Handler for this button
        Handler movementHandler = new Handler();

        // Tracks which direction is active
        final String[] currentDirection = {null};
        Runnable movementRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentDirection[0] != null) {
                    doMovementAction(currentDirection[0]); // Use current direction
                    movementHandler.postDelayed(this, 100); // Repeat
                }
            }
        };

        dpad.setOnTouchListener((v, event) ->
        {
            float x = event.getX();
            float y = event.getY();

            float centerX = v.getWidth() / 2f;
            float centerY = v.getHeight() / 2f;

            float dx = x - centerX;
            float dy = y - centerY;

            String newDirection = null;

            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance > 20) // Minimum distance for movement (dead zone)
                    {
                        if (Math.abs(dx) > Math.abs(dy))
                            newDirection = dx < 0 ? "a" : "d";
                        else newDirection = dy < 0 ? "w" : "s";
                    }

                    if (!Objects.equals(currentDirection[0], newDirection)) {
                        currentDirection[0] = newDirection;
                        movementHandler.removeCallbacks(movementRunnable);
                        if (newDirection != null)
                            movementHandler.post(movementRunnable);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    currentDirection[0] = null;
                    movementHandler.removeCallbacks(movementRunnable);
                    break;
            }
            return true;
        });
    }

    private void startGameLoop()
    {
        final Handler fallHandler = new Handler();
        final Handler moveHandler = new Handler();

        // Falling speed loop
        Runnable fallRunnable = new Runnable()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void run()
            {
                lines.setText("lines:\n"+tetrisGame.getLinesCleared());
                scores.setText("score:\n"+tetrisGame.getScoreGained());
                tetrisGame.updateGame();
                tetrisGame.renderGame(monitor);
                fallHandler.postDelayed(this, 800); // Falling speed interval
            }
        };

        // Start the falling loop
        fallHandler.post(fallRunnable);

        // Movement speed loop (optional continuous movement handling)
        Runnable moveRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                // Movement speed interval
                moveHandler.postDelayed(this, 50);
            }
        };

        // Start the movement loop
        moveHandler.post(moveRunnable);
    }
}