package com.example.tetrisgame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ShooterMenuGame(
    onStartGame: () -> Unit,
    onNavigateTetrisMenu: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "shooter_menu")

    val titleGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_glow"
    )
    
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )
    
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460),
                        Color(0xFF533A7B)
                    )
                )
            )
    ) {
        // Animated background stars
        AnimatedStarsBackground(modifier = Modifier.fillMaxSize())
        
        // Particle effects
        ParticleField(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Animated title with glow effect
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Glow effect behind title
                Text(
                    text = "SHOOTER BLOCKS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Cyan.copy(alpha = titleGlow * 0.3f),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Cyan,
                            blurRadius = 20.dp.value * titleGlow
                        )
                    )
                )
                
                // Main title
                Text(
                    text = "SHOOTER BLOCKS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Cyan,
                            blurRadius = 10.dp.value
                        )
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle with animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) + 
                       slideInVertically(animationSpec = tween(1000))
            ) {
                Text(
                    text = "Defend the Galaxy with Block Ships",
                    fontSize = 18.sp,
                    color = Color.Cyan.copy(alpha = 0.8f),
                    modifier = Modifier.alpha(starTwinkle)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Animated start button
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D4FF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(250.dp)
                    .height(64.dp)
                    .graphicsLayer {
                        scaleX = buttonPulse
                        scaleY = buttonPulse
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🚀",
                        fontSize = 24.sp
                    )
                    Text(
                        "START MISSION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Back to Tetris button
            OutlinedButton(
                onClick = onNavigateTetrisMenu,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp, 
                    Color.Cyan.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = "🎮 Back to Tetris",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AnimatedStarsBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    
    Canvas(modifier = modifier) {
        val stars = (0..50).map {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height
            val size = Random.nextFloat() * 3 + 1
            val alpha = Random.nextFloat() * 0.8f + 0.2f
            StarData(x, y, size, alpha)
        }
        
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(star.x, star.y)
            )
        }
    }
}

@Composable
fun ParticleField(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val particleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_animation"
    )
    
    Canvas(modifier = modifier) {
        val particles = (0..20).map { i ->
            val progress = (particleAnimation + i * 0.05f) % 1f
            val x = progress * size.width
            val y = size.height * 0.8f - (progress * size.height * 0.3f)
            val size = (1f - progress) * 4f + 1f
            val alpha = (1f - progress) * 0.6f
            ParticleData(x, y, size, alpha)
        }
        
        particles.forEach { particle ->
            drawCircle(
                color = Color.Cyan.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

private data class StarData(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float
)

private data class ParticleData(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float
)