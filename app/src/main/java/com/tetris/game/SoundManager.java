package com.tetris.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Handler;

public class SoundManager {
    private static final String PREFS_NAME = "TetrisSettings";
    private static final String KEY_MUTED = "sound_muted";

    private Context context;
    private SharedPreferences prefs;
    private ToneGenerator toneGenerator;
    private boolean isMuted;
    private Handler handler;

    public SoundManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.isMuted = prefs.getBoolean(KEY_MUTED, false);
        this.handler = new Handler();

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }
    }

    public void playMove() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50));
        }
    }

    public void playRotate() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 80));
        }
    }

    public void playDrop() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> {
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100);
            });
        }
    }

    public void playLineClear() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            });
        }
    }

    public void playGameOver() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500);
            });
        }
    }

    public void playLevelUp() {
        if (!isMuted && toneGenerator != null) {
            handler.post(() -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300);
            });
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        prefs.edit().putBoolean(KEY_MUTED, muted).apply();
    }

    public void toggleMute() {
        setMuted(!isMuted);
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
