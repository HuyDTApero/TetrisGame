package com.example.tetrisgame.ui.components.dialogs
import com.example.tetrisgame.ui.theme.TetrisTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.game.TetrisGameState

@Composable
fun PauseMenuDialog(
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
