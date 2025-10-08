package com.example.tetrisgame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.game.*
import com.example.tetrisgame.ui.effects.OptimizedParticleSystem
import com.example.tetrisgame.ui.effects.LineClearAnimation
import com.example.tetrisgame.ui.effects.drawLineClearFlashOptimized
import kotlinx.coroutines.delay

@Composable
fun TetrisBoard(
    gameState: TetrisGameState,
    modifier: Modifier = Modifier
) {
    val cellSize = 28.dp
    val cellSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { cellSize.toPx() }

    // Create TetrisEngine instance
    val tetrisEngine = remember { TetrisEngine() }

    // Animation state
    val particleSystem = remember { OptimizedParticleSystem() }
    var currentAnimation by remember { mutableStateOf<LineClearAnimation?>(null) }

    // Detect new line clears
    LaunchedEffect(gameState.lastClearedLines) {
        if (gameState.lastClearedLines.isNotEmpty()) {
            currentAnimation = LineClearAnimation(gameState.lastClearedLines)

            // Emit particles for each cleared line
            gameState.lastClearedLines.forEach { lineIndex ->
                // Emit particles across the line
                for (col in 0 until BOARD_WIDTH) {
                    val x = (col + 0.5f) * cellSizePx
                    val y = (lineIndex + 0.5f) * cellSizePx
                    val color = gameState.board.cells.getOrNull(lineIndex)?.getOrNull(col)
                        ?: Color.White
                    particleSystem.emit(x, y, color, count = 5)
                }
            }
        }
    }

    // Update particle system with async processing
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            // Use async update for better performance
            particleSystem.updateAsync(0.016f)

            // Clear animation when finished
            currentAnimation?.let { animation ->
                if (animation.isFinished()) {
                    currentAnimation = null
                }
            }
        }
    }

    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .border(2.dp, Color.Cyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Canvas(
            modifier = Modifier.size(cellSize * BOARD_WIDTH, cellSize * BOARD_HEIGHT)
        ) {
            drawTetrisBoard(gameState, cellSize.toPx(), tetrisEngine)

            // Draw line clear flash animation
            currentAnimation?.let { animation ->
                drawLineClearFlashOptimized(
                    lineIndices = animation.lineIndices,
                    cellSize = cellSize.toPx(),
                    boardWidth = BOARD_WIDTH,
                    progress = animation.getProgress()
                )
            }

            // Draw particles
            particleSystem.draw(this)
        }
    }
}

private fun DrawScope.drawTetrisBoard(
    gameState: TetrisGameState,
    cellSize: Float,
    tetrisEngine: TetrisEngine
) {
    val boardWidth = cellSize * BOARD_WIDTH
    val boardHeight = cellSize * BOARD_HEIGHT

    drawRect(
        color = Color(0xFF0A0A0A),
        size = Size(boardWidth, boardHeight)
    )

    for (i in 0..BOARD_WIDTH) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(i * cellSize, 0f),
            end = Offset(i * cellSize, boardHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    for (i in 0..BOARD_HEIGHT) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, i * cellSize),
            end = Offset(boardWidth, i * cellSize),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Draw placed pieces (hidden in Invisible Mode)
    if (gameState.gameMode != com.example.tetrisgame.data.models.GameMode.INVISIBLE) {
        for (row in gameState.board.cells.indices) {
            for (col in gameState.board.cells[row].indices) {
                val cell = gameState.board.cells[row][col]
                if (cell != null) {
                    drawCell(
                        color = cell,
                        x = col * cellSize,
                        y = row * cellSize,
                        size = cellSize
                    )
                }
            }
        }
    }

    // Draw ghost piece (preview where piece will land)
    val ghostPiece = tetrisEngine.getGhostPieceOptimized(gameState)
    ghostPiece?.let { ghost ->
        val shape = ghost.getRotatedShape()
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val x = (ghost.x + col) * cellSize
                    val y = (ghost.y + row) * cellSize
                    drawCell(
                        color = ghost.tetromino.color.copy(alpha = 0.3f),
                        x = x,
                        y = y,
                        size = cellSize,
                        isGhost = true
                    )
                }
            }
        }
    }

    // Draw current piece
    gameState.currentPiece?.let { piece ->
        val shape = piece.getRotatedShape()
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val x = (piece.x + col) * cellSize
                    val y = (piece.y + row) * cellSize
                    drawCell(
                        color = piece.tetromino.color,
                        x = x,
                        y = y,
                        size = cellSize
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCell(
    color: Color,
    x: Float,
    y: Float,
    size: Float,
    isGhost: Boolean = false
) {
    val margin = size * 0.05f
    val cellSize = size - margin * 2

    if (isGhost) {
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize, cellSize)
        )

        // Ghost border
        val borderWidth = 2.dp.toPx()
        drawRect(
            color = color,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize, borderWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(x + margin, y + size - margin - borderWidth),
            size = Size(cellSize, borderWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(x + margin, y + margin),
            size = Size(borderWidth, cellSize)
        )
        drawRect(
            color = color,
            topLeft = Offset(x + size - margin - borderWidth, y + margin),
            size = Size(borderWidth, cellSize)
        )
    } else {
        // Draw solid cell
        drawRect(
            color = color,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize, cellSize)
        )

        // Inner highlight for 3D effect
        val highlight = Color.White.copy(alpha = 0.3f)
        drawRect(
            color = highlight,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize, cellSize * 0.3f)
        )
        drawRect(
            color = highlight,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize * 0.3f, cellSize)
        )
    }
}

@Composable
fun NextPiecePreview(
    nextPiece: Tetromino?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEXT",
                color = Color.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                nextPiece?.let { piece ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = 16.dp.toPx()
                        val shape = piece.shape
                        val startX = (size.width - shape[0].size * cellSize) / 2
                        val startY = (size.height - shape.size * cellSize) / 2

                        for (row in shape.indices) {
                            for (col in shape[row].indices) {
                                if (shape[row][col]) {
                                    val x = startX + col * cellSize
                                    val y = startY + row * cellSize
                                    drawCell(
                                        color = piece.color,
                                        x = x,
                                        y = y,
                                        size = cellSize
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScorePanel(
    gameState: TetrisGameState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SCORE",
                color = Color.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = gameState.score.toString(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL",
                        color = Color.Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = gameState.level.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LINES",
                        color = Color.Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = gameState.lines.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GameControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    onPause: () -> Unit,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameButton(
                onClick = onRotate,
                icon = Icons.Default.Refresh,
                color = Color.Magenta,
                label = "ROTATE"
            )

            GameButton(
                onClick = onHardDrop,
                icon = Icons.Default.KeyboardArrowDown,
                color = Color.Red,
                label = "DROP"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameButton(
                onClick = onMoveLeft,
                icon = Icons.Default.KeyboardArrowLeft,
                color = Color.Blue,
                label = "LEFT"
            )

            GameButton(
                onClick = onMoveDown,
                icon = Icons.Default.KeyboardArrowDown,
                color = Color.Green,
                label = "DOWN"
            )

            GameButton(
                onClick = onMoveRight,
                icon = Icons.Default.KeyboardArrowRight,
                color = Color.Blue,
                label = "RIGHT"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GameButton(
            onClick = onPause,
            icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Clear,
            color = Color(0xFFFF9800),
            label = if (isPaused) "RESUME" else "PAUSE",
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
private fun GameButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) color.copy(alpha = 0.7f) else color
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun GameOverDialog(
    gameState: TetrisGameState,
    highScore: Int,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    if (gameState.isGameOver) {
        val isNewHighScore = gameState.score > 0 && gameState.score >= highScore
        val isWin = gameState.isWon

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E),
                                    Color(0xFF16213E),
                                    Color(0xFF0F3460)
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Game Over Title with effects
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = if (isWin) {
                                        listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            Color(0xFF81C784).copy(alpha = 0.3f),
                                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFFF5722).copy(alpha = 0.2f),
                                            Color(0xFFFF8A65).copy(alpha = 0.3f),
                                            Color(0xFFFF5722).copy(alpha = 0.2f)
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isWin) "🎉 VICTORY! 🎉" else "💀 GAME OVER 💀",
                                color = if (isWin) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center
                            )

                            if (isNewHighScore) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🏆 NEW HIGH SCORE! 🏆",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F0F23)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "FINAL STATS",
                                color = Color(0xFF00D4FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mode-specific primary stat
                            when (gameState.gameMode) {
                                com.example.tetrisgame.data.models.GameMode.SPRINT_40 -> {
                                    val minutes = gameState.elapsedTimeSeconds / 60
                                    val seconds = gameState.elapsedTimeSeconds % 60
                                    StatRow(
                                        label = "⏱️ TIME",
                                        value = String.format("%d:%02d", minutes, seconds),
                                        valueColor = Color(0xFF00D4FF),
                                        isHighlight = true
                                    )
                                }

                                com.example.tetrisgame.data.models.GameMode.ULTRA_2MIN -> {
                                    StatRow(
                                        label = "🎯 FINAL SCORE",
                                        value = gameState.score.toString(),
                                        valueColor = Color(0xFFFFD700),
                                        isHighlight = true
                                    )
                                }

                                com.example.tetrisgame.data.models.GameMode.COUNTDOWN -> {
                                    val minutes = gameState.elapsedTimeSeconds / 60
                                    val seconds = gameState.elapsedTimeSeconds % 60
                                    StatRow(
                                        label = "⏰ SURVIVED",
                                        value = String.format("%d:%02d", minutes, seconds),
                                        valueColor = Color(0xFF4CAF50),
                                        isHighlight = true
                                    )
                                }

                                else -> {
                                    StatRow(
                                        label = "🎯 SCORE",
                                        value = gameState.score.toString(),
                                        valueColor = if (isNewHighScore) Color(0xFFFFD700) else Color.White,
                                        isHighlight = true
                                    )
                                }
                            }

                            // Show high score for score-based modes
                            if (gameState.gameMode == com.example.tetrisgame.data.models.GameMode.CLASSIC ||
                                gameState.gameMode == com.example.tetrisgame.data.models.GameMode.CHALLENGE ||
                                gameState.gameMode == com.example.tetrisgame.data.models.GameMode.ZEN
                            ) {
                                StatRow(
                                    label = "👑 HIGH SCORE",
                                    value = highScore.toString(),
                                    valueColor = Color(0xFF00D4FF)
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF333366))
                            )

                            // Secondary stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SecondaryStatCard(
                                    icon = "📊",
                                    label = "LEVEL",
                                    value = gameState.level.toString(),
                                    color = Color(0xFF9C27B0)
                                )

                                SecondaryStatCard(
                                    icon = "📏",
                                    label = "LINES",
                                    value = gameState.lines.toString(),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Play Again Button
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎮",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "PLAY AGAIN",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Menu Button
                        Button(
                            onClick = onBackToMenu,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF757575)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🏠",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "MENU",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlight) {
                    Modifier
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    valueColor.copy(alpha = 0.1f),
                                    valueColor.copy(alpha = 0.2f),
                                    valueColor.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (isHighlight) 20.sp else 16.sp
        )
    }
}

@Composable
private fun SecondaryStatCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                color = Color(0xFFCCCCCC),
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}