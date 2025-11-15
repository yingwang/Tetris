package com.tetris.game;

import java.util.Random;

public class TetrisGame {
    private TetrisBoard board;
    private TetrisPiece currentPiece;
    private TetrisPiece nextPiece;
    private int score;
    private int level;
    private int speed;
    private boolean gameOver;
    private boolean paused;
    private Random random;
    private SoundManager soundManager;

    public interface GameListener {
        void onScoreChanged(int score);
        void onLevelChanged(int level);
        void onGameOver();
        void onBoardChanged();
        void onLinesClearing(int[] lines);
    }

    private GameListener listener;

    public TetrisGame(int speed, SoundManager soundManager) {
        this(speed, soundManager, 0);
    }

    public TetrisGame(int speed, SoundManager soundManager, int startingLines) {
        this.speed = speed;
        this.soundManager = soundManager;
        this.random = new Random();
        this.board = new TetrisBoard();
        this.score = 0;
        this.level = 1;
        this.gameOver = false;
        this.paused = false;
        this.currentPiece = createRandomPiece();
        this.nextPiece = createRandomPiece();

        if (startingLines > 0) {
            board.addStartingLines(startingLines);
        }
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    private TetrisPiece createRandomPiece() {
        TetrisPiece.PieceType[] types = TetrisPiece.PieceType.values();
        TetrisPiece.PieceType randomType = types[random.nextInt(types.length)];
        return new TetrisPiece(randomType);
    }

    public void moveLeft() {
        if (gameOver || paused) return;
        TetrisPiece temp = currentPiece.copy();
        temp.moveLeft();
        if (board.isValidPosition(temp)) {
            currentPiece.moveLeft();
            if (soundManager != null) soundManager.playMove();
            notifyBoardChanged();
        }
    }

    public void moveRight() {
        if (gameOver || paused) return;
        TetrisPiece temp = currentPiece.copy();
        temp.moveRight();
        if (board.isValidPosition(temp)) {
            currentPiece.moveRight();
            if (soundManager != null) soundManager.playMove();
            notifyBoardChanged();
        }
    }

    public void rotate() {
        if (gameOver || paused) return;

        // Try wall kicks: attempt rotation with different horizontal offsets
        int[] wallKickOffsets = {0, -1, 1, -2, 2};

        for (int offset : wallKickOffsets) {
            TetrisPiece temp = currentPiece.copy();
            temp.rotate();
            temp.setX(temp.getX() + offset);

            if (board.isValidPosition(temp)) {
                currentPiece.rotate();
                currentPiece.setX(currentPiece.getX() + offset);
                if (soundManager != null) soundManager.playRotate();
                notifyBoardChanged();
                return; // Rotation successful
            }
        }

        // If no wall kick worked, rotation fails silently
    }

    public void drop() {
        if (gameOver || paused) return;
        while (moveDown()) {
            // Keep moving down until it can't
        }
        if (soundManager != null) soundManager.playDrop();
    }

    public boolean moveDown() {
        if (gameOver || paused) return false;

        TetrisPiece temp = currentPiece.copy();
        temp.moveDown();

        if (board.isValidPosition(temp)) {
            currentPiece.moveDown();
            notifyBoardChanged();
            return true;
        } else {
            // Piece can't move down, place it on the board
            board.placePiece(currentPiece);

            // Check for full lines first
            int[] fullLines = board.getFullLines();
            if (fullLines.length > 0) {
                // Notify for animation
                notifyLinesClearing(fullLines);
            }

            // Clear lines and update score
            int linesCleared = board.clearLines();
            if (linesCleared > 0) {
                if (soundManager != null) soundManager.playLineClear();
                updateScore(linesCleared);
            }

            // Update music speed based on board fill level
            updateMusicSpeed();

            // Get next piece
            currentPiece = nextPiece;
            nextPiece = createRandomPiece();

            // Check if game over
            if (!board.isValidPosition(currentPiece)) {
                gameOver = true;
                if (soundManager != null) soundManager.playGameOver();
                notifyGameOver();
            }

            notifyBoardChanged();
            return false;
        }
    }

    private void updateScore(int linesCleared) {
        int points = 0;
        switch (linesCleared) {
            case 1:
                points = 100;
                break;
            case 2:
                points = 300;
                break;
            case 3:
                points = 500;
                break;
            case 4:
                points = 800;
                break;
        }
        score += points * level;

        // Update level based on score
        int newLevel = (score / 1000) + 1;
        if (newLevel != level) {
            level = newLevel;
            if (soundManager != null) soundManager.playLevelUp();
            notifyLevelChanged();
        }

        notifyScoreChanged();
    }

    public TetrisBoard getBoard() {
        return board;
    }

    public TetrisPiece getCurrentPiece() {
        return currentPiece;
    }

    public TetrisPiece getNextPiece() {
        return nextPiece;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    private void notifyScoreChanged() {
        if (listener != null) {
            listener.onScoreChanged(score);
        }
    }

    private void notifyLevelChanged() {
        if (listener != null) {
            listener.onLevelChanged(level);
        }
    }

    private void notifyGameOver() {
        if (listener != null) {
            listener.onGameOver();
        }
    }

    private void notifyBoardChanged() {
        if (listener != null) {
            listener.onBoardChanged();
        }
    }

    private void notifyLinesClearing(int[] lines) {
        if (listener != null) {
            listener.onLinesClearing(lines);
        }
    }

    private void updateMusicSpeed() {
        if (soundManager == null) return;

        float fillLevel = board.getBoardFillLevel();

        // Speed up music when board is 40% or more full
        // Normal speed (1.0) when empty, up to 1.5x when 80%+ full
        float musicSpeed = 1.0f;
        if (fillLevel > 0.4f) {
            // Gradually increase speed from 1.0 to 1.5 as fillLevel goes from 0.4 to 0.8+
            musicSpeed = 1.0f + ((fillLevel - 0.4f) / 0.4f) * 0.5f;
            musicSpeed = Math.min(1.5f, musicSpeed); // Cap at 1.5x
        }

        soundManager.setMusicSpeed(musicSpeed);
    }
}
