package com.example.tetrisgame.game

import androidx.compose.ui.graphics.Color

// Tetris game board dimensions
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
    val cells: List<List<Color?>> = List(BOARD_HEIGHT) { List(BOARD_WIDTH) { null } }
) {
    fun isValidPosition(piece: GamePiece): Boolean {
        val shape = piece.getRotatedShape()
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val boardX = piece.x + col
                    val boardY = piece.y + row

                    // Check boundaries
                    if (boardX < 0 || boardX >= BOARD_WIDTH ||
                        boardY < 0 || boardY >= BOARD_HEIGHT
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
                    if (boardY >= 0 && boardY < BOARD_HEIGHT &&
                        boardX >= 0 && boardX < BOARD_WIDTH
                    ) {
                        newCells[boardY][boardX] = piece.tetromino.color
                    }
                }
            }
        }

        return GameBoard(newCells)
    }

    fun clearLines(): Pair<GameBoard, Int> {
        val newCells = mutableListOf<List<Color?>>()
        var clearedLines = 0

        for (row in cells) {
            if (row.any { it == null }) {
                newCells.add(row)
            } else {
                clearedLines++
            }
        }

        // Add empty lines at the top
        repeat(clearedLines) {
            newCells.add(0, List(BOARD_WIDTH) { null })
        }

        return Pair(GameBoard(newCells), clearedLines)
    }
}

// Game state
data class TetrisGameState(
    val board: GameBoard = GameBoard(),
    val currentPiece: GamePiece? = null,
    val nextPiece: Tetromino? = null,
    val score: Int = 0,
    val level: Int = 1,
    val lines: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false
) {
    fun calculateDropSpeed(): Long {
        // Speed increases with level (milliseconds)
        return maxOf(50, 1000 - (level - 1) * 100).toLong()
    }
}