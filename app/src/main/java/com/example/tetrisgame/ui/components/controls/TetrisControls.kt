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
        // D-pad cross shape background - Game Boy style
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF4F4F4),
                            Color(0xFFE8E8E8),
                            Color(0xFFDCDCDC),
                            Color(0xFFC8C8C8)
                        ),
                        radius = 80f
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(6.dp, RoundedCornerShape(12.dp))
        )

        // Vertical bar of the cross - with inset shadow effect
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(104.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD8D8D8),
                            Color(0xFFF0F0F0),
                            Color(0xFFE8E8E8),
                            Color(0xFFD0D0D0)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(1.dp, RoundedCornerShape(8.dp))
        )

        // Horizontal bar of the cross - with inset shadow effect
        Box(
            modifier = Modifier
                .width(104.dp)
                .height(42.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD8D8D8),
                            Color(0xFFF0F0F0),
                            Color(0xFFE8E8E8),
                            Color(0xFFD0D0D0)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(1.dp, RoundedCornerShape(8.dp))
        )

        // Direction buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up button (visual only)
            DPadDirectionButton(
                icon = Icons.Default.KeyboardArrowUp,
                onClick = { },
                enabled = false,
                modifier = Modifier.size(40.dp, 35.dp)
            )

            // Middle row - Left and Right
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadDirectionButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = onLeft,
                    modifier = Modifier.size(35.dp, 40.dp)
                )

                Spacer(modifier = Modifier.width(30.dp))

                DPadDirectionButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    onClick = onRight,
                    modifier = Modifier.size(35.dp, 40.dp)
                )
            }

            // Down button
            DPadDirectionButton(
                icon = Icons.Default.KeyboardArrowDown,
                onClick = onDown,
                modifier = Modifier.size(40.dp, 35.dp)
            )
        }
    }
}

@Composable
private fun DPadDirectionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8F8F8),
                            Color(0xFFE8E8E8),
                            Color(0xFFD8D8D8)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0E0E0),
                            Color(0xFFD0D0D0),
                            Color(0xFFC0C0C0)
                        )
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color(0xFF404040) else Color(0xFF808080),
            modifier = Modifier.size(24.dp)
        )
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
            GameBoyButton(
                text = "B",
                onClick = onHardDrop,
                color = Color(0xFF9C5AAB) // Purple like original Game Boy
            )
        }

        // A Button (Rotate) - positioned higher and to the right  
        Box(
            modifier = Modifier
                .offset(x = 30.dp, y = (-30).dp)
        ) {
            GameBoyButton(
                text = "A",
                onClick = onRotate,
                color = Color(0xFF9C5AAB) // Purple like original Game Boy
            )
        }
    }
}

@Composable
private fun GameBoyButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8E8E8),
            contentColor = color
        ),
        border = BorderStroke(3.dp, Color(0xFFD0D0D0)),
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
                            Color(0xFFF8F8F8),
                            Color(0xFFE8E8E8),
                            Color(0xFFD8D8D8),
                            Color(0xFFC8C8C8)
                        ),
                        radius = 40f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )
        }
    }
}