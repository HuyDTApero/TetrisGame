package com.example.tetrisgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.example.tetrisgame.audio.EnhancedSoundManager
import com.example.tetrisgame.audio.MusicGenerator
import com.example.tetrisgame.game.TetrisEngine
import com.example.tetrisgame.game.TetrisGameState
import com.example.tetrisgame.ui.components.header.CompactGameHeader
import com.example.tetrisgame.ui.components.controls.TetrisStyledControls
import com.example.tetrisgame.ui.components.dialogs.PauseMenuDialog
import com.example.tetrisgame.ui.components.dialogs.GestureHintOverlay
import com.example.tetrisgame.ui.components.dialogs.LevelUnlockDialog
import com.example.tetrisgame.ui.theme.TetrisTheme
import com.example.tetrisgame.input.HapticFeedbackManager
import com.example.tetrisgame.input.swipeGestures
import com.example.tetrisgame.input.GestureType
import com.example.tetrisgame.ui.effects.rememberShakeController
import com.example.tetrisgame.ui.effects.AnimatedBackground
import com.example.tetrisgame.ui.TetrisBoard
import com.example.tetrisgame.ui.GameOverDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TetrisGame(
    onBackToMenu: () -> Unit,
    isSoundEnabled: Boolean = true,
    isMusicEnabled: Boolean = true,
    gameLevel: com.example.tetrisgame.data.models.GameLevel = com.example.tetrisgame.data.models.GameLevel.CLASSIC,
    onAchievementUnlocked: (com.example.tetrisgame.data.models.Achievement) -> Unit = {},
    onLevelComplete: (com.example.tetrisgame.data.models.GameLevel) -> Unit = {},
    onSwitchToLevel: (com.example.tetrisgame.data.models.GameLevel) -> Unit = {}
) {
    val engine = remember { TetrisEngine() }
    var gameState by remember { mutableStateOf(engine.resetGame(gameLevel)) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var previousScore by remember { mutableStateOf(0) }
    var previousLevel by remember { mutableStateOf(1) }
    var showGestureHint by remember { mutableStateOf(true) }
    var showLevelUnlockDialog by remember { mutableStateOf<com.example.tetrisgame.data.models.GameLevel?>(null) }

    // Achievement tracking (only for game-over check)
    var hardDropCount by remember { mutableStateOf(0) }
    var rotationCount by remember { mutableStateOf(0) }
    var piecesPlaced by remember { mutableStateOf(0) }
    var gameStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var maxLinesClearedAtOnce by remember { mutableStateOf(0) }

    // Helper function to reset achievement stats
    fun resetAchievementStats() {
        hardDropCount = 0
        rotationCount = 0
        piecesPlaced = 0
        gameStartTime = System.currentTimeMillis()
        maxLinesClearedAtOnce = 0
    }

    // Settings Manager - read all settings
    val settingsManager = remember { com.example.tetrisgame.data.managers.SettingsManager(context) }
    val sfxVolume by settingsManager.sfxVolume.collectAsState(initial = 0.7f)
    val musicVolume by settingsManager.musicVolume.collectAsState(initial = 0.5f)
    val isHapticEnabled by settingsManager.isHapticEnabled.collectAsState(initial = true)
    val gestureSensitivity by settingsManager.gestureSensitivity.collectAsState(initial = 50f)
    val currentTheme by settingsManager.theme.collectAsState(initial = com.example.tetrisgame.data.models.GameTheme.NEON)

    val musicGenerator = remember { MusicGenerator() }
    val soundManager = remember { EnhancedSoundManager(context, coroutineScope) }
    val hapticManager = remember { HapticFeedbackManager(context) }
    val shakeController = rememberShakeController()
    val highScoreManager = remember { com.example.tetrisgame.data.managers.HighScoreManager(context) }
    val highScore by highScoreManager.highScore.collectAsState(initial = 0)
    val achievementManager = remember { com.example.tetrisgame.data.managers.AchievementManager(context) }
    val progressManager = remember { com.example.tetrisgame.data.managers.ProgressManager(context) }

    // Apply volume settings
    LaunchedEffect(sfxVolume) {
        soundManager.setSoundVolume(sfxVolume)
    }

    LaunchedEffect(musicVolume) {
        musicGenerator.setVolume(musicVolume)
    }

    // Cleanup sound manager
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    LaunchedEffect(Unit) {
        gameState = engine.spawnNewPiece(gameState)
        if (isMusicEnabled) {
            musicGenerator.startMusic(this)
        }
    }

    // Control music based on game state
    LaunchedEffect(isMusicEnabled, gameState.isPaused, gameState.isGameOver) {
        if (isMusicEnabled && !gameState.isPaused && !gameState.isGameOver) {
            if (!musicGenerator.isPlaying()) {
                musicGenerator.startMusic(this)
            }
        } else {
            musicGenerator.stopMusic()
        }
    }

    // Cleanup music when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            musicGenerator.stopMusic()
        }
    }

    // Detect score changes for sound effects, haptics, screen shake, and level unlock
    LaunchedEffect(gameState.score, gameState.lines) {
        if (gameState.score > previousScore) {
            val scoreIncrease = gameState.score - previousScore
            val linesCleared = gameState.lastClearedLines.size

            if (scoreIncrease >= 1200) {
                // Tetris = 4 lines cleared at once
                maxLinesClearedAtOnce = maxOf(maxLinesClearedAtOnce, 4)
                if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.TETRIS)
                if (isHapticEnabled) hapticManager.onTetris()
                shakeController.shake(intensity = 20f, duration = 200)
            } else if (scoreIncrease > 0) {
                // Track max lines cleared at once
                maxLinesClearedAtOnce = maxOf(maxLinesClearedAtOnce, linesCleared)
                if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LINE_CLEAR)
                if (isHapticEnabled) hapticManager.onLineClear()
                shakeController.shake(intensity = 5f * linesCleared, duration = 100)
            }
        }
        previousScore = gameState.score

        // Check for level unlock during gameplay (not just game over)
        if (!gameState.isGameOver && showLevelUnlockDialog == null) {
            val shouldUnlockNext = when (gameLevel) {
                com.example.tetrisgame.data.models.GameLevel.CLASSIC -> gameState.score >= 1000 || gameState.lines >= 10
                com.example.tetrisgame.data.models.GameLevel.SPEED -> gameState.score >= 2000 || gameState.lines >= 20
                com.example.tetrisgame.data.models.GameLevel.CHALLENGE -> false
            }

            if (shouldUnlockNext) {
                val nextLevel = when (gameLevel) {
                    com.example.tetrisgame.data.models.GameLevel.CLASSIC -> com.example.tetrisgame.data.models.GameLevel.SPEED
                    com.example.tetrisgame.data.models.GameLevel.SPEED -> com.example.tetrisgame.data.models.GameLevel.CHALLENGE
                    com.example.tetrisgame.data.models.GameLevel.CHALLENGE -> null
                }
                nextLevel?.let {
                    progressManager.unlockLevel(it)
                    onLevelComplete(it)
                    gameState = engine.togglePause(gameState) // Pause game
                    showLevelUnlockDialog = it // Show dialog
                }
            }
        }
    }

    // Detect level up
    LaunchedEffect(gameState.level) {
        if (gameState.level > previousLevel) {
            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LEVEL_UP)
            if (isHapticEnabled) hapticManager.onLevelUp()
        }
        previousLevel = gameState.level
    }

    // Detect game over and save score
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.GAME_OVER)
            if (isHapticEnabled) hapticManager.onGameOver()

            // Save game result
            highScoreManager.saveGameResult(
                score = gameState.score,
                lines = gameState.lines,
                level = gameState.level
            )

            // Save progress for this level
            val playTime = System.currentTimeMillis() - gameStartTime
            progressManager.updateGameResult(
                level = gameLevel,
                score = gameState.score,
                lines = gameState.lines,
                playTime = playTime
            )

            // Check if level should be unlocked (based on score or lines)
            val shouldUnlockNext = when (gameLevel) {
                com.example.tetrisgame.data.models.GameLevel.CLASSIC -> gameState.score >= 1000 || gameState.lines >= 10
                com.example.tetrisgame.data.models.GameLevel.SPEED -> gameState.score >= 2000 || gameState.lines >= 20
                com.example.tetrisgame.data.models.GameLevel.CHALLENGE -> false // Max level
            }

            if (shouldUnlockNext) {
                val nextLevel = when (gameLevel) {
                    com.example.tetrisgame.data.models.GameLevel.CLASSIC -> com.example.tetrisgame.data.models.GameLevel.SPEED
                    com.example.tetrisgame.data.models.GameLevel.SPEED -> com.example.tetrisgame.data.models.GameLevel.CHALLENGE
                    com.example.tetrisgame.data.models.GameLevel.CHALLENGE -> null
                }
                nextLevel?.let {
                    progressManager.unlockLevel(it)
                    onLevelComplete(it)
                }
            }

            // Check ALL achievements once at game over
            val gameEndTime = System.currentTimeMillis()
            val newAchievements = achievementManager.checkAchievements(
                score = gameState.score,
                level = gameState.level,
                linesInGame = gameState.lines,
                linesClearedAtOnce = maxLinesClearedAtOnce,
                hardDropUsed = hardDropCount > 0,
                rotationUsed = rotationCount > 0,
                gameStartTime = gameStartTime,
                gameEndTime = gameEndTime,
                piecesPlaced = piecesPlaced
            )
            newAchievements.forEach { achievement ->
                onAchievementUnlocked(achievement)
            }

            // Increment game count
            achievementManager.incrementGameCount()
        }
    }

    LaunchedEffect(gameState.isPaused, gameState.isGameOver) {
        while (!gameState.isPaused && !gameState.isGameOver && gameState.currentPiece != null) {
            delay(gameState.calculateDropSpeed())
            gameState = engine.movePieceDown(gameState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(
            modifier = Modifier.fillMaxSize(),
            theme = gameState.levelConfig.theme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Header with Score and Next Piece (Side by Side)
            CompactGameHeader(
                gameState = gameState,
                onBackToMenu = onBackToMenu,
                onPauseToggle = {
                    gameState = engine.togglePause(gameState)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Board (takes most space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize()
                    .offset(
                        x = shakeController.getOffset().x.dp,
                        y = shakeController.getOffset().y.dp
                    )
                    .swipeGestures(
                        onGesture = { gestureType ->
                            if (!gameState.isPaused && !gameState.isGameOver) {
                                when (gestureType) {
                                    GestureType.SWIPE_LEFT -> {
                                        gameState = engine.movePieceLeft(gameState)
                                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                                        if (isHapticEnabled) hapticManager.onMove()
                                    }
                                    GestureType.SWIPE_RIGHT -> {
                                        gameState = engine.movePieceRight(gameState)
                                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                                        if (isHapticEnabled) hapticManager.onMove()
                                    }
                                    GestureType.SWIPE_DOWN -> {
                                        gameState = engine.movePieceDown(gameState)
                                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                                        if (isHapticEnabled) hapticManager.onMove()
                                    }
                                    GestureType.SWIPE_UP -> {
                                        gameState = engine.hardDrop(gameState)
                                        hardDropCount++
                                        piecesPlaced++
                                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LOCK)
                                        if (isHapticEnabled) hapticManager.onLock()
                                    }
                                    GestureType.TAP -> {
                                        gameState = engine.rotatePiece(gameState)
                                        rotationCount++
                                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                                        if (isHapticEnabled) hapticManager.onRotate()
                                    }
                                }
                            }
                        },
                        swipeThreshold = gestureSensitivity
                    )
            ) {
                TetrisBoard(
                    gameState = gameState
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tetris-themed Game Controls
            TetrisStyledControls(
                onMoveLeft = {
                    if (!gameState.isPaused) {
                        gameState = engine.movePieceLeft(gameState)
                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                        if (isHapticEnabled) hapticManager.onMove()
                    }
                },
                onMoveRight = {
                    if (!gameState.isPaused) {
                        gameState = engine.movePieceRight(gameState)
                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                        if (isHapticEnabled) hapticManager.onMove()
                    }
                },
                onMoveDown = {
                    if (!gameState.isPaused) {
                        gameState = engine.movePieceDown(gameState)
                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                        if (isHapticEnabled) hapticManager.onMove()
                    }
                },
                onRotate = {
                    if (!gameState.isPaused) {
                        gameState = engine.rotatePiece(gameState)
                        rotationCount++
                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                        if (isHapticEnabled) hapticManager.onRotate()
                    }
                },
                onHardDrop = {
                    if (!gameState.isPaused) {
                        gameState = engine.hardDrop(gameState)
                        hardDropCount++
                        piecesPlaced++
                        if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LOCK)
                        if (isHapticEnabled) hapticManager.onLock()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        GameOverDialog(
            gameState = gameState,
            highScore = highScore,
            onRestart = {
                gameState = engine.resetGame(gameLevel)
                gameState = engine.spawnNewPiece(gameState)
                resetAchievementStats()
            },
            onBackToMenu = onBackToMenu
        )

        PauseMenuDialog(
            gameState = gameState,
            onResume = {
                gameState = engine.togglePause(gameState)
            },
            onRestart = {
                gameState = engine.resetGame(gameLevel)
                gameState = engine.spawnNewPiece(gameState)
                resetAchievementStats()
            },
            onBackToMenu = onBackToMenu
        )

        // Gesture hint overlay (show for first few seconds)
        if (showGestureHint && !gameState.isGameOver) {
            GestureHintOverlay(
                onDismiss = { showGestureHint = false }
            )
        }

        // Level unlock dialog - show when new level is unlocked
        LevelUnlockDialog(
            unlockedLevel = showLevelUnlockDialog,
            onSwitchToLevel = {
                // User wants to switch to new level immediately
                showLevelUnlockDialog?.let { newLevel ->
                    onSwitchToLevel(newLevel)
                    showLevelUnlockDialog = null
                }
            },
            onContinue = {
                // User wants to continue current game
                showLevelUnlockDialog = null
                gameState = engine.togglePause(gameState) // Resume game
            }
        )
    }
}