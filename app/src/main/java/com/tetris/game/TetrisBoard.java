package com.tetris.game;

import android.graphics.Color;

public class TetrisBoard {
    private static final int ROWS = 20;
    private static final int COLS = 10;

    private int[][] board;
    private int[][] colors;

    public TetrisBoard() {
        board = new int[ROWS][COLS];
        colors = new int[ROWS][COLS];
        clear();
    }

    public void clear() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = 0;
                colors[i][j] = Color.TRANSPARENT;
            }
        }
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }

    public int[][] getBoard() {
        return board;
    }

    public int[][] getColors() {
        return colors;
    }

    public boolean isValidPosition(TetrisPiece piece) {
        int[][] shape = piece.getShape();
        int pieceX = piece.getX();
        int pieceY = piece.getY();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardX = pieceX + j;
                    int boardY = pieceY + i;

                    // Check boundaries
                    if (boardX < 0 || boardX >= COLS || boardY >= ROWS) {
                        return false;
                    }

                    // Check if position is occupied (only if not above the board)
                    if (boardY >= 0 && board[boardY][boardX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void placePiece(TetrisPiece piece) {
        int[][] shape = piece.getShape();
        int pieceX = piece.getX();
        int pieceY = piece.getY();
        int pieceColor = piece.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardX = pieceX + j;
                    int boardY = pieceY + i;
                    if (boardY >= 0 && boardY < ROWS && boardX >= 0 && boardX < COLS) {
                        board[boardY][boardX] = 1;
                        colors[boardY][boardX] = pieceColor;
                    }
                }
            }
        }
    }

    public int[] getFullLines() {
        int[] fullLines = new int[ROWS];
        int count = 0;
        for (int i = 0; i < ROWS; i++) {
            if (isLineFull(i)) {
                fullLines[count++] = i;
            }
        }
        int[] result = new int[count];
        System.arraycopy(fullLines, 0, result, 0, count);
        return result;
    }

    public int clearLines() {
        int linesCleared = 0;
        for (int i = ROWS - 1; i >= 0; i--) {
            if (isLineFull(i)) {
                removeLine(i);
                linesCleared++;
                i++; // Check the same row again since rows shifted down
            }
        }
        return linesCleared;
    }

    public void addStartingLines(int numLines) {
        if (numLines <= 0 || numLines >= ROWS) return;

        // Shift existing content up
        for (int i = 0; i < ROWS - numLines; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = board[i + numLines][j];
                colors[i][j] = colors[i + numLines][j];
            }
        }

        // Use actual tetromino colors to make it look like accumulated blocks
        int[] tetrominoColors = {
            Color.CYAN,      // I piece
            Color.YELLOW,    // O piece
            Color.MAGENTA,   // T piece
            Color.GREEN,     // S piece
            Color.RED,       // Z piece
            Color.BLUE,      // J piece
            Color.rgb(255, 165, 0)  // L piece (Orange)
        };

        for (int i = ROWS - numLines; i < ROWS; i++) {
            // Random gap position (1-2 gaps per line)
            int gapPos1 = (int) (Math.random() * COLS);
            int gapPos2 = (int) (Math.random() * COLS);
            while (gapPos2 == gapPos1) {
                gapPos2 = (int) (Math.random() * COLS);
            }

            for (int j = 0; j < COLS; j++) {
                if (j == gapPos1 || (numLines > 3 && j == gapPos2)) {
                    board[i][j] = 0;
                    colors[i][j] = Color.TRANSPARENT;
                } else {
                    board[i][j] = 1;
                    // Randomly select a tetromino color
                    colors[i][j] = tetrominoColors[(int) (Math.random() * tetrominoColors.length)];
                }
            }
        }
    }

    private boolean isLineFull(int row) {
        for (int j = 0; j < COLS; j++) {
            if (board[row][j] == 0) {
                return false;
            }
        }
        return true;
    }

    private void removeLine(int row) {
        // Shift all rows above down
        for (int i = row; i > 0; i--) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = board[i - 1][j];
                colors[i][j] = colors[i - 1][j];
            }
        }
        // Clear top row
        for (int j = 0; j < COLS; j++) {
            board[0][j] = 0;
            colors[0][j] = Color.TRANSPARENT;
        }
    }

    /**
     * Get the highest row that contains blocks (0 = top, ROWS-1 = bottom)
     * Returns ROWS if board is empty
     */
    public int getHighestBlockRow() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] != 0) {
                    return i;
                }
            }
        }
        return ROWS; // Board is empty
    }

    /**
     * Get fill percentage (0.0 to 1.0) based on how close blocks are to top
     * Returns higher values when blocks are near the top
     */
    public float getBoardFillLevel() {
        int highestRow = getHighestBlockRow();
        if (highestRow == ROWS) return 0.0f; // Empty board

        // Convert to fill level: higher when blocks are near top
        // Top 25% of board (rows 0-4) = danger zone
        float fillLevel = 1.0f - ((float) highestRow / ROWS);
        return Math.max(0.0f, Math.min(1.0f, fillLevel));
    }
}
