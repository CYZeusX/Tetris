package com.cyzco.game;

import java.util.Map;
import android.util.Log;
import java.util.HashMap;
import android.os.Bundle;
import android.view.View;
import android.os.Looper;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.Button;
import android.graphics.Color;
import android.widget.TextView;
import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import android.widget.RelativeLayout;
import android.view.ViewTreeObserver;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements GameOverFragment.GameOverListener
{
    public Button A, B, X, Y, l1, r1, orientPortrait, orientLandscape, pause, setting;
    public RelativeLayout joystickKnob, joystickBase, joystick_pad;
    public Vibrator vibrator;
    public TextView scores, lines;
    public SurfaceView monitor;
    public TetrisGame tetrisGame;
    public SettingsDialogFragment settingsFragment = new SettingsDialogFragment();
    public PauseDialogFragment pauseFragment = new PauseDialogFragment();

    // Game Loop resource
    public Handler gameLoopHandler;
    public Runnable gameLoopRunnable;
    public boolean isGameLoopRunning = false;

    // Game Loop Settings
    private static final long FPS_RATE = 240;
    private static final long GAME_LOOP_INTERVAL_MS = 1000 / FPS_RATE;
    public long nextFallTime = 0; // Time when the next gravity fall should happen

    /* 手感 Control */
    private static final float DEAD_ZONE = 0.1f;
    private static final long DAS_DELAY = 160;
    private static final long ARR_INTERVAL = 70;
    public long fallingSpeed = 750;  //Large value = slow speed

    /* Controller Support Variables */
    // Constants for joystick processing
    private static final float JOYSTICK_DEAD_ZONE = 2.5f; // Ignore movements smaller than this
    private static final float JOYSTICK_ACTION_THRESHOLD = 0.7f; // Stick must pass this threshold for action

    // State for joystick directions (to prevent multiple triggers)
    public boolean isStickLeftActive = false;
    public boolean isStickRightActive = false;
    public boolean isStickDownActive = false;

    // --- State for Continuous Movement (DAS/ARR) ---
    public enum MoveDirection {NONE, LEFT, RIGHT} // Helper enum

    // Horizontal Movement Timing
    public MoveDirection activeHorizontalDirection = MoveDirection.NONE;
    private long horizontalMoveStartTime = 0; // Time when L/R stick was first activated
    public long nextHorizontalMoveTime = 0;  // Time when the next auto-repeat move should happen
    public boolean dasChargedHorizontal = false; // True if initial delay has passed

    // Vertical (Soft Drop) Movement Timing
    public boolean softDropActive = false; // Separate flag for down movement state
    private long softDropStartTime = 0; // Time when Down stick was first activated
    public long nextSoftDropTime = 0; // Time for next auto-repeat soft drop
    public boolean dasChargedSoftDrop = false; // True if initial soft drop delay passed


    @SuppressLint({"MissingInflatedId", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tetrisGame = new TetrisGame(this);

        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setRequestedOrientation(orientation);

        // Hide the status bar and navigation bar
        hideSystemBars();

        // Initialize views and game logic
        A = findViewById(R.id.A);
        B = findViewById(R.id.B);
        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        l1 = findViewById(R.id.l1);
        r1 = findViewById(R.id.r1);
        joystick_pad = findViewById(R.id.joystick_controlArea);
        joystickKnob = findViewById(R.id.joystick_knob);
        joystickBase = findViewById(R.id.joystick_base);

        orientPortrait = findViewById(R.id.orient_at_portrait);
        orientLandscape = findViewById(R.id.orient_at_land);
        pause = findViewById(R.id.pause);
        setting = findViewById(R.id.setting);
        scores = findViewById(R.id.scores);
        lines = findViewById(R.id.lines);
        monitor = findViewById(R.id.monitor);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (joystick_pad != null) {
            joystick_pad.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Layout is complete, views have dimensions
                    resetJoystickPosition();

                    // --- Remove the listener so it doesn't run repeatedly ---
                    joystick_pad.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        tetrisGame.initEffects(getResources());
        setButtonListeners();
        isGamePadConnected();
        startGameLoop();
    }

    // Inside MainActivity.java
    public void restartGame() {
        Log.d("GameRestart", "Restarting game...");

        // Stops the current loop execution
        // Stops the handler from posting more runnables
        pauseGameLoop();

        // Create the new game instance
        tetrisGame = new TetrisGame(this);
        resumeGameLoop();
        Log.d("GameRestart", "Game restart complete.");
    }

    @Override
    public void onRestartGame() {
        tetrisGame.togglePause();
        tetrisGame = new TetrisGame(this);
    }

    @Override
    public void onQuitGame() {
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        hideSystemBars();
        outState.putInt("linesCleared", tetrisGame.getLinesCleared());
        outState.putInt("scoreGained", tetrisGame.getScoreGained());
        outState.putBundle("gameState", tetrisGame.saveGameState());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        hideSystemBars();

        // Restore the game state
        int linesCleared = savedInstanceState.getInt("linesCleared");
        int scoreGained = savedInstanceState.getInt("scoreGained");
        Bundle gameState = savedInstanceState.getBundle("gameState");
        if (gameState != null) {
            tetrisGame.restoreGameState(gameState);
            tetrisGame.renderGame(monitor);
        }

        tetrisGame.setLinesCleared(linesCleared);
        tetrisGame.setScoreGained(scoreGained);

        lines.setText("lines:\n" + tetrisGame.getLinesCleared());
        scores.setText("score:\n" + tetrisGame.getScoreGained());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
    {
        // Handle the configuration change (e.g., update UI if necessary)
        super.onConfigurationChanged(newConfig);
        hideSystemBars();
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
    protected void onPause()
    {
        super.onPause();
        Log.d("MainActivity: onPause()", "onPause() called.");

        // Always stop the loop posting when the activity pauses
        pauseGameLoop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("MainActivity: onResume()", "onResume() called.");

        hideSystemBars();
        resumeGameLoop();
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

//    @Override
//    public void onSwitchapp()
//    {
//        tetrisGame.togglePause();
//    }

    @Override
    public void setRequestedOrientation(int requestedOrientation)
    {
        super.setRequestedOrientation(requestedOrientation);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("orientation", requestedOrientation).apply();
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
                pauseFragment.show(getSupportFragmentManager(), "pauseDialog"));

        setting.setOnClickListener(v ->
            settingsFragment.show(getSupportFragmentManager(), "settingsDialog"));

        // Set up joystick
        joystick_pad.setOnTouchListener((v, event) -> handleJoystickTouch(event, joystick_pad, joystickKnob));

        // Drop the block immediately
        buttonOnTouch(A, "A");

        // Rotate the block clockwise
        buttonOnTouch(B, "B");

        // Rotate the block anti-clockwise
        buttonOnTouch(X, "X");

        // Rotate the block clockwise
        buttonOnTouch(Y, "Y");

        // Rotate the block anti-clockwise
        buttonOnTouch(l1, "l1");

        // Rotate the block clockwise
        buttonOnTouch(r1, "r1");
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private void buttonOnTouch(Button button, String action)
    {
        button.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    tetrisGame.getAction(action);
                    button.setElevation(0f);
                    button.setTextColor(Color.WHITE);
                    if (action.equals("l1") || action.equals("r1")) {
                        button.setBackground(getResources().getDrawable(R.drawable.shoulder_background_pressed, getTheme()));
                        break;
                    }
                    button.setBackground(getResources().getDrawable(R.drawable.button_right_pressed, getTheme()));
                    break;

                case MotionEvent.ACTION_UP:
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

                // Apply dead zone for animation
                final float adjustedAnimateX = Math.abs(normalizedX) < 0 ? 0 : normalizedX;
                final float adjustedAnimateY = Math.abs(normalizedY) < 0 ? 0 : normalizedY;

                // Apply dead zone for movement
                final float adjustedNormalizedX = Math.abs(normalizedX) < DEAD_ZONE ? 0 : normalizedX;
                final float adjustedNormalizedY = Math.abs(normalizedY) < DEAD_ZONE ? 0 : normalizedY;

                // Animate Knob movement
                final float handleX = centerX + adjustedAnimateX * maxRadius;
                final float handleY = centerY + adjustedAnimateY * maxRadius;
                joystickHandle.setX(handleX - joystickHandle.getWidth() / 2f);
                joystickHandle.setY(handleY - joystickHandle.getHeight() / 2f);

                // Animate Knob rotation
                joystickHandle.setRotationX(-adjustedAnimateY * 35);
                joystickHandle.setRotationY(adjustedAnimateX * 35);

                // Determine direction
                String newDirection = null;
                if (adjustedNormalizedX != 0 || adjustedNormalizedY != 0) {
                    if (Math.abs(adjustedNormalizedX) > Math.abs(adjustedNormalizedY))
                        newDirection = adjustedNormalizedX < 0 ? "a" : "d";
                    else
                        newDirection = adjustedNormalizedY < 0 ? "w" : "s";
                }

                // Update direction + start movement
                updateJoystickDirection(newDirection);
                break;
            }

            case MotionEvent.ACTION_UP: {
                joystickHandle.setX(centerX - joystickHandle.getWidth() / 2f);
                joystickHandle.setY(centerY - joystickHandle.getHeight() / 2f);

                joystickHandle.setRotationX(0);
                joystickHandle.setRotationY(0);

                //Stop movement
                updateJoystickDirection(null);
                break;
            }

            default:
                break;
        }
        return true;
    }

    public void updateJoystickDirection(String newDirection)
    {
        long currentTime = System.currentTimeMillis();

        // Determine which direction stopped (if any)
        boolean stoppedLeft = activeHorizontalDirection == MoveDirection.LEFT && !"a".equals(newDirection);
        boolean stoppedRight = activeHorizontalDirection == MoveDirection.RIGHT && !"d".equals(newDirection);
        boolean stoppedDown = softDropActive && !"s".equals(newDirection);

        // Update state based on newDirection
        if ("a".equals(newDirection))
        {
            if (!isStickLeftActive) {
                isStickLeftActive = true;
                doMovementAction("a");
                activeHorizontalDirection = MoveDirection.LEFT;
                horizontalMoveStartTime = currentTime;
                dasChargedHorizontal = false;
                nextHorizontalMoveTime = 0;
            }
        }

        else if ("d".equals(newDirection))
        {
            if (!isStickRightActive)
            {
                isStickRightActive = true;
                doMovementAction("d");
                activeHorizontalDirection = MoveDirection.RIGHT;
                horizontalMoveStartTime = currentTime;
                dasChargedHorizontal = false;
                nextHorizontalMoveTime = 0;
            }
        }

        else if ("s".equals(newDirection))
        {
            if (!isStickDownActive)
            {
                isStickDownActive = true;
                softDropActive = true;
                softDropStartTime = currentTime;
                dasChargedSoftDrop = false;
                nextSoftDropTime = 0;
            }
        }

        // Handle stopping
        if (!"a".equals(newDirection)) {
            isStickLeftActive = false;
            if (stoppedLeft) activeHorizontalDirection = MoveDirection.NONE;
        }

        if (!"d".equals(newDirection)) {
            isStickRightActive = false;
            if (stoppedRight) activeHorizontalDirection = MoveDirection.NONE;
        }

        if (!"s".equals(newDirection)) {
            isStickDownActive = false;
            if (stoppedDown) softDropActive = false;
        }

        // Ensure horizontal stops if stick is centered horizontally
        if ((!"a".equals(newDirection) && !"d".equals(newDirection))) {
            if (!isStickRightActive)
                activeHorizontalDirection = MoveDirection.NONE;
        }
    }

    // Inside MainActivity.java
    private void resetJoystickPosition()
    {
        // Check if views are available and have dimensions
        if (joystick_pad != null && joystickKnob != null &&
                joystick_pad.getWidth() > 0 && joystick_pad.getHeight() > 0 &&
                joystickKnob.getWidth() > 0 && joystickKnob.getHeight() > 0)
        {
            Log.d("JoystickReset", "Resetting joystick position after layout.");
            final float centerX = joystick_pad.getWidth() / 2f;
            final float centerY = joystick_pad.getHeight() / 2f;
            final float radiusX = joystickKnob.getWidth() / 2f;
            final float radiusY = joystickKnob.getHeight() / 2f;

            // Set position directly, no animation needed for initial setup
            joystickKnob.setX(centerX - radiusX);
            joystickKnob.setY(centerY - radiusY);
            joystickKnob.setRotationX(0);
            joystickKnob.setRotationY(0);
        }

        else {
            Log.w("JoystickReset", "Could not reset joystick position - views not ready.");
        }
    }

    // performs control actions for block movement :WASD
    private void doMovementAction(String action) {
        tetrisGame.getAction(action);
    }

    /* Controller Support */
    private final Map<String, Boolean> keyState = new HashMap<>();

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        String action = switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT -> "a";
            case KeyEvent.KEYCODE_DPAD_RIGHT -> "d";
            case KeyEvent.KEYCODE_DPAD_DOWN -> "s";
            default -> null;
        };

        if (action != null && (keyState.get(action) == null || Boolean.FALSE.equals(keyState.get(action))))
        {
            keyState.put(action, true);
            doMovementAction(action);
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        String action = switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT -> "a";
            case KeyEvent.KEYCODE_DPAD_RIGHT -> "d";
            case KeyEvent.KEYCODE_DPAD_DOWN -> "s";
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
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        String action = switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT -> "a";
            case KeyEvent.KEYCODE_DPAD_RIGHT -> "d";
            case KeyEvent.KEYCODE_DPAD_DOWN -> "s";
            case KeyEvent.KEYCODE_BUTTON_A -> "A";
            case KeyEvent.KEYCODE_BUTTON_X -> "X";
            case KeyEvent.KEYCODE_BUTTON_Y -> "Y";
            case KeyEvent.KEYCODE_BUTTON_L1 -> "l1";
            case KeyEvent.KEYCODE_BUTTON_R1 -> "r1";
            default -> null;
        };

        if (action != null) {
            keyState.put(action, false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
        {
            // Joystick movement
            float leftStickX = event.getAxisValue(MotionEvent.AXIS_X);
            float leftStickY = event.getAxisValue(MotionEvent.AXIS_Y);

            // Process joystick input
            processJoystickInput(leftStickX, leftStickY);
        }
        return super.onGenericMotionEvent(event);
    }

    // Process controller joystick input
    private void processJoystickInput(float leftX, float leftY)
    {
        long currentTime = System.currentTimeMillis();

        // --- Handle Left Joystick WASD Movement ---

        /* Check LEFT movement: A */
        if (leftX < -JOYSTICK_ACTION_THRESHOLD)
        {
            if (!isStickLeftActive)
            {
                // Perform FIRST movement
                isStickLeftActive = true;

                // Set state for DAS/ARR
                activeHorizontalDirection = MoveDirection.LEFT;
                horizontalMoveStartTime = currentTime;
                dasChargedHorizontal = false;   //reset DAS flag
                nextHorizontalMoveTime = 0;     //reset repeat timer
            }
        }

        else
        {
            // Check if stick returned from the left active zone
            if (isStickLeftActive)
            {
                isStickLeftActive = false;
                if (activeHorizontalDirection == MoveDirection.LEFT)
                    activeHorizontalDirection = MoveDirection.NONE;
            }
        }

        /* Check RIGHT movement: D */
        if (leftX > JOYSTICK_ACTION_THRESHOLD)
        {
            if (!isStickRightActive)
            {
                // Perform FIRST movement
                isStickRightActive = true;

                // Set state for game loop's DAS/ARR logic
                activeHorizontalDirection = MoveDirection.RIGHT;
                horizontalMoveStartTime = currentTime;
                dasChargedHorizontal = false;
                nextHorizontalMoveTime = 0;
            }
        }

        else
        {
            if (isStickRightActive)
            {
                isStickRightActive = false;
                if (activeHorizontalDirection == MoveDirection.RIGHT)
                    activeHorizontalDirection = MoveDirection.NONE;
            }
        }

        // If stick returns to center horizontally, stop horizontal movement
        if (Math.abs(leftX) < JOYSTICK_DEAD_ZONE)
        {
            // Clear active direction if neither left nor right is active anymore
            if (!isStickLeftActive && !isStickRightActive)
                activeHorizontalDirection = MoveDirection.NONE;
        }

        /* Check DOWN movement: S (Soft Drop) */
        if (leftY > JOYSTICK_ACTION_THRESHOLD)
        {
            if (!isStickDownActive)
            {
                // Set state for game loop's soft drop logic
                isStickDownActive = true;
                softDropActive = true;
                softDropStartTime = currentTime;
                dasChargedSoftDrop = false;
                nextSoftDropTime = 0;
            }
        }

        else
        {
            if (isStickDownActive)
            {
                // Stop soft drop state
                isStickDownActive = false;
                softDropActive = false;
            }
        }
    }

    // --- Method to handle DAS/ARR based on joystick state ---
    private void handleControllerContinuousInput(long currentTime)
    {
        // --- Handle Horizontal DAS/ARR ---
        if (activeHorizontalDirection != MoveDirection.NONE)
        {
            if (!dasChargedHorizontal)
            {
                // Check if initial DAS delay has passed
                if (currentTime - horizontalMoveStartTime >= DAS_DELAY)
                {
                    dasChargedHorizontal = true;

                    // Perform the first auto-repeat move and schedule the next one
                    doMovementAction(activeHorizontalDirection == MoveDirection.LEFT ? "a" : "d");
                    nextHorizontalMoveTime = currentTime + ARR_INTERVAL;
                }
            }

            else
            {
                // DAS is charged, check if it's time for the next auto-repeat move
                if (currentTime >= nextHorizontalMoveTime)
                {
                    doMovementAction(activeHorizontalDirection == MoveDirection.LEFT ? "a" : "d");
                    nextHorizontalMoveTime = currentTime + ARR_INTERVAL;
                    // Schedule next repeat
                }
            }
        }

        // --- Handle Soft Drop DAS/ARR ---
        if (softDropActive)
        {
            // Using simpler version: Repeat immediately based on interval (no initial delay)
            if (currentTime >= nextSoftDropTime)
            {
                doMovementAction("s");

                // Use the *current* time to schedule next to prevent drift
                nextSoftDropTime = currentTime + ARR_INTERVAL;
            }
        }
    }

    // feature is for exclusive UI layout
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

    // Full Screening
    private void hideSystemBars()
    {
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }

    public void pauseGameLoop()
    {
        Log.i("MainActivity pauseGameLoop()", "MainActivity pauseGameLoop(): isGameLoopRunning = T/F ::: " + isGameLoopRunning);
        if (isGameLoopRunning && gameLoopHandler != null && gameLoopRunnable != null)
        {
            if (tetrisGame.isPaused) return;

            // Attempt removal
            gameLoopHandler.removeCallbacks(gameLoopRunnable);
            isGameLoopRunning = false;

            Log.i("MainActivity pauseGameLoop()", "MainActivity pauseGameLoop(): isGameLoopRunning = T/F ::: " + isGameLoopRunning);
        }

        else {
            Log.d("MainActivity pauseGameLoop()", "Callbacks NOT removed (loop not running or handler/runnable null).");
        }
    }

    public void resumeGameLoop()
    {
        if (gameLoopHandler != null && gameLoopRunnable != null && !isGameLoopRunning)
        {
            // Reset timing references crucial for resuming correctly
            nextFallTime = System.currentTimeMillis() + fallingSpeed;
            activeHorizontalDirection = MoveDirection.NONE;
            softDropActive = false;
            nextHorizontalMoveTime = 0;
            nextSoftDropTime = 0;
            dasChargedHorizontal = false;
            dasChargedSoftDrop = false;
            isStickLeftActive = false;
            isGameLoopRunning = true;

            // Start posting
            gameLoopHandler.post(gameLoopRunnable);
            Log.d("GameLoop", "Game loop posting resumed.");
        }

        else if (gameLoopHandler == null || gameLoopRunnable == null)
            Log.e("GameLoop", "Cannot resume loop, handler or runnable is null.");
    }

    /* Game Loop */
    private void startGameLoop()
    {
        gameLoopHandler = new Handler(Looper.getMainLooper());
        gameLoopRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                // Check the definitive pause state from the game logic object
                if (tetrisGame.isPaused) {
                    isGameLoopRunning = false;
                    return;
                }

                /* --- Game Loop Logic --- */
                // Calculate durations
                long frameStartTime = System.nanoTime();

                // Update game logic
                long currentTime = System.currentTimeMillis();

                // Controller support
                handleControllerContinuousInput(currentTime);

                // Check if the block is time to fall
                if (currentTime >= nextFallTime) {
                    boolean pieceShouldLock = tetrisGame.applyGravityAndCheckLock();
                    if (pieceShouldLock) {
                        tetrisGame.placeBlock();
                        tetrisGame.spawnNewPiece();
                        tetrisGame.isTouchingGround = false;
                        tetrisGame.lockStartTime = 0;
                    }
                    nextFallTime = currentTime + fallingSpeed;
                }

                // Update game state
                tetrisGame.updateGame();

                // UI updates + Render
                lines.setText("lines:\n".concat(String.valueOf(tetrisGame.getLinesCleared())));
                scores.setText("score:\n".concat(String.valueOf(tetrisGame.getScoreGained())));
                tetrisGame.renderGame(monitor);
                isGamePadConnected();

                // Calculate durations in ms
                long renderEndTime = System.nanoTime();
                long totalMs = (renderEndTime - frameStartTime) / 1_000_000;
                Log.d("GameLoopTiming", "Frame Total: " + totalMs + "ms");

                // --- Reschedule ---
                if (isGameLoopRunning)
                    gameLoopHandler.postDelayed(this, GAME_LOOP_INTERVAL_MS);
            }
        };
    }
}