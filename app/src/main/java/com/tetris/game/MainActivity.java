package com.tetris.game;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private HighScoreManager scoreManager;
    private SoundManager soundManager;

    private int selectedSpeed = 5; // Default speed
    private int selectedStartingLines = 0; // Default starting lines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpeedButtons();
        setupStartingLinesButtons();
        setupStartGameButton();
        setupGameControls();
    }

    private void initializeViews() {
        speedSelectionLayout = findViewById(R.id.speedSelectionLayout);
        gameLayout = findViewById(R.id.gameLayout);
        tetrisView = findViewById(R.id.tetrisView);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);
        scoreManager = new HighScoreManager(this);
        soundManager = new SoundManager(this);
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
            btn.setOnClickListener(v -> {
                selectedSpeed = speed;
                Toast.makeText(this, "Speed: " + speed, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupStartingLinesButtons() {
        int[] linesButtonIds = {
            R.id.btnLines0, R.id.btnLines1, R.id.btnLines2,
            R.id.btnLines3, R.id.btnLines4, R.id.btnLines5,
            R.id.btnLines6, R.id.btnLines7, R.id.btnLines8,
            R.id.btnLines9
        };

        for (int i = 0; i < linesButtonIds.length; i++) {
            final int lines = i;
            Button btn = findViewById(linesButtonIds[i]);
            btn.setOnClickListener(v -> {
                selectedStartingLines = lines;
                Toast.makeText(this, "Starting Lines: " + lines, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupStartGameButton() {
        Button btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(v -> startGame());
    }

    private void setupGameControls() {
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);
        ImageButton btnRotate = findViewById(R.id.btnRotate);
        ImageButton btnDrop = findViewById(R.id.btnDrop);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseItem = menu.findItem(R.id.menu_pause);
        if (game != null && game.isPaused()) {
            pauseItem.setTitle(R.string.resume);
        } else {
            pauseItem.setTitle(R.string.pause);
        }

        MenuItem muteItem = menu.findItem(R.id.menu_mute);
        if (soundManager != null && soundManager.isMuted()) {
            muteItem.setTitle(R.string.unmute);
        } else {
            muteItem.setTitle(R.string.mute);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_new_game) {
            showSpeedSelection();
            return true;
        } else if (id == R.id.menu_pause) {
            togglePause();
            return true;
        } else if (id == R.id.menu_high_scores) {
            Intent intent = new Intent(this, HighScoresActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_mute) {
            if (soundManager != null) {
                soundManager.toggleMute();
                invalidateOptionsMenu();
                String message = soundManager.isMuted() ? "Sound Muted" : "Sound Enabled";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startGame() {
        speedSelectionLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        startNewGame();
    }

    private void startNewGame() {
        if (isGameRunning) {
            stopGame();
        }

        game = new TetrisGame(selectedSpeed, soundManager, selectedStartingLines);
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
            invalidateOptionsMenu(); // Update menu to change Pause/Resume text
            if (game.isPaused()) {
                Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
            } else {
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

            int finalScore = game.getScore();
            int finalLevel = game.getLevel();

            // Save score to high scores
            scoreManager.addScore(finalScore, finalLevel);
            int rank = scoreManager.getRank(finalScore);

            String message = getString(R.string.game_over_message, finalScore, rank);
            if (scoreManager.isHighScore(finalScore)) {
                message = getString(R.string.new_high_score) + "\n" + message;
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBoardChanged() {
        runOnUiThread(() -> tetrisView.refresh());
    }

    @Override
    public void onLinesClearing(int[] lines) {
        runOnUiThread(() -> tetrisView.startLineClearAnimation(lines));
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
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGame();
        if (soundManager != null) {
            soundManager.release();
        }
    }
}
