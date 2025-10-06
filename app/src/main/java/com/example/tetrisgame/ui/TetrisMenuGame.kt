package com.example.tetrisgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TetrisMenuGame(
    onStartGame: () -> Unit,
    onHighScores: () -> Unit,
    isSoundEnabled: MutableState<Boolean>,
    isMusicEnabled: MutableState<Boolean>,
    highScore: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "TETRIS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )

            Text(
                text = "GAME",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00D4FF),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            // High Score Display
            if (highScore > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆 HIGH SCORE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = highScore.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D4FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            MenuButton(
                text = "START GAME",
                onClick = onStartGame,
                backgroundColor = Color(0xFF00D4FF)
            )

            MenuButton(
                text = "HIGH SCORES",
                onClick = onHighScores,
                backgroundColor = Color(0xFF4CAF50)
            )

            MenuButton(
                text = "SETTINGS",
                onClick = {},
                backgroundColor = Color(0xFFFF9800)
            )

            MenuButton(
                text = "EXIT",
                onClick = {},
                backgroundColor = Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Audio Controls Section
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AUDIO SETTINGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D4FF),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AudioToggleButton(
                            label = "SFX",
                            isEnabled = isSoundEnabled.value,
                            onToggle = { isSoundEnabled.value = !isSoundEnabled.value },
                            enabledColor = Color(0xFF4CAF50),
                            icon = Icons.Default.Notifications
                        )

                        AudioToggleButton(
                            label = "MUSIC",
                            isEnabled = isMusicEnabled.value,
                            onToggle = { isMusicEnabled.value = !isMusicEnabled.value },
                            enabledColor = Color(0xFF2196F3),
                            icon = Icons.Default.Star
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AudioToggleButton(
    label: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    enabledColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isEnabled) enabledColor else Color.Gray
            )
        ) {
            Icon(
                imageVector = if (isEnabled) icon else Icons.Default.Close,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) enabledColor else Color.Gray
        )
    }
}