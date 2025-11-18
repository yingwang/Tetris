package com.tetris.game;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
    private Button btnMute;
    private HighScoreManager scoreManager;
    private SoundManager soundManager;

    private int selectedSpeed = 1; // Default speed
    private int selectedStartingLines = 0; // Default starting lines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        initializeViews();
        loadSettings();
        setupSeekBars();
        setupStartGameButton();
        setupGameControls();
        setupMenuButtons();
    }

    private void initializeViews() {
        speedSelectionLayout = findViewById(R.id.speedSelectionLayout);
        gameLayout = findViewById(R.id.gameLayout);
        tetrisView = findViewById(R.id.tetrisView);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);
        btnPause = findViewById(R.id.btnPause);
        btnMute = findViewById(R.id.btnMute);
        scoreManager = new HighScoreManager(this);
        soundManager = new SoundManager(this);
    }

    private void loadSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("TetrisSettings", MODE_PRIVATE);
        selectedSpeed = prefs.getInt("speed", 1);
        selectedStartingLines = prefs.getInt("startingLines", 0);
    }

    private void saveSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("TetrisSettings", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("speed", selectedSpeed);
        editor.putInt("startingLines", selectedStartingLines);
        editor.apply();
    }

    private void setupSeekBars() {
        SeekBar seekBarSpeed = findViewById(R.id.seekBarSpeed);
        SeekBar seekBarLines = findViewById(R.id.seekBarLines);
        TextView tvSpeedValue = findViewById(R.id.tvSpeedValue);
        TextView tvLinesValue = findViewById(R.id.tvLinesValue);

        // Set initial values from loaded settings
        seekBarSpeed.setProgress(selectedSpeed - 1); // speed 1-9 becomes progress 0-8
        tvSpeedValue.setText(String.valueOf(selectedSpeed));
        seekBarLines.setProgress(selectedStartingLines);
        tvLinesValue.setText(String.valueOf(selectedStartingLines));

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedSpeed = progress + 1; // 0-8 becomes 1-9
                tvSpeedValue.setText(String.valueOf(selectedSpeed));
                if (fromUser) {
                    saveSettings();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarLines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedStartingLines = progress; // 0-9
                tvLinesValue.setText(String.valueOf(selectedStartingLines));
                if (fromUser) {
                    saveSettings();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
        ImageButton btnDown = findViewById(R.id.btnDown);

        btnLeft.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (game != null) game.moveLeft();
        });

        btnRight.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (game != null) game.moveRight();
        });

        btnRotate.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (game != null) game.rotate();
        });

        // Hard drop - instant drop to bottom
        btnDrop.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (game != null) game.drop();
        });

        // Soft drop / speed up - moves piece down faster
        btnDown.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (game != null) game.moveDown();
        });
    }

    private void setupMenuButtons() {
        Button btnNewGame = findViewById(R.id.btnNewGame);
        Button btnHighScores = findViewById(R.id.btnHighScores);

        // New button shows settings dialog to start new game
        btnNewGame.setOnClickListener(v -> showSpeedSelection());

        btnPause.setOnClickListener(v -> togglePause());

        btnMute.setOnClickListener(v -> {
            if (soundManager != null) {
                soundManager.toggleMute();
                updateMuteButton();
                String message = soundManager.isMuted() ? "Sound Muted" : "Sound Enabled";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnHighScores.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighScoresActivity.class);
            startActivity(intent);
        });

        // Initialize button states
        updatePauseButton();
        updateMuteButton();
    }

    private void updatePauseButton() {
        if (btnPause != null && game != null) {
            btnPause.setText(game.isPaused() ? "Resume" : "Pause");
        }
    }

    private void updateMuteButton() {
        if (btnMute != null && soundManager != null) {
            btnMute.setText(soundManager.isMuted() ? "Unmute" : "Mute");
        }
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
        updatePauseButton();

        startGameLoop();

        // Start background music
        if (soundManager != null) {
            soundManager.startBackgroundMusic();
        }
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
        // Stop background music
        if (soundManager != null) {
            soundManager.stopBackgroundMusic();
        }
    }

    private void togglePause() {
        if (game != null) {
            game.togglePause();
            updatePauseButton(); // Update button text
            if (game.isPaused()) {
                Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
                if (soundManager != null) {
                    soundManager.pauseMusic();
                }
            } else {
                Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
                if (soundManager != null) {
                    soundManager.resumeMusic();
                }
            }
        }
    }

    private void showSpeedSelection() {
        stopGame();
        if (soundManager != null) {
            soundManager.stopBackgroundMusic();
        }
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
            updatePauseButton();
        }
        // Pause music when app goes to background
        if (soundManager != null) {
            soundManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music when app comes back, but only if game is not paused
        if (soundManager != null && game != null && !game.isPaused() && !game.isGameOver()) {
            soundManager.resumeMusic();
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
