package com.cyzco.game;

import android.graphics.BitmapFactory;
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
    private int boardWidth = 10; // 10 blocks wide
    private int boardHeight = 20; // 20 blocks tall
    private final char BOARD_BLOCK = Shapes.space;
    private final char BLOCK_PIECE = Shapes.block;
    private int linesCleared = 0;
    private int scoreGained = 0;
    private int placeBlockScore = 36;
    private int removeLineScore = 100;
    private boolean showEffect = false;
    private long effectEndTime = 1000; // Timestamp for when the effect ends
    private Context context;
    private Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

    public TetrisGame(Context context)
    {
        this.context = context;

        BOARD = new char[boardHeight][boardWidth];
        for (int y = 0; y < boardHeight; y++)
        {
            for (int x = 0; x < boardWidth; x++)
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

        int startX = (boardWidth - currentPiece.getShape()[0].length) / 2;
        int startY = 0;
        currentPiece.setPosition(startX, startY);

        // Check if the piece can be placed
        if (!canMove(currentPiece, startX, startY))
        {
            handleGameOver();  // Handle game over if the piece can't fit
        }
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
        movePieceDown();  // Move the piece down incrementally
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
                    if (boardX < 0 || boardX >= boardWidth || boardY >= boardHeight || (boardY >= 0 && BOARD[boardY][boardX] != BOARD_BLOCK))
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
                    if (boardY >= 0 && boardY < boardHeight && boardX >= 0 && boardX < boardWidth)
                    {
                        BOARD[boardY][boardX] = shape[i][j];
                    }
                }
            }
        }

        scoreGained += placeBlockScore;
        checkCompletedLines(); // Check for completed lines after locking
    }


    private void checkCompletedLines()
    {
        int rowsClearedInThisStep = 0; // Counter for rows cleared in one step

        for (int y = 0; y < boardHeight; y++)
        {
            boolean lineComplete = true;
            for (int x = 0; x < boardWidth; x++)
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
            scoreGained += removeLineScore * 2; // Double the score for clearing 4 rows
            showEffect = true;
            effectEndTime = System.currentTimeMillis() + 1000; // Show for 1 second
        }
        else
        {
            scoreGained += removeLineScore * rowsClearedInThisStep; // Normal score for other cases
        }

        linesCleared += rowsClearedInThisStep; // Update the total lines cleared
    }

    private void removeLine(int lineIndex)
    {
        for (int y = lineIndex; y > 0; y--)
        {
            System.arraycopy(BOARD[y - 1], 0, BOARD[y], 0, boardWidth);
        }
        for (int x = 0; x < boardWidth; x++)
        {
            BOARD[0][x] = BOARD_BLOCK;
        }
        linesCleared++;
        scoreGained += removeLineScore;
    }

    public void renderGame(SurfaceView monitor)
    {
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
            int blockSize = Math.min(canvasWidth / boardWidth, canvasHeight / boardHeight); // Square blocks

            // Calculate padding for centering the board
            int paddingLeft = (canvasWidth - (boardWidth * blockSize)) / 2;
            int paddingTop = (canvasHeight - (boardHeight * blockSize)) / 2;

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

            // Render the current piece
            char[][] shape = currentPiece.getShape();
            int pieceX = currentPiece.getX();
            int pieceY = currentPiece.getY();

            for (int i = 0; i < shape.length; i++)
            {
                for (int j = 0; j < shape[i].length; j++)
                {
                    if (shape[i][j] != BOARD_BLOCK)
                    {
                        // Draw '@' for each block of the current piece
                        String symbol = String.valueOf(BLOCK_PIECE); // The symbol to render
                        float blockX = paddingLeft + (pieceX + j) * blockSize; // Adjust position with padding
                        float blockY = paddingTop + (pieceY + i + 1) * blockSize; // Adjust position with padding
                        canvas.drawText(symbol, blockX, blockY, paint);
                    }
                }
            }

            // Draw the effect if active
            if (showEffect) {
                long currentTime = System.currentTimeMillis();
                if (currentTime <= effectEndTime) {
                    // Load the bitmap
                    Bitmap effectBitmap = BitmapFactory.decodeResource(monitor.getResources(), R.drawable.tetris_boom);

                    // Define desired fixed size (e.g., 200x200 pixels)
                    int targetWidth = 250; // Desired width in pixels
                    int targetHeight = 120; // Desired height in pixels

                    // Resize the bitmap to the fixed dimensions
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(effectBitmap, targetWidth, targetHeight, true);

                    // Determine position to center the resized image
                    float posX = (canvasWidth - targetWidth) / 2.0f;
                    float posY = (canvasHeight - targetHeight) / 2.0f;

                    if (vibrator != null)
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));

                    // Draw the resized bitmap
                    canvas.drawBitmap(resizedBitmap, posX, posY, null);

                    // Recycle the resized bitmap to free memory
                    resizedBitmap.recycle();
                } else {
                    // Effect duration ended
                    showEffect = false;
                }
            }

            holder.unlockCanvasAndPost(canvas); // Ensure canvas is posted correctly
        }
    }

}