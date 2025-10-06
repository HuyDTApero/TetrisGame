# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Tetris game built with Jetpack Compose and Kotlin. This is a clean architecture implementation featuring Canvas-based rendering, pure functional game logic, and animated visual effects.

## Build & Run Commands

### Development
```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on connected device/emulator
./gradlew installDebug

# Build and install in one command
./gradlew assembleDebug && ./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "com.example.tetrisgame.ExampleUnitTest"
```

### Code Quality
```bash
# Clean build
./gradlew clean

# Build release APK
./gradlew assembleRelease
```

## Architecture Overview

### Clean Architecture Pattern

The codebase follows a strict separation of concerns:

**Game Logic Layer** (`game/` package) - Pure Kotlin, no Android dependencies
- `TetrisModels.kt`: Immutable data models (Tetromino, GamePiece, GameBoard, TetrisGameState)
- `TetrisEngine.kt`: Pure functions for game logic (movement, rotation, collision detection, scoring)

**UI Layer** (`ui/` package) - Jetpack Compose UI
- `TetrisGame.kt`: Main game screen with state management
- `TetrisMenuGame.kt`: Menu screen
- `GameComponents.kt`: Reusable UI components (TetrisBoard, NextPiecePreview, ScorePanel, GameControls, GameOverDialog)
- `AnimatedBackground.kt`: Visual effects system (matrix rain, particles, animated gradients)
- `Screen.kt`: Navigation enum

**Audio Layer** (`audio/` package) - Sound system
- `SoundGenerator.kt`: Programmatic sound effects generation
- `MusicGenerator.kt`: Background music synthesis and playback
- `SoundManager.kt`: Optional file-based audio manager

**Entry Point**
- `MainActivity.kt`: Handles screen navigation between menu and game

### State Management Flow

Unidirectional data flow:
```
User Input → TetrisEngine method → New TetrisGameState → UI Recomposition
```

Example: `onMoveLeft()` → `engine.movePieceLeft(gameState)` → `newGameState` → Canvas redraws

### Key Design Principles

1. **Immutability**: All game state changes return new objects (no mutations)
2. **Pure Functions**: TetrisEngine methods are side-effect free and testable
3. **Separation of Concerns**: Game logic completely decoupled from UI
4. **Canvas-Based Rendering**: All game elements drawn using Compose Canvas API

## Game Mechanics Implementation

### Core Systems

**Tetromino System**
- 7 standard pieces (I, O, T, S, Z, J, L) with rotation
- Rotation uses matrix transformation in `GamePiece.getRotatedShape()`
- Wall kick system for edge rotation (SRS-simplified)

**Collision Detection**
- `GameBoard.isValidPosition()`: O(n) validation checking boundaries and existing pieces
- Ghost piece calculation shows landing preview

**Scoring Algorithm** (in `TetrisEngine.calculateLineScore()`)
- Single: 40 × level
- Double: 100 × level
- Triple: 300 × level
- Tetris: 1200 × level
- Hard drop bonus: 2 points per cell

**Game Loop**
- Auto-drop speed: `calculateDropSpeed() = max(50, 1000 - (level - 1) * 100)` ms
- Level progression: Every 10 lines cleared

### Visual Effects Architecture

**AnimatedBackground.kt** runs independently at 20 FPS:
- Matrix rain (digital character drops)
- Floating particle system with pulsing
- Animated gradient backgrounds
- Pulsing grid overlay

Game loop runs at variable speed (1000ms → 50ms based on level).

## Important Implementation Details

### Navigation System
The app uses a simple enum-based navigation in `MainActivity.kt`:
- `Screen.TETRIS_MENU` → Menu screen
- `Screen.TETRIS_GAME` → Active game
- `Screen.SHOOTER_MENU`, `Screen.SHOOTER_GAME` → Placeholder for future modes

### JAVA_HOME Configuration
If encountering Java/Gradle issues, set Android Studio's JDK:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

### Canvas Rendering Strategy
- Board cells drawn with 3D highlight effects
- Ghost piece rendered with 30% opacity
- All controls use Canvas touch detection (no Compose buttons for game area)
- Performance: 60 FPS game UI, 20 FPS background effects

### State Initialization
Game starts without current piece - `TetrisEngine.spawnNewPiece()` must be called to begin gameplay. The initial state in `TetrisGame.kt` uses `LaunchedEffect` to spawn first piece.

## Development Guidelines

### When Adding Features

1. **New Game Logic**: Add pure functions to `TetrisEngine`, keep them testable
2. **New UI Components**: Create composables in `GameComponents.kt`, accept state as parameters
3. **New Animations**: Add to `AnimatedBackground.kt` animation system
4. **State Changes**: Always return new `TetrisGameState` copies, never mutate

### Performance Considerations

- Use `remember` for TetrisEngine instance (create once per screen)
- Canvas operations are batched - avoid unnecessary recompositions
- Background animations capped at 20 FPS to save battery
- Line clearing is single-pass O(n) algorithm

### Testing Strategy

- Unit tests for `TetrisEngine` methods (pure functions, easy to test)
- Instrumented tests for UI components and Canvas rendering
- Test collision detection edge cases (wall kicks, rotation near boundaries)
- Validate scoring calculations for all line clear combinations

## Audio System

### Sound Effects
Game uses programmatic audio generation via `SoundGenerator.kt`:
- Move/Rotate/Drop sounds (beep tones)
- Line clear effects (rising tone)
- Tetris (4-line clear) special sound
- Level up and game over sounds

### Background Music
`MusicGenerator.kt` synthesizes Tetris melody:
- 19-note sequence loop
- Auto-stops on pause/game over
- Independent volume control (15% default)

### Controls
- **SFX Button**: Toggle sound effects (green = on, gray = off)
- **MUSIC Button**: Toggle background music (blue = on, gray = off)
- Both use coroutines for non-blocking playback

See `AUDIO_SYSTEM.md` for detailed documentation.

## Project Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **JVM Target**: 11
- **Kotlin**: Latest stable (check `libs.versions.toml`)
- **Compose**: BOM-managed versions
