package com.example.tetrisgame.game

import androidx.compose.ui.graphics.Color
import com.example.tetrisgame.data.models.GameMode
import com.example.tetrisgame.data.models.GameModeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TetrisEngine {

    // Cache for board calculations to avoid repeated work
    private var cachedBoardHash: Int = 0
    private var cachedValidPositions: Set<Pair<Int, Int>> = emptySet()

    // State caching for performance
    private var lastGameState: TetrisGameState? = null
    private var cachedGhostPiece: GamePiece? = null

    // Optimized movement with caching
    fun movePieceLeftOptimized(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveLeft()

        return if (gameState.board.isValidPositionOptimized(newPiece)) {
            gameState.copy(currentPiece = newPiece).also {
                invalidateGhostPieceCache()
            }
        } else {
            gameState
        }
    }

    fun movePieceRightOptimized(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveRight()

        return if (gameState.board.isValidPositionOptimized(newPiece)) {
            gameState.copy(currentPiece = newPiece).also {
                invalidateGhostPieceCache()
            }
        } else {
            gameState
        }
    }

    fun movePieceDownOptimized(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveDown()

        return if (gameState.board.isValidPositionOptimized(newPiece)) {
            gameState.copy(currentPiece = newPiece).also {
                invalidateGhostPieceCache()
            }
        } else {
            // Piece can't move down, place it and spawn new one
            placePieceAndContinue(gameState)
        }
    }

    fun rotatePieceOptimized(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val rotatedPiece = piece.rotate()

        // Try basic rotation
        if (gameState.board.isValidPositionOptimized(rotatedPiece)) {
            return gameState.copy(currentPiece = rotatedPiece).also {
                invalidateGhostPieceCache()
            }
        }

        // Try wall kicks (SRS - Super Rotation System)
        val wallKicks = getWallKicks(piece.rotation % 4, (piece.rotation + 1) % 4)
        for (kick in wallKicks) {
            val kickedPiece = rotatedPiece.copy(
                x = rotatedPiece.x + kick.first,
                y = rotatedPiece.y + kick.second
            )
            if (gameState.board.isValidPositionOptimized(kickedPiece)) {
                return gameState.copy(currentPiece = kickedPiece).also {
                    invalidateGhostPieceCache()
                }
            }
        }

        return gameState // Rotation not possible
    }

    // Optimized ghost piece with caching
    fun getGhostPieceOptimized(gameState: TetrisGameState): GamePiece? {
        val piece = gameState.currentPiece ?: return null

        // Check if we can use cached ghost piece
        if (cachedGhostPiece != null && lastGameState != null &&
            lastGameState?.currentPiece == piece &&
            lastGameState?.board == gameState.board
        ) {
            return cachedGhostPiece
        }

        var ghostPiece = piece

        // Move ghost piece down until it would collide
        while (gameState.board.isValidPositionOptimized(ghostPiece.moveDown())) {
            ghostPiece = ghostPiece.moveDown()
        }

        // Cache the result
        cachedGhostPiece = ghostPiece
        lastGameState = gameState

        return ghostPiece
    }

    private fun invalidateGhostPieceCache() {
        cachedGhostPiece = null
        lastGameState = null
    }

    fun spawnNewPiece(gameState: TetrisGameState): TetrisGameState {
        val currentPiece = gameState.nextPiece ?: getRandomTetromino()
        val nextPiece = getRandomTetromino()

        val newPiece = GamePiece(
            tetromino = currentPiece,
            x = BOARD_WIDTH / 2 - 1,
            y = 0
        )

        // Check if game is over
        val wouldCollide = !gameState.board.isValidPosition(newPiece)

        // ZEN mode: never game over, just clear bottom line if needed
        if (wouldCollide && gameState.gameMode == GameMode.ZEN) {
            val clearedBoard = clearBottomLine(gameState.board)
            return gameState.copy(
                board = clearedBoard,
                currentPiece = newPiece,
                nextPiece = nextPiece,
                isGameOver = false
            )
        }

        val isGameOver = wouldCollide

        return gameState.copy(
            currentPiece = if (isGameOver) null else newPiece,
            nextPiece = nextPiece,
            isGameOver = isGameOver
        )
    }

    fun movePieceLeft(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveLeft()

        return if (gameState.board.isValidPosition(newPiece)) {
            gameState.copy(currentPiece = newPiece)
        } else {
            gameState
        }
    }

    fun movePieceRight(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveRight()

        return if (gameState.board.isValidPosition(newPiece)) {
            gameState.copy(currentPiece = newPiece)
        } else {
            gameState
        }
    }

    fun movePieceDown(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val newPiece = piece.moveDown()

        return if (gameState.board.isValidPosition(newPiece)) {
            gameState.copy(currentPiece = newPiece)
        } else {
            // Piece can't move down, place it and spawn new one
            placePieceAndContinue(gameState)
        }
    }

    fun rotatePiece(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState
        val rotatedPiece = piece.rotate()

        // Try basic rotation
        if (gameState.board.isValidPosition(rotatedPiece)) {
            return gameState.copy(currentPiece = rotatedPiece)
        }

        // Try wall kicks (SRS - Super Rotation System)
        val wallKicks = getWallKicks(piece.rotation % 4, (piece.rotation + 1) % 4)
        for (kick in wallKicks) {
            val kickedPiece = rotatedPiece.copy(
                x = rotatedPiece.x + kick.first,
                y = rotatedPiece.y + kick.second
            )
            if (gameState.board.isValidPosition(kickedPiece)) {
                return gameState.copy(currentPiece = kickedPiece)
            }
        }

        return gameState // Rotation not possible
    }

    fun hardDrop(gameState: TetrisGameState): TetrisGameState {
        var piece = gameState.currentPiece ?: return gameState
        var dropDistance = 0

        // Move piece down until it can't move anymore
        while (gameState.board.isValidPosition(piece.moveDown())) {
            piece = piece.moveDown()
            dropDistance++
        }

        val updatedGameState = gameState.copy(currentPiece = piece)
        val finalGameState = placePieceAndContinue(updatedGameState)

        // Add bonus score for hard drop
        val bonusScore = dropDistance * 2
        return finalGameState.copy(score = finalGameState.score + bonusScore)
    }

    // Optimized version for immediate use (non-async)
    private fun placePieceAndContinue(gameState: TetrisGameState): TetrisGameState {
        val piece = gameState.currentPiece ?: return gameState

        // Place piece on board (optimized)
        val newBoard = gameState.board.placePiece(piece)

        // Clear completed lines (now optimized)
        val (clearedBoard, linesCleared, clearedLineIndices) = newBoard.clearLines()

        // Calculate score and other game state updates
        val lineScore = calculateLineScore(linesCleared, gameState.level)
        val newScore = gameState.score + lineScore
        val newLines = gameState.lines + linesCleared
        val newLevel = calculateLevel(newLines)

        // Mode-specific: Add time for Countdown mode when clearing lines
        var newTimeRemaining = gameState.timeRemainingSeconds
        if (gameState.gameMode == GameMode.COUNTDOWN && linesCleared > 0) {
            newTimeRemaining += linesCleared * 5 // +5 seconds per line
        }

        // Create new state without current piece
        var intermediateState = gameState.copy(
            board = clearedBoard,
            currentPiece = null,
            score = newScore,
            lines = newLines,
            level = newLevel,
            lastClearedLines = clearedLineIndices,
            timeRemainingSeconds = newTimeRemaining
        )

        // Check win condition (e.g., Sprint 40 lines)
        if (intermediateState.checkWinCondition()) {
            intermediateState = intermediateState.copy(
                isWon = true,
                isGameOver = true
            )
            return intermediateState
        }

        // Spawn new piece
        return spawnNewPieceOptimized(intermediateState)
    }

    suspend fun placePieceAndContinueAsync(gameState: TetrisGameState): TetrisGameState =
        withContext(Dispatchers.Default) {
            val piece = gameState.currentPiece ?: return@withContext gameState

            // Place piece on board (optimized)
            val newBoard = gameState.board.placePiece(piece)

            // Clear completed lines (now optimized)
            val (clearedBoard, linesCleared, clearedLineIndices) = newBoard.clearLines()

            // Calculate score and other game state updates
            val lineScore = calculateLineScore(linesCleared, gameState.level)
            val newScore = gameState.score + lineScore
            val newLines = gameState.lines + linesCleared
            val newLevel = calculateLevel(newLines)

            // Mode-specific: Add time for Countdown mode when clearing lines
            var newTimeRemaining = gameState.timeRemainingSeconds
            if (gameState.gameMode == GameMode.COUNTDOWN && linesCleared > 0) {
                newTimeRemaining += linesCleared * 5 // +5 seconds per line
            }

            // Create new state without current piece
            var intermediateState = gameState.copy(
                board = clearedBoard,
                currentPiece = null,
                score = newScore,
                lines = newLines,
                level = newLevel,
                lastClearedLines = clearedLineIndices,
                timeRemainingSeconds = newTimeRemaining
            )

            // Check win condition (e.g., Sprint 40 lines)
            if (intermediateState.checkWinCondition()) {
                intermediateState = intermediateState.copy(
                    isWon = true,
                    isGameOver = true
                )
                return@withContext intermediateState
            }

            // Spawn new piece (this can also be optimized)
            return@withContext spawnNewPieceOptimized(intermediateState)
        }

    // Optimized spawn new piece with caching
    private fun spawnNewPieceOptimized(gameState: TetrisGameState): TetrisGameState {
        val currentPiece = gameState.nextPiece ?: getRandomTetromino()
        val nextPiece = getRandomTetromino()

        val newPiece = GamePiece(
            tetromino = currentPiece,
            x = BOARD_WIDTH / 2 - 1,
            y = 0
        )

        // Check if game is over (optimized collision detection)
        val wouldCollide = !gameState.board.isValidPositionOptimized(newPiece)

        // ZEN mode: never game over, just clear bottom line if needed
        if (wouldCollide && gameState.gameMode == GameMode.ZEN) {
            val clearedBoard = clearBottomLine(gameState.board)
            return gameState.copy(
                board = clearedBoard,
                currentPiece = newPiece,
                nextPiece = nextPiece,
                isGameOver = false
            )
        }

        val isGameOver = wouldCollide

        return gameState.copy(
            currentPiece = if (isGameOver) null else newPiece,
            nextPiece = nextPiece,
            isGameOver = isGameOver
        )
    }

    private fun getRandomTetromino(): Tetromino {
        val types = TetrominoType.values()
        return Tetromino.createTetromino(types[Random.nextInt(types.size)])
    }

    private fun calculateLineScore(linesCleared: Int, level: Int): Int {
        return when (linesCleared) {
            1 -> 40 * level      // Single
            2 -> 100 * level     // Double  
            3 -> 300 * level     // Triple
            4 -> 1200 * level    // Tetris
            else -> 0
        }
    }

    private fun calculateLevel(totalLines: Int): Int {
        return (totalLines / 10) + 1
    }

    private fun getWallKicks(fromRotation: Int, toRotation: Int): List<Pair<Int, Int>> {
        // Simplified wall kick system
        return listOf(
            Pair(-1, 0),  // Left
            Pair(1, 0),   // Right
            Pair(0, -1),  // Up
            Pair(-1, -1), // Left-Up
            Pair(1, -1)   // Right-Up
        )
    }

    fun togglePause(gameState: TetrisGameState): TetrisGameState {
        return gameState.copy(isPaused = !gameState.isPaused)
    }

    fun resetGame(mode: GameMode = GameMode.CLASSIC): TetrisGameState {
        val config = GameModeConfig.getConfig(mode)

        // Create initial board based on mode
        val initialBoard = when (mode) {
            GameMode.CHALLENGE -> createGarbageBoard(5, 0.6f) // 5 rows, 60% fill
            GameMode.CHEESE -> createGarbageBoard(10, 0.9f) // 10 rows, 90% fill with guaranteed holes
            else -> GameBoard()
        }

        // Calculate initial time remaining for time-limited modes
        val timeRemaining = if (config.hasTimeLimit) {
            config.timeLimitSeconds
        } else {
            0
        }

        // Initialize tide timer for Rising Tide mode
        val nextTide = if (mode == GameMode.RISING_TIDE) 10 else 10

        return TetrisGameState(
            board = initialBoard,
            gameMode = mode,
            level = config.startLevel,
            gameStartTime = System.currentTimeMillis(),
            timeRemainingSeconds = timeRemaining,
            nextTideSeconds = nextTide
        )
    }

    fun getGhostPiece(gameState: TetrisGameState): GamePiece? {
        val piece = gameState.currentPiece ?: return null
        var ghostPiece = piece

        // Move ghost piece down until it would collide
        while (gameState.board.isValidPosition(ghostPiece.moveDown())) {
            ghostPiece = ghostPiece.moveDown()
        }

        return ghostPiece
    }

    // Helper: Clear bottom line (for ZEN mode)
    private fun clearBottomLine(board: GameBoard): GameBoard {
        val newCells = board.cells.toMutableList()
        newCells.removeAt(newCells.size - 1) // Remove bottom row
        newCells.add(0, List(BOARD_WIDTH) { null }) // Add empty row at top
        return GameBoard(newCells)
    }

    // Helper: Create board with random obstacles (for Challenge and Cheese modes)
    private fun createGarbageBoard(numRows: Int, fillRate: Float): GameBoard {
        val cells = MutableList(BOARD_HEIGHT) { MutableList<Color?>(BOARD_WIDTH) { null } }

        // Fill bottom N rows with garbage
        for (row in (BOARD_HEIGHT - numRows) until BOARD_HEIGHT) {
            // For each row, ensure at least one hole
            val holeIndex = Random.nextInt(BOARD_WIDTH)
            for (col in 0 until BOARD_WIDTH) {
                if (col != holeIndex && Random.nextFloat() < fillRate) {
                    cells[row][col] = Color.Gray
                }
            }
        }

        return GameBoard(cells.map { it.toList() })
    }

    // Helper: Add garbage line from bottom (for Rising Tide mode)
    fun addGarbageLine(gameState: TetrisGameState): TetrisGameState {
        val cells = gameState.board.cells.toMutableList()

        // Remove top row
        cells.removeAt(0)

        // Add garbage line at bottom with one random hole
        val holeIndex = Random.nextInt(BOARD_WIDTH)
        val garbageLine = List(BOARD_WIDTH) { col ->
            if (col == holeIndex) null else Color(0xFF444444)
        }
        cells.add(garbageLine)

        val newBoard = GameBoard(cells)

        // Check if current piece is still valid after adding garbage
        val currentPiece = gameState.currentPiece
        if (currentPiece != null && !newBoard.isValidPosition(currentPiece)) {
            // Game over if piece collides with new garbage
            return gameState.copy(
                board = newBoard,
                isGameOver = true,
                currentPiece = null
            )
        }

        return gameState.copy(board = newBoard)
    }
}