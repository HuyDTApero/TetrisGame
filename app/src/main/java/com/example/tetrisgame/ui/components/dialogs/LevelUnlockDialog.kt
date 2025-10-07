package com.example.tetrisgame.ui.components.dialogs

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tetrisgame.data.models.GameLevel

@Composable
fun LevelUnlockDialog(
    unlockedLevel: GameLevel?,
    onSwitchToLevel: () -> Unit,
    onContinue: () -> Unit
) {
    if (unlockedLevel != null) {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E),
                                    Color(0xFF16213E)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 LEVEL UNLOCKED! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val (levelName, levelDescription, levelColor) = when (unlockedLevel) {
                        GameLevel.SPEED -> Triple(
                            "SPEED MODE",
                            "Faster gameplay with 2x hard drop bonus!\n\nBoard: 10x18\nSpeed: Very Fast (500ms)",
                            Color(0xFFFF6B35)
                        )
                        GameLevel.CHALLENGE -> Triple(
                            "CHALLENGE MODE",
                            "Obstacles appear every 5 lines!\n\nBoard: 12x22\nSpeed: Medium (700ms)\nObstacles: Active",
                            Color(0xFF00FF41)
                        )
                        else -> Triple("", "", Color.White)
                    }

                    Text(
                        text = levelName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = levelDescription,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Do you want to switch to this level now?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onContinue,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Continue Current Level")
                        }

                        Button(
                            onClick = onSwitchToLevel,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = levelColor
                            )
                        ) {
                            Text("Switch Now!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}