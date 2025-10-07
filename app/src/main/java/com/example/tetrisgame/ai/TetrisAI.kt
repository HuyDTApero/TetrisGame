package com.example.tetrisgame.ai

import com.example.tetrisgame.game.*
import kotlin.math.abs

/**
 * Core Tetris AI that evaluates board positions and suggests optimal moves
 * Uses classic Tetris AI heuristics for piece placement evaluation
 */
class TetrisAI {

    data class Move(
        val x: Int,
        val rotation: Int,
        val score: Double,
        val reasoning: String = ""
    )

    data class BoardEvaluation(
        val aggregateHeight: Double,
        val completeLines: Int,
        val holes: Int,
        val bumpiness: Double,
        val totalScore: Double
    )

    // Heuristic weights - tuned for optimal play
    private val weights = mapOf(
        "aggregateHeight" to -0.510066,
        "completeLines" to 0.760666,
        "holes" to -0.35663,
        "bumpiness" to -0.184483
    )

    /**
     * Find the best possible move for the current piece and board state
     */
    fun findBestMove(gameState: TetrisGameState): Move? {
        val currentPiece = gameState.currentPiece ?: return null

        val possibleMoves = generateAllPossibleMoves(currentPiece.tetromino, gameState.board)
        if (possibleMoves.isEmpty()) return null

        return possibleMoves.maxByOrNull { it.score }
    }

    /**
     * Generate all possible moves for a tetromino on the current board
     */
    private fun generateAllPossibleMoves(tetromino: Tetromino, board: GameBoard): List<Move> {
        val moves = mutableListOf<Move>()

        // Try all 4 rotations
        for (rotation in 0..3) {
            // Try all horizontal positions
            for (x in 0 until BOARD_WIDTH) {
                val testPiece = GamePiece(tetromino, x, 0, rotation)

                // Find where piece would land (hard drop simulation)
                val landingY = findLandingPosition(testPiece, board)
                if (landingY >= 0) {
                    val finalPiece = testPiece.copy(y = landingY)

                    // Check if placement is valid
                    if (board.isValidPosition(finalPiece)) {
                        val simulatedBoard = board.placePiece(finalPiece)
                        val (clearedBoard, _, _) = simulatedBoard.clearLines()
                        val evaluation = evaluateBoard(clearedBoard)
                        val reasoning = generateReasoning(evaluation)

                        moves.add(
                            Move(
                                x = x,
                                rotation = rotation,
                                score = evaluation.totalScore,
                                reasoning = reasoning
                            )
                        )
                    }
                }
            }
        }

        return moves
    }

    /**
     * Find where a piece would land if hard dropped
     */
    private fun findLandingPosition(piece: GamePiece, board: GameBoard): Int {
        // Start from the piece's current Y position
        var currentY = piece.y

        // If the starting position is already invalid, try to find a valid position from top
        if (!board.isValidPosition(piece)) {
            // Try to find first valid Y position from top
            for (testY in 0 until BOARD_HEIGHT) {
                val testPiece = piece.copy(y = testY)
                if (board.isValidPosition(testPiece)) {
                    currentY = testY
                    break
                }
            }

            // If no valid position found from top, return -1 to indicate invalid move
            if (currentY == piece.y && !board.isValidPosition(piece)) {
                return -1
            }
        }

        // Now find the landing position by moving down
        for (y in currentY until BOARD_HEIGHT) {
            val testPiece = piece.copy(y = y)
            if (!board.isValidPosition(testPiece)) {
                return maxOf(0, y - 1) // Return last valid position
            }
        }

        // If we reach here, piece can go all the way to bottom
        return BOARD_HEIGHT - 1
    }

    /**
     * Evaluate a board position using heuristic scoring
     */
    private fun evaluateBoard(board: GameBoard): BoardEvaluation {
        val aggregateHeight = calculateAggregateHeight(board)
        val completeLines = countCompleteLines(board)
        val holes = countHoles(board)
        val bumpiness = calculateBumpiness(board)

        val totalScore = (aggregateHeight * weights["aggregateHeight"]!!) +
                (completeLines * weights["completeLines"]!!) +
                (holes * weights["holes"]!!) +
                (bumpiness * weights["bumpiness"]!!)

        return BoardEvaluation(
            aggregateHeight = aggregateHeight,
            completeLines = completeLines,
            holes = holes,
            bumpiness = bumpiness,
            totalScore = totalScore
        )
    }

    /**
     * Calculate total height of all columns
     */
    private fun calculateAggregateHeight(board: GameBoard): Double {
        var totalHeight = 0.0

        for (col in 0 until BOARD_WIDTH) {
            for (row in 0 until BOARD_HEIGHT) {
                if (board.cells[row][col] != null) {
                    totalHeight += (BOARD_HEIGHT - row)
                    break
                }
            }
        }

        return totalHeight
    }

    /**
     * Count complete lines that can be cleared
     */
    private fun countCompleteLines(board: GameBoard): Int {
        return board.cells.count { row ->
            row.all { it != null }
        }
    }

    /**
     * Count holes (empty cells with filled cells above)
     */
    private fun countHoles(board: GameBoard): Int {
        var holes = 0

        for (col in 0 until BOARD_WIDTH) {
            var foundBlock = false
            for (row in 0 until BOARD_HEIGHT) {
                if (board.cells[row][col] != null) {
                    foundBlock = true
                } else if (foundBlock) {
                    holes++
                }
            }
        }

        return holes
    }

    /**
     * Calculate bumpiness (height differences between adjacent columns)
     */
    private fun calculateBumpiness(board: GameBoard): Double {
        val heights = IntArray(BOARD_WIDTH) { 0 }

        // Calculate height of each column
        for (col in 0 until BOARD_WIDTH) {
            for (row in 0 until BOARD_HEIGHT) {
                if (board.cells[row][col] != null) {
                    heights[col] = BOARD_HEIGHT - row
                    break
                }
            }
        }

        // Calculate total bumpiness
        var bumpiness = 0.0
        for (i in 0 until BOARD_WIDTH - 1) {
            bumpiness += abs(heights[i] - heights[i + 1])
        }

        return bumpiness
    }


    private fun generateReasoning(evaluation: BoardEvaluation): String {
        return when {
            evaluation.completeLines > 0 -> {
                "Clears ${evaluation.completeLines} line${if (evaluation.completeLines > 1) "s" else ""}! 🎉"
            }

            evaluation.holes == 0 && evaluation.bumpiness < 3 -> {
                "Clean placement, keeps board tidy ✨"
            }

            evaluation.holes > 3 -> {
                "Creates holes, but might be necessary 😅"
            }

            evaluation.aggregateHeight > 50 -> {
                "High stack, be careful! ⚠️"
            }

            else -> {
                "Decent move, maintains board structure 👍"
            }
        }
    }

}