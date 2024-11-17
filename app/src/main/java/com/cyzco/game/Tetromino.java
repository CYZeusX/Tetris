package com.cyzco.game;

public class Tetromino
{
    private char[][] shape; // The shape of the piece
    private int x, y; // Position of the piece

    public Tetromino(char[][] shape)
    {
        this.shape = shape;
    }

    public char[][] getShape()
    {
        return shape;
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

    // Method to rotate the piece clockwise
    public void rotateClockwise()
    {
        int n = shape.length;
        char[][] newShape = new char[n][n];
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
        char[][] newShape = new char[n][n];
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