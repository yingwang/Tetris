package com.tetris.game;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements TetrisGame.GameListener {
    private static final String PREFS_NAME = "TetrisPrefs";
    private static final String PREF_SPEED = "speed";
    private static final String PREF_LINES = "starting_lines";

    private TetrisView tetrisView;
    private TetrisGame game;
    private Handler gameHandler;
    private Runnable gameRunnable;
    private boolean isGameRunning = false;

    private LinearLayout mainMenuLayout;
    private LinearLayout settingsLayout;
    private LinearLayout gameLayout;
    private TextView tvScore;
    private TextView tvLevel;
    private ImageButton btnPauseGame;
    private ImageButton btnSettings;
    private HighScoreManager scoreManager;
    private SoundManager soundManager;
    private SharedPreferences preferences;

    // For down button long press
    private Handler downButtonHandler = new Handler();
    private Runnable downButtonRunnable;
    private boolean isDownButtonPressed = false;

    private int selectedSpeed = 5; // Default speed
    private int selectedStartingLines = 0; // Default starting lines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        loadSettings();
        initializeViews();
        setupSeekBars();
        setupMenuButtons();
        setupGameControls();
        setupGameControlButtons();

        // Start directly in game
        startGame();
    }

    private void loadSettings() {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedSpeed = preferences.getInt(PREF_SPEED, 5);
        selectedStartingLines = preferences.getInt(PREF_LINES, 0);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_SPEED, selectedSpeed);
        editor.putInt(PREF_LINES, selectedStartingLines);
        editor.apply();
    }

    private void initializeViews() {
        mainMenuLayout = findViewById(R.id.mainMenuLayout);
        settingsLayout = findViewById(R.id.settingsLayout);
        gameLayout = findViewById(R.id.gameLayout);
        tetrisView = findViewById(R.id.tetrisView);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);
        btnPauseGame = findViewById(R.id.btnPauseGame);
        btnSettings = findViewById(R.id.btnSettings);
        scoreManager = new HighScoreManager(this);
        soundManager = new SoundManager(this);
    }

    private void setupSeekBars() {
        SeekBar seekBarSpeed = findViewById(R.id.seekBarSpeed);
        SeekBar seekBarLines = findViewById(R.id.seekBarLines);
        TextView tvSpeedValue = findViewById(R.id.tvSpeedValue);
        TextView tvLinesValue = findViewById(R.id.tvLinesValue);

        // Set initial values from saved settings
        seekBarSpeed.setProgress(selectedSpeed - 1); // 1-9 becomes 0-8
        seekBarLines.setProgress(selectedStartingLines);
        tvSpeedValue.setText(String.valueOf(selectedSpeed));
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

    private void setupMenuButtons() {
        // Main menu buttons
        Button btnStartGame = findViewById(R.id.btnStartGame);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnHighScoresMenu = findViewById(R.id.btnHighScoresMenu);

        btnStartGame.setOnClickListener(v -> startGame());
        btnSettings.setOnClickListener(v -> showSettings());
        btnHighScoresMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighScoresActivity.class);
            startActivity(intent);
        });

        // Settings screen button
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(v -> showMainMenu());
    }

    private void setupGameControls() {
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);
        ImageButton btnDown = findViewById(R.id.btnDown);
        ImageButton btnRotate = findViewById(R.id.btnRotate);
        ImageButton btnDrop = findViewById(R.id.btnDrop);

        btnLeft.setOnClickListener(v -> {
            if (game != null) game.moveLeft();
        });

        btnRight.setOnClickListener(v -> {
            if (game != null) game.moveRight();
        });

        // Down button with long press support for continuous speed up
        btnDown.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Initial press
                    if (game != null) game.moveDown();
                    isDownButtonPressed = true;

                    // Start repeating after short delay
                    downButtonRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (isDownButtonPressed && game != null) {
                                game.moveDown();
                                downButtonHandler.postDelayed(this, 100); // Repeat every 100ms
                            }
                        }
                    };
                    downButtonHandler.postDelayed(downButtonRunnable, 150); // Start repeating after 150ms
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Stop repeating
                    isDownButtonPressed = false;
                    downButtonHandler.removeCallbacks(downButtonRunnable);
                    v.performClick(); // Accessibility
                    return true;
            }
            return false;
        });

        btnRotate.setOnClickListener(v -> {
            if (game != null) game.rotate();
        });

        btnDrop.setOnClickListener(v -> {
            if (game != null) game.drop();
        });
    }

    private void setupGameControlButtons() {
        btnPauseGame.setOnClickListener(v -> togglePauseFromButton());

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        // Initialize button states
        updatePauseButton();
    }

    private void updatePauseButton() {
        if (game != null && game.isPaused()) {
            btnPauseGame.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnPauseGame.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void showSettingsDialog() {
        // Pause game if playing
        boolean wasPaused = game != null && game.isPaused();
        if (game != null && !game.isGameOver()) {
            game.setPaused(true);
            if (soundManager != null) {
                soundManager.pauseMusic();
            }
        }

        Dialog settingsDialog = new Dialog(this);
        settingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(R.layout.dialog_settings);
        settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        settingsDialog.setCancelable(false);

        // Get views
        SeekBar speedSeekBar = settingsDialog.findViewById(R.id.settingsSeekBarSpeed);
        TextView speedValue = settingsDialog.findViewById(R.id.settingsTvSpeedValue);
        SeekBar linesSeekBar = settingsDialog.findViewById(R.id.settingsSeekBarLines);
        TextView linesValue = settingsDialog.findViewById(R.id.settingsTvLinesValue);
        ImageButton muteBtn = settingsDialog.findViewById(R.id.settingsBtnMute);
        Button closeBtn = settingsDialog.findViewById(R.id.settingsBtnClose);

        // Set current values
        speedSeekBar.setProgress(selectedSpeed - 1);
        speedValue.setText(String.valueOf(selectedSpeed));
        linesSeekBar.setProgress(selectedStartingLines);
        linesValue.setText(String.valueOf(selectedStartingLines));
        updateMuteButtonIcon(muteBtn);

        // Speed seekbar listener
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedSpeed = progress + 1;
                speedValue.setText(String.valueOf(selectedSpeed));
                if (fromUser) {
                    saveSettings();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Lines seekbar listener
        linesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedStartingLines = progress;
                linesValue.setText(String.valueOf(selectedStartingLines));
                if (fromUser) {
                    saveSettings();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Mute button listener
        muteBtn.setOnClickListener(v -> {
            if (soundManager != null) {
                soundManager.toggleMute();
                updateMuteButtonIcon(muteBtn);
            }
        });

        // Close button listener
        closeBtn.setOnClickListener(v -> {
            settingsDialog.dismiss();
            // Resume game if it wasn't paused before
            if (game != null && !game.isGameOver() && !wasPaused) {
                game.setPaused(false);
                if (soundManager != null) {
                    soundManager.resumeMusic();
                }
                updatePauseButton();
            }
        });

        settingsDialog.show();
    }

    private void updateMuteButtonIcon(ImageButton btn) {
        if (soundManager != null && soundManager.isMuted()) {
            btn.setImageResource(android.R.drawable.ic_lock_silent_mode);
        } else {
            btn.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
        }
    }

    private void showPauseDialog() {
        Dialog pauseDialog = new Dialog(this);
        pauseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pauseDialog.setContentView(R.layout.dialog_pause);
        pauseDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pauseDialog.setCancelable(false);

        Button btnContinue = pauseDialog.findViewById(R.id.pauseBtnContinue);
        Button btnNewGame = pauseDialog.findViewById(R.id.pauseBtnNewGame);
        Button btnQuit = pauseDialog.findViewById(R.id.pauseBtnQuit);

        btnContinue.setOnClickListener(v -> {
            pauseDialog.dismiss();
            game.setPaused(false);
            updatePauseButton();
            if (soundManager != null) {
                soundManager.resumeMusic();
            }
        });

        btnNewGame.setOnClickListener(v -> {
            pauseDialog.dismiss();
            startNewGame();
        });

        btnQuit.setOnClickListener(v -> {
            pauseDialog.dismiss();
            stopGame();
            finish(); // Exit the app
        });

        pauseDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No menu needed anymore
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // No menu needed
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // No menu needed
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        mainMenuLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.GONE);
    }

    private void showMainMenu() {
        // Stop game if running
        if (isGameRunning) {
            stopGame();
            if (soundManager != null) {
                soundManager.stopBackgroundMusic();
            }
        }
        mainMenuLayout.setVisibility(View.VISIBLE);
        settingsLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.GONE);
    }

    private void startGame() {
        mainMenuLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);
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

    private void togglePauseFromButton() {
        // Only allow pause if game is running and game layout is visible
        if (game != null && isGameRunning && gameLayout.getVisibility() == View.VISIBLE) {
            game.togglePause();
            updatePauseButton();
            if (game.isPaused()) {
                if (soundManager != null) {
                    soundManager.pauseMusic();
                }
                // Show pause dialog with Continue, New Game, and Quit options
                showPauseDialog();
            } else {
                if (soundManager != null) {
                    soundManager.resumeMusic();
                }
            }
        }
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
            String title = "GAME OVER";
            if (scoreManager.isHighScore(finalScore)) {
                title = "★ NEW HIGH SCORE ★";
            }

            // Show game over dialog with New Game and High Scores options
            new RetroDialog(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setButton("New Game", v -> {
                        // Start a new game immediately
                        startNewGame();
                    })
                    .setSecondButton("High Scores", v -> {
                        // Show high scores, user can return with back button
                        Intent intent = new Intent(MainActivity.this, HighScoresActivity.class);
                        startActivity(intent);
                    })
                    .show();
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

        // Clean up down button handler
        isDownButtonPressed = false;
        if (downButtonHandler != null && downButtonRunnable != null) {
            downButtonHandler.removeCallbacks(downButtonRunnable);
        }

        if (soundManager != null) {
            soundManager.release();
        }
    }
}
