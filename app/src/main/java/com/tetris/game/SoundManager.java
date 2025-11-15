package com.tetris.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

public class SoundManager {
    private static final String PREFS_NAME = "TetrisSettings";
    private static final String KEY_MUTED = "sound_muted";
    private static final int SAMPLE_RATE = 22050;

    private Context context;
    private SharedPreferences prefs;
    private boolean isMuted;
    private Handler handler;
    private Thread musicThread;
    private volatile boolean isPlayingMusic = false;
    private AudioTrack musicTrack;
    private volatile boolean isMusicPaused = false;
    private volatile float musicSpeed = 1.0f; // 1.0 = normal, higher = faster

    public SoundManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.isMuted = prefs.getBoolean(KEY_MUTED, false);
        this.handler = new Handler(Looper.getMainLooper());
    }

    private void playSound(final double[] frequencies, final int[] durations) {
        if (isMuted) return;

        new Thread(() -> {
            try {
                for (int i = 0; i < frequencies.length; i++) {
                    playTone(frequencies[i], durations[i]);
                    if (i < frequencies.length - 1) {
                        Thread.sleep(10);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playTone(double frequency, int durationMs) {
        int numSamples = durationMs * SAMPLE_RATE / 1000;
        byte[] sound = new byte[2 * numSamples];
        double sample;

        // Generate square wave for retro game sound
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            sample = Math.sin(angle) > 0 ? 1 : -1;

            // Apply volume envelope (fade out)
            double envelope = 1.0 - (double) i / numSamples * 0.7;
            sample = sample * envelope;

            // Convert to 16-bit PCM
            short val = (short) (sample * 32767 * 0.3); // 30% volume
            sound[i * 2] = (byte) (val & 0x00ff);
            sound[i * 2 + 1] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(sound.length)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();

        // Release after playback
        handler.postDelayed(() -> {
            audioTrack.stop();
            audioTrack.release();
        }, durationMs + 50);
    }

    public void playMove() {
        // Very short, simple blip - classic Tetris move sound
        playSound(new double[]{1200}, new int[]{30});
    }

    public void playRotate() {
        // Slightly higher pitched blip - classic Tetris rotate
        playSound(new double[]{1400}, new int[]{35});
    }

    public void playDrop() {
        // Deep thud - piece locking sound
        playSound(new double[]{150}, new int[]{80});
    }

    public void playLineClear() {
        // Pleasant bell-like chime - classic Tetris line clear
        playSound(new double[]{1568, 1976, 2349}, new int[]{120, 120, 200});
    }

    public void playGameOver() {
        // Simple descending tones - classic game over
        playSound(new double[]{523, 440, 349, 262}, new int[]{150, 150, 150, 400});
    }

    public void playLevelUp() {
        // Bright ascending fanfare
        playSound(new double[]{523, 659, 784, 1047, 1319}, new int[]{80, 80, 80, 80, 300});
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
        if (isMuted) {
            stopBackgroundMusic();
        }
    }

    public void startBackgroundMusic() {
        if (isPlayingMusic) return;

        isPlayingMusic = true;
        isMusicPaused = false;
        musicThread = new Thread(() -> {
            while (isPlayingMusic) {
                if (!isMuted && !isMusicPaused) {
                    playMusicLoop();
                    // 旋律之间停顿600ms，节奏更流畅
                    try {
                        Thread.sleep(600); // 较短停顿，保持节奏
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // If muted or paused, just wait a bit before checking again
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        musicThread.start();
    }

    public void stopBackgroundMusic() {
        isPlayingMusic = false;
        isMusicPaused = false;
        if (musicThread != null) {
            musicThread.interrupt();
            musicThread = null;
        }
        if (musicTrack != null) {
            try {
                musicTrack.stop();
                musicTrack.release();
            } catch (Exception e) {
                // Ignore
            }
            musicTrack = null;
        }
    }

    public void pauseMusic() {
        isMusicPaused = true;
    }

    public void resumeMusic() {
        isMusicPaused = false;
    }

    public void setMusicSpeed(float speed) {
        // speed: 1.0 = normal (slow), 1.3-1.5 = faster when board fills up
        this.musicSpeed = Math.max(0.5f, Math.min(2.0f, speed));
    }

    private void playMusicLoop() {
        // 简单清爽的俄罗斯方块旋律 - Simple, clear, upbeat Tetris melody
        // 只保留最经典的主旋律片段
        double[] notes = {
            659, 494, 523, 587,     // E, B, C, D
            523, 494, 440,          // C, B, A
            440, 523, 659,          // A, C, E
            587, 523, 494           // D, C, B
        };

        // 更快的节奏，稍微欢快 - faster, more upbeat rhythm
        int noteDuration = 380; // 每个音符380ms，更欢快的节奏

        try {
            for (int i = 0; i < notes.length && isPlayingMusic && !isMusicPaused; i++) {
                playMusicTone(notes[i], noteDuration);
                // 音符之间停顿60ms，流畅清晰
                Thread.sleep(60);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void playMusicTone(double frequency, int durationMs) {
        if (!isPlayingMusic || isMuted || isMusicPaused) return;

        int numSamples = durationMs * SAMPLE_RATE / 1000;
        byte[] sound = new byte[2 * numSamples];
        double sample;

        // Generate smoother sine wave for music (not square wave)
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            sample = Math.sin(angle);

            // Apply gentle envelope for smoother music
            double envelope = 1.0;
            if (i < numSamples * 0.05) {
                // Fade in
                envelope = (double) i / (numSamples * 0.05);
            } else if (i > numSamples * 0.8) {
                // Fade out
                envelope = 1.0 - ((double) i - numSamples * 0.8) / (numSamples * 0.2);
            }
            sample = sample * envelope;

            // Convert to 16-bit PCM (very low volume for chill background music)
            short val = (short) (sample * 32767 * 0.12); // 12% volume for clear but relaxing sound
            sound[i * 2] = (byte) (val & 0x00ff);
            sound[i * 2 + 1] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(sound.length)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();

        // 同步等待音符播放完成 - Wait synchronously for note to finish
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    public void release() {
        stopBackgroundMusic();
    }
}
