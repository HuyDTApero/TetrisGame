package com.example.tetrisgame.ai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.tetrisgame.game.*

/**
 * AI Assistant that provides real-time hints and suggestions to players
 */
class AIAssistant(private val tetrisAI: TetrisAI = TetrisAI()) {

    @Composable
    fun AIHintOverlay(
        gameState: TetrisGameState,
        showHints: Boolean,
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
    // Create the suggested piece with the AI's recommended position and rotation
    // Start from TOP of board (y=0) to properly calculate landing position
    val suggestedPiece = currentPiece.copy(
        x = bestMove.x,
        y = 0, // Always start from top for accurate landing calculation
        rotation = bestMove.rotation
    )

    // Find where this piece would actually land if dropped from top
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
    // Ensure we start from the very top (y = 0) for accurate landing detection
    for (y in 0 until BOARD_HEIGHT) {
        val testPiece = piece.copy(y = y)
        if (!board.isValidPosition(testPiece)) {
            return y - 1 // Return the last valid position
        }
    }
    // If no collision found, return the last row
    return BOARD_HEIGHT - 1
}