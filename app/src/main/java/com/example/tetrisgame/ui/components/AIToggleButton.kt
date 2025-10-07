package com.example.tetrisgame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AIToggleButton(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF00D4FF) else Color(0xFF404040),
        animationSpec = tween(300),
        label = "AI Toggle Background"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isEnabled) Color.White else Color.Gray,
        animationSpec = tween(300),
        label = "AI Toggle Content"
    )

    Surface(
        onClick = onToggle,
        modifier = modifier
            .shadow(if (isEnabled) 8.dp else 4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = if (isEnabled) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00D4FF),
                                Color(0xFF0099CC),
                                Color(0xFF00D4FF)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF404040),
                                Color(0xFF303030),
                                Color(0xFF404040)
                            )
                        )
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "AI Assistant",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "🤖 AI",
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (isEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "ON",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}