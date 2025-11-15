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

        for (int i = 0; i < scores.size(); i++) {
            HighScoreManager.ScoreEntry entry = scores.get(i);

            // Create a container for each score entry
            LinearLayout entryLayout = new LinearLayout(this);
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);
            entryLayout.setPadding(16, 12, 16, 12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            entryLayout.setLayoutParams(params);
            entryLayout.setBackgroundColor(getResources().getColor(
                i % 2 == 0 ? android.R.color.transparent : android.R.color.black
            ));

            // Rank
            TextView rankView = new TextView(this);
            rankView.setText(String.format(Locale.getDefault(), "#%d", i + 1));
            rankView.setTextColor(getResources().getColor(
                i < 3 ? android.R.color.holo_orange_light : android.R.color.white
            ));
            rankView.setTextSize(20);
            rankView.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
            entryLayout.addView(rankView);

            // Score and details
            LinearLayout detailsLayout = new LinearLayout(this);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            ));
            detailsLayout.setPadding(16, 0, 0, 0); // Add left padding for spacing

            TextView scoreView = new TextView(this);
            scoreView.setText(String.format(Locale.getDefault(), "Score: %,d", entry.score));
            scoreView.setTextColor(getResources().getColor(android.R.color.white));
            scoreView.setTextSize(18);
            detailsLayout.addView(scoreView);

            TextView levelView = new TextView(this);
            levelView.setText(String.format(Locale.getDefault(), "Level: %d  •  %s",
                entry.level, dateFormat.format(new Date(entry.timestamp))));
            levelView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            levelView.setTextSize(14);
            LinearLayout.LayoutParams levelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            levelParams.setMargins(0, 4, 0, 0); // Add small top margin
            levelView.setLayoutParams(levelParams);
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
