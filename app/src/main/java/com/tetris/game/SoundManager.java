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
        if (isPlayingMusic || isMuted) return;

        isPlayingMusic = true;
        musicThread = new Thread(() -> {
            // Classic Tetris Theme A (Korobeiniki) - simplified melody
            // Notes: E5, B4, C5, D5, C5, B4, A4, A4, C5, E5, D5, C5, B4, C5, D5, E5, C5, A4, A4
            double[] melody = {
                659, 494, 523, 587, 523, 494, 440, 440, 523, 659, 587, 523,
                494, 523, 587, 659, 523, 440, 440, 0,  // Rest
                587, 698, 880, 784, 698, 659, 523, 523, 659, 587, 523,
                494, 494, 523, 587, 659, 523, 440, 440, 0   // Rest
            };

            int[] durations = new int[melody.length];
            for (int i = 0; i < melody.length; i++) {
                durations[i] = melody[i] == 0 ? 200 : 250;  // Notes are 250ms, rests are 200ms
            }

            try {
                while (isPlayingMusic && !Thread.currentThread().isInterrupted()) {
                    for (int i = 0; i < melody.length && isPlayingMusic; i++) {
                        if (melody[i] > 0) {
                            playMusicTone(melody[i], durations[i]);
                        } else {
                            Thread.sleep(durations[i]);
                        }
                        Thread.sleep(10); // Small gap between notes
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        musicThread.start();
    }

    public void stopBackgroundMusic() {
        isPlayingMusic = false;
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

    private void playMusicTone(double frequency, int durationMs) {
        if (!isPlayingMusic || isMuted) return;

        int numSamples = durationMs * SAMPLE_RATE / 1000;
        byte[] sound = new byte[2 * numSamples];

        // Generate sine wave for smoother music
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            double sample = Math.sin(angle);

            // Apply gentle envelope for music
            double envelope = 1.0;
            if (i < numSamples * 0.1) {
                envelope = (double) i / (numSamples * 0.1);  // Fade in
            } else if (i > numSamples * 0.9) {
                envelope = 1.0 - ((double) (i - numSamples * 0.9) / (numSamples * 0.1));  // Fade out
            }
            sample = sample * envelope;

            // Convert to 16-bit PCM at lower volume for background music
            short val = (short) (sample * 32767 * 0.15);  // 15% volume for background
            sound[i * 2] = (byte) (val & 0x00ff);
            sound[i * 2 + 1] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack track = new AudioTrack.Builder()
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

        track.write(sound, 0, sound.length);
        track.play();

        try {
            Thread.sleep(durationMs);
            track.stop();
            track.release();
        } catch (InterruptedException e) {
            track.stop();
            track.release();
            Thread.currentThread().interrupt();
        }
    }

    public void release() {
        stopBackgroundMusic();
    }
}
