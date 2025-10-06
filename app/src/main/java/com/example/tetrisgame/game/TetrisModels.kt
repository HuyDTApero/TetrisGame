package com.example.tetrisgame.game

import androidx.compose.ui.graphics.Color
import com.example.tetrisgame.data.models.GameLevel
import com.example.tetrisgame.data.models.LevelConfig

// Tetris game board dimensions (default - can be overridden by level config)
const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20

// Tetromino types
enum class TetrominoType {
    I, O, T, S, Z, J, L
}

// Tetromino shapes and colors
data class Tetromino(
    val type: TetrominoType,
    val shape: List<List<Boolean>>,
    val color: Color
) {
    companion object {
        fun createTetromino(type: TetrominoType): Tetromino {
            return when (type) {
                TetrominoType.I -> Tetromino(
                    type = TetrominoType.I,
                    shape = listOf(
                        listOf(true, true, true, true)
                    ),
                    color = Color.Cyan
                )

                TetrominoType.O -> Tetromino(
                    type = TetrominoType.O,
                    shape = listOf(
                        listOf(true, true),
                        listOf(true, true)
                    ),
                    color = Color.Yellow
                )

                TetrominoType.T -> Tetromino(
                    type = TetrominoType.T,
                    shape = listOf(
                        listOf(false, true, false),
                        listOf(true, true, true)
                    ),
                    color = Color.Magenta
                )

                TetrominoType.S -> Tetromino(
                    type = TetrominoType.S,
                    shape = listOf(
                        listOf(false, true, true),
                        listOf(true, true, false)
                    ),
                    color = Color.Green
                )

                TetrominoType.Z -> Tetromino(
                    type = TetrominoType.Z,
                    shape = listOf(
                        listOf(true, true, false),
                        listOf(false, true, true)
                    ),
                    color = Color.Red
                )

                TetrominoType.J -> Tetromino(
                    type = TetrominoType.J,
                    shape = listOf(
                        listOf(true, false, false),
                        listOf(true, true, true)
                    ),
                    color = Color.Blue
                )

                TetrominoType.L -> Tetromino(
                    type = TetrominoType.L,
                    shape = listOf(
                        listOf(false, false, true),
                        listOf(true, true, true)
                    ),
                    color = Color(0xFFFF8C00) // Orange
                )
            }
        }
    }
}

// Game piece with position and rotation
data class GamePiece(
    val tetromino: Tetromino,
    val x: Int,
    val y: Int,
    val rotation: Int = 0
) {
    fun getRotatedShape(): List<List<Boolean>> {
        var shape = tetromino.shape
        repeat(rotation % 4) {
            shape = rotateClockwise(shape)
        }
        return shape
    }

    private fun rotateClockwise(matrix: List<List<Boolean>>): List<List<Boolean>> {
        val rows = matrix.size
        val cols = matrix[0].size
        return List(cols) { col ->
            List(rows) { row ->
                matrix[rows - 1 - row][col]
            }
        }
    }

    fun rotate(): GamePiece = copy(rotation = rotation + 1)
    fun moveLeft(): GamePiece = copy(x = x - 1)
    fun moveRight(): GamePiece = copy(x = x + 1)
    fun moveDown(): GamePiece = copy(y = y + 1)
}

// Game board state
data class GameBoard(
    val cells: List<List<Color?>> = List(BOARD_HEIGHT) { List(BOARD_WIDTH) { null } },
    val width: Int = BOARD_WIDTH,
    val height: Int = BOARD_HEIGHT
) {
    fun isValidPosition(piece: GamePiece): Boolean {
        val shape = piece.getRotatedShape()
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val boardX = piece.x + col
                    val boardY = piece.y + row

                    // Check boundaries
                    if (boardX < 0 || boardX >= width ||
                        boardY < 0 || boardY >= height
                    ) {
                        return false
                    }

                    // Check collision with existing pieces
                    if (cells[boardY][boardX] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun placePiece(piece: GamePiece): GameBoard {
        val newCells = cells.map { it.toMutableList() }.toMutableList()
        val shape = piece.getRotatedShape()

        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val boardX = piece.x + col
                    val boardY = piece.y + row
                    if (boardY >= 0 && boardY < height &&
                        boardX >= 0 && boardX < width
                    ) {
                        newCells[boardY][boardX] = piece.tetromino.color
                    }
                }
            }
        }

        return GameBoard(newCells, width, height)
    }

    fun clearLines(): Triple<GameBoard, Int, List<Int>> {
        val newCells = mutableListOf<List<Color?>>()
        var clearedLines = 0
        val clearedLineIndices = mutableListOf<Int>()

        for ((index, row) in cells.withIndex()) {
            if (row.any { it == null }) {
                newCells.add(row)
            } else {
                clearedLines++
                clearedLineIndices.add(index)
            }
        }

        repeat(clearedLines) {
            newCells.add(0, List(width) { null })
        }

        return Triple(GameBoard(newCells, width, height), clearedLines, clearedLineIndices)
    }

    fun addObstacle(row: Int, col: Int): GameBoard {
        val newCells = cells.map { it.toMutableList() }.toMutableList()
        if (row in 0 until height && col in 0 until width) {
            newCells[row][col] = Color.DarkGray // Obstacle color
        }
        return GameBoard(newCells, width, height)
    }

    fun addRandomObstacles(count: Int): GameBoard {
        var board = this
        repeat(count) {
            // Add obstacle in bottom half of board
            val row = (height / 2 until height).random()
            val col = (0 until width).random()
            if (board.cells[row][col] == null) {
                board = board.addObstacle(row, col)
            }
        }
        return board
    }
}

data class TetrisGameState(
    val board: GameBoard = GameBoard(),
    val currentPiece: GamePiece? = null,
    val nextPiece: Tetromino? = null,
    val score: Int = 0,
    val level: Int = 1,
    val lines: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val lastClearedLines: List<Int> = emptyList(), // For animations
    val gameLevel: GameLevel = GameLevel.CLASSIC,
    val levelConfig: LevelConfig = LevelConfig.getConfig(GameLevel.CLASSIC)
) {
    fun calculateDropSpeed(): Long {
        val baseSpeed = levelConfig.initialSpeed
        val speedDecrement = levelConfig.speedDecrement
        val minSpeed = levelConfig.minSpeed
        return maxOf(minSpeed, baseSpeed - (level - 1) * speedDecrement)
    }
}