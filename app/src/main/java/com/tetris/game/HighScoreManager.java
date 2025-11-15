package com.tetris.game;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final String PREF_NAME = "TetrisHighScores";
    private static final String KEY_SCORES = "high_scores";
    private static final int MAX_SCORES = 10;
    private SharedPreferences prefs;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public int score;
        public int level;
        public long timestamp;

        public ScoreEntry(int score, int level, long timestamp) {
            this.score = score;
            this.level = level;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            // Sort by score descending
            return Integer.compare(other.score, this.score);
        }
    }

    public HighScoreManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addScore(int score, int level) {
        List<ScoreEntry> scores = getHighScores();
        scores.add(new ScoreEntry(score, level, System.currentTimeMillis()));
        Collections.sort(scores);

        // Keep only top MAX_SCORES
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

        saveScores(scores);
    }

    public List<ScoreEntry> getHighScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        String scoresStr = prefs.getString(KEY_SCORES, "");

        if (!scoresStr.isEmpty()) {
            String[] entries = scoresStr.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(",");
                if (parts.length == 3) {
                    try {
                        int score = Integer.parseInt(parts[0]);
                        int level = Integer.parseInt(parts[1]);
                        long timestamp = Long.parseLong(parts[2]);
                        scores.add(new ScoreEntry(score, level, timestamp));
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }

        return scores;
    }

    private void saveScores(List<ScoreEntry> scores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            sb.append(entry.score).append(",")
              .append(entry.level).append(",")
              .append(entry.timestamp);
            if (i < scores.size() - 1) {
                sb.append(";");
            }
        }
        prefs.edit().putString(KEY_SCORES, sb.toString()).apply();
    }

    public boolean isHighScore(int score) {
        List<ScoreEntry> scores = getHighScores();
        return scores.size() < MAX_SCORES || score > scores.get(scores.size() - 1).score;
    }

    public int getRank(int score) {
        List<ScoreEntry> scores = getHighScores();
        int rank = 1;
        for (ScoreEntry entry : scores) {
            if (score > entry.score) {
                return rank;
            }
            rank++;
        }
        return rank;
    }
}
