package com.tetris.game;

import android.graphics.Color;

public class TetrisPiece {
    public enum PieceType {
        I, O, T, S, Z, J, L
    }

    private PieceType type;
    private int[][] shape;
    private int color;
    private int x, y;

    public TetrisPiece(PieceType type) {
        this.type = type;
        this.x = 3;
        this.y = -1;  // Start above the board for proper game over detection
        initializeShape();
        initializeColor();
    }

    private void initializeShape() {
        switch (type) {
            case I:
                shape = new int[][]{
                    {1, 1, 1, 1}
                };
                break;
            case O:
                shape = new int[][]{
                    {1, 1},
                    {1, 1}
                };
                break;
            case T:
                shape = new int[][]{
                    {0, 1, 0},
                    {1, 1, 1}
                };
                break;
            case S:
                shape = new int[][]{
                    {0, 1, 1},
                    {1, 1, 0}
                };
                break;
            case Z:
                shape = new int[][]{
                    {1, 1, 0},
                    {0, 1, 1}
                };
                break;
            case J:
                shape = new int[][]{
                    {1, 0, 0},
                    {1, 1, 1}
                };
                break;
            case L:
                shape = new int[][]{
                    {0, 0, 1},
                    {1, 1, 1}
                };
                break;
        }
    }

    private void initializeColor() {
        // Game Boy Color themed colors - vibrant but retro
        switch (type) {
            case I:
                color = Color.parseColor("#00E5E5"); // GBC Cyan
                break;
            case O:
                color = Color.parseColor("#FFD700"); // GBC Yellow
                break;
            case T:
                color = Color.parseColor("#D946EF"); // GBC Purple
                break;
            case S:
                color = Color.parseColor("#00D500"); // GBC Green
                break;
            case Z:
                color = Color.parseColor("#FF3030"); // GBC Red
                break;
            case J:
                color = Color.parseColor("#4169FF"); // GBC Blue
                break;
            case L:
                color = Color.parseColor("#FF8C00"); // GBC Orange
                break;
        }
    }

    public void rotate() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    public int[][] getShape() {
        return shape;
    }

    public int getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public void moveDown() {
        y++;
    }

    public TetrisPiece copy() {
        TetrisPiece copy = new TetrisPiece(this.type);
        copy.x = this.x;
        copy.y = this.y;
        copy.shape = new int[this.shape.length][this.shape[0].length];
        for (int i = 0; i < this.shape.length; i++) {
            System.arraycopy(this.shape[i], 0, copy.shape[i], 0, this.shape[i].length);
        }
        return copy;
    }
}
