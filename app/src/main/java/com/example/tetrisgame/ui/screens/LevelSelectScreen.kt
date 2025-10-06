package com.example.tetrisgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.data.managers.ProgressManager
import com.example.tetrisgame.data.models.GameLevel
import com.example.tetrisgame.data.models.LevelConfig
import com.example.tetrisgame.data.models.PlayerProgress
import com.example.tetrisgame.ui.effects.AnimatedBackground
import com.example.tetrisgame.ui.theme.TetrisTheme

@Composable
fun LevelSelectScreen(
    onLevelSelected: (GameLevel) -> Unit,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val progressManager = remember { ProgressManager(context) }
    val playerProgress by progressManager.playerProgress.collectAsState(initial = PlayerProgress())

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToMenu) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TetrisTheme.NeonCyan
                    )
                }

                Text(
                    text = "SELECT LEVEL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TetrisTheme.NeonCyan
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance layout
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Level Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LevelCard(
                    level = GameLevel.CLASSIC,
                    config = LevelConfig.getConfig(GameLevel.CLASSIC),
                    isUnlocked = playerProgress.isLevelUnlocked(GameLevel.CLASSIC),
                    highScore = playerProgress.getHighScore(GameLevel.CLASSIC),
                    linesCleared = playerProgress.getLinesCleared(GameLevel.CLASSIC),
                    onClick = { onLevelSelected(GameLevel.CLASSIC) }
                )

                LevelCard(
                    level = GameLevel.SPEED,
                    config = LevelConfig.getConfig(GameLevel.SPEED),
                    isUnlocked = playerProgress.isLevelUnlocked(GameLevel.SPEED),
                    highScore = playerProgress.getHighScore(GameLevel.SPEED),
                    linesCleared = playerProgress.getLinesCleared(GameLevel.SPEED),
                    onClick = { onLevelSelected(GameLevel.SPEED) },
                    unlockRequirement = "Complete Classic mode to unlock"
                )

                LevelCard(
                    level = GameLevel.CHALLENGE,
                    config = LevelConfig.getConfig(GameLevel.CHALLENGE),
                    isUnlocked = playerProgress.isLevelUnlocked(GameLevel.CHALLENGE),
                    highScore = playerProgress.getHighScore(GameLevel.CHALLENGE),
                    linesCleared = playerProgress.getLinesCleared(GameLevel.CHALLENGE),
                    onClick = { onLevelSelected(GameLevel.CHALLENGE) },
                    unlockRequirement = "Complete Speed mode to unlock"
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: GameLevel,
    config: LevelConfig,
    isUnlocked: Boolean,
    highScore: Int,
    linesCleared: Int,
    onClick: () -> Unit,
    unlockRequirement: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "level_card")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(if (isUnlocked) pulseScale else 1f)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .then(
                if (isUnlocked) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = if (isUnlocked) {
                        listOf(
                            config.theme.primaryColor,
                            config.theme.secondaryColor
                        )
                    } else {
                        listOf(Color.Gray, Color.DarkGray)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                config.theme.backgroundColor.copy(alpha = 0.8f)
            } else {
                Color(0xFF1A1A1A).copy(alpha = 0.8f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = level.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) config.theme.primaryColor else Color.Gray
                    )

                    Text(
                        text = when (level) {
                            GameLevel.CLASSIC -> "Classic Tetris Experience"
                            GameLevel.SPEED -> "Fast-Paced Challenge"
                            GameLevel.CHALLENGE -> "Extreme Mode with Obstacles"
                        },
                        fontSize = 12.sp,
                        color = if (isUnlocked) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Board",
                                fontSize = 10.sp,
                                color = if (isUnlocked) config.theme.accentColor.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${config.boardWidth}x${config.boardHeight}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) config.theme.accentColor else Color.Gray
                            )
                        }

                        if (config.hardDropMultiplier > 1) {
                            Column {
                                Text(
                                    text = "Bonus",
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) config.theme.secondaryColor.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${config.hardDropMultiplier}x Drop",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) config.theme.secondaryColor else Color.Gray
                                )
                            }
                        }

                        if (config.hasObstacles) {
                            Column {
                                Text(
                                    text = "Feature",
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) Color(0xFFFF6B35).copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Obstacles",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) Color(0xFFFF6B35) else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Stats or Lock
                if (isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HIGH SCORE",
                            fontSize = 10.sp,
                            color = TetrisTheme.NeonYellow.copy(alpha = 0.7f)
                        )
                        Text(
                            text = highScore.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TetrisTheme.NeonYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$linesCleared lines",
                            fontSize = 12.sp,
                            color = TetrisTheme.NeonGreen.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        if (unlockRequirement != null) {
                            Text(
                                text = unlockRequirement,
                                fontSize = 10.sp,
                                color = Color.Gray.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
