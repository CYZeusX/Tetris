package com.cyzco.game;

public class Tetromino
{
    private String[][] shape; // The shape of the piece
    private int x, y; // Position of the piece

    public Tetromino(String[][] shape)
    {
        this.shape = shape;
        this.x = 0;
        this.y = 0;
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public String[][] getShape()
    {
        return shape;
    }

    // Method to rotate the piece clockwise
    public void rotateClockwise()
    {
        int n = shape.length;
        String[][] newShape = new String[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                newShape[j][n - 1 - i] = shape[i][j];
            }
        }
        shape = newShape;
    }

    // Method to rotate the piece counterclockwise
    public void rotateCounterClockwise()
    {
        int n = shape.length;
        String[][] newShape = new String[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                newShape[n - 1 - j][i] = shape[i][j];
            }
        }
        shape = newShape;
    }
}