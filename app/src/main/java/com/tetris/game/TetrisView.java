package com.tetris.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TetrisView extends View {
    private TetrisGame game;
    private Paint paint;
    private Paint gridPaint;
    private Paint textPaint;
    private float blockSize;
    private float offsetX;
    private float offsetY;

    public TetrisView(Context context) {
        super(context);
        init();
    }

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.parseColor("#666666"));
        gridPaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
    }

    public void setGame(TetrisGame game) {
        this.game = game;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (game != null) {
            TetrisBoard board = game.getBoard();
            float boardWidth = w * 0.8f;
            float boardHeight = h * 0.9f;

            float blockWidth = boardWidth / board.getCols();
            float blockHeight = boardHeight / board.getRows();
            blockSize = Math.min(blockWidth, blockHeight);

            offsetX = (w - blockSize * board.getCols()) / 2;
            offsetY = (h - blockSize * board.getRows()) / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (game == null) return;

        // Draw background
        canvas.drawColor(Color.parseColor("#222222"));

        TetrisBoard board = game.getBoard();
        int[][] boardState = board.getBoard();
        int[][] colors = board.getColors();

        // Draw the board
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                float x = offsetX + j * blockSize;
                float y = offsetY + i * blockSize;

                // Draw grid
                canvas.drawRect(x, y, x + blockSize, y + blockSize, gridPaint);

                // Draw placed blocks
                if (boardState[i][j] != 0) {
                    paint.setColor(colors[i][j]);
                    canvas.drawRect(x + 2, y + 2, x + blockSize - 2, y + blockSize - 2, paint);
                }
            }
        }

        // Draw current piece
        if (!game.isGameOver()) {
            TetrisPiece currentPiece = game.getCurrentPiece();
            int[][] shape = currentPiece.getShape();
            paint.setColor(currentPiece.getColor());

            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        int boardX = currentPiece.getX() + j;
                        int boardY = currentPiece.getY() + i;

                        if (boardY >= 0) {
                            float x = offsetX + boardX * blockSize;
                            float y = offsetY + boardY * blockSize;
                            canvas.drawRect(x + 2, y + 2, x + blockSize - 2, y + blockSize - 2, paint);
                        }
                    }
                }
            }
        }

        // Draw next piece preview
        drawNextPiece(canvas);

        // Draw game over text
        if (game.isGameOver()) {
            textPaint.setTextSize(80);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("GAME OVER", getWidth() / 2f, getHeight() / 2f, textPaint);
        }
    }

    private void drawNextPiece(Canvas canvas) {
        TetrisPiece nextPiece = game.getNextPiece();
        if (nextPiece == null) return;

        int[][] shape = nextPiece.getShape();
        float previewBlockSize = blockSize * 0.6f;
        float previewX = offsetX + (game.getBoard().getCols() * blockSize) + 20;
        float previewY = offsetY + 20;

        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Next:", previewX, previewY, textPaint);

        paint.setColor(nextPiece.getColor());
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    float x = previewX + j * previewBlockSize;
                    float y = previewY + 20 + i * previewBlockSize;
                    canvas.drawRect(x + 1, y + 1, x + previewBlockSize - 1, y + previewBlockSize - 1, paint);
                }
            }
        }
    }

    public void refresh() {
        invalidate();
    }
}
