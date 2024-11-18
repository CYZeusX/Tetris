package com.cyzco.game;

import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.os.VibrationEffect;
import android.view.SurfaceHolder;
import android.graphics.Typeface;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Color;
import android.os.Vibrator;

public class TetrisGame
{
    private final char[][] BOARD;
    private Tetromino currentPiece;
    private final int BOARD_WIDTH = 10; // 10 blocks wide
    private final int BOARD_HEIGHT = 20; // 20 blocks tall
    private final char BOARD_BLOCK = Shapes.space;
    private final char BLOCK_PIECE = Shapes.block;
    private int linesCleared = 0;
    private int scoreGained = 0;
    private final int REMOVE_LINE_SCORE = 100;
    private boolean showEffect = false;
    private long effectEndTime = 550; // Timestamp for when the effect ends
    private final Context CONTEXT;
    private boolean vibrationTriggered = false;
    private Bitmap effectBitmap; // Class member for reuse
    private Bitmap resizedEffectBitmap;

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
        spawnNewPiece();
    }

    public int getLinesCleared()
    {
        return linesCleared;
    }

    public int getScoreGained()
    {
        return scoreGained;
    }

    private void spawnNewPiece()
    {
        int randomIndex = (int) (Math.random() * Shapes.SHAPES.length);
        char[][] randomShape = Shapes.SHAPES[randomIndex];
        currentPiece = new Tetromino(randomShape);

        int startX = (BOARD_WIDTH - currentPiece.getShape()[0].length) / 2;
        int startY = 0;
        currentPiece.setPosition(startX, startY);

        // Check if the piece can be placed
        if (!canMove(currentPiece, startX, startY))
        {
            handleGameOver();  // Handle game over if the piece can't fit
        }
    }

    private boolean isPaused = false;

    public void togglePause()
    {
        isPaused = !isPaused;
    }

    private void handleGameOver()
    {
        //System.out.println("Game Over!");
    }

    public void movePieceLeft()
    {
        if (canMove(currentPiece, currentPiece.getX() - 1, currentPiece.getY()))
        {
            currentPiece.setPosition(currentPiece.getX() - 1, currentPiece.getY());
        }
    }

    public void movePieceRight()
    {
        if (canMove(currentPiece, currentPiece.getX() + 1, currentPiece.getY()))
        {
            currentPiece.setPosition(currentPiece.getX() + 1, currentPiece.getY());
        }
    }

    public void updateGame()
    {
        if (!isPaused) {
            movePieceDown();
        }
    }

    public void movePieceDown()
    {
        int currentX = currentPiece.getX();
        int currentY = currentPiece.getY();

        if (canMove(currentPiece, currentX, currentY + 1))
        {
            // Move the piece down by one row
            currentPiece.setPosition(currentX, currentY + 1);
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
            effectEndTime = System.currentTimeMillis() + 550;
        }
        else
        {
            scoreGained += REMOVE_LINE_SCORE * rowsClearedInThisStep; // Normal score for other cases
        }

        linesCleared += rowsClearedInThisStep; // Update the total lines cleared
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
            effectBitmap = BitmapFactory.decodeResource(resources, R.drawable.tetris_boom);
            if (effectBitmap != null)
            {
                resizedEffectBitmap = Bitmap.createScaledBitmap(effectBitmap, 250, 120, true);
                System.out.println("Effect bitmaps initialized.");
            }
            else
            {
                System.err.println("Error: Could not decode resource for tetris_boom.");
            }
        }
    }


    public void cleanupEffects()
    {
        if (resizedEffectBitmap != null && !resizedEffectBitmap.isRecycled())
        {
            resizedEffectBitmap.recycle();
            resizedEffectBitmap = null;
        }
        if (effectBitmap != null && !effectBitmap.isRecycled())
        {
            effectBitmap.recycle();
            effectBitmap = null;
        }
    }

    public void renderGame(SurfaceView monitor)
    {
        // Ensure effect bitmaps are initialized
        if (effectBitmap == null || resizedEffectBitmap == null)
        {
            initEffects(CONTEXT.getResources());
        }

        SurfaceHolder holder = monitor.getHolder();
        Canvas canvas = holder.lockCanvas();

        if (canvas != null)
        {
            // Clear the canvas with a background color
            canvas.drawColor(Color.rgb(167, 179, 75)); // Background color

            Paint paint = new Paint();
            paint.setColor(Color.BLACK); // Text color
            paint.setTextSize(28);       // Adjust size as needed
            paint.setTypeface(Typeface.MONOSPACE); // Monospace font for alignment

            // Calculate block size dynamically based on monitor dimensions
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            int blockSize = Math.min(canvasWidth / BOARD_WIDTH, canvasHeight / BOARD_HEIGHT); // Square blocks

            // Calculate padding for centering the board
            int paddingLeft = (canvasWidth - (BOARD_WIDTH * blockSize)) / 2;
            int paddingTop = (canvasHeight - (BOARD_HEIGHT * blockSize)) / 2;

            // Render the board
            for (int y = 0; y < BOARD.length; y++)
            {
                for (int x = 0; x < BOARD[y].length; x++)
                {
                    if (BOARD[y][x] != BOARD_BLOCK)
                    {
                        // Render '@' for non-empty cells
                        String symbol = String.valueOf(BLOCK_PIECE); // The symbol to render
                        float posX = paddingLeft + x * blockSize; // Adjusted position with dynamic padding
                        float posY = paddingTop + (y + 1) * blockSize; // Adjusted position with dynamic padding
                        canvas.drawText(symbol, posX, posY, paint);
                    }
                }
            }

            // Render the shadow
            int shadowY = calculateShadowPosition(); // Calculate where the shadow should be
            char[][] shape = currentPiece.getShape();
            int pieceX = currentPiece.getX();

            paint.setColor(Color.argb(150, 100, 100, 100)); // Light gray for shadow
            paint.setAlpha(150);          // Semi-transparent shadow

            for (int i = 0; i < shape.length; i++)
            {
                for (int j = 0; j < shape[i].length; j++)
                {
                    if (shape[i][j] != BOARD_BLOCK)
                    {
                        // Calculate shadow block positions
                        float blockX = paddingLeft + (pieceX + j) * blockSize;
                        float blockY = paddingTop + (shadowY + i + 1) * blockSize;

                        // Draw the shadow as a character
                        canvas.drawText(String.valueOf(BLOCK_PIECE), blockX, blockY, paint);
                    }
                }
            }

            // Reset paint for rendering the actual piece
            paint.setColor(Color.BLACK);

            // Render the current piece
            int pieceY = currentPiece.getY();
            for (int i = 0; i < shape.length; i++)
            {
                for (int j = 0; j < shape[i].length; j++)
                {
                    if (shape[i][j] != BOARD_BLOCK)
                    {
                        // Draw current piece blocks
                        float blockX = paddingLeft + (pieceX + j) * blockSize;
                        float blockY = paddingTop + (pieceY + i) * blockSize;
                        canvas.drawText(String.valueOf(BLOCK_PIECE), blockX, blockY, paint);
                    }
                }
            }

            // Handle the effect
            if (showEffect && resizedEffectBitmap != null)
            {
                long currentTime = System.currentTimeMillis();
                if (currentTime <= effectEndTime)
                {
                    float posX = (canvasWidth - resizedEffectBitmap.getWidth()) / 2.0f;
                    float posY = (canvasHeight - resizedEffectBitmap.getHeight()) / 2.0f;

                    triggerEffectVibration(); // Call the refactored vibration method
                    canvas.drawBitmap(resizedEffectBitmap, posX, posY, null);
                }
                else
                {
                    showEffect = false;
                    vibrationTriggered = false;
                }
            }
            else if (showEffect)
            {
                System.err.println("Error: resizedEffectBitmap is null. Effect will not be rendered.");
            }

            holder.unlockCanvasAndPost(canvas); // Ensure canvas is posted correctly
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

    public void triggerEffectVibration()
    {
        if (!vibrationTriggered) {
            setVibrator(new long[]{0, 500}, new int[]{0, 30}); // Example vibration pattern
            vibrationTriggered = true;
        }
    }

}