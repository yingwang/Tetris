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
        playTone(frequency, durationMs, 0.5); // Default 50% duty cycle
    }

    private void playTone(double frequency, int durationMs, double dutyCycle) {
        int numSamples = durationMs * SAMPLE_RATE / 1000;
        byte[] sound = new byte[2 * numSamples];
        double sample;

        // Generate Game Boy style square wave with adjustable duty cycle
        for (int i = 0; i < numSamples; i++) {
            double phase = (i * frequency / SAMPLE_RATE) % 1.0;
            sample = phase < dutyCycle ? 1.0 : -1.0;

            // Game Boy style volume envelope - sharp attack, quick decay
            double envelope = 1.0;
            if (i < numSamples * 0.02) {
                // Very fast attack (2%)
                envelope = (double) i / (numSamples * 0.02);
            } else if (i > numSamples * 0.3) {
                // Quick exponential decay starting at 30%
                double decayPos = ((double) i - numSamples * 0.3) / (numSamples * 0.7);
                envelope = Math.pow(1.0 - decayPos, 2.0); // Exponential decay
            }
            sample = sample * envelope;

            // Convert to 16-bit PCM - Game Boy had limited bit depth
            short val = (short) (sample * 32767 * 0.15); // Slightly louder for clarity
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
        // Game Boy style - ultra short blip with 25% duty cycle (thinner sound)
        if (isMuted) return;
        new Thread(() -> playTone(1047, 25, 0.25)).start(); // High C, very short
    }

    public void playRotate() {
        // Game Boy style - short chirp with 12.5% duty cycle (even thinner)
        if (isMuted) return;
        new Thread(() -> {
            playTone(1318, 30, 0.125); // E, thin pulse
        }).start();
    }

    public void playDrop() {
        // Game Boy style - quick descending tone (like original GB Tetris)
        if (isMuted) return;
        new Thread(() -> {
            playTone(784, 40, 0.5);  // G
            try { Thread.sleep(5); } catch (InterruptedException e) {}
            playTone(523, 60, 0.5);  // C
        }).start();
    }

    public void playLineClear() {
        // Game Boy Tetris style - characteristic ascending arpeggio
        // Using 50% duty cycle for fuller sound on important events
        playSound(new double[]{659, 784, 988, 1318}, new int[]{60, 60, 60, 200});
    }

    public void playGameOver() {
        // Game Boy style - descending tones with sustain on final note
        playSound(new double[]{659, 523, 440, 349}, new int[]{120, 120, 120, 350});
    }

    public void playLevelUp() {
        // Game Boy style - triumphant fanfare
        playSound(new double[]{784, 988, 1175, 1568}, new int[]{70, 70, 70, 250});
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
        } else {
            // 取消静音时恢复背景音乐
            startBackgroundMusic();
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
                    // 旋律之间停顿，根据音乐速度调整
                    try {
                        int loopGap = (int) (600 / musicSpeed); // 危险时加快
                        Thread.sleep(loopGap);
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
        // Classic Game Boy Tetris Theme (Korobeiniki / Type A)
        // The iconic Russian folk melody
        double[] notes = {
            // Main theme - first phrase
            659, 494, 523, 587,     // E B C D
            523, 494, 440,          // C B A
            440, 523, 659,          // A C E
            587, 523, 494,          // D C B

            // Second phrase
            523, 587, 659,          // C D E
            523, 440, 440,          // C A A
            0,                      // rest

            // Third phrase
            587, 698, 880,          // D F A
            784, 698, 659,          // G F E
            523, 659, 587,          // C E D
            523, 494,               // C B

            // Fourth phrase (ending)
            494, 523, 587, 659,     // B C D E
            523, 440, 440,          // C A A
            0                       // rest
        };

        // Rhythm pattern matching original Game Boy Tetris
        // 1.0 = quarter note, 0.5 = eighth note, 2.0 = half note
        double[] rhythmPattern = {
            1.0, 0.5, 0.5, 1.0,     // E B C D
            0.5, 0.5, 1.0,          // C B A
            0.5, 0.5, 1.0,          // A C E
            0.5, 0.5, 1.0,          // D C B

            0.5, 0.5, 1.0,          // C D E
            1.0, 1.0, 1.0,          // C A A
            1.0,                    // rest

            0.5, 0.5, 1.0,          // D F A
            0.5, 0.5, 1.0,          // G F E
            0.5, 0.5, 1.0,          // C E D
            0.5, 0.5,               // C B

            1.0, 0.5, 0.5, 1.0,     // B C D E
            1.0, 1.0, 1.0,          // C A A
            1.0                     // rest
        };

        // Base tempo adjusted for music speed
        int baseNoteDuration = 280; // Classic Tetris tempo

        try {
            for (int i = 0; i < notes.length && isPlayingMusic && !isMusicPaused; i++) {
                if (notes[i] > 0) {
                    int noteDuration = (int) (baseNoteDuration * rhythmPattern[i] / musicSpeed);
                    playMusicTone(notes[i], noteDuration);
                    Thread.sleep((int) (40 / musicSpeed)); // 短暂间隔
                } else {
                    // 休止符
                    Thread.sleep((int) (baseNoteDuration * rhythmPattern[i] / musicSpeed));
                }
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

        // Generate Game Boy style square wave for music (50% duty cycle)
        for (int i = 0; i < numSamples; i++) {
            // Pure square wave - authentic Game Boy sound
            double phase = (i * frequency / SAMPLE_RATE) % 1.0;
            sample = phase < 0.5 ? 1.0 : -1.0;

            // Game Boy style envelope - maintain volume with slight fade
            double envelope = 1.0;
            if (i < numSamples * 0.01) {
                // Very quick attack
                envelope = (double) i / (numSamples * 0.01);
            } else if (i > numSamples * 0.85) {
                // Quick fade out at end of note
                envelope = 1.0 - ((double) i - numSamples * 0.85) / (numSamples * 0.15);
            }
            sample = sample * envelope;

            // Convert to 16-bit PCM - Game Boy music volume
            short val = (short) (sample * 32767 * 0.12); // Lower volume for background music
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
