package com.example.tetrisgame.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.ai.AIAssistant
import com.example.tetrisgame.ai.TetrisAI
import com.example.tetrisgame.audio.EnhancedSoundManager
import com.example.tetrisgame.audio.MusicGenerator
import com.example.tetrisgame.game.TetrisEngine
import com.example.tetrisgame.game.TetrisGameState
import com.example.tetrisgame.input.GestureType
import com.example.tetrisgame.input.HapticFeedbackManager
import com.example.tetrisgame.input.swipeGestures
import com.example.tetrisgame.ui.GameOverDialog
import com.example.tetrisgame.ui.TetrisBoard
import com.example.tetrisgame.ui.components.controls.TetrisStyledControls
import com.example.tetrisgame.ui.components.dialogs.GestureHintOverlay
import com.example.tetrisgame.ui.components.dialogs.PauseMenuDialog
import com.example.tetrisgame.ui.components.header.CompactGameHeader
import com.example.tetrisgame.ui.effects.AnimatedBackground
import com.example.tetrisgame.ui.effects.rememberShakeController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TetrisGame(
    onBackToMenu: () -> Unit,
    gameMode: com.example.tetrisgame.data.models.GameMode = com.example.tetrisgame.data.models.GameMode.CLASSIC,
    isSoundEnabled: Boolean = true,
    isMusicEnabled: Boolean = true,
    onAchievementUnlocked: (com.example.tetrisgame.data.models.Achievement) -> Unit = {}
) {
    val engine = remember { TetrisEngine() }
    var gameState by remember { mutableStateOf(engine.resetGame(gameMode)) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val modeConfig = remember { com.example.tetrisgame.data.models.GameModeConfig.getConfig(gameMode) }

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
    val highScoreManager =
        remember { com.example.tetrisgame.data.managers.HighScoreManager(context) }
    val highScore by highScoreManager.highScore.collectAsState(initial = 0)
    val achievementManager =
        remember { com.example.tetrisgame.data.managers.AchievementManager(context) }

    // AI Assistant integration
    val tetrisAI = remember { TetrisAI() }
    val aiAssistant = remember { AIAssistant(tetrisAI) }
    val isAIAssistantEnabled by settingsManager.isAIAssistantEnabled.collectAsState(initial = false)
    var aiHint by remember { mutableStateOf<String?>(null) }
    var showAIHintOverlay by remember { mutableStateOf(false) }

    var previousScore by remember { mutableStateOf(0) }
    var previousLevel by remember { mutableStateOf(1) }
    var showGestureHint by remember { mutableStateOf(true) }

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

    // Optimized deferred update for sound and music
    LaunchedEffect(sfxVolume) {
        soundManager.setSoundVolume(sfxVolume)
    }
    LaunchedEffect(musicVolume) {
        musicGenerator.setVolume(musicVolume)
    }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
            musicGenerator.stopMusic()
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            gameState = engine.spawnNewPiece(gameState)
            if (isMusicEnabled) {
                musicGenerator.startMusic(this)
            }
        }
    }

    // Control music based on game state with deferred check
    LaunchedEffect(isMusicEnabled, gameState.isPaused, gameState.isGameOver) {
        coroutineScope.launch {
            if (isMusicEnabled && !gameState.isPaused && !gameState.isGameOver) {
                if (!musicGenerator.isPlaying()) {
                    musicGenerator.startMusic(this)
                }
            } else {
                musicGenerator.stopMusic()
            }
        }
    }

    // Score/level/gameover deferred side effects
    LaunchedEffect(gameState.score) {
        coroutineScope.launch {
            if (gameState.score > previousScore) {
                val scoreIncrease = gameState.score - previousScore
                val linesCleared = gameState.lastClearedLines.size

                if (scoreIncrease >= 1200) {
                    maxLinesClearedAtOnce = maxOf(maxLinesClearedAtOnce, 4)
                    if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.TETRIS)
                    if (isHapticEnabled) hapticManager.onTetris()
                    shakeController.shake(intensity = 20f, duration = 200)
                } else if (scoreIncrease > 0) {
                    maxLinesClearedAtOnce = maxOf(maxLinesClearedAtOnce, linesCleared)
                    if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LINE_CLEAR)
                    if (isHapticEnabled) hapticManager.onLineClear()
                    shakeController.shake(intensity = 5f * linesCleared, duration = 100)
                }
            }
            previousScore = gameState.score
        }
    }

    LaunchedEffect(gameState.level) {
        coroutineScope.launch {
            if (gameState.level > previousLevel) {
                if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LEVEL_UP)
                if (isHapticEnabled) hapticManager.onLevelUp()
            }
            previousLevel = gameState.level
        }
    }

    LaunchedEffect(gameState.isGameOver) {
        coroutineScope.launch {
            if (gameState.isGameOver) {
                if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.GAME_OVER)
                if (isHapticEnabled) hapticManager.onGameOver()

                highScoreManager.saveGameResult(
                    score = gameState.score,
                    lines = gameState.lines,
                    level = gameState.level
                )

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
                achievementManager.incrementGameCount()
            }
        }
    }

    // Optimized timer/garbage fall/auto drop: use launch for async processing
    LaunchedEffect(gameState.isPaused, gameState.isGameOver, modeConfig.hasTimeLimit) {
        coroutineScope.launch {
            if (modeConfig.hasTimeLimit && !gameState.isPaused && !gameState.isGameOver) {
                while (!gameState.isPaused && !gameState.isGameOver) {
                    delay(1000)
                    val newElapsedTime = gameState.elapsedTimeSeconds + 1
                    var newTimeRemaining = gameState.timeRemainingSeconds - 1
                    if (gameMode == com.example.tetrisgame.data.models.GameMode.ULTRA_2MIN ||
                        gameMode == com.example.tetrisgame.data.models.GameMode.COUNTDOWN
                    ) {

                        if (newTimeRemaining <= 0) {
                            newTimeRemaining = 0
                            gameState = gameState.copy(
                                elapsedTimeSeconds = newElapsedTime,
                                timeRemainingSeconds = newTimeRemaining,
                                isGameOver = true
                            )
                            break
                        }
                    }

                    gameState = gameState.copy(
                        elapsedTimeSeconds = newElapsedTime,
                        timeRemainingSeconds = newTimeRemaining
                    )
                }
            } else {
                while (!gameState.isPaused && !gameState.isGameOver) {
                    delay(1000)
                    gameState = gameState.copy(
                        elapsedTimeSeconds = gameState.elapsedTimeSeconds + 1
                    )
                }
            }
        }
    }

    LaunchedEffect(gameState.isPaused, gameState.isGameOver, gameMode) {
        coroutineScope.launch {
            if (gameMode == com.example.tetrisgame.data.models.GameMode.RISING_TIDE &&
                !gameState.isPaused && !gameState.isGameOver
            ) {
                while (!gameState.isPaused && !gameState.isGameOver) {
                    delay(1000)
                    val newNextTide = gameState.nextTideSeconds - 1
                    if (newNextTide <= 0) {
                        gameState = engine.addGarbageLine(gameState)
                        gameState = gameState.copy(nextTideSeconds = 10)
                    } else {
                        gameState = gameState.copy(nextTideSeconds = newNextTide)
                    }
                }
            }
        }
    }

    LaunchedEffect(gameState.isPaused, gameState.isGameOver) {
        coroutineScope.launch {
            while (!gameState.isPaused && !gameState.isGameOver && gameState.currentPiece != null) {
                delay(gameState.calculateDropSpeed())
                gameState = engine.movePieceDown(gameState)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CompactGameHeader(
                gameState = gameState,
                onBackToMenu = onBackToMenu,
                onPauseToggle = {
                    gameState = engine.togglePause(gameState)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                                coroutineScope.launch {
                                    when (gestureType) {
                                        GestureType.SWIPE_LEFT -> {
                                            gameState = engine.movePieceLeft(gameState)
                                            if (isSoundEnabled) soundManager.playSound(
                                                EnhancedSoundManager.SoundType.MOVE
                                            )
                                            if (isHapticEnabled) hapticManager.onMove()
                                        }

                                        GestureType.SWIPE_RIGHT -> {
                                            gameState = engine.movePieceRight(gameState)
                                            if (isSoundEnabled) soundManager.playSound(
                                                EnhancedSoundManager.SoundType.MOVE
                                            )
                                            if (isHapticEnabled) hapticManager.onMove()
                                        }

                                        GestureType.SWIPE_DOWN -> {
                                            gameState = engine.movePieceDown(gameState)
                                            if (isSoundEnabled) soundManager.playSound(
                                                EnhancedSoundManager.SoundType.MOVE
                                            )
                                            if (isHapticEnabled) hapticManager.onMove()
                                        }

                                        GestureType.SWIPE_UP -> {
                                            gameState = engine.hardDrop(gameState)
                                            hardDropCount++
                                            piecesPlaced++
                                            if (isSoundEnabled) soundManager.playSound(
                                                EnhancedSoundManager.SoundType.LOCK
                                            )
                                            if (isHapticEnabled) hapticManager.onLock()
                                        }

                                        GestureType.TAP -> {
                                            gameState = engine.rotatePiece(gameState)
                                            rotationCount++
                                            if (isSoundEnabled) soundManager.playSound(
                                                EnhancedSoundManager.SoundType.MOVE
                                            )
                                            if (isHapticEnabled) hapticManager.onRotate()
                                        }
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

                if (isAIAssistantEnabled && !gameState.isPaused && !gameState.isGameOver) {
                    aiAssistant.AIHintOverlay(
                        gameState = gameState,
                        showHints = true,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TetrisStyledControls(
                onMoveLeft = {
                    if (!gameState.isPaused) {
                        coroutineScope.launch {
                            gameState = engine.movePieceLeft(gameState)
                            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                            if (isHapticEnabled) hapticManager.onMove()
                        }
                    }
                },
                onMoveRight = {
                    if (!gameState.isPaused) {
                        coroutineScope.launch {
                            gameState = engine.movePieceRight(gameState)
                            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                            if (isHapticEnabled) hapticManager.onMove()
                        }
                    }
                },
                onMoveDown = {
                    if (!gameState.isPaused) {
                        coroutineScope.launch {
                            gameState = engine.movePieceDown(gameState)
                            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                            if (isHapticEnabled) hapticManager.onMove()
                        }
                    }
                },
                onRotate = {
                    if (!gameState.isPaused) {
                        coroutineScope.launch {
                            gameState = engine.rotatePiece(gameState)
                            rotationCount++
                            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.MOVE)
                            if (isHapticEnabled) hapticManager.onRotate()
                        }
                    }
                },
                onHardDrop = {
                    if (!gameState.isPaused) {
                        coroutineScope.launch {
                            gameState = engine.hardDrop(gameState)
                            hardDropCount++
                            piecesPlaced++
                            if (isSoundEnabled) soundManager.playSound(EnhancedSoundManager.SoundType.LOCK)
                            if (isHapticEnabled) hapticManager.onLock()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        GameOverDialog(
            gameState = gameState,
            highScore = highScore,
            onRestart = {
                coroutineScope.launch {
                    gameState = engine.resetGame(gameMode)
                    gameState = engine.spawnNewPiece(gameState)
                    resetAchievementStats()
                }
            },
            onBackToMenu = onBackToMenu
        )

        PauseMenuDialog(
            gameState = gameState,
            onResume = {
                coroutineScope.launch {
                    gameState = engine.togglePause(gameState)
                }
            },
            onRestart = {
                coroutineScope.launch {
                    gameState = engine.resetGame(gameMode)
                    gameState = engine.spawnNewPiece(gameState)
                    resetAchievementStats()
                }
            },
            onBackToMenu = onBackToMenu
        )

        // Mode-specific objective/timer overlay
        if (!gameState.isGameOver && !gameState.isPaused) {
            ModeObjectiveOverlay(
                gameState = gameState,
                modeConfig = modeConfig,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )
        }

        // Gesture hint overlay (show for first few seconds)
        if (showGestureHint && !gameState.isGameOver) {
            GestureHintOverlay(
                onDismiss = { showGestureHint = false }
            )
        }

        // AI Assistant Hint Overlay
        if (showAIHintOverlay && aiHint != null && isAIAssistantEnabled && !gameState.isGameOver) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showAIHintOverlay = false },
                title = {
                    Text(text = "AI Assistant")
                },
                text = {
                    Text(text = aiHint ?: "")
                },
                confirmButton = {
                    Button(
                        onClick = { showAIHintOverlay = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeObjectiveOverlay(
    gameState: TetrisGameState,
    modeConfig: com.example.tetrisgame.data.models.GameModeConfig,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .wrapContentSize()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC1A1A2E)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode name
            Text(
                text = "${modeConfig.icon} ${modeConfig.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mode-specific objective
            when (gameState.gameMode) {
                com.example.tetrisgame.data.models.GameMode.SPRINT_40 -> {
                    val remaining = gameState.getTargetLinesRemaining()
                    Text(
                        text = "$remaining lines remaining",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining <= 10) Color(0xFFFF6B6B) else Color(0xFF00D4FF)
                    )
                }
                com.example.tetrisgame.data.models.GameMode.ULTRA_2MIN -> {
                    val minutes = gameState.timeRemainingSeconds / 60
                    val seconds = gameState.timeRemainingSeconds % 60
                    Text(
                        text = String.format("%d:%02d", minutes, seconds),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gameState.timeRemainingSeconds <= 30) Color(0xFFFF6B6B) else Color(0xFF4CAF50)
                    )
                }
                com.example.tetrisgame.data.models.GameMode.COUNTDOWN -> {
                    val minutes = gameState.timeRemainingSeconds / 60
                    val seconds = gameState.timeRemainingSeconds % 60
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Time",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = String.format("%d:%02d", minutes, seconds),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.timeRemainingSeconds <= 30) Color(0xFFFF6B6B) else Color(0xFF4CAF50)
                        )
                    }
                }
                com.example.tetrisgame.data.models.GameMode.ZEN -> {
                    Text(
                        text = "Relax & Practice",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
                com.example.tetrisgame.data.models.GameMode.CHALLENGE -> {
                    Text(
                        text = "Survive!",
                        fontSize = 14.sp,
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }
                com.example.tetrisgame.data.models.GameMode.RISING_TIDE -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Next Wave",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${gameState.nextTideSeconds}s",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.nextTideSeconds <= 3) Color(0xFFFF6B6B) else Color(0xFF00D4FF)
                        )
                    }
                }
                com.example.tetrisgame.data.models.GameMode.INVISIBLE -> {
                    Text(
                        text = "👻 Invisible Mode",
                        fontSize = 14.sp,
                        color = Color(0xFFBB86FC),
                        fontWeight = FontWeight.Bold
                    )
                }
                com.example.tetrisgame.data.models.GameMode.CHEESE -> {
                    Text(
                        text = "Dig through the cheese!",
                        fontSize = 14.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    // Classic mode - show elapsed time
                    val minutes = gameState.elapsedTimeSeconds / 60
                    val seconds = gameState.elapsedTimeSeconds % 60
                    Text(
                        text = String.format("Time: %d:%02d", minutes, seconds),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}