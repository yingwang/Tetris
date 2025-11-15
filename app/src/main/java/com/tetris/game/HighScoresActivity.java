package com.tetris.game;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HighScoresActivity extends AppCompatActivity {
    private LinearLayout scoresContainer;
    private HighScoreManager scoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.high_scores);
        }

        scoresContainer = findViewById(R.id.scoresContainer);
        scoreManager = new HighScoreManager(this);

        displayHighScores();
    }

    private void displayHighScores() {
        List<HighScoreManager.ScoreEntry> scores = scoreManager.getHighScores();

        if (scores.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(R.string.no_scores);
            emptyView.setTextColor(getResources().getColor(android.R.color.white));
            emptyView.setTextSize(18);
            emptyView.setPadding(16, 16, 16, 16);
            scoresContainer.addView(emptyView);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        for (int i = 0; i < scores.size() && i < 10; i++) {
            HighScoreManager.ScoreEntry entry = scores.get(i);
            boolean isTopThree = i < 3;

            // Create a container for each score entry
            LinearLayout entryLayout = new LinearLayout(this);
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            entryLayout.setLayoutParams(params);
            entryLayout.setBackgroundResource(
                isTopThree ? R.drawable.high_score_entry_top : R.drawable.high_score_entry
            );

            // Rank
            TextView rankView = new TextView(this);
            rankView.setText(String.format(Locale.getDefault(), "#%d", i + 1));
            rankView.setTextColor(isTopThree ? 0xFFFFD700 : 0xFF4CAF50);
            rankView.setTextSize(28);
            rankView.setTypeface(null, android.graphics.Typeface.BOLD);
            rankView.setShadowLayer(3, 2, 2, 0xFF000000);
            LinearLayout.LayoutParams rankParams = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
            rankParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            rankView.setLayoutParams(rankParams);
            rankView.setGravity(android.view.Gravity.CENTER);
            entryLayout.addView(rankView);

            // Score and details
            LinearLayout detailsLayout = new LinearLayout(this);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            ));

            TextView scoreView = new TextView(this);
            scoreView.setText(String.format(Locale.getDefault(), "%,d", entry.score));
            scoreView.setTextColor(isTopThree ? 0xFFFFD700 : 0xFFFFFFFF);
            scoreView.setTextSize(22);
            scoreView.setTypeface(null, android.graphics.Typeface.BOLD);
            scoreView.setShadowLayer(2, 1, 1, 0xFF000000);
            detailsLayout.addView(scoreView);

            TextView levelView = new TextView(this);
            levelView.setText(String.format(Locale.getDefault(), "Level %d  •  %s",
                entry.level, dateFormat.format(new Date(entry.timestamp))));
            levelView.setTextColor(0xFF999999);
            levelView.setTextSize(14);
            levelView.setPadding(0, 4, 0, 0);
            detailsLayout.addView(levelView);

            entryLayout.addView(detailsLayout);
            scoresContainer.addView(entryLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
