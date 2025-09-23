package com.example.tetrisgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import com.example.tetrisgame.game.*
import kotlinx.coroutines.delay

@Composable
fun ShooterGame(onBackToMenu: () -> Unit) {
    var gameState by remember { mutableStateOf(ShooterGameState()) }
    val engine = remember { ShooterEngine() }
    

    // Game loop
    LaunchedEffect(gameState.isPaused, gameState.isGameOver) {
        while (!gameState.isPaused && !gameState.isGameOver) {
            delay(33L) // ~30 FPS - chậm hơn để dễ chơi
            gameState = engine.updateGameState(gameState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top UI - Score and Health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score and Level
                Column {
                    Text(
                        text = "SCORE: ${gameState.score}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LEVEL: ${gameState.level}",
                        color = Color.Cyan,
                        fontSize = 14.sp
                    )
                }

                // Health and Shield
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Health hearts
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(gameState.player.maxHealth) { index ->
                            val isAlive = index < gameState.player.health
                            Icon(
                                imageVector = if (isAlive) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.Close
                                },
                                contentDescription = null,
                                tint = if (isAlive) Color.Red else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Power-up indicators
                    if (gameState.isShieldActive()) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (gameState.isRapidFireActive()) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.Cyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (gameState.isMultiShotActive()) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.Magenta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Board
            ShooterGameBoard(
                gameState = gameState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Cyan, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Controls
            ShooterGameControls(
                onMoveLeft = { gameState = engine.movePlayerLeft(gameState) },
                onMoveRight = { gameState = engine.movePlayerRight(gameState) },
                onMoveUp = { gameState = engine.movePlayerUp(gameState) },
                onMoveDown = { gameState = engine.movePlayerDown(gameState) },
                onShoot = { gameState = engine.shootBullet(gameState) },
                onPause = { gameState = engine.togglePause(gameState) },
                isPaused = gameState.isPaused
            )
        }

        // Game Over Dialog
        if (gameState.isGameOver) {
            ShooterGameOverDialog(
                score = engine.getGameOverScore(gameState),
                onRestart = {
                    gameState = engine.resetGame()
                },
                onBackToMenu = onBackToMenu
            )
        }

        // Pause Overlay
        if (gameState.isPaused && !gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PAUSED",
                            color = Color.Cyan,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to resume",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}