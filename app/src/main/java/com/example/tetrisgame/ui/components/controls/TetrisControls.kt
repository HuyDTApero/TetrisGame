package com.example.tetrisgame.ui.components.controls
import com.example.tetrisgame.ui.theme.TetrisTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun TetrisStyledControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Realistic D-pad
        GameBoyDPad(
            onLeft = onMoveLeft,
            onRight = onMoveRight,
            onDown = onMoveDown,
            modifier = Modifier.size(140.dp)
        )

        // Right side - Game Boy Action Buttons
        GameBoyActionButtons(
            onRotate = onRotate,
            onHardDrop = onHardDrop,
            modifier = Modifier.size(140.dp)
        )
    }
}

@Composable
private fun GameBoyDPad(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Clean cross pattern with only 3 buttons needed for Tetris
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row - Left and Right
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularDPadButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = onLeft,
                    enabled = true,
                    color = Color(0xFF45B7D1) // Blue
                )

                CircularDPadButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    onClick = onRight,
                    enabled = true,
                    color = Color(0xFF96CEB4) // Green
                )
            }

            // Bottom row - Down button centered
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                CircularDPadButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    onClick = onDown,
                    enabled = true,
                    color = Color(0xFF45B7D1) // Same blue as left button for consistency
                )
            }
        }
    }
}

@Composable
private fun CircularDPadButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(60.dp) // Increased from 50dp to 60dp
            .shadow(if (enabled) 8.dp else 3.dp, CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White
        ),
        border = BorderStroke(2.dp, Color(0xFF2A2A2A)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 8.dp else 3.dp,
            pressedElevation = 2.dp,
            disabledElevation = 2.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = if (enabled) {
                            listOf(
                                color.copy(alpha = 1.0f),
                                color.copy(alpha = 0.9f),
                                color.copy(alpha = 0.8f),
                                color.copy(alpha = 0.7f)
                            )
                        } else {
                            listOf(
                                Color(0xFF3A3A3A),
                                Color(0xFF2A2A2A),
                                Color(0xFF1A1A1A)
                            )
                        },
                        radius = 35f // Increased radius for larger button
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Circular highlight effect
            if (enabled) {
                Box(
                    modifier = Modifier
                        .size(35.dp) // Increased highlight size
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                radius = 18f // Increased radius
                            ),
                            shape = CircleShape
                        )
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp) // Increased icon size
            )
        }
    }
}

@Composable
private fun GameBoyActionButtons(
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // B Button (Hard Drop) - positioned lower and to the left
        Box(
            modifier = Modifier
                .offset(x = (-30).dp, y = 30.dp)
        ) {
            CircularActionButton(
                text = "B",
                onClick = onHardDrop,
                color = Color(0xFFFF6B9D) // Bright pink
            )
        }

        // A Button (Rotate) - positioned higher and to the right  
        Box(
            modifier = Modifier
                .offset(x = 30.dp, y = (-30).dp)
        ) {
            CircularActionButton(
                text = "A",
                onClick = onRotate,
                color = Color(0xFF4ECDC4) // Bright cyan
            )
        }
    }
}

@Composable
private fun CircularActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(60.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape, // Perfect circle
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        border = BorderStroke(2.dp, Color(0xFF2A2A2A)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 1.0f),
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.8f),
                            color.copy(alpha = 0.7f)
                        ),
                        radius = 35f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Circular highlight effect
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 20f
                        ),
                        shape = CircleShape
                    )
            )

            Text(
                text = text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}