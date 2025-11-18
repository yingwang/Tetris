# Game Boy Color Audio Update Guide

This guide explains how to update the Tetris game audio to match authentic Game Boy Color sound.

## Overview

The current game uses placeholder audio files. To achieve authentic Game Boy Color audio, you need to replace the existing audio files with chiptune/8-bit style sounds.

## Audio Files Location

All audio files are located in: `app/src/main/res/raw/`

Current audio files used by the game:
- `tetris_theme.mp3` - Background music
- `line_clear.mp3` - Line clear sound effect
- `piece_drop.mp3` - Piece placement sound effect
- `game_over.mp3` - Game over sound effect

## Game Boy Color Audio Characteristics

### Technical Specifications
- **Sample Rate**: 8-bit or 16-bit at 22050 Hz or lower
- **Channels**: Mono or stereo
- **Format**: Use OGG or MP3 format for Android compatibility

### Sound Characteristics
1. **Chiptune/8-bit Style**
   - Square wave, triangle wave, and noise channels
   - Limited polyphony (typically 4 channels on GBC)
   - Retro lo-fi aesthetic

2. **Background Music**
   - Classic Tetris theme (Korobeiniki) in chiptune arrangement
   - Loop seamlessly
   - Moderate tempo (around 120-140 BPM)

3. **Sound Effects**
   - **Line Clear**: Short ascending arpeggio (50-100ms)
   - **Piece Drop**: Brief low-frequency thud (20-50ms)
   - **Game Over**: Descending melody (500-1000ms)

## How to Create/Obtain GBC-Style Audio

### Option 1: Use Chiptune Music Trackers
- **BeepBox** (https://beepbox.co) - Web-based, free
- **FamiTracker** - For authentic NES/GB sound
- **LSDJ** - Game Boy music tracker (requires emulator)

### Option 2: Find Royalty-Free Chiptune Music
- **OpenGameArt.org** - Search for "chiptune" or "8-bit"
- **Freesound.org** - Search for "game boy" or "chiptune"
- **Zapsplat.com** - Game sound effects section

### Option 3: Convert Existing Audio
Use audio software (Audacity, FL Studio, etc.) with 8-bit/chiptune plugins:
1. Reduce bit depth to 8-bit
2. Downsample to 22050 Hz
3. Apply lo-fi/bitcrusher effects
4. Use square wave synthesizers

## Implementation Steps

1. **Create or obtain GBC-style audio files**
2. **Replace the files in** `app/src/main/res/raw/`:
   ```
   tetris_theme.mp3 (or .ogg)
   line_clear.mp3
   piece_drop.mp3
   game_over.mp3
   ```
3. **Test the audio** in the game
4. **Adjust volume levels** in `SoundManager.java` if needed

## Volume Recommendations

Adjust these in `SoundManager.java`:
- Background music: 0.5 - 0.7
- Line clear SFX: 0.6 - 0.8
- Piece drop SFX: 0.4 - 0.6
- Game over SFX: 0.7 - 0.9

## Code Reference

Audio is managed in `app/src/main/java/com/tetris/game/SoundManager.java`

Key methods:
- `playLineSound()` - Plays when lines are cleared
- `playDropSound()` - Plays when piece lands
- `startBackgroundMusic()` - Starts looping background music
- `playGameOverSound()` - Plays game over sound

## Testing

After updating audio files:
1. Clean and rebuild the project
2. Test all sound effects in-game
3. Verify background music loops smoothly
4. Check volume levels are balanced
5. Test mute functionality

## License Considerations

Ensure all audio files are:
- Royalty-free or properly licensed
- Attributed if required by license
- Compatible with your game's license

## Additional Resources

- **GB Studio Audio Tools**: https://www.gbstudio.dev/
- **Chiptune Subreddit**: r/chiptunes
- **Game Boy Sound Hardware**: https://gbdev.gg8.se/wiki/articles/Sound

---

**Note**: The audio files included in the repository are placeholders. Replace them with authentic GBC-style audio for the best retro experience!
