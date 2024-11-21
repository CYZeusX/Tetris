package com.cyzco.game;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Button;
import android.graphics.Color;
import android.widget.TextView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.graphics.Typeface;
import android.view.WindowInsets;
import android.os.VibrationEffect;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.content.pm.ActivityInfo;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.WindowInsetsController;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    Button a, s, d,  A, X, Y, l1, r1, orientPortrait, orientLandscape, setting;
    Vibrator vibrator;
    TextView scores, lines;
    SurfaceView monitor;
    TetrisGame tetrisGame = new TetrisGame(this);  // 'this' refers to the Activity context

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
        setting = findViewById(R.id.pause);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonListeners()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        orientPortrait.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            editor.apply();
        });

        orientLandscape.setOnClickListener(v ->
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            editor.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            editor.apply();
        });

        //setting
        setting.setOnClickListener(v ->
        {
            tetrisGame.togglePause();
        });

        // Move the block left
        setupContinuousMovement(a, () ->
        {tetrisGame.movePieceLeft();
        tetrisGame.renderGame(monitor);});

        // Move the block right
        setupContinuousMovement(d, () ->
        {tetrisGame.movePieceRight();
        tetrisGame.renderGame(monitor);});

        // Move the block down
        setupContinuousMovement(s, () ->
        {tetrisGame.movePieceDown();
        tetrisGame.renderGame(monitor);});

        // Drop the block immediately
        A.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                tetrisGame.dropPiece();
                setVibrateAndRender();
            }
            return true;
        });

        // Rotate the block anti-clockwise
        X.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                tetrisGame.rotatePieceCounterClockwise();
                setVibrateAndRender();
            }
            return true;
        });

        // Rotate the block clockwise
        Y.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                tetrisGame.rotatePieceClockwise();
                setVibrateAndRender();
            }
            return true;
        });

        // Rotate the block anti-clockwise
        l1.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                tetrisGame.rotatePieceCounterClockwise();
                setVibrateAndRender();
            }
            return true;
        });

        // Rotate the block clockwise
        r1.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                tetrisGame.rotatePieceClockwise();
                setVibrateAndRender();
            }
            return true;
        });
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
