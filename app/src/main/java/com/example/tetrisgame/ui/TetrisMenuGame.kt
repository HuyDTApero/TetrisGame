package com.example.tetrisgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onSettings: () -> Unit,
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
                onClick = onSettings,
                backgroundColor = Color(0xFFFF9800)
            )

            MenuButton(
                text = "EXIT",
                onClick = {},
                backgroundColor = Color(0xFFF44336)
            )
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