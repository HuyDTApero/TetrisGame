# Tetris Game - Android Canvas & Jetpack Compose

🎮 A modern, feature-rich Tetris game built with Jetpack Compose and Canvas API, showcasing advanced
Android development techniques.

![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Language](https://img.shields.io/badge/language-Kotlin-blue.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-orange.svg)

## 📱 Screenshots

| Main Menu                               | Game Modes                                | Gameplay                              | Settings                              |
|-----------------------------------------|-------------------------------------------|---------------------------------------|---------------------------------------|
| ![Main Menu](screenshots/main_menu.png) | ![Game Modes](screenshots/game_modes.png) | ![Gameplay](screenshots/gameplay.png) | ![Settings](screenshots/settings.png) |

| Achievements                                  | High Scores                                 | Zen Mode                              | Challenge Mode                               |
|-----------------------------------------------|---------------------------------------------|---------------------------------------|----------------------------------------------|
| ![Achievements](screenshots/achievements.png) | ![High Scores](screenshots/high_scores.png) | ![Zen Mode](screenshots/zen_mode.png) | ![Challenge](screenshots/challenge_mode.png) |

## ✨ Features

### 🎯 Multiple Game Modes

- **Classic** ⭐ - Traditional Tetris gameplay
- **Sprint 40L** 🏃 - Clear 40 lines as fast as possible
- **Ultra 2 Min** ⏱️ - Maximum score in 2 minutes
- **Zen Mode** 🧘 - Relaxing mode without game over
- **Challenge** 💪 - Start at level 10 with obstacles
- **Countdown** ⏳ - Time management with bonus seconds
- **Invisible** 🌙 - Memory challenge mode
- **Cheese Mode** 🧀 - Dig through garbage blocks
- **Rising Tide** 🌊 - Survive rising garbage

### 🎨 Advanced Canvas Implementation

- **Pure Canvas Rendering** - Game board, pieces, and UI elements drawn with Canvas API
- **Smooth Animations** - Fluid piece movements and line clearing effects
- **Visual Effects** - Particle systems, screen shake, and visual feedback
- **Custom UI Components** - Canvas-based buttons and interactive elements

### 🎵 Enhanced Audio Experience

- **Dynamic Music Generation** - Procedurally generated background music
- **Immersive Sound Effects** - Piece placement, line clears, and UI interactions
- **Audio Settings** - Separate controls for music and sound effects

### 🏆 Achievement System

- **30+ Achievements** - Various gameplay milestones and challenges
- **Progress Tracking** - Visual progress indicators and unlocks
- **Achievement Notifications** - Beautiful unlock animations

### ⚙️ Advanced Features

- **AI Assistant** - Optional AI hints and suggestions
- **Haptic Feedback** - Tactile responses for enhanced gameplay
- **Gesture Controls** - Swipe gestures in addition to touch buttons
- **High Score Persistence** - Local storage with DataStore
- **Customizable Settings** - Difficulty, speed, and visual preferences

## 🏗️ Architecture

### Project Structure
```
app/src/main/java/com/example/tetrisgame/
├── MainActivity.kt                    # Main entry point
├── ai/                               # AI system for hints
│   ├── TetrisAI.kt                  # Core AI logic
│   └── AIAssistant.kt               # AI assistant integration
├── audio/                           # Sound and music system
│   ├── SoundManager.kt              # Sound effects
│   ├── MusicGenerator.kt            # Dynamic music generation
│   └── EnhancedSoundManager.kt      # Advanced audio features
├── data/                            # Data layer
│   ├── models/                      # Data models
│   └── managers/                    # Data persistence
├── game/                            # Core game logic
│   ├── TetrisEngine.kt              # Game engine
│   └── TetrisModels.kt              # Game models
├── input/                           # Input handling
│   ├── SwipeGestureHandler.kt       # Gesture recognition
│   └── HapticFeedbackManager.kt     # Haptic feedback
└── ui/                              # User interface
    ├── screens/                     # Screen composables
    ├── components/                  # Reusable UI components
    ├── effects/                     # Visual effects
    └── theme/                       # App theming
```

### Key Technologies

- **Jetpack Compose** - Modern UI toolkit
- **Canvas API** - Custom graphics rendering
- **Coroutines** - Asynchronous programming
- **DataStore** - Data persistence
- **StateFlow** - Reactive state management
- **Kotlinx Serialization** - JSON serialization

## 🎮 Gameplay Features

### Classic Tetris Mechanics

- **7 Standard Tetromino Pieces** (I, O, T, S, Z, J, L)
- **Piece Rotation** - Standard SRS (Super Rotation System)
- **Line Clearing** - Single, Double, Triple, and Tetris
- **Level Progression** - Increasing speed and difficulty
- **Hold Function** - Save piece for later use
- **Ghost Piece** - Preview piece placement

### Advanced Game Mechanics

- **T-Spin Detection** - Bonus points for T-spin clears
- **Combo System** - Consecutive line clears for bonus points
- **Soft Drop/Hard Drop** - Speed control mechanics
- **Lock Delay** - Brief delay before piece locks
- **Wall Kicks** - Advanced rotation mechanics

### Canvas Rendering System
```kotlin
// Example: Custom Canvas rendering
Canvas(modifier = Modifier.fillMaxSize()) {
    // Draw game board
    drawGameBoard(gameState.board)
    
    // Draw active piece
    drawTetromino(gameState.currentPiece)
    
    // Draw effects
    drawParticleEffects(effectsState)
}
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Kotlin 1.9+
- Android SDK API 24+
- Gradle 8.0+

## 🎯 Game Controls

### Touch Controls

- **Left/Right Arrows** - Move piece horizontally
- **Down Arrow** - Soft drop (faster descent)
- **Rotate Button** - Rotate piece clockwise
- **Hold Button** - Hold current piece
- **Hard Drop** - Instantly drop piece

### Gesture Controls

- **Swipe Left/Right** - Move piece
- **Swipe Down** - Soft drop
- **Tap** - Rotate piece
- **Long Press** - Hold piece

## 🎨 Customization

### Visual Settings

- **Theme Selection** - Multiple color schemes
- **Grid Visibility** - Toggle game board grid
- **Animation Speed** - Adjust visual effect speed
- **Particle Effects** - Enable/disable particle systems

### Gameplay Settings

- **Starting Level** - Begin at higher difficulty
- **Ghost Piece** - Toggle piece preview
- **Hold Function** - Enable/disable hold feature
- **Auto-Repeat** - Continuous movement when holding buttons

## 🏆 Achievements

The game features 30+ achievements across different categories:

- **Score Milestones** - Reach specific score targets
- **Line Clearing** - Different types of line clears
- **Survival** - Time-based achievements
- **Mode-Specific** - Unique achievements for each game mode
- **Special Moves** - T-spins and advanced techniques

## 📊 Performance

### Optimizations

- **Efficient Canvas Rendering** - Minimal recomposition
- **Smart State Management** - Optimized StateFlow usage
- **Memory Management** - Proper object pooling
- **Battery Optimization** - Efficient game loop implementation

### Metrics

- **60 FPS** - Smooth gameplay on most devices
- **Low Memory Usage** - ~50MB RAM typical usage
- **Fast Startup** - <2 seconds cold start
- **Responsive Controls** - <16ms input latency

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Tetris** - Original game by Alexey Pajitnov
- **Jetpack Compose** - Google's modern UI toolkit
- **Android Community** - For continuous inspiration and support


⭐ **Star this repository if you find it helpful!** ⭐

*Built with ❤️ using Jetpack Compose and Canvas API*