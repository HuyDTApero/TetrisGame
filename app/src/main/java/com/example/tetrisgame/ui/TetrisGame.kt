package com.example.tetrisgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Tetris Theme Colors
object TetrisTheme {
    val NeonCyan = Color(0xFF00FFFF)
    val NeonPink = Color(0xFFFF006E)
    val NeonGreen = Color(0xFF00FF41)
    val NeonYellow = Color(0xFFFFFF00)
    val NeonPurple = Color(0xFFBF40BF)
    val DarkBg = Color(0xFF0A0A0A)
    val CardBg = Color(0xFF1A1A2E)
}

@Composable
fun TetrisGame(
    onBackToMenu: () -> Unit,
    isSoundEnabled: Boolean = true,
    isMusicEnabled: Boolean = true,
    onAchievementUnlocked: (com.example.tetrisgame.data.Achievement) -> Unit = {}
) {
    var gameState by remember { mutableStateOf(TetrisGameState()) }
    val engine = remember { TetrisEngine() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    // Settings Manager - read all settings
    val settingsManager = remember { com.example.tetrisgame.data.SettingsManager(context) }
    val sfxVolume by settingsManager.sfxVolume.collectAsState(initial = 0.7f)
    val musicVolume by settingsManager.musicVolume.collectAsState(initial = 0.5f)
    val isHapticEnabled by settingsManager.isHapticEnabled.collectAsState(initial = true)
    val gestureSensitivity by settingsManager.gestureSensitivity.collectAsState(initial = 50f)
    val currentTheme by settingsManager.theme.collectAsState(initial = com.example.tetrisgame.data.GameTheme.NEON)

    val musicGenerator = remember { MusicGenerator() }
    val soundManager = remember { EnhancedSoundManager(context, coroutineScope) }
    val hapticManager = remember { HapticFeedbackManager(context) }
    val shakeController = rememberShakeController()
    val highScoreManager = remember { com.example.tetrisgame.data.HighScoreManager(context) }
    val highScore by highScoreManager.highScore.collectAsState(initial = 0)
    val achievementManager = remember { com.example.tetrisgame.data.AchievementManager(context) }

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

    // Detect score changes for sound effects, haptics, and screen shake
    LaunchedEffect(gameState.score) {
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
        AnimatedBackground(modifier = Modifier.fillMaxSize())

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
                gameState = engine.resetGame()
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
                gameState = engine.resetGame()
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
    }
}

@Composable
private fun CompactGameHeader(
    gameState: TetrisGameState,
    onBackToMenu: () -> Unit,
    onPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        TetrisTheme.CardBg.copy(alpha = 0.8f),
                        TetrisTheme.CardBg.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, TetrisTheme.NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back and Pause Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onBackToMenu,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TetrisTheme.NeonCyan
                )
            }

            IconButton(
                onClick = onPauseToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (gameState.isPaused) Icons.Default.PlayArrow else Icons.Default.Clear,
                    contentDescription = if (gameState.isPaused) "Resume" else "Pause",
                    tint = TetrisTheme.NeonYellow
                )
            }
        }

        // Score
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SCORE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonYellow.copy(alpha = 0.7f)
            )
            Text(
                text = gameState.score.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonYellow
            )
        }

        // Level
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LV",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonPink.copy(alpha = 0.7f)
            )
            Text(
                text = gameState.level.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonPink
            )
        }

        // Lines
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LINES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonGreen.copy(alpha = 0.7f)
            )
            Text(
                text = gameState.lines.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TetrisTheme.NeonGreen
            )
        }

        // Next Piece (Mini)
        MiniNextPiecePreview(nextPiece = gameState.nextPiece)
    }
}

@Composable
private fun MiniNextPiecePreview(nextPiece: com.example.tetrisgame.game.Tetromino?) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(
                color = TetrisTheme.DarkBg.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, TetrisTheme.NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        nextPiece?.let { piece ->
            androidx.compose.foundation.Canvas(modifier = Modifier.size(40.dp)) {
                val cellSize = 8.dp.toPx()
                val shape = piece.shape
                val startX = (size.width - shape[0].size * cellSize) / 2
                val startY = (size.height - shape.size * cellSize) / 2

                for (row in shape.indices) {
                    for (col in shape[row].indices) {
                        if (shape[row][col]) {
                            val x = startX + col * cellSize
                            val y = startY + row * cellSize
                            drawRect(
                                color = piece.color,
                                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(cellSize * 0.8f, cellSize * 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TetrisStyledControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Row: Rotate and Drop
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TetrisButton(
                onClick = onRotate,
                icon = Icons.Default.Refresh,
                label = "ROTATE",
                color = TetrisTheme.NeonPurple,
                modifier = Modifier.size(70.dp)
            )

            TetrisButton(
                onClick = onHardDrop,
                icon = Icons.Default.KeyboardArrowDown,
                label = "DROP",
                color = TetrisTheme.NeonPink,
                modifier = Modifier.size(70.dp)
            )
        }

        // Middle Row: Directional Pad
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TetrisButton(
                onClick = onMoveLeft,
                icon = Icons.Default.KeyboardArrowLeft,
                label = "",
                color = TetrisTheme.NeonCyan,
                modifier = Modifier.size(60.dp)
            )

            TetrisButton(
                onClick = onMoveDown,
                icon = Icons.Default.KeyboardArrowDown,
                label = "",
                color = TetrisTheme.NeonGreen,
                modifier = Modifier.size(60.dp)
            )

            TetrisButton(
                onClick = onMoveRight,
                icon = Icons.Default.KeyboardArrowRight,
                label = "",
                color = TetrisTheme.NeonCyan,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
private fun TetrisButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = if (modifier == Modifier.size(width = 140.dp, height = 50.dp)) {
        RoundedCornerShape(25.dp)
    } else {
        CircleShape
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(8.dp, shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(if (label.isEmpty()) 32.dp else 24.dp)
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun PauseMenuDialog(
    gameState: TetrisGameState,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    if (gameState.isPaused && !gameState.isGameOver) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TetrisTheme.CardBg.copy(alpha = 0.98f)
                ),
                modifier = Modifier
                    .padding(24.dp)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                TetrisTheme.NeonCyan,
                                TetrisTheme.NeonPink,
                                TetrisTheme.NeonPurple
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .width(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title
                    Text(
                        text = "⏸ PAUSED",
                        color = TetrisTheme.NeonCyan,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Game Stats
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = TetrisTheme.DarkBg.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Score:", color = TetrisTheme.NeonYellow, fontSize = 16.sp)
                                Text(
                                    gameState.score.toString(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Level:", color = TetrisTheme.NeonPink, fontSize = 16.sp)
                                Text(
                                    gameState.level.toString(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lines:", color = TetrisTheme.NeonGreen, fontSize = 16.sp)
                                Text(
                                    gameState.lines.toString(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Menu Buttons
                    PauseMenuButton(
                        text = "▶ RESUME",
                        color = TetrisTheme.NeonGreen,
                        onClick = onResume
                    )

                    PauseMenuButton(
                        text = "↻ RESTART",
                        color = TetrisTheme.NeonYellow,
                        onClick = onRestart
                    )

                    PauseMenuButton(
                        text = "← MAIN MENU",
                        color = TetrisTheme.NeonPink,
                        onClick = onBackToMenu
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseMenuButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GestureHintOverlay(
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(5000) // Auto-dismiss after 5 seconds
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = TetrisTheme.CardBg.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "👆 TOUCH CONTROLS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TetrisTheme.NeonCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                GestureHintItem("⬅️ Swipe Left", "Move left")
                GestureHintItem("➡️ Swipe Right", "Move right")
                GestureHintItem("⬇️ Swipe Down", "Soft drop")
                GestureHintItem("⬆️ Swipe Up", "Hard drop")
                GestureHintItem("👆 Tap", "Rotate")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TetrisTheme.NeonCyan.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GOT IT!",
                        color = TetrisTheme.NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureHintItem(gesture: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = gesture,
            fontSize = 16.sp,
            color = Color.White
        )
        Text(
            text = action,
            fontSize = 14.sp,
            color = TetrisTheme.NeonYellow
        )
    }
}
