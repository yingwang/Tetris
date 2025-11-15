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
            short val = (short) (sample * 32767 * 0.12); // 12% volume - 再降低音效音量
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
        // Bright, satisfying drop sound - descending chirp
        playSound(new double[]{880, 660}, new int[]{60, 80});
    }

    public void playLineClear() {
        // Bright, triumphant ascending arpeggio - rewarding and celebratory
        // C - E - G - C(high) - E(high) with crescendo ending
        playSound(new double[]{523, 659, 784, 1047, 1319}, new int[]{80, 80, 80, 100, 140});
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
        // 欢快有节奏感的游戏旋律 - Energetic, rhythmic game melody
        // 类似经典街机游戏风格
        double[] notes = {
            659, 659, 0, 659,       // E E - E  (节奏感开场)
            523, 659, 784,          // C E G    (跳跃上行)
            392, 0, 523, 0,         // G - C -  (停顿增加节奏)
            392, 330, 349, 440,     // G E F A  (快速音阶)
            523, 587, 523, 440,     // C D C A  (活泼跳跃)
            392, 523, 659,          // G C E    (再次上行)
            587, 659, 587, 523,     // D E D C  (下行回旋)
            440, 392, 523           // A G C    (结束)
        };

        // 节奏模式 - varied rhythm for more energy
        // 1 = 正常, 0.5 = 短促, 1.5 = 长音
        double[] rhythmPattern = {
            0.7, 0.7, 0.4, 0.7,     // 快速节奏
            0.7, 0.7, 1.2,          // 跳跃感
            1.0, 0.4, 1.0, 0.4,     // 停顿节奏
            0.6, 0.6, 0.6, 0.6,     // 快速音阶
            0.7, 0.7, 0.7, 0.7,     // 均匀节奏
            0.7, 0.7, 1.2,          // 再次跳跃
            0.6, 0.6, 0.6, 0.6,     // 快速下行
            0.7, 0.7, 1.5           // 结束长音
        };

        // 根据musicSpeed调整节奏 - rhythm adjusted by music speed
        int baseNoteDuration = 320; // 更短的基础时长，更快节奏

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

            // Convert to 16-bit PCM (balanced volume for background music)
            short val = (short) (sample * 32767 * 0.18); // 18% volume - 背景音乐更清晰
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
