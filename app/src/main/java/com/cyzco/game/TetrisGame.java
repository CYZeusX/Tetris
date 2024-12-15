package com.cyzco.game;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
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
import android.os.Vibrator;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

public class TetrisGame
{
    private final char[][] BOARD;
    private final int BOARD_WIDTH = 10; // 10 blocks wide
    private final int BOARD_HEIGHT = 20; // 20 blocks tall
    private final char BOARD_BLOCK = Shapes.space;
    private final int REMOVE_LINE_SCORE = 100;
    private final Context CONTEXT;
    public int tetrisGained = 0;
    private int linesCleared = 0;
    private int scoreGained = 0;
    private boolean showEffect = false;
    private boolean vibrationTriggered = false;
    private long effectEndTime = 135; // Timestamp for when the effect ends
    private Tetromino currentPiece;
    private Bitmap effectBitmap; // Class member for reuse
    private Bitmap resizedEffectBitmap;
    private Map<Character, Integer> blockColors = new HashMap<>();
    private EditText stringShape;
    private Shapes shapes = new Shapes();
    private GameOverFragment gameOverFragment = new GameOverFragment();
    private GameWinFragment gameWinFragment = new GameWinFragment();

    public TetrisGame(Context context)
    {
        this.CONTEXT = context;

        BOARD = new char[BOARD_HEIGHT][BOARD_WIDTH];
        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                BOARD[y][x] = BOARD_BLOCK;
            }
        }

        initializeBlockColorsAndCharacters();
        spawnNewPiece();
    }

    public void setStringShape(EditText stringShape)
    {
        this.stringShape = stringShape;
    }

    public void changeBlock()
    {
        if (stringShape != null)
        {
            String shape = stringShape.getText().toString().strip();
            if (shape.isEmpty())
                shape = "回";
            shapes.setShape(shape);
        }
    }

    public String getBlock()
    {
        return shapes.getShape();
    }

    public int getLinesCleared()
    {
        return linesCleared;
    }

    public int getScoreGained()
    {
        return scoreGained;
    }

    public void setLinesCleared(int linesCleared)
    {
        this.linesCleared = linesCleared;
    }

    public void setScoreGained(int scoreGained)
    {
        this.scoreGained = scoreGained;
    }

    private void spawnNewPiece()
    {
        int randomIndex = (int) (Math.random() * Shapes.SHAPES.length);  // Random index for shapes
        char[][] randomShape = Shapes.SHAPES[randomIndex];  // Get the random shape from the Shapes array

        // Debugging: Print the selected shape
        System.out.println("Random shape index: " + randomIndex);
        for (char[] row : randomShape) {
            System.out.println(Arrays.toString(row));  // Print each row of the shape
        }

        currentPiece = new Tetromino(randomShape);  // Pass the shape to the Tetromino constructor

        // Position the new piece at the top of the board
        int startX = (BOARD_WIDTH - currentPiece.getShape()[0].length) / 2;
        int startY = 0;
        currentPiece.setPosition(startX, startY);

        // Check if the piece can be placed
        if (!canMove(currentPiece, startX, startY)) {
            handleGameOver();  // Handle game over if the piece can't fit
        }
    }

    private boolean isPaused = false;

    public void togglePause()
    {
        isPaused = !isPaused;
    }

    private void handleGameWin()
    {
        togglePause();
        gameWinFragment.show(((MainActivity) CONTEXT).getSupportFragmentManager(), "GameWinFragment");
    }

    private void handleGameOver()
    {
        togglePause();
        gameOverFragment.show(((MainActivity) CONTEXT).getSupportFragmentManager(), "GameOverFragment");
    }

    public void movePieceLeft()
    {
        if (canMove(currentPiece, currentPiece.getX() - 1, currentPiece.getY()))
        {
            currentPiece.setPosition(currentPiece.getX() - 1, currentPiece.getY());
            setVibrator(new long[]{0, 3}, new int[]{0, 50});
        }
    }

    public void movePieceRight()
    {
        if (canMove(currentPiece, currentPiece.getX() + 1, currentPiece.getY()))
        {
            currentPiece.setPosition(currentPiece.getX() + 1, currentPiece.getY());
            setVibrator(new long[]{0, 3}, new int[]{0, 50});
        }
    }

    // for right buttons controls
    public void getAction(String action)
    {
        switch (action)
        {
            // left buttons
            case "a" -> movePieceLeft();
            case "s" -> movePieceDown();
            case "d" -> movePieceRight();

            // right buttons
            case "A" -> dropPiece();
            case "X", "l1" -> rotatePieceCounterClockwise();
            case "Y", "r1" -> rotatePieceClockwise();
            default -> System.out.println("Unknown action: " + action);
        };
    }

    public void updateGame()
    {
        if (!isPaused)
        {movePieceDown();}
    }

    public void movePieceDown()
    {
        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX, currentY + 1))
        {
            // Move the piece down by one row
            currentPiece.setPosition(currentX, currentY + 1);
            setVibrator(new long[]{0, 2}, new int[]{0, 50});
        } else
        {
            // Lock the piece in place and spawn a new one
            lockPiece();
            spawnNewPiece();
        }
    }

    public void dropPiece()
    {
        // Move the piece down to the lowest possible position
        int newY = currentPiece.getY();

        while (canMove(currentPiece, currentPiece.getX(), newY + 1))
        {
            newY++; // Keep moving down until we can't
        }

        // Set the piece to the final position
        currentPiece.setPosition(currentPiece.getX(), newY);
        lockPiece();  // Lock the piece in place
        spawnNewPiece(); // Spawn a new piece after the drop
    }

    public void rotatePieceClockwise()
    {
        currentPiece.rotateClockwise();
        if (!canMove(currentPiece, currentPiece.getX(), currentPiece.getY()))
        {
            currentPiece.rotateCounterClockwise(); // Undo rotation if invalid
        }
    }

    public void rotatePieceCounterClockwise()
    {
        currentPiece.rotateCounterClockwise();
        if (!canMove(currentPiece, currentPiece.getX(), currentPiece.getY()))
        {
            currentPiece.rotateClockwise(); // Undo rotation if invalid
        }
    }

    public boolean canMove(Tetromino piece, int newX, int newY)
    {
        char[][] shape = piece.getShape();
        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (shape[i][j] != BOARD_BLOCK)
                {
                    int boardX = newX + j;
                    int boardY = newY + i;
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT || (boardY >= 0 && BOARD[boardY][boardX] != BOARD_BLOCK))
                    {
                        System.out.println("Cannot move: Collision detected at X=" + boardX + " Y=" + boardY); // Debugging log
                        return false; // Blocked, cannot move
                    }
                }
            }
        }
        return true; // No collisions, can move
    }

    private void lockPiece()
    {
        char[][] shape = currentPiece.getShape();
        int startX = currentPiece.getX();
        int startY = currentPiece.getY();

        // Transfer the Tetromino shape into the board
        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (shape[i][j] != BOARD_BLOCK)
                {
                    int boardX = startX + j;
                    int boardY = startY + i;
                    if (boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH)
                    {
                        BOARD[boardY][boardX] = shape[i][j];
                    }
                }
            }
        }

        int placeBlockScore = 36;
        scoreGained += placeBlockScore;
        checkCompletedLines(); // Check for completed lines after locking
    }

    private void checkCompletedLines()
    {
        int rowsClearedInThisStep = 0; // Counter for rows cleared in one step

        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            boolean lineComplete = true;
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                if (BOARD[y][x] == BOARD_BLOCK)
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

        // Check if 4 rows were cleared at once
        if (rowsClearedInThisStep == 4)
        {
            scoreGained += REMOVE_LINE_SCORE * 2; // Double the score for clearing 4 rows
            showEffect = true;
            effectEndTime = System.currentTimeMillis() + 140;
            tetrisGained++;
        }
        else
        {
            scoreGained += REMOVE_LINE_SCORE * rowsClearedInThisStep; // Normal score for other cases
        }

        linesCleared += rowsClearedInThisStep; // Update the total lines cleared

        if (linesCleared >= 150)
        {
            handleGameWin();
        }
    }

    private void removeLine(int lineIndex)
    {
        for (int y = lineIndex; y > 0; y--)
        {
            System.arraycopy(BOARD[y - 1], 0, BOARD[y], 0, BOARD_WIDTH);
        }
        for (int x = 0; x < BOARD_WIDTH; x++)
        {
            BOARD[0][x] = BOARD_BLOCK;
        }
        linesCleared++;
        scoreGained += REMOVE_LINE_SCORE;
    }

    public int calculateShadowPosition()
    {
        int shadowY = currentPiece.getY();
        while (canMove(currentPiece, currentPiece.getX(), shadowY + 1))
        {
            shadowY++;
        }
        return shadowY; // The Y-coordinate where the shadow will rest
    }

    public void initEffects(Resources resources)
    {
        if (effectBitmap == null)
        {
            effectBitmap = BitmapFactory.decodeResource(resources, R.drawable.tetris_boom_max);
            if (effectBitmap != null)
            {
                resizedEffectBitmap = Bitmap.createScaledBitmap(effectBitmap, 250, 95, true);
                System.out.println("Effect bitmaps initialized.");
            }
            else
            {
                System.err.println("Error: Could not decode resource for tetris_boom.");
            }
        }
    }

    private void initializeBlockColorsAndCharacters()
    {
        blockColors.put(Shapes.I_BLOCK, Color.parseColor("#00FFFF")); // I-shape
        blockColors.put(Shapes.O_BLOCK, Color.parseColor("#FFEB3B")); // O-shape
        blockColors.put(Shapes.T_BLOCK, Color.parseColor("#9500FF")); // T-shape
        blockColors.put(Shapes.S_BLOCK, Color.parseColor("#59FF33")); // S-shape
        blockColors.put(Shapes.Z_BLOCK, Color.parseColor("#FF4D4D")); // Z-shape
        blockColors.put(Shapes.J_BLOCK, Color.parseColor("#3460FF")); // J-shape
        blockColors.put(Shapes.L_BLOCK, Color.parseColor("#FFA621")); // L-shape

        blockColors.put(Shapes.space, Color.WHITE); // No color for empty spaces
    }

    public void renderGame(SurfaceView monitor)
    {
        // Ensure effect bitmaps are initialized
        if (effectBitmap == null || resizedEffectBitmap == null)
        {
            System.err.println("Effect bitmaps not initialized!");
            initEffects(CONTEXT.getResources());
        }

        monitor.setZOrderOnTop(true);
        SurfaceHolder holder = monitor.getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
        Canvas canvas = holder.lockCanvas();

        if (canvas != null)
        {
            // Clear the canvas with a transparent background
            clearCanvas(canvas);

            Paint paint = createPaint(monitor.getContext());

            // Calculate block size and padding
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            int blockSize = Math.min(canvasWidth / BOARD_WIDTH, canvasHeight / BOARD_HEIGHT);
            int paddingLeft = (canvasWidth - (BOARD_WIDTH * blockSize)) / 2;
            int paddingTop = (canvasHeight - (BOARD_HEIGHT * blockSize)) / 2;

            renderBoardBackground(canvas, paint, blockSize, paddingLeft, paddingTop);
            renderFixedBlocks(canvas, paint, blockSize, paddingLeft, paddingTop);
            renderCurrentPiece(canvas, paint, blockSize, paddingLeft, paddingTop);
            renderShadow(canvas, paint, blockSize, paddingLeft, paddingTop);
            renderEffect(canvas, canvasWidth, canvasHeight);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void clearCanvas(Canvas canvas)
    {
        canvas.drawColor(Color.argb(0, 255, 255, 255), PorterDuff.Mode.CLEAR);
    }

    private Paint createPaint(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // Determine the shape size based on orientation
        int orientation = sharedPreferences.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int shapeSize = (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ? 50 : 36;

        // Create and configure the Paint object
        Paint paint = new Paint();
        paint.setTextSize(shapeSize);
        paint.setTypeface(Typeface.MONOSPACE);
        return paint;
    }

    private void renderBoardBackground(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        paint.setColor(Color.rgb(110, 110, 110));
        for (int y = 0; y < BOARD_HEIGHT; y++)
        {
            for (int x = 0; x < BOARD_WIDTH; x++)
            {
                float posX = paddingLeft + (x + 0f) * blockSize;
                float posY = paddingTop + (y + 1f) * blockSize;
                paint.setAlpha(220);
                canvas.drawText(String.valueOf(Shapes.space), posX, posY, paint);
            }
        }
    }

    private void renderFixedBlocks(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        for (int y = 0; y < BOARD.length; y++)
        {
            for (int x = 0; x < BOARD[y].length; x++)
            {
                char blockChar = BOARD[y][x];
                if (blockChar != Shapes.space)
                {
                    Integer color = blockColors.get(blockChar);
                    if (color != null)
                    {
                        paint.setColor(color);
                        paint.setFakeBoldText(true);
                    }
                    float posX = paddingLeft + x * blockSize;
                    float posY = paddingTop + (y + 1) * blockSize;
                    canvas.drawText(getBlock(), posX, posY, paint);
                }
            }
        }
    }

    private void renderCurrentPiece(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        char[][] shape = currentPiece.getShape();
        int pieceX = currentPiece.getX();
        int pieceY = currentPiece.getY();

        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (shape[i][j] != Shapes.space)
                {
                    drawBlock(canvas, paint, shape[i][j], pieceX + j, pieceY + i, blockSize, paddingLeft, paddingTop, 255);
                }
            }
        }
    }

    private void renderShadow(Canvas canvas, Paint paint, int blockSize, int paddingLeft, int paddingTop)
    {
        int shadowY = calculateShadowPosition();
        char[][] shape = currentPiece.getShape();
        int pieceX = currentPiece.getX();

        for (int i = 0; i < shape.length; i++)
        {
            for (int j = 0; j < shape[i].length; j++)
            {
                if (shape[i][j] != Shapes.space)
                {
                    drawBlock(canvas, paint, shape[i][j], pieceX + j, shadowY + i + 1, blockSize, paddingLeft, paddingTop, 120);
                }
            }
        }
    }

    private void drawBlock(Canvas canvas, Paint paint, char blockChar, int x, int y, int blockSize, int paddingLeft, int paddingTop, int alpha)
    {
        Integer color = blockColors.get(blockChar);
        if (color != null)
        {
            paint.setColor(color);
            paint.setFakeBoldText(true);
            paint.setAlpha(alpha);
        }
        float blockX = paddingLeft + x * blockSize;
        float blockY = paddingTop + y * blockSize;
        canvas.drawText(getBlock(), blockX, blockY, paint);
    }

    private void renderEffect(Canvas canvas, int canvasWidth, int canvasHeight)
    {
        if (showEffect && resizedEffectBitmap != null)
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime <= effectEndTime)
            {
                float posX = (canvasWidth - resizedEffectBitmap.getWidth()) / 2.0f;
                float posY = (canvasHeight - resizedEffectBitmap.getHeight()) / 2.0f;

                setVibrator(new long[]{0, 210}, new int[]{0, 30});
                canvas.drawBitmap(resizedEffectBitmap, posX, posY, null);
            } else
            {
                showEffect = false;
                vibrationTriggered = false;
            }
        } else if (showEffect)
        {
            System.err.println("Error: resizedEffectBitmap is null. Effect will not be rendered.");
        }
    }

    public void setVibrator(long[] timings, int[] amplitudes)
    {
        if (CONTEXT != null)
        {
            Vibrator vibrator = (Vibrator) CONTEXT.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator())
            {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            }
        }
    }

}