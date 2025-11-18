package com.tetris.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class TetrisView extends View {
    private TetrisGame game;
    private Paint paint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint highlightPaint;
    private Paint shadowPaint;
    private Paint borderPaint;
    private Paint flashPaint;
    private Paint ghostPaint;
    private float blockSize;
    private float offsetX;
    private float offsetY;
    private int[] clearingLines;
    private int flashAlpha = 0;
    private boolean isFlashing = false;

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
        paint.setAntiAlias(false);  // Pixel-perfect rendering

        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.parseColor("#8BAC0F"));  // GB light
        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(false);

        highlightPaint = new Paint();
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAntiAlias(false);

        shadowPaint = new Paint();
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#306230"));  // GB dark
        shadowPaint.setAntiAlias(false);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.parseColor("#0F380F"));  // GB darkest
        borderPaint.setStrokeWidth(4);
        borderPaint.setAntiAlias(false);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#0F380F"));  // GB darkest
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(false);
        textPaint.setTypeface(android.graphics.Typeface.MONOSPACE);

        flashPaint = new Paint();
        flashPaint.setStyle(Paint.Style.FILL);
        flashPaint.setAntiAlias(false);

        ghostPaint = new Paint();
        ghostPaint.setStyle(Paint.Style.STROKE);
        ghostPaint.setStrokeWidth(2);
        ghostPaint.setAntiAlias(false);
        ghostPaint.setColor(Color.parseColor("#306230"));  // GB dark
        ghostPaint.setAlpha(150);
    }

    public void startLineClearAnimation(int[] lines) {
        this.clearingLines = lines;
        this.isFlashing = true;
        this.flashAlpha = 0;
        animateFlash();
    }

    private void animateFlash() {
        if (!isFlashing) return;

        flashAlpha += 40;
        if (flashAlpha > 255) {
            flashAlpha = 255;
            isFlashing = false;
            clearingLines = null;
        }

        invalidate();

        if (isFlashing) {
            postDelayed(this::animateFlash, 40);
        }
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
            float boardWidth = w * 0.85f;
            float boardHeight = h * 0.87f;

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

        // Draw Game Boy LCD background
        canvas.drawColor(Color.parseColor("#9BBC0F"));  // GB lightest

        TetrisBoard board = game.getBoard();
        int[][] boardState = board.getBoard();
        int[][] colors = board.getColors();

        // Draw board border
        float borderLeft = offsetX - 4;
        float borderTop = offsetY - 4;
        float borderRight = offsetX + blockSize * board.getCols() + 4;
        float borderBottom = offsetY + blockSize * board.getRows() + 4;
        canvas.drawRect(borderLeft, borderTop, borderRight, borderBottom, borderPaint);

        // Draw the board
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                float x = offsetX + j * blockSize;
                float y = offsetY + i * blockSize;

                // Draw grid
                canvas.drawRect(x, y, x + blockSize, y + blockSize, gridPaint);

                // Draw placed blocks - simple flat style
                if (boardState[i][j] != 0) {
                    drawPixelBlock(canvas, x, y, blockSize);
                }
            }
        }

        // Draw ghost piece (shadow showing where piece will land)
        if (!game.isGameOver()) {
            TetrisPiece ghostPiece = game.getGhostPiece();
            if (ghostPiece != null) {
                int[][] ghostShape = ghostPiece.getShape();
                ghostPaint.setColor(game.getCurrentPiece().getColor());

                for (int i = 0; i < ghostShape.length; i++) {
                    for (int j = 0; j < ghostShape[i].length; j++) {
                        if (ghostShape[i][j] != 0) {
                            int boardX = ghostPiece.getX() + j;
                            int boardY = ghostPiece.getY() + i;

                            if (boardY >= 0) {
                                float x = offsetX + boardX * blockSize;
                                float y = offsetY + boardY * blockSize;
                                float inset = 4;
                                canvas.drawRect(x + inset, y + inset,
                                              x + blockSize - inset, y + blockSize - inset,
                                              ghostPaint);
                            }
                        }
                    }
                }
            }
        }

        // Draw current piece - simple flat style
        if (!game.isGameOver()) {
            TetrisPiece currentPiece = game.getCurrentPiece();
            int[][] shape = currentPiece.getShape();

            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        int boardX = currentPiece.getX() + j;
                        int boardY = currentPiece.getY() + i;

                        if (boardY >= 0) {
                            float x = offsetX + boardX * blockSize;
                            float y = offsetY + boardY * blockSize;
                            drawPixelBlock(canvas, x, y, blockSize);
                        }
                    }
                }
            }
        }

        // Draw next piece preview
        drawNextPiece(canvas);

        // Draw line clear flash animation - Game Boy style
        if (isFlashing && clearingLines != null) {
            int alpha = (int) (flashAlpha * 0.6f);  // Reduce intensity
            flashPaint.setColor(Color.argb(alpha, 155, 188, 15));  // GB lightest with transparency
            for (int lineIndex : clearingLines) {
                float y = offsetY + lineIndex * blockSize;
                canvas.drawRect(offsetX, y, offsetX + blockSize * board.getCols(), y + blockSize, flashPaint);
            }
        }

        // Draw game over text - Game Boy style
        if (game.isGameOver()) {
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.parseColor("#0F380F"));  // GB darkest
            canvas.drawText("GAME OVER", getWidth() / 2f, getHeight() / 2f, textPaint);
        }
    }

    private void drawPixelBlock(Canvas canvas, float x, float y, float size) {
        // Game Boy style - simple filled rectangle with border
        paint.setColor(Color.parseColor("#0F380F"));  // GB darkest for blocks
        canvas.drawRect(x + 1, y + 1, x + size - 1, y + size - 1, paint);

        // Optional: Add a subtle border for definition
        paint.setColor(Color.parseColor("#306230"));  // GB dark for border
        canvas.drawRect(x + 2, y + 2, x + size - 2, y + 2 + 1, paint);  // Top highlight
        canvas.drawRect(x + 2, y + 2, x + 2 + 1, y + size - 2, paint);  // Left highlight
    }

    private void drawNextPiece(Canvas canvas) {
        TetrisPiece nextPiece = game.getNextPiece();
        if (nextPiece == null) return;

        int[][] shape = nextPiece.getShape();
        float previewBlockSize = blockSize * 0.55f;
        float previewX = offsetX + (game.getBoard().getCols() * blockSize) + 15;
        float previewY = offsetY + 10;

        // Draw "NEXT" label - Game Boy style
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.parseColor("#0F380F"));  // GB darkest
        canvas.drawText("NEXT", previewX, previewY + 20, textPaint);

        // Draw next piece - simple pixel style
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    float x = previewX + j * previewBlockSize;
                    float y = previewY + 30 + i * previewBlockSize;
                    drawPixelBlock(canvas, x, y, previewBlockSize);
                }
            }
        }
    }

    public void refresh() {
        invalidate();
    }
}
