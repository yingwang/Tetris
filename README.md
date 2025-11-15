# Tetris Game for Android

A professional Tetris game implementation for Android with retro sounds, 3D graphics, and customizable gameplay.

## Features

### Game Features
- **Classic Tetris gameplay** with all 7 standard tetromino pieces (I, O, T, S, Z, J, L)
- **Customizable difficulty**: Choose speed levels 1-9 (1 = slowest, 9 = fastest)
- **Starting lines configuration**: Start with 0-9 pre-filled lines for added challenge
- **Level progression**: Automatic level advancement based on score
- **High scores**: Top 10 scores stored persistently with date and level
- **Line clear animation**: Visual flash effect when clearing lines

### Visual Design
- **Professional 3D graphics**: Blocks rendered with gradients, highlights, and shadows
- **Dark gradient background**: Modern gaming aesthetic
- **Next piece preview**: See the upcoming tetromino
- **Green-themed UI**: High-visibility buttons with 3D styling
- **Score and level displays**: Real-time updates with golden text

### Audio
- **Retro 8-bit sound effects**: Mario-style procedurally generated sounds
- **Sound events**:
  - Move: Short blip
  - Rotate: Two-tone rising sound
  - Drop: Descending triple tone
  - Line clear: Rising arpeggio (C-E-G-C)
  - Level up: Victory fanfare
  - Game over: Sad descending melody
- **Mute functionality**: Toggle sound on/off from the menu

### Controls
- **Left/Right arrows**: Move piece horizontally
- **Rotate button**: Rotate piece clockwise
- **Drop button**: Instantly drop piece to bottom
- **Pause/Resume**: Access from menu
- **New Game**: Access from menu
- **High Scores**: View top scores from menu

## How to Build and Run

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API Level 26 or higher
- Java 8 or higher

### Building
1. Clone the repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Build the project: `Build > Make Project`

### Running
1. Connect an Android device or start an emulator
2. Click Run (▶️) in Android Studio
3. Select your target device

### Installation
The APK will be generated in `app/build/outputs/apk/debug/` after building.

## Gameplay Instructions

### Starting a Game
1. **Select Speed**: Choose from 1 (slow) to 9 (fast)
2. **Select Starting Lines**: Choose 0-9 pre-filled lines for extra challenge
3. Click **Start Game**

### During Gameplay
- Use left/right arrows to position pieces
- Use rotate button to orient pieces
- Use drop button for quick placement
- Lines are cleared when completely filled
- Game ends when pieces stack to the top

### Scoring
- 1 line: 100 × level
- 2 lines: 300 × level
- 3 lines: 500 × level
- 4 lines: 800 × level

### Levels
- Advance one level for every 1000 points
- Higher levels award more points per line

## Project Structure

```
app/src/main/
├── java/com/tetris/game/
│   ├── MainActivity.java           # Main activity, game loop, UI control
│   ├── TetrisGame.java            # Game logic, piece movement, scoring
│   ├── TetrisBoard.java           # Board state, collision detection
│   ├── TetrisPiece.java           # Piece shapes, rotation, movement
│   ├── TetrisView.java            # Custom view, 3D rendering, animations
│   ├── SoundManager.java          # Audio synthesis, sound effects
│   ├── HighScoreManager.java      # Persistent score storage
│   └── HighScoresActivity.java    # High scores display
├── res/
│   ├── drawable/                  # Button backgrounds, game background
│   ├── layout/                    # Activity layouts
│   ├── menu/                      # Menu definitions
│   ├── mipmap/                    # Launcher icons
│   ├── raw/                       # Sound files (currently using procedural audio)
│   └── values/                    # Strings, colors
└── AndroidManifest.xml
```

## Technical Details

### Architecture
- **MVC pattern**: Game logic separated from view rendering
- **Observer pattern**: GameListener interface for UI updates
- **Custom View**: TetrisView extends View for efficient Canvas rendering

### Graphics Rendering
- 3D block effect using LinearGradient shaders
- Highlight/shadow layers for depth perception
- Line clear animation with alpha blending
- Dynamic color assignment per piece type

### Sound System
- AudioTrack for low-latency audio playback
- Procedural square wave generation
- Musical note frequencies (A4=440Hz based)
- Envelope shaping for retro gaming feel

### Data Persistence
- SharedPreferences for high scores
- Semicolon-delimited score entries (score,level,timestamp)
- Automatic sorting and top-10 limitation

### Performance
- Game loop using Handler.postDelayed()
- Efficient Canvas invalidation
- Minimal object allocation in game loop
- Thread-safe UI updates via runOnUiThread()

## License

This project is available for educational and personal use.

## Version History

### Current Version
- Visual line clear animation
- Configurable starting lines (0-9)
- Retro sound effects
- Professional 3D graphics
- High score tracking
- Customizable speed selection

### Previous Updates
- Added high scores system
- Improved button layout
- Enhanced visual design
- Added sound effects and mute functionality
- Implemented 3D block rendering
- Created launcher icons
