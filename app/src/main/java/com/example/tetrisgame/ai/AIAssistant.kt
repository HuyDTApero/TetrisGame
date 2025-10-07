package com.example.tetrisgame.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.game.*

/**
 * AI Assistant that provides real-time hints and suggestions to players
 */
class AIAssistant(private val tetrisAI: TetrisAI = TetrisAI()) {

    @Composable
    fun AIHintOverlay(
        gameState: TetrisGameState,
        showHints: Boolean,
        hintLevel: TetrisAI.HintLevel = TetrisAI.HintLevel.MODERATE,
        modifier: Modifier = Modifier
    ) {
        if (!showHints || gameState.currentPiece == null || gameState.isPaused || gameState.isGameOver) return

        val bestMove = tetrisAI.findBestMove(gameState)

        if (bestMove != null) {
            Box(modifier = modifier.fillMaxSize()) {
                // Visual hint overlay on the board
                AIVisualHint(
                    gameState = gameState,
                    bestMove = bestMove,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun AIVisualHint(
        gameState: TetrisGameState,
        bestMove: TetrisAI.Move,
        modifier: Modifier = Modifier
    ) {
        val cellSize = 28.dp
        val cellSizePx = with(LocalDensity.current) { cellSize.toPx() }

        Canvas(
            modifier = modifier.size(cellSize * BOARD_WIDTH, cellSize * BOARD_HEIGHT)
        ) {
            drawBestMoveHint(
                bestMove = bestMove,
                currentPiece = gameState.currentPiece!!,
                cellSize = cellSizePx,
                boardWidth = BOARD_WIDTH,
                boardHeight = BOARD_HEIGHT,
                board = gameState.board
            )
        }
    }

    @Composable
    private fun AIReasoningPanel(
        move: TetrisAI.Move,
        hintLevel: TetrisAI.HintLevel,
        modifier: Modifier = Modifier
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = modifier
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00D4FF).copy(alpha = 0.1f),
                                    Color(0xFF00D4FF).copy(alpha = 0.2f),
                                    Color(0xFF00D4FF).copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "AI Hint",
                                tint = Color(0xFF00D4FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🤖 AI Assistant",
                                color = Color(0xFF00D4FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Score indicator
                        ScoreIndicator(score = move.score)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggestion content based on hint level
                    when (hintLevel) {
                        TetrisAI.HintLevel.DETAILED -> {
                            DetailedHint(move)
                        }

                        TetrisAI.HintLevel.MODERATE -> {
                            ModerateHint(move)
                        }

                        TetrisAI.HintLevel.MINIMAL -> {
                            MinimalHint(move)
                        }

                        TetrisAI.HintLevel.NONE -> {
                            // No hint content
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ScoreIndicator(score: Double) {
        val color = when {
            score > 0.5 -> Color(0xFF4CAF50) // Green - Good
            score > 0.0 -> Color(0xFFFF9800) // Orange - Okay
            else -> Color(0xFFFF5722) // Red - Poor
        }

        val rating = when {
            score > 0.8 -> "EXCELLENT"
            score > 0.5 -> "GOOD"
            score > 0.2 -> "OKAY"
            else -> "RISKY"
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = rating,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    @Composable
    private fun DetailedHint(move: TetrisAI.Move) {
        Column {
            Text(
                text = move.reasoning,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HintDetail("Position", "Column ${move.x + 1}")
                HintDetail("Rotation", "${move.rotation * 90}°")
            }
        }
    }

    @Composable
    private fun ModerateHint(move: TetrisAI.Move) {
        Text(
            text = move.reasoning,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun MinimalHint(move: TetrisAI.Move) {
        Text(
            text = "👍 Suggested move available",
            color = Color(0xFF4CAF50),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun HintDetail(label: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Draw the AI's suggested move on the game board
 */
private fun DrawScope.drawBestMoveHint(
    bestMove: TetrisAI.Move,
    currentPiece: GamePiece,
    cellSize: Float,
    boardWidth: Int,
    boardHeight: Int,
    board: GameBoard
) {
    // Create the suggested piece
    val suggestedPiece = currentPiece.copy(
        x = bestMove.x,
        y = currentPiece.y, // Start from current position, not 0
        rotation = bestMove.rotation
    )

    // Find landing position
    val landingY = findHintLandingPosition(suggestedPiece, board)
    val finalPiece = suggestedPiece.copy(y = landingY)

    val shape = finalPiece.getRotatedShape()

    // Draw hint outline
    for (row in shape.indices) {
        for (col in shape[row].indices) {
            if (shape[row][col]) {
                val x = (finalPiece.x + col) * cellSize
                val y = (finalPiece.y + row) * cellSize

                // Draw glowing hint outline
                drawRect(
                    color = Color(0xFF00D4FF).copy(alpha = 0.3f),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )

                // Draw hint border
                drawRect(
                    color = Color(0xFF00D4FF),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw corner indicators
                val cornerSize = cellSize * 0.2f
                // Top-left corner
                drawRect(
                    color = Color(0xFF00D4FF),
                    topLeft = Offset(x, y),
                    size = Size(cornerSize, cornerSize)
                )
                // Top-right corner
                drawRect(
                    color = Color(0xFF00D4FF),
                    topLeft = Offset(x + cellSize - cornerSize, y),
                    size = Size(cornerSize, cornerSize)
                )
                // Bottom-left corner
                drawRect(
                    color = Color(0xFF00D4FF),
                    topLeft = Offset(x, y + cellSize - cornerSize),
                    size = Size(cornerSize, cornerSize)
                )
                // Bottom-right corner
                drawRect(
                    color = Color(0xFF00D4FF),
                    topLeft = Offset(x + cellSize - cornerSize, y + cellSize - cornerSize),
                    size = Size(cornerSize, cornerSize)
                )
            }
        }
    }
}

/**
 * Find where the hinted piece would land - using actual board collision detection
 */
private fun findHintLandingPosition(piece: GamePiece, board: GameBoard): Int {
    // Start from piece's current position and move down until collision
    for (y in piece.y until BOARD_HEIGHT) {
        val testPiece = piece.copy(y = y)
        if (!board.isValidPosition(testPiece)) {
            return y - 1 // Return the last valid position
        }
    }
    // If no collision found, return current y (shouldn't happen in normal cases)
    return piece.y
}