package com.example.tetrisgame.ui.components.header
import com.example.tetrisgame.ui.theme.TetrisTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.game.TetrisGameState

@Composable
fun CompactGameHeader(
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
