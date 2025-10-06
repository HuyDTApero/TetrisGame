package com.example.tetrisgame.ui.components.controls
import com.example.tetrisgame.ui.theme.TetrisTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TetrisStyledControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Row: Rotate and Drop
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TetrisButton(
                onClick = onRotate,
                icon = Icons.Default.Refresh,
                label = "ROTATE",
                color = TetrisTheme.NeonPurple,
                modifier = Modifier.size(70.dp)
            )

            TetrisButton(
                onClick = onHardDrop,
                icon = Icons.Default.KeyboardArrowDown,
                label = "DROP",
                color = TetrisTheme.NeonPink,
                modifier = Modifier.size(70.dp)
            )
        }

        // Middle Row: Directional Pad
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TetrisButton(
                onClick = onMoveLeft,
                icon = Icons.Default.KeyboardArrowLeft,
                label = "",
                color = TetrisTheme.NeonCyan,
                modifier = Modifier.size(60.dp)
            )

            TetrisButton(
                onClick = onMoveDown,
                icon = Icons.Default.KeyboardArrowDown,
                label = "",
                color = TetrisTheme.NeonGreen,
                modifier = Modifier.size(60.dp)
            )

            TetrisButton(
                onClick = onMoveRight,
                icon = Icons.Default.KeyboardArrowRight,
                label = "",
                color = TetrisTheme.NeonCyan,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun TetrisButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = if (modifier == Modifier.size(width = 140.dp, height = 50.dp)) {
        RoundedCornerShape(25.dp)
    } else {
        CircleShape
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(8.dp, shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        border = BorderStroke(2.dp, color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(if (label.isEmpty()) 32.dp else 24.dp)
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
