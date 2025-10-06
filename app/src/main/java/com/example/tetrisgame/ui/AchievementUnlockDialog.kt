package com.example.tetrisgame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tetrisgame.data.Achievement
import com.example.tetrisgame.data.AchievementRarity
import kotlinx.coroutines.delay

@Composable
fun AchievementUnlockDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iconScale by rememberInfiniteTransition(label = "icon").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(3000) // Show for 3 seconds
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .scale(scale)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                getRarityColor(achievement.rarity),
                                getRarityColor(achievement.rarity).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "🎉 ACHIEVEMENT UNLOCKED! 🎉",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon with animation
                    Text(
                        text = achievement.icon,
                        fontSize = 80.sp,
                        modifier = Modifier.scale(iconScale),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Achievement Name
                    Text(
                        text = achievement.name.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = getRarityColor(achievement.rarity),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = achievement.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rarity
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐".repeat(achievement.rarity.stars),
                            fontSize = 16.sp,
                            color = getRarityColor(achievement.rarity)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = achievement.rarity.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = getRarityColor(achievement.rarity)
                        )
                    }
                }
            }

            // Confetti particles (simple version)
            ConfettiEffect()
        }
    }
}

@Composable
private fun ConfettiEffect() {
    val particles = remember {
        List(20) {
            ConfettiParticle(
                x = (0..300).random().dp,
                y = (-50..0).random().dp,
                color = listOf(
                    Color(0xFFFF006E),
                    Color(0xFF00D4FF),
                    Color(0xFF00FF41),
                    Color(0xFFFFD700),
                    Color(0xFFBF40BF)
                ).random()
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val offsetY by rememberInfiniteTransition(label = "confetti").animateFloat(
                initialValue = particle.y.value,
                targetValue = particle.y.value + 400,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "offsetY"
            )

            Box(
                modifier = Modifier
                    .offset(x = particle.x, y = offsetY.dp)
                    .size(8.dp)
                    .background(particle.color, RoundedCornerShape(2.dp))
            )
        }
    }
}

private data class ConfettiParticle(
    val x: androidx.compose.ui.unit.Dp,
    val y: androidx.compose.ui.unit.Dp,
    val color: Color
)

private fun getRarityColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> Color(0xFF888888)
        AchievementRarity.RARE -> Color(0xFF00D4FF)
        AchievementRarity.EPIC -> Color(0xFFBF40BF)
        AchievementRarity.LEGENDARY -> Color(0xFFFFD700)
    }
}
