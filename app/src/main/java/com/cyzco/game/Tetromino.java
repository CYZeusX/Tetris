package com.cyzco.game;

import java.util.Arrays;

public class Tetromino
{

    private String[][] shape; // The current shape matrix
    private int x; // Current x position on the board (leftmost column of the shape matrix)
    private int y; // Current y position on the board (topmost row of the shape matrix)
    private int rotationState; // 0: initial, 1: rotated 90 deg clockwise, 2: 180 deg, 3: 270 deg clockwise
    private Shapes.PieceType shapeType; // Use PieceType enum (defined elsewhere)

    // Reference to the empty space character
    private static final String EMPTY_SPACE = Shapes.space;

    // --- Standard SRS Wall Kick Data ---
    private static final int[][][] KICK_DATA_JLSTZ = {
            // 0 -> 1 (Clockwise)
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},
            // 1 -> 0 (Counter-Clockwise)
            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},
            // 1 -> 2 (Clockwise)
            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},
            // 2 -> 1 (Counter-Clockwise)
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},
            // 2 -> 3 (Clockwise)
            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},
            // 3 -> 2 (Counter-Clockwise)
            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},
            // 3 -> 0 (Clockwise)
            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},
            // 0 -> 3 (Counter-Clockwise)
            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}}
    };

    private static final int[][][] KICK_DATA_I = {
            // 0 -> 1 (Clockwise)
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},
            // 1 -> 0 (Counter-Clockwise)
            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},
            // 1 -> 2 (Clockwise)
            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},
            // 2 -> 1 (Counter-Clockwise)
            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},
            // 2 -> 3 (Clockwise)
            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},
            // 3 -> 2 (Counter-Clockwise)
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},
            // 3 -> 0 (Clockwise)
            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},
            // 0 -> 3 (Counter-Clockwise)
            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}}
    };

    /**
     * Constructor for a Tetromino.
     * @param type The type of shape (using PieceType enum).
     * @param initialShape The initial 2D array representing the shape.
     * @param startX Initial X position on the board.
     * @param startY Initial Y position on the board.
     */
    public Tetromino(Shapes.PieceType type, String[][] initialShape, int startX, int startY) { // Use PieceType
        this.shapeType = type;
        this.shape = Arrays.stream(initialShape).map(String[]::clone).toArray(String[][]::new);
        this.x = startX;
        this.y = startY;
        this.rotationState = 0;
    }

    // --- Basic Getters and Setters ---
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public String[][] getShapeCopy() {
        return Arrays.stream(shape).map(String[]::clone).toArray(String[][]::new);
    }

    public String[][] getShapeInternal() { return shape; }

    public Shapes.PieceType getShapeType() { return shapeType; }

    public int getRotationState() { return rotationState; }

    public void setRotationState(int rotationState) {
        if (rotationState >= 0 && rotationState < 4) {
            this.rotationState = rotationState;
        } else {
            System.err.println("Warning: Invalid rotation state (" + rotationState + ") passed to setRotationState. Using 0.");
            this.rotationState = 0;
        }
    }

    public void setShape(String[][] savedShape) {
        if (savedShape != null)
            this.shape = Arrays.stream(savedShape).map(String[]::clone).toArray(String[][]::new);
        else System.err.println("Warning: Null shape passed to setShape.");
    }

    // --- Rotation Logic with Wall Kicks ---
    public boolean rotateClockwise(String[][] board)
    {
        // Use PieceType
        if (this.shapeType == Shapes.PieceType.O) return true;

        int shapeLength = shape.length;
        String[][] rotatedShape = new String[shapeLength][shapeLength];
        for (int i = 0; i < shapeLength; i++) {
            for (int j = 0; j < shapeLength; j++)
                rotatedShape[j][shapeLength - 1 - i] = shape[i][j];
        }

        int nextRotationState = (this.rotationState + 1) % 4;
        int[][] kicks = getKicks(this.rotationState, nextRotationState);

        for (int[] kick : kicks) {
            int kickX = kick[0];
            int kickY = kick[1];
            int testX = this.x + kickX;
            int testY = this.y - kickY;

            if (isValidPosition(rotatedShape, testX, testY, board)) {
                this.shape = rotatedShape;
                this.x = testX;
                this.y = testY;
                this.rotationState = nextRotationState;
                return true;
            }
        }
        return false;
    }

    public boolean rotateCounterClockwise(String[][] board)
    {
        // Use PieceType
        if (this.shapeType == Shapes.PieceType.O)
            return true;

        int shapeLength = shape.length;
        String[][] rotatedShape = new String[shapeLength][shapeLength];
        for (int i = 0; i < shapeLength; i++) {
            for (int j = 0; j < shapeLength; j++)
                rotatedShape[shapeLength - 1 - j][i] = shape[i][j];
        }

        int nextRotationState = (this.rotationState == 0) ? 3 : this.rotationState - 1;
        int[][] kicks = getKicks(this.rotationState, nextRotationState);

        for (int[] kick : kicks) {
            int kickX = kick[0];
            int kickY = kick[1];
            int testX = this.x + kickX;
            int testY = this.y - kickY;

            if (isValidPosition(rotatedShape, testX, testY, board)) {
                this.shape = rotatedShape;
                this.x = testX;
                this.y = testY;
                this.rotationState = nextRotationState;
                return true;
            }
        }
        return false;
    }

    private int[][] getKicks(int currentRotation, int nextRotation)
    {
        int kickIndex = -1;

        if (currentRotation == 0 && nextRotation == 1) kickIndex = 0;
        else if (currentRotation == 1 && nextRotation == 0) kickIndex = 1;
        else if (currentRotation == 1 && nextRotation == 2) kickIndex = 2;
        else if (currentRotation == 2 && nextRotation == 1) kickIndex = 3;
        else if (currentRotation == 2 && nextRotation == 3) kickIndex = 4;
        else if (currentRotation == 3 && nextRotation == 2) kickIndex = 5;
        else if (currentRotation == 3 && nextRotation == 0) kickIndex = 6;
        else if (currentRotation == 0 && nextRotation == 3) kickIndex = 7;

        if (kickIndex == -1) {
            System.err.println("Error: Invalid rotation transition requested: " + currentRotation + " -> " + nextRotation);
            return new int[0][0];
        }

        // Use PieceType for comparisons
        if (shapeType == Shapes.PieceType.I) return KICK_DATA_I[kickIndex];
        else if (shapeType != Shapes.PieceType.O) return KICK_DATA_JLSTZ[kickIndex];
        else return new int[][]{{0, 0}};
    }

    public boolean isValidPosition(String[][] testShape, int testX, int testY, String[][] board) {
        if (board == null || board.length == 0 || board[0].length == 0) {
            System.err.println("Error: Board is invalid in isValidPosition.");
            return false;
        }

        int boardHeight = board.length;
        int boardWidth = board[0].length;
        int shapeSize = testShape.length;

        for (int row = 0; row < shapeSize; row++) {
            for (int column = 0; column < shapeSize; column++) {
                if (testShape[row][column] != null && !testShape[row][column].equals(EMPTY_SPACE)) {
                    int boardCol = testX + column;
                    int boardRow = testY + row;

                    if (boardCol < 0 || boardCol >= boardWidth || boardRow >= boardHeight) {
                        return false; // Out of bounds
                    }

                    if (boardRow >= 0) {
                        if (board[boardRow][boardCol] != null && !board[boardRow][boardCol].equals(EMPTY_SPACE)) {
                            return false; // Collision
                        }
                    }
                }
            }
        }
        return true;
    }
}