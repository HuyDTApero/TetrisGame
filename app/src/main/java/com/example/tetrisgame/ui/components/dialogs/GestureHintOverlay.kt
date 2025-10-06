package com.example.tetrisgame.ui.components.dialogs
import com.example.tetrisgame.ui.theme.TetrisTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GestureHintOverlay(
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(5000) // Auto-dismiss after 5 seconds
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = TetrisTheme.CardBg.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "👆 TOUCH CONTROLS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TetrisTheme.NeonCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                GestureHintItem("⬅️ Swipe Left", "Move left")
                GestureHintItem("➡️ Swipe Right", "Move right")
                GestureHintItem("⬇️ Swipe Down", "Soft drop")
                GestureHintItem("⬆️ Swipe Up", "Hard drop")
                GestureHintItem("👆 Tap", "Rotate")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TetrisTheme.NeonCyan.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GOT IT!",
                        color = TetrisTheme.NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureHintItem(gesture: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = gesture,
            fontSize = 16.sp,
            color = Color.White
        )
        Text(
            text = action,
            fontSize = 14.sp,
            color = TetrisTheme.NeonYellow
        )
    }
}
