package com.tetris.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    private GestureDetector gestureDetector;
    private float boardWidth;
    private float boardHeight;

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
        paint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.parseColor("#3C3836")); // Dark gray grid lines for terminal theme
        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(true);

        highlightPaint = new Paint();
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAntiAlias(true);

        shadowPaint = new Paint();
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#30000000"));
        shadowPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.parseColor("#FE8019")); // Amber border for terminal theme
        borderPaint.setStrokeWidth(8);
        borderPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(5, 2, 2, Color.BLACK);

        flashPaint = new Paint();
        flashPaint.setStyle(Paint.Style.FILL);
        flashPaint.setAntiAlias(true);

        ghostPaint = new Paint();
        ghostPaint.setStyle(Paint.Style.STROKE);
        ghostPaint.setStrokeWidth(3);
        ghostPaint.setAntiAlias(true);
        ghostPaint.setAlpha(100); // 半透明虚线效果

        // Initialize gesture detector for touch controls
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Tap on game board to rotate - using onSingleTapUp for faster response
                if (game != null && !game.isGameOver() && !game.isPaused()) {
                    float x = e.getX();
                    float y = e.getY();

                    // Check if tap is within game board
                    if (x >= offsetX && x <= offsetX + boardWidth &&
                        y >= offsetY && y <= offsetY + boardHeight) {
                        game.rotate();
                        postInvalidate();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (game == null || game.isGameOver() || game.isPaused()) {
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Reduced threshold from 50 to 30 for faster response
                // Swipe left - smart distance-based movement
                if (Math.abs(diffX) > Math.abs(diffY) && diffX < -30) {
                    // Calculate number of moves based on swipe distance
                    int moves = Math.max(1, Math.min(5, (int)(Math.abs(diffX) / (blockSize * 1.5f))));
                    for (int i = 0; i < moves; i++) {
                        game.moveLeft();
                    }
                    postInvalidate();
                    return true;
                }
                // Swipe right - smart distance-based movement
                else if (Math.abs(diffX) > Math.abs(diffY) && diffX > 30) {
                    // Calculate number of moves based on swipe distance
                    int moves = Math.max(1, Math.min(5, (int)(Math.abs(diffX) / (blockSize * 1.5f))));
                    for (int i = 0; i < moves; i++) {
                        game.moveRight();
                    }
                    postInvalidate();
                    return true;
                }
                // Swipe down (drop)
                else if (Math.abs(diffY) > Math.abs(diffX) && diffY > 30) {
                    game.drop();
                    postInvalidate();
                    return true;
                }

                return false;
            }
        });
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
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (game != null) {
            TetrisBoard board = game.getBoard();

            // Reserve space for next preview on the right (about 25% of width)
            float previewSpace = w * 0.25f;
            float availableWidth = w - previewSpace;

            // Calculate max board dimensions
            boardWidth = availableWidth * 0.85f;  // Use 85% of available space
            boardHeight = h * 0.9f;

            float blockWidth = boardWidth / board.getCols();
            float blockHeight = boardHeight / board.getRows();
            blockSize = Math.min(blockWidth, blockHeight);

            // Recalculate actual board dimensions based on blockSize
            this.boardWidth = blockSize * board.getCols();
            this.boardHeight = blockSize * board.getRows();

            // Center the board horizontally in the available space
            offsetX = (availableWidth - this.boardWidth) / 2;
            offsetY = (h - this.boardHeight) / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (game == null) return;

        // Draw background - Terminal dark theme
        canvas.drawColor(Color.parseColor("#1D2021"));

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

                // Draw placed blocks with 3D effect
                if (boardState[i][j] != 0) {
                    draw3DBlock(canvas, x, y, blockSize, colors[i][j]);
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

        // Draw current piece with 3D effect
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
                            draw3DBlock(canvas, x, y, blockSize, currentPiece.getColor());
                        }
                    }
                }
            }
        }

        // Draw next piece preview
        drawNextPiece(canvas);

        // Draw line clear flash animation
        if (isFlashing && clearingLines != null) {
            flashPaint.setColor(Color.argb(flashAlpha, 255, 255, 255));
            for (int lineIndex : clearingLines) {
                float y = offsetY + lineIndex * blockSize;
                canvas.drawRect(offsetX, y, offsetX + blockSize * board.getCols(), y + blockSize, flashPaint);
            }
        }

        // Draw game over text
        if (game.isGameOver()) {
            textPaint.setTextSize(80);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.parseColor("#FF5252"));
            canvas.drawText("GAME OVER", getWidth() / 2f, getHeight() / 2f, textPaint);
        }
    }

    private void draw3DBlock(Canvas canvas, float x, float y, float size, int color) {
        float inset = 3;
        float highlightInset = 5;

        // Draw shadow (bottom-right)
        canvas.drawRect(x + size - 4, y + 4, x + size, y + size, shadowPaint);
        canvas.drawRect(x + 4, y + size - 4, x + size, y + size, shadowPaint);

        // Main block with gradient
        int baseColor = color;
        int lightColor = lightenColor(baseColor, 0.3f);
        int darkColor = darkenColor(baseColor, 0.2f);

        LinearGradient gradient = new LinearGradient(
            x, y, x, y + size,
            lightColor, darkColor,
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawRect(x + inset, y + inset, x + size - inset, y + size - inset, paint);
        paint.setShader(null);

        // Highlight (top-left)
        highlightPaint.setColor(lightenColor(baseColor, 0.5f));
        canvas.drawRect(
            x + highlightInset,
            y + highlightInset,
            x + size - highlightInset,
            y + highlightInset + 2,
            highlightPaint
        );
        canvas.drawRect(
            x + highlightInset,
            y + highlightInset,
            x + highlightInset + 2,
            y + size - highlightInset,
            highlightPaint
        );

        // Dark edge (bottom-right)
        highlightPaint.setColor(darkenColor(baseColor, 0.4f));
        canvas.drawRect(
            x + highlightInset,
            y + size - highlightInset - 2,
            x + size - highlightInset,
            y + size - highlightInset,
            highlightPaint
        );
        canvas.drawRect(
            x + size - highlightInset - 2,
            y + highlightInset,
            x + size - highlightInset,
            y + size - highlightInset,
            highlightPaint
        );
    }

    private int lightenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = Math.min(255, (int) (r + (255 - r) * factor));
        g = Math.min(255, (int) (g + (255 - g) * factor));
        b = Math.min(255, (int) (b + (255 - b) * factor));

        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = Math.max(0, (int) (r * (1 - factor)));
        g = Math.max(0, (int) (g * (1 - factor)));
        b = Math.max(0, (int) (b * (1 - factor)));

        return Color.rgb(r, g, b);
    }

    private void drawNextPiece(Canvas canvas) {
        TetrisPiece nextPiece = game.getNextPiece();
        if (nextPiece == null) return;

        int[][] shape = nextPiece.getShape();
        float previewBlockSize = blockSize * 0.7f;
        float previewX = offsetX + (game.getBoard().getCols() * blockSize) + 30;
        float previewY = offsetY + 10;

        // Draw "Next:" label with background
        textPaint.setTextSize(28);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.parseColor("#FE8019")); // Amber color for terminal theme
        canvas.drawText("NEXT", previewX, previewY + 25, textPaint);

        // Draw next piece with 3D effect
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    float x = previewX + j * previewBlockSize;
                    float y = previewY + 40 + i * previewBlockSize;
                    draw3DBlock(canvas, x, y, previewBlockSize, nextPiece.getColor());
                }
            }
        }
    }

    public void refresh() {
        invalidate();
    }
}
