package com.cyzco.game;

import android.graphics.BitmapFactory;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.os.VibratorManager;
import android.os.VibrationEffect;
import android.view.SurfaceHolder;
import android.graphics.Typeface;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Context;
import android.widget.EditText;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Vibrator;
import java.util.ArrayList;
import android.os.Handler;
import java.util.HashMap;
import android.os.Bundle;
import java.util.HashSet;
import android.os.Looper;
import java.util.Objects;
import android.util.Log;
import android.os.Build;
import java.util.List;
import java.util.Map;

public class TetrisGame
{
    // board setup
    private String[][] BOARD;
    private final int BOARD_WIDTH = 10; // 10 blocks wide
    private final int BOARD_HEIGHT = 18; // 18 blocks tall
    private final float ASPECT_RATIO = (float) BOARD_WIDTH / (float) BOARD_HEIGHT;
    private final String BOARD_BLOCK = Shapes.space;
    private final Context CONTEXT;
    private final Rect textBoundsRect = new Rect();
    public boolean isPaused = false;

    // scoring setting
    private final int REMOVE_LINE_SCORE = 100;
    public int tetrisGained, linesCleared, scoreGained = 0;
    private final int PLACE_BLOCK_SCORE = 36;
    private static final long LOCK_DELAY = 500; // in ms

    // block lock delay setup
    public boolean isTouchingGround = false;
    public long lockStartTime = 0;

    // Cleared 4 effect setup
    private boolean showEffect = false;
    private long effectEndTime = 135; // Timestamp for when the effect ends
    private Bitmap effectBitmap, resizedEffectBitmap = null;
    private volatile boolean effectBitmapLoading = false;
    private volatile boolean effectBitmapReady = false;
    private int lastCanvasWidthForEffect = -1; // Track size to avoid rescaling unnecessarily
    private int lastCanvasHeightForEffect = -1;

    // Haptic Feedback effect setup
    private boolean hapticFeedback = false;
    private int hapticIndex = 200;
    private Vibrator vibrator;

    // edit shape and block setup
    private Tetromino currentPiece;
    private int currentShadowY = -1;
    public EditText stringShape;
    public String block;
    private final Map<String, Integer> characterCenterYOffsets = new HashMap<>();
    private final Map<String, Integer> blockColors = new HashMap<>();
    private final Shapes shapes = new Shapes();

    // instantiations
    private final GameOverFragment gameOverFragment = new GameOverFragment();
    private final GameWinFragment gameWinFragment = new GameWinFragment();

    // shape emoji mode, false is custom String shape
    public boolean emojiMode;

    // paint setup for game rendering
    private Paint boardPaint;
    private Paint blockPaint;

    // Restore Constants
    private static final String TAG_RESTORE = "RestoreState"; // Tag for restore logs
    private static final String TAG_DRAW = "DrawBlockDebug"; // Tag for draw logs


    /* --- Constructor --- */
    public TetrisGame(Context context)
    {
        this.CONTEXT = context;
        Log.d("TetrisGameInit", "Constructor started.");
        long startTime = System.nanoTime();

        // Initialize Board
        BOARD = new String[BOARD_HEIGHT][BOARD_WIDTH];
        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            for (int x = 0; x < BOARD_WIDTH; x++)
                BOARD[y][x] = BOARD_BLOCK;
        }

        // Initialize Resources
        // Set up color map and initial block char
        long time1 = System.nanoTime();
        initializeBlockColorsAndCharacters();

        // Initialize Paint objects *ONCE*
        long time2 = System.nanoTime();
        initPaints();

        // Initialize effect Bitmaps *ONCE*
        long time3 = System.nanoTime();
        initEffects(context.getResources());

        // Get Vibrator service *ONCE*
        long time4 = System.nanoTime();
        initVibrator(context);

        // Spawn First Piece
        long time5 = System.nanoTime();
        spawnNewPiece();
        long endTime = System.nanoTime();

        // Log initialization timings
        Log.d("TetrisGameTiming", "Constructor Total: " + (endTime - startTime) / 1_000_000 + "ms");
        Log.d("TetrisGameTiming", "  - Colors/Chars: " + (time2 - time1) / 1_000_000 + "ms");
        Log.d("TetrisGameTiming", "  - Paints: " + (time3 - time2) / 1_000_000 + "ms");
        Log.d("TetrisGameTiming", "  - Effects: " + (time4 - time3) / 1_000_000 + "ms");
        Log.d("TetrisGameTiming", "  - Vibrator: " + (time5 - time4) / 1_000_000 + "ms");
        Log.d("TetrisGameTiming", "  - Spawn: " + (endTime - time5) / 1_000_000 + "ms");
        Log.d("TetrisGameInit", "Constructor finished.");
    }

    private void initPaints()
    {
        Log.d("TetrisGameInit", "Initializing Paints...");

        boardPaint = new Paint();
        boardPaint.setColor(Color.WHITE);
        boardPaint.setAlpha(30);
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeWidth(2);
        boardPaint.setAntiAlias(true);

        blockPaint = new Paint();
        blockPaint.setTypeface(Typeface.SANS_SERIF);
        blockPaint.setTextAlign(Paint.Align.CENTER); // Default alignment
        blockPaint.setAntiAlias(true);
        // Text size will be set dynamically in drawBlock based on blockSize

        Log.d("TetrisGameInit", "Paints Initialized.");
    }

    public void initEffects(final Resources resources)
    {
        // Only start loading if not already loaded and not currently loading
        if (effectBitmap == null && !effectBitmapLoading)
        {
            effectBitmapLoading = true;
            effectBitmapReady = false;

            // Create new background thread
            new Thread(() ->
            {
                int effectResourceId = R.drawable.tetris_clear_effect;
                Bitmap loadedBitmap = null;
                Bitmap scaledBitmap = null;

                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    loadedBitmap = BitmapFactory.decodeResource(resources, effectResourceId, options);
                    if (loadedBitmap != null)
                        scaledBitmap = loadedBitmap;
                }

                catch (OutOfMemoryError oom) {
                    Log.e("TetrisGameInit", "Background thread: Out of Memory Error loading effect bitmap!", oom);
                }

                catch (Exception e) {
                    Log.e("TetrisGameInit", "Background thread: Error loading effect bitmap", e);
                }

                finally
                {
                    // --- Update member variables on the main thread ---
                    final Bitmap finalLoaded = loadedBitmap;
                    final Bitmap finalScaled = scaledBitmap;

                    new Handler(Looper.getMainLooper()).post(() ->
                    {
                        effectBitmap = finalLoaded; // Assign potentially null original
                        resizedEffectBitmap = finalScaled; // Assign potentially null scaled
                        effectBitmapReady = (resizedEffectBitmap != null); // Mark as ready if successful
                        effectBitmapLoading = false; // Mark loading as finished
                        Log.d("TetrisGameInit", "Main thread: Updated effect bitmap variables. Ready=" + effectBitmapReady);
                    });
                }
            }).start();
        }

        else if (effectBitmapLoading)
            Log.d("TetrisGameInit", "Effects Bitmap is already loading in the background.");

        else effectBitmapReady = true;
    }

    private void initVibrator(Context context)
    {
        Log.d("TetrisGameInit", "Initializing Vibrator...");

        // Use VibratorManager for API 31+ for better compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null)
                vibrator = vibratorManager.getDefaultVibrator();

            else
            {
                Log.w("TetrisGameInit", "VibratorManager service not available.");
                vibrator = null;
            }
        }
        // Use deprecated Vibrator for older APIs
        else vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Check if vibrator exists and has capability (hasVibrator deprecated but still useful check)
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.w("TetrisGameInit", "Vibrator service not available or device doesn't have a vibrator.");
            vibrator = null; // Ensure it's null if not usable
        }
        else Log.d("TetrisGameInit", "Vibrator Initialized successfully.");
    }

    private void initializeBlockColorsAndCharacters()
    {
        Log.d("TetrisGameInit", "Initializing Block Colors/Chars...");

        // set initial block character based on current state
        this.block = shapes.getBlock();
        Log.d("TetrisGameInit", "Initial block character set to: '" + this.block + "'");

        blockColors.put(Shapes.I_BLOCK, Color.parseColor("#00FFFF")); // I-shape
        blockColors.put(Shapes.O_BLOCK, Color.parseColor("#FFEB3B")); // O-shape
        blockColors.put(Shapes.T_BLOCK, Color.parseColor("#9500FF")); // T-shape
        blockColors.put(Shapes.S_BLOCK, Color.parseColor("#59FF33")); // S-shape
        blockColors.put(Shapes.Z_BLOCK, Color.parseColor("#FF4D4D")); // Z-shape
        blockColors.put(Shapes.J_BLOCK, Color.parseColor("#3460FF")); // J-shape
        blockColors.put(Shapes.L_BLOCK, Color.parseColor("#FFA621")); // L-shape
        blockColors.put(Shapes.space, Color.TRANSPARENT);

        // --- Pre-Measure Characters ---
        Log.d("TetrisGameInit", "Pre-measuring character offsets...");
        Paint measurementPaint = new Paint();
        measurementPaint.setTypeface(Typeface.SANS_SERIF);
        Rect bounds = new Rect();
        List<String> charsToMeasure = new ArrayList<>();

        // Add all possible block characters/emojis
        charsToMeasure.add(this.block);
        charsToMeasure.addAll(Shapes.EMOJI_MAP.values());

        float typicalBlockSizePx = 54f;
        measurementPaint.setTextSize(typicalBlockSizePx);

        for (String character : new HashSet<>(charsToMeasure))
        {
            // Use HashSet to avoid duplicates
            if (character != null && !character.isEmpty())
            {
                measurementPaint.getTextBounds(character, 0, character.length(), bounds);
                characterCenterYOffsets.put(character, bounds.centerY());
                Log.d("CharMeasure", "Char: '" + character + "', CenterY Offset: " + bounds.centerY());
            }
        }

        Log.d("TetrisGameInit", "Character offsets measured.");
        Log.d("TetrisGameInit", "Block Colors/Chars Initialized.");
    }

    public void setStringShape(EditText stringShape) {
        this.stringShape = stringShape;
    }

    public void changeBlock()
    {
        if (stringShape != null)
        {
            String shape = stringShape.getText().toString().strip();
            if (shape.isEmpty())
                shape = "■";
            shapes.setBlock(shape);
        }
    }

    public void changeBlock(String block) {
        shapes.setBlock(block);
    }

    public String getBlock() {
        return shapes.getBlock();
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public int getScoreGained() {
        return scoreGained;
    }

    public void setLinesCleared(int linesCleared) {
        this.linesCleared = linesCleared;
    }

    public void setScoreGained(int scoreGained) {
        this.scoreGained = scoreGained;
    }

    public void spawnNewPiece()
    {
        // Random index for shapes
        int randomIndex = (int) (Math.random() * Shapes.SHAPES.length);

        // Get the random shape from the Shapes array
        String[][] randomShape = Shapes.SHAPES[randomIndex];

        // Get the corresponding enum type - IMPORTANT: Assumes order matches
        Shapes.PieceType type = Shapes.PieceType.values()[randomIndex];

        // Calculate starting position *before* creating the piece
        int shapeWidth;
        if (randomShape.length > 0 && randomShape[0] != null)
            shapeWidth = randomShape[0].length;

        else {
            System.err.println("Error: Invalid shape matrix selected.");
            return;
        }

        int startX = (BOARD_WIDTH - shapeWidth) / 2;
        int startY = -1;
        Tetromino potentialPiece = new Tetromino(type, randomShape, startX, startY);

        // Check if the piece can be placed at the calculated starting position using isValidPosition
        if (potentialPiece.isValidPosition(potentialPiece.getShapeCopy(), startX, startY, BOARD)) {
            currentPiece = potentialPiece;
            updateShadowPosition();
        }

        else
        {
            potentialPiece = new Tetromino(type, randomShape, startX, startY);
            if (potentialPiece.isValidPosition(potentialPiece.getShapeCopy(), startX, startY, BOARD)) {
                currentPiece = potentialPiece;
                return;
            }

            handleGameOver();
        }
    }

    public void togglePause()
    {
        //Resources for pause
        MainActivity activity = ((MainActivity) CONTEXT);

        if (!isPaused)
        {
            // --- Pause the game ---
            isPaused = true;

            // Log state change
            activity.pauseGameLoop();
            Log.d("TetrisGame/togglePause()/MA.pauseGameLoop()", "MA.pauseGameLoop(): isPaused = true ::: " + isPaused);

            // Disable the joystick pad interaction if needed
            if (activity.joystick_pad != null)
                activity.joystick_pad.setEnabled(false);
        }

        else
        {
            // --- Resume the game ---
            isPaused = false;

            Log.d("TetrisGame togglePause()", "TetrisGame togglePause(): isPaused = false ::: " + isPaused);

            // Ensure input states are reset BEFORE resuming the loop
            if (activity != null) {
                resetInputStates(activity);
            }

            assert activity != null;
            activity.resumeGameLoop();
            Log.d("TetrisGame/togglePause()/MA.resumeGameLoop()","MA.resumeGameLoop(): isPaused = false ::: " + isPaused);

            // Re-enable the joystick pad interaction
            if (activity.joystick_pad != null)
                activity.joystick_pad.setEnabled(true);
        }
    }

    // Helper to clear input states on pause/resume
    private void resetInputStates(MainActivity activity)
    {
        // Reset DAS/ARR state to prevent instant movement on resume
        activity.activeHorizontalDirection = MainActivity.MoveDirection.NONE;
        activity.softDropActive = false;
        activity.nextHorizontalMoveTime = 0; // Reset repeat timers
        activity.nextSoftDropTime = 0;
        activity.dasChargedHorizontal = false; // Reset DAS charge state
        activity.dasChargedSoftDrop = false; // Reset DAS charge state

        // Clear joystick active flags to be safe
        activity.isStickLeftActive = false;
        activity.isStickRightActive = false;
        activity.isStickDownActive = false;
        activity.updateJoystickDirection(null); // Stop any ongoing DAS/ARR
    }

    private void handleGameWin()
    {
        togglePause();
        gameWinFragment.show(((MainActivity) CONTEXT).getSupportFragmentManager(), "GameWinFragment");
    }

    private void handleGameOver()
    {
        togglePause();
        MainActivity activity = ((MainActivity) CONTEXT);

        try {
            gameOverFragment.show(activity.getSupportFragmentManager(), "GameOverFragment");
        }

        catch (Exception e) {
            Log.e("GameOverFragment", "Error showing GameOverFragment", e);
        }
    }

    // for right buttons controls
    public void getAction(String action)
    {
        switch (action)
        {
            // left buttons
            case "a" -> moveBlockLeft();
            case "s" -> moveBlockDown();
            case "d" -> moveBlockRight();

            // right buttons
            case "A" -> dropBlock();
            case "B" -> holdBlock();
            case "X", "l1" -> rotatePieceCounterClockwise();
            case "Y", "r1" -> rotatePieceClockwise();
            default -> System.out.println("Unknown action: " + action);
        }
    }

    public void updateGame()
    {
        if (showEffect && System.currentTimeMillis() > effectEndTime + 200)
            showEffect = false;
    }

    /**
     * Attempts to move the current piece down by one step due to gravity.
     * If the piece cannot move down, it manages the lock delay timer.
     * @return true if the piece has locked in place after the delay, false otherwise.
     */
    public boolean applyGravityAndCheckLock()
    {
        if (currentPiece == null) return false;
        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX, currentY + 1))
        {
            currentPiece.setPosition(currentX, currentY + 1);
            isTouchingGround = false;
            lockStartTime = 0;
            return false;
        }

        else
        {
            if (!isTouchingGround)
            {
                isTouchingGround = true;
                lockStartTime = System.currentTimeMillis();
                return false; // Just touched
            }

            else
            {
                long elapsedTime = System.currentTimeMillis() - lockStartTime;
                return elapsedTime >= LOCK_DELAY;
            }
        }
    }

    public void moveBlockDown()
    {
        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX, currentY + 1))
        {
            // Move down by one row
            currentPiece.setPosition(currentX, currentY + 1);
            isTouchingGround = false;
            lockStartTime = 0;
        }

        else
        {
            if (!isTouchingGround)
            {
                isTouchingGround = true;
                lockStartTime = System.currentTimeMillis();
            }

            else
            {
                long elapsedTime = System.currentTimeMillis() - lockStartTime;
                if (elapsedTime >= LOCK_DELAY)
                {
                    // Lock the block, Spawn a block
                    placeBlock();
                    spawnNewPiece();
                    isTouchingGround = false;
                    lockStartTime = 0;
                }
            }
        }

        hapticFeedback = true;
        hapticIndex = 10;
    }

    public void moveBlockLeft()
    {
        if (isPaused || currentPiece == null) return;

        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX - 1, currentY))
        {
            currentPiece.setPosition(currentX - 1, currentY);
            hapticFeedback = true;
            hapticIndex = 10;

            // Reset lock delay. Only if grounded & successful move
            if (isTouchingGround) lockStartTime = System.currentTimeMillis();

            updateShadowPosition();
        }
    }

    public void moveBlockRight()
    {
        if (isPaused || currentPiece == null) return;

        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX + 1, currentY))
        {
            currentPiece.setPosition(currentX + 1, currentY);
            hapticFeedback = true;
            hapticIndex = 10;

            // Reset lock delay. Only if grounded & successful move
            if (isTouchingGround) lockStartTime = System.currentTimeMillis();

            updateShadowPosition();
        }
    }

    public void dropBlock()
    {
        // Move the piece down to the lowest possible position
        int newY = currentPiece.getY();

        // Keep moving down until we can't
        while (canMove(currentPiece, currentPiece.getX(), newY + 1))
            newY++;

        // Set the piece to the final position
        currentPiece.setPosition(currentPiece.getX(), newY);
        placeBlock();  // Lock the piece in place
        spawnNewPiece(); // Spawn a new piece after the drop
    }

    public void rotatePieceClockwise()
    {
        if (isPaused || currentPiece == null) return;

        if (currentPiece.rotateClockwise(BOARD))
        {
            if (isTouchingGround)
                lockStartTime = System.currentTimeMillis();
            updateShadowPosition();
        }
    }

    public void rotatePieceCounterClockwise()
    {
        if (isPaused || currentPiece == null) return;


        if (currentPiece.rotateCounterClockwise(BOARD))
        {
            if (isTouchingGround)
                lockStartTime = System.currentTimeMillis();
            updateShadowPosition();
        }
    }

    public void holdBlock()
    {
//        if (isPaused)
//            return;
        // TODO: Implement hold logic
        Log.w("HoldBlock", "Hold function not implemented yet.");
    }

    public boolean canMove(Tetromino piece, int newX, int newY)
    {
        String[][] shape = piece.getShapeInternal();

        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (!Objects.equals(shape[i][j], BOARD_BLOCK))
                {
                    int boardX = newX + j;
                    int boardY = newY + i;

                    // Check if the piece is outside the board
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT)
                        return false;

                    // Only check collision if within bounds
                    if (boardY >= 0) {
                        // Check if there's a locked block at that position
                        if (!Objects.equals(BOARD[boardY][boardX], BOARD_BLOCK))
                            return false;
                    }
                }
            }
        }

        // No collisions, can move
        return true;
    }

    public void placeBlock()
    {
        String[][] shape = currentPiece.getShapeInternal();
        int startX = currentPiece.getX();
        int startY = currentPiece.getY();

        // Transfer the Tetromino shape into the board
        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (!Objects.equals(shape[i][j], BOARD_BLOCK))
                {
                    int boardX = startX + j;
                    int boardY = startY + i;
                    if (boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH)
                        BOARD[boardY][boardX] = shape[i][j];
                }
            }
        }

        scoreGained += PLACE_BLOCK_SCORE;
        checkCompletedLines(); // Check for completed lines after locking
    }

    public void checkCompletedLines()
    {
        int rowsClearedInThisStep = 0; // Counter for rows cleared in one step

        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            boolean lineComplete = true;
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                if (Objects.equals(BOARD[y][x], BOARD_BLOCK))
                {
                    lineComplete = false;
                    break;
                }
            }

            if (lineComplete)
            {
                removeLine(y);
                rowsClearedInThisStep++; // Increment the cleared rows counter
                y--; // Adjust 'y' since rows above shift down
            }
        }

        hapticFeedback = true;
        hapticIndex = rowsClearedInThisStep;

        // Check if 4 rows were cleared at once
        if (rowsClearedInThisStep == 4)
        {
            scoreGained += REMOVE_LINE_SCORE * 2; // Double the score for clearing 4 rows
            showEffect = true;
            long newEndTime = System.currentTimeMillis() + 140;
            effectEndTime = Math.max(effectEndTime, newEndTime);
            tetrisGained++;
        }

        // Normal score for other cases
        else scoreGained += REMOVE_LINE_SCORE * rowsClearedInThisStep;

        // Update the total lines cleared
        linesCleared += rowsClearedInThisStep;

        if (linesCleared >= 50)
            handleGameWin();
    }

    private void removeLine(int lineIndex)
    {
        for (int y = lineIndex; y > 0; y--)
            System.arraycopy(BOARD[y - 1], 0, BOARD[y], 0, BOARD_WIDTH);
        for (int x = 0; x < BOARD_WIDTH; x++)
            BOARD[0][x] = BOARD_BLOCK;
    }

    /* --- Rendering Methods --- */
    public void renderGame(SurfaceView monitor)
    {
        SurfaceHolder holder = monitor.getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        // Start timing renderGame
        long frameRenderStartTime = System.nanoTime();

        try
        {
            // --- Aspect Ratio Calculation ---
            long aspectCalcStart = System.nanoTime();
            int canvasHeight = canvas.getHeight();
            int canvasWidth = canvas.getWidth();

            // Unlock before returning
            if (canvasWidth <= 0 || canvasHeight <= 0) {
                holder.unlockCanvasAndPost(canvas);
                return;
            }

            float viewAspectRatio = (float) canvasWidth / canvasHeight;
            int blockSize;
            int paddingLeft;
            int paddingTop;

            if (viewAspectRatio > ASPECT_RATIO)
            {
                blockSize = canvasHeight / BOARD_HEIGHT;
                int gameAreaWidth = blockSize * BOARD_WIDTH;
                paddingLeft = (canvasWidth - gameAreaWidth) / 2;
                paddingTop = 0;
            }

            else
            {
                blockSize = canvasWidth / BOARD_WIDTH;
                int gameAreaHeight = blockSize * BOARD_HEIGHT;
                paddingLeft = 0;
                paddingTop = (canvasHeight - gameAreaHeight) / 2;
            }
            long aspectCalcEnd = System.nanoTime();

            // --- >>> Check and Scale Effect Bitmap <<< ---
            long effectScaleStart = System.nanoTime();
            if (effectBitmapReady && effectBitmap != null)
            {
                boolean needsRescaling = resizedEffectBitmap == null ||
                        lastCanvasWidthForEffect != canvasWidth ||
                        lastCanvasHeightForEffect != canvasHeight;

                if (needsRescaling)
                {
                    // Calculate target size based on game board size for consistency
                    Log.d("RenderGame", "Rescaling effect bitmap...");
                    int targetWidth = blockSize * BOARD_WIDTH;
                    float originalAspectRatio = (float) effectBitmap.getWidth() / effectBitmap.getHeight();
                    int targetHeight = (int) (targetWidth / originalAspectRatio);

                    if (targetWidth > 0 && targetHeight > 0)
                    {
                        try
                        {
                            Log.w("RenderGame", "Performing effect bitmap scaling (can cause jank)...");
                            long scaleStartTime = System.nanoTime();
                            // Recycle old one first
                            if (resizedEffectBitmap != null && resizedEffectBitmap != effectBitmap) {
                                resizedEffectBitmap.recycle();
                            }

                            // Scale the ORIGINAL bitmap
                            resizedEffectBitmap = Bitmap.createScaledBitmap(effectBitmap, targetWidth, targetHeight, true);
                            lastCanvasWidthForEffect = canvasWidth;
                            lastCanvasHeightForEffect = canvasHeight;
                            long scaleEndTime = System.nanoTime();
                            Log.d("RenderGame", "Effect scaling took: " + (scaleEndTime - scaleStartTime) / 1_000_000 + "ms");
                        }

                        catch (Exception e) {
                            Log.e("RenderGame", "Error scaling effect bitmap", e);
                            resizedEffectBitmap = null; // Ensure it's null on error
                        }
                    }
                    else resizedEffectBitmap = null;
                }
            }
            long effectScaleEnd = System.nanoTime();

            // Clear the canvas with a transparent background
            long clearStart = System.nanoTime();
            clearCanvas(canvas);
            long clearEnd = System.nanoTime();

            // --- Render Board Background ---
            long boardBgStart = System.nanoTime();
            renderBoardBackground(canvas, boardPaint, blockSize, paddingLeft, paddingTop);
            long boardBgEnd = System.nanoTime();

            // --- Render Fixed Blocks ---
            long fixedBlocksStart = System.nanoTime();
            renderFixedBlocks(canvas, blockPaint, blockSize, paddingLeft, paddingTop);
            long fixedBlocksEnd = System.nanoTime();

            // --- Render Shadow Block ---
            long shadowStart = System.nanoTime();
            if (currentPiece != null)
                renderShadowBlock(canvas, blockPaint, blockSize, paddingLeft, paddingTop);
            long shadowEnd = System.nanoTime();

            // --- Render Current Piece ---
            long currentPieceStart = System.nanoTime();
            if (currentPiece != null)
                renderCurrentBlock(canvas, blockPaint, blockSize, paddingLeft, paddingTop);
            long currentPieceEnd = System.nanoTime();

            // --- Render Effects ---
            long effectRenderStart = System.nanoTime();
            renderEffect(canvas, canvasWidth, canvasHeight);
            long effectRenderEnd = System.nanoTime();

            setHapticFeedbacks(hapticIndex);


            // --- Calculate Durations (in microseconds for more detail) ---
            long aspectCalcUs = (aspectCalcEnd - aspectCalcStart) / 1000;
            long effectScaleUs = (effectScaleEnd - effectScaleStart) / 1000;
            long clearUs = (clearEnd - clearStart) / 1000;
            long boardBgUs = (boardBgEnd - boardBgStart) / 1000;
            long fixedBlocksUs = (fixedBlocksEnd - fixedBlocksStart) / 1000;
            long shadowUs = (shadowEnd - shadowStart) / 1000;
            long currentPieceUs = (currentPieceEnd - currentPieceStart) / 1000;
            long effectRenderUs = (effectRenderEnd - effectRenderStart) / 1000;
            long totalRenderUs = (effectRenderEnd - frameRenderStartTime) / 1000; // Total time spent in renderGame

            // Log detailed timings
//            Log.d("RenderTiming", String.format(
//                    "Render Total: %d us [Aspect:%d Scale:%d Clear:%d Bg:%d Fixed:%d Shadow:%d Current:%d Effect:%d]",
//                    totalRenderUs, aspectCalcUs, effectScaleUs, clearUs, boardBgUs, fixedBlocksUs, shadowUs, currentPieceUs, effectRenderUs
//            ));
        }

        catch (Exception e) {
            Log.e("RenderGame", "Error during rendering", e);
        }

        // Always unlock canvas even if errors occur
        finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
    }

    private String checkShapeToEmoji(String blockType) {
        return Shapes.EMOJI_MAP.getOrDefault(blockType, blockType);
    }

    // --- Helper Method to Calculate Shadow ---
    private void updateShadowPosition()
    {
        // No block, no shadow
        if (currentPiece == null) {
            currentShadowY = -1;
            return;
        }

        int pieceX = currentPiece.getX();
        int testY = currentPiece.getY();

        // Use the piece's current shape for the check
        while (canMove(currentPiece, pieceX, testY + 1))
            testY++;

        // Store the calculated position
        currentShadowY = testY;
    }

    private void renderBoardBackground(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                float left = paddingLeft + x * blockSize;
                float top = paddingTop + y * blockSize;
                float right = left + blockSize;
                float bottom = top + blockSize;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }

    private void renderCurrentBlock(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        // Use the passed-in blockPaint
        if (currentPiece == null) return;
        String[][] shape = currentPiece.getShapeInternal(); // Use internal getter
        int pieceX = currentPiece.getX();
        int pieceY = currentPiece.getY();

        for (int y = 0; y < shape.length; y++)
        {
            for (int x = 0; x < shape[y].length; x++)
            {
                // Bounds check
                if (x < shape[y].length) {
                    if (shape[y][x] != null && !Objects.equals(shape[y][x], BOARD_BLOCK))
                        drawBlock(canvas, paint, shape[y][x], pieceX + x, pieceY + y, blockSize, paddingLeft, paddingTop, 255);
                }
            }
        }
    }

    private void renderShadowBlock(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        if (currentPiece == null || currentShadowY < 0) return;

        String[][] shape = currentPiece.getShapeInternal();
        int pieceX = currentPiece.getX();

        for (int y = 0; y < shape.length; y++)
        {
            for (int x = 0; x < shape[y].length; x++)
            {
                if (shape[y][x] != null && !Objects.equals(shape[y][x], BOARD_BLOCK))
                    drawBlock(canvas, paint, shape[y][x], pieceX + x, currentShadowY + y , blockSize, paddingLeft, paddingTop, 120);
            }
        }
    }

    private void renderFixedBlocks(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                // Bounds check
                if (y < BOARD.length && x < BOARD[y].length)
                {
                    String blockType = BOARD[y][x];
                    if (blockType != null && !Objects.equals(blockType, BOARD_BLOCK))
                        drawBlock(canvas, paint, blockType, x, y, blockSize, paddingLeft, paddingTop, 255);
                }
            }
        }
    }

    private void drawBlock(@NonNull Canvas canvas, @NonNull Paint paint, @NonNull String blockType,
                           int x, int y, int blockSize, int paddingLeft, int paddingTop, int alpha)
    {
        if (y < 0 || y >= BOARD_HEIGHT) return;

        Integer color = blockColors.get(blockType);
        if (color == null) color = Color.GRAY;

        String characterToDraw = emojiMode ? checkShapeToEmoji(blockType) : getBlock();

        if (characterToDraw == null || characterToDraw.isEmpty() || Objects.equals(characterToDraw, BOARD_BLOCK)) {
            Log.d(TAG_DRAW, "Skipping draw at [" + x + "," + y + "]: Empty/Null character.");
            return;
        }

        float computedBlockSize = emojiMode ? blockSize * 0.85f : blockSize * 1f;

        // Configure Paint
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(computedBlockSize);
        paint.setStyle(Paint.Style.FILL);

        // Calculate positioning
        float cellCenterX = paddingLeft + x * blockSize + (blockSize / 2f);
        float cellCenterY = paddingTop + y * blockSize + (blockSize / 2f);

        // Use Pre-Measured Offset
        Integer preMeasuredOffsetY = characterCenterYOffsets.get(characterToDraw);

        // If character is not pre-measured:
        if (preMeasuredOffsetY == null)
        {
            Log.w("DrawBlock", "Warning: Character '" + characterToDraw + "' not pre-measured. Measuring now.");
            paint.getTextBounds(characterToDraw, 0, characterToDraw.length(), textBoundsRect);
            preMeasuredOffsetY = textBoundsRect.centerY();
            characterCenterYOffsets.put(characterToDraw, preMeasuredOffsetY);
        }

        float baselineY = cellCenterY - preMeasuredOffsetY;

        canvas.drawText(characterToDraw, cellCenterX, baselineY, paint);
    }

    private void setHapticFeedbacks(int hapticIndex)
    {
        if (!hapticFeedback) return;

        if (vibrator != null && vibrator.hasVibrator())
        {
            long[] timings;
            int[] amplitudes = switch (hapticIndex)
            {
                // Placing blocks
                case 0 -> {
                    timings = new long[] {0, 40};
                    yield new int[] {0, 20};
                }

                // Cleared 4 at once
                case 4 -> {
                    timings = new long[] {0, 80, 15, 180, 15, 550};
                    yield new int[] {0, 20, 0, 20, 0, 20};
                }

                // Joystick movement
                case 10 -> {
                    timings = new long[] {0, 3};
                    yield new int[] {0, 30};
                }

                // Normal clearing
                default -> {
                    timings = new long[] {0, 180};
                    yield new int[] {0, 20};
                }
            };

            try
            {
                // Use VibrationEffect for API 26+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                }
                
                else {
                    Log.w("Vibration", "VibrationEffect not supported on this API level, skipping complex vibration.");
                    vibrator.vibrate(50);
                }
            } 
            
            catch (Exception e) {
                Log.e("Vibration", "Error triggering vibration", e);
            }

            hapticFeedback = false;
        } 
        
        else Log.w("Vibration", "Vibrator not available, cannot vibrate.");
    }

    private void renderEffect(Canvas canvas, int canvasWidth, int canvasHeight)
    {
        if (showEffect && resizedEffectBitmap != null)
        {
            float posX = (canvasWidth - resizedEffectBitmap.getWidth()) / 2.0f;
            float posY = (canvasHeight - resizedEffectBitmap.getHeight()) / 2.0f;
            canvas.drawBitmap(resizedEffectBitmap, posX, posY, null);
        }

        else if (showEffect)
            Log.e("renderEffect Error", "resizedEffectBitmap is null. Effect will not be rendered.");
    }

    public Bundle saveGameState()
    {
        Bundle bundle = new Bundle();
        Log.d("GameState", "Saving state...");

        // Save general game state
        bundle.putInt("linesCleared", linesCleared);
        bundle.putInt("scoreGained", scoreGained);
        bundle.putInt("tetrisGained", tetrisGained);
        bundle.putString("currentBlock", getBlock());
        bundle.putBoolean("emojiMode", emojiMode);
        bundle.putLong("lockStartTime", lockStartTime);
        bundle.putBoolean("isTouchingGround", isTouchingGround);
        bundle.putInt("currentShadowY", currentShadowY);
        bundle.putBoolean("showEffect", showEffect);
        bundle.putLong("effectEndTime", effectEndTime);

        // --- Save Board ---
        if (BOARD != null && BOARD.length > 0 && BOARD[0] != null)
        {
            int rows = BOARD.length;
            int cols = BOARD[0].length;
            String[] flatGrid = new String[rows * cols];
            for (int i = 0; i < rows; i++) {
                // Check if BOARD[i] is null before copying, though ideally it shouldn't be
                if (BOARD[i] != null && BOARD[i].length == cols) {
                    System.arraycopy(BOARD[i], 0, flatGrid, i * cols, cols);
                } else {
                    System.err.println("Warning: Invalid row in BOARD during save at index " + i);
                }
            }

            bundle.putStringArray("boardGrid", flatGrid);
            bundle.putInt("rows", rows);
            bundle.putInt("cols", cols);
        }

        else {
            System.err.println("Warning: BOARD is null or invalid during saveGameState.");
            bundle.putInt("rows", 0);
            bundle.putInt("cols", 0);
        }

        // --- Save Current Piece (Only if it exists) ---
        if (currentPiece != null)
        {
            Log.d(TAG_RESTORE, "Saving piece: Type=" + currentPiece.getShapeType().name() + " X=" + currentPiece.getX() + " Y=" + currentPiece.getY() + " Rot=" + currentPiece.getRotationState());

            // --- >>> SAVE MISSING DATA <<< ---
            bundle.putString("shapeType", currentPiece.getShapeType().name()); // Save enum name as String
            bundle.putInt("rotationState", currentPiece.getRotationState());   // Save rotation state

            // Save shape
            String[][] shape = currentPiece.getShapeCopy();
            int shapeRows = shape.length;
            int shapeCols = (shapeRows > 0 && shape[0] != null) ? shape[0].length : 0;

            // Proceed only if shape dimensions are valid
            if (shapeCols > 0)
            {
                bundle.putInt("shapeRows", shapeRows);
                bundle.putInt("shapeCols", shapeCols);
                String[] flatShape = new String[shapeRows * shapeCols];

                for (int i = 0; i < shapeRows; i++)
                {
                    if (shape[i] != null && shape[i].length == shapeCols)
                        System.arraycopy(shape[i], 0, flatShape, i * shapeCols, shapeCols);

                    else System.err.println("Warning: Invalid row in piece shape during save at index " + i);
                }

                // Save position
                bundle.putStringArray("currentShape", flatShape);
                bundle.putInt("currentPieceX", currentPiece.getX());
                bundle.putInt("currentPieceY", currentPiece.getY());
            } else Log.e(TAG_RESTORE,"Error saving piece: Invalid shape dimensions.");
        }
        else Log.d(TAG_RESTORE, "Saving state: currentPiece is null.");

        Log.d("GameState", "State saved. isPaused=" + isPaused);
        return bundle;
    }

    public void restoreGameState(Bundle bundle)
    {
        if (bundle == null) { Log.w(TAG_RESTORE,"Restore bundle is null."); return; }
        Log.d(TAG_RESTORE, "Restoring state...");

        // Rebuild game from the saved data
        linesCleared = bundle.getInt("linesCleared", 0);
        scoreGained = bundle.getInt("scoreGained", 0);
        tetrisGained = bundle.getInt("tetrisGained", 0);
        emojiMode = bundle.getBoolean("emojiMode", false);
        changeBlock(bundle.getString("currentBlock", "■"));

        // --- >>> RESTORE isPaused STATE <<< ---
        lockStartTime = bundle.getLong("lockStartTime", 0);
        isTouchingGround = bundle.getBoolean("isTouchingGround", false);
        currentShadowY = bundle.getInt("currentShadowY", -1);
        showEffect = bundle.getBoolean("showEffect", false);
        effectEndTime = bundle.getLong("effectEndTime", 0);
        Log.d(TAG_RESTORE, "Restored flags: isPaused=" + isPaused + " emojiMode=" + emojiMode);

        // Restore board grid
        int rows = bundle.getInt("rows", BOARD_HEIGHT);
        int cols = bundle.getInt("cols", BOARD_WIDTH);
        String[] flatGrid = bundle.getStringArray("boardGrid");
        BOARD = new String[rows][cols];

        // Always fill with default block first
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
                BOARD[i][j] = BOARD_BLOCK;
        }

        // Then fill with saved state if available
        if (flatGrid != null && rows > 0 && cols > 0)
        {
            Log.d(TAG_RESTORE, "Restoring board grid...");
            for (int i = 0; i < rows; i++)
            {
                if ((i * cols + cols) <= flatGrid.length)
                    System.arraycopy(flatGrid, i * cols, BOARD[i], 0, cols);

                else {
                    Log.e(TAG_RESTORE,"Error restoring board grid: index OOB.");
                    break;
                }
            }
        }

        // Restore current Tetromino shape
        currentPiece = null;
        String shapeType = bundle.getString("shapeType");
        if (shapeType != null)
        {
            int shapeRows = bundle.getInt("shapeRows", 0);
            int shapeCols = bundle.getInt("shapeCols", 0);
            String[] flatShape = bundle.getStringArray("currentShape");
            int rotationState = bundle.getInt("rotationState", 0);
            int x = bundle.getInt("currentPieceX", 0);
            int y = bundle.getInt("currentPieceY", 0);

            if (flatShape != null && shapeRows > 0 && shapeCols > 0)
            {
                // ... (Reconstruct shape array with bounds checks) ...
                String[][] shape = new String[shapeRows][shapeCols];
                boolean shapeOk = true;

                for (int i = 0; i < shapeRows; i++)
                {
                    if ((i * shapeCols + shapeCols) <= flatShape.length)
                        System.arraycopy(flatShape, i * shapeCols, shape[i], 0, shapeCols);

                    else {
                        Log.e("Error restoring shape", "Error restoring shape: Array index out of bounds.");
                        shapeOk = false;
                        break;
                    }
                }

                if (shapeOk)
                {
                    try
                    {
                        Shapes.PieceType type = Shapes.PieceType.valueOf(shapeType);
                        currentPiece = new Tetromino(type, shape, x, y);
                        currentPiece.setRotationState(rotationState);
                    }

                    catch (Exception e) {
                        Log.e("GameState", "Error fully restoring piece", e);
                        currentPiece = null;
                    }
                }
                else { currentPiece = null; }
            }
            else { currentPiece = null; }
        }
        Log.d("GameState", "State restored. isPaused=" + isPaused + ", currentPiece=" + (currentPiece != null));
    }
}