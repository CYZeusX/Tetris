package com.cyzco.game;

import java.util.Map;
import android.util.Log;
import java.util.HashMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.Button;
import android.os.Parcelable;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowInsets;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity
{
    Button a, s, d,  A, X, Y, l1, r1, orientPortrait, orientLandscape, pause, lightDark, setting;
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

        TetrisGame tetrisGame;
        // Retrieve the orientation from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d("MainActivity", "Retrieved orientation: " + orientation);
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            setRequestedOrientation(orientation);
        }

        setContentView(R.layout.activity_main);

        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null)
        {
            // Hide the status bar and navigation bar
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

            // Enable gestures for immersive experience (if needed)
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and game logic
        a = findViewById(R.id.a);
        s = findViewById(R.id.s);
        d = findViewById(R.id.d);
        A = findViewById(R.id.A);
        X = findViewById(R.id.X);
        Y = findViewById(R.id.Y);
        l1 = findViewById(R.id.l1);
        r1 = findViewById(R.id.r1);
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
        {
            setRequestedOrientation(orientation);
        }
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

        // Move the block left
        setupContinuousMovement(a, () -> doMovementAction("a"));

        // Move the block right
        setupContinuousMovement(d, () -> doMovementAction("d"));

        // Move the block down
        setupContinuousMovement(s, () -> doMovementAction("s"));

        // Drop the block immediately
        A.setOnTouchListener((v, event) -> doControlAction(event, "A"));

        // Rotate the block anti-clockwise
        X.setOnTouchListener((v, event) -> doControlAction(event, "X"));

        // Rotate the block clockwise
        Y.setOnTouchListener((v, event) -> doControlAction(event, "Y"));

        // Rotate the block anti-clockwise
        l1.setOnTouchListener((v, event) -> doControlAction(event, "l1"));

        // Rotate the block clockwise
        r1.setOnTouchListener((v, event) -> doControlAction(event, "r1"));
    }

    // performs control actions for right buttons
    private boolean doControlAction(MotionEvent event, String action)
    {
        setControlAction(event, action);
        return true;
    }

    // setup action logics for right control buttons
    private void setControlAction(MotionEvent event, String action)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            tetrisGame.getAction(action);
            setVibrateAndRender();
        }
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

        if (action != null && ( keyState.get(action) == null || Boolean.FALSE.equals(keyState.get(action)) ) )
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
        String action = null;
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BUTTON_A: action = "A"; break;
            case KeyEvent.KEYCODE_BUTTON_X: action = "X"; break;
            case KeyEvent.KEYCODE_BUTTON_Y: action = "Y"; break;
            case KeyEvent.KEYCODE_BUTTON_L1: action = "l1"; break;
            case KeyEvent.KEYCODE_BUTTON_R1: action = "r1"; break;
        }

        if (action != null)
        {
            keyState.put(action, false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

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
    private void setupContinuousMovement(Button button, Runnable moveAction)
    {
        Handler movementHandler = new Handler(); // Create a new Handler for this button
        Runnable movementRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                moveAction.run(); // Execute the specific movement action
                movementHandler.postDelayed(this, 100); // Adjust delay for speed
            }
        };

        button.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    movementHandler.post(movementRunnable); // Start the movement
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    movementHandler.removeCallbacks(movementRunnable); // Stop the movement
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
                moveHandler.postDelayed(this, 50);
                // Movement speed interval
            }
        };

        // Start the movement loop
        moveHandler.post(moveRunnable);
    }
}
