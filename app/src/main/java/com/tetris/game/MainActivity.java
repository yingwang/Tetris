package com.tetris.game;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements TetrisGame.GameListener {
    private TetrisView tetrisView;
    private TetrisGame game;
    private Handler gameHandler;
    private Runnable gameRunnable;
    private boolean isGameRunning = false;

    private LinearLayout speedSelectionLayout;
    private LinearLayout gameLayout;
    private TextView tvScore;
    private TextView tvLevel;
    private Button btnPause;

    private int selectedSpeed = 5; // Default speed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpeedButtons();
        setupGameControls();
    }

    private void initializeViews() {
        speedSelectionLayout = findViewById(R.id.speedSelectionLayout);
        gameLayout = findViewById(R.id.gameLayout);
        tetrisView = findViewById(R.id.tetrisView);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);
        btnPause = findViewById(R.id.btnPause);
    }

    private void setupSpeedButtons() {
        int[] speedButtonIds = {
            R.id.btnSpeed1, R.id.btnSpeed2, R.id.btnSpeed3,
            R.id.btnSpeed4, R.id.btnSpeed5, R.id.btnSpeed6,
            R.id.btnSpeed7, R.id.btnSpeed8, R.id.btnSpeed9
        };

        for (int i = 0; i < speedButtonIds.length; i++) {
            final int speed = i + 1;
            Button btn = findViewById(speedButtonIds[i]);
            btn.setOnClickListener(v -> startGameWithSpeed(speed));
        }
    }

    private void setupGameControls() {
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);
        Button btnRotate = findViewById(R.id.btnRotate);
        Button btnDrop = findViewById(R.id.btnDrop);
        Button btnNewGame = findViewById(R.id.btnNewGame);

        btnLeft.setOnClickListener(v -> {
            if (game != null) game.moveLeft();
        });

        btnRight.setOnClickListener(v -> {
            if (game != null) game.moveRight();
        });

        btnRotate.setOnClickListener(v -> {
            if (game != null) game.rotate();
        });

        btnDrop.setOnClickListener(v -> {
            if (game != null) game.drop();
        });

        btnPause.setOnClickListener(v -> togglePause());

        btnNewGame.setOnClickListener(v -> showSpeedSelection());
    }

    private void startGameWithSpeed(int speed) {
        selectedSpeed = speed;
        speedSelectionLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        startNewGame();
    }

    private void startNewGame() {
        if (isGameRunning) {
            stopGame();
        }

        game = new TetrisGame(selectedSpeed);
        game.setGameListener(this);
        tetrisView.setGame(game);

        updateScore(game.getScore());
        updateLevel(game.getLevel());

        startGameLoop();
    }

    private void startGameLoop() {
        isGameRunning = true;
        gameHandler = new Handler();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (game != null && !game.isGameOver() && !game.isPaused()) {
                    game.moveDown();
                }
                if (isGameRunning) {
                    // Speed determines delay: speed 1 = slowest (1000ms), speed 9 = fastest (~200ms)
                    int delay = Math.max(200, 1100 - (selectedSpeed * 100));
                    gameHandler.postDelayed(this, delay);
                }
            }
        };
        gameHandler.post(gameRunnable);
    }

    private void stopGame() {
        isGameRunning = false;
        if (gameHandler != null && gameRunnable != null) {
            gameHandler.removeCallbacks(gameRunnable);
        }
    }

    private void togglePause() {
        if (game != null) {
            game.togglePause();
            if (game.isPaused()) {
                btnPause.setText(R.string.resume);
                Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
            } else {
                btnPause.setText(R.string.pause);
                Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSpeedSelection() {
        stopGame();
        gameLayout.setVisibility(View.GONE);
        speedSelectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScoreChanged(int score) {
        runOnUiThread(() -> updateScore(score));
    }

    @Override
    public void onLevelChanged(int level) {
        runOnUiThread(() -> updateLevel(level));
    }

    @Override
    public void onGameOver() {
        runOnUiThread(() -> {
            stopGame();
            tetrisView.refresh();
            Toast.makeText(MainActivity.this,
                getString(R.string.game_over) + " Score: " + game.getScore(),
                Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBoardChanged() {
        runOnUiThread(() -> tetrisView.refresh());
    }

    private void updateScore(int score) {
        tvScore.setText(getString(R.string.score, score));
    }

    private void updateLevel(int level) {
        tvLevel.setText(getString(R.string.level, level));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (game != null && !game.isGameOver()) {
            game.setPaused(true);
            btnPause.setText(R.string.resume);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGame();
    }
}
