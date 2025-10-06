package com.example.tetrisgame.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class MatrixDrop(
    var x: Float,
    var y: Float,
    var speed: Float,
    var character: String,
    var alpha: Float = 1f
)

data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var alpha: Float,
    var size: Float
)

@Composable
fun rememberAnimationState(): AnimationState {
    val animationTime = remember { mutableFloatStateOf(0f) }
    val matrixDrops = remember { mutableStateOf(generateMatrixDrops()) }
    val particles = remember { mutableStateOf(generateParticles()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(50) // 20 FPS
            animationTime.floatValue += 0.05f

            matrixDrops.value = matrixDrops.value.map { drop ->
                drop.copy(
                    y = drop.y + drop.speed,
                    alpha = if (drop.y > 800) 0f else (1f - drop.y / 800f).coerceAtLeast(0.1f)
                )
            }.filter { it.y < 800f }.let { drops ->
                if (Random.nextFloat() < 0.1f) {
                    drops + MatrixDrop(
                        x = Random.nextFloat() * 400f,
                        y = 0f,
                        speed = Random.nextFloat() * 3f + 1f,
                        character = listOf("0", "1", "テ", "ト", "リ", "ス")[Random.nextInt(6)]
                    )
                } else drops
            }

            particles.value = particles.value.map { particle ->
                particle.copy(
                    x = particle.x + particle.velocityX,
                    y = particle.y + particle.velocityY,
                    alpha = (particle.alpha - 0.01f).coerceAtLeast(0f)
                )
            }.filter { it.alpha > 0f }.let { currentParticles ->
                if (Random.nextFloat() < 0.3f) {
                    currentParticles + Particle(
                        x = Random.nextFloat() * 400f,
                        y = Random.nextFloat() * 800f,
                        velocityX = Random.nextFloat() * 2f - 1f,
                        velocityY = Random.nextFloat() * 2f - 1f,
                        alpha = 1f,
                        size = Random.nextFloat() * 3f + 1f
                    )
                } else currentParticles
            }
        }
    }

    return AnimationState(
        time = animationTime.floatValue,
        matrixDrops = matrixDrops.value,
        particles = particles.value
    )
}

data class AnimationState(
    val time: Float,
    val matrixDrops: List<MatrixDrop>,
    val particles: List<Particle>
)

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    theme: com.example.tetrisgame.data.models.LevelTheme? = null
) {
    val animationState = rememberAnimationState()

    Canvas(modifier = modifier) {
        drawAnimatedBackground(
            theme = theme,
            time = animationState.time,
            matrixDrops = animationState.matrixDrops,
            particles = animationState.particles
        )
    }
}

private fun DrawScope.drawAnimatedBackground(
    time: Float,
    matrixDrops: List<MatrixDrop>,
    particles: List<Particle>,
    theme: com.example.tetrisgame.data.models.LevelTheme? = null
) {
    val width = size.width
    val height = size.height

    val angle = (sin(time * 0.5f) + 1f) / 2f

    // Background based on theme
    if (theme?.showGradient == true) {
        // Gradient background for Speed mode
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.backgroundColor,
                    theme.primaryColor.copy(alpha = 0.3f * angle),
                    theme.secondaryColor.copy(alpha = 0.2f * (1f - angle))
                ),
                startY = 0f,
                endY = height
            ),
            size = size
        )
    } else {
        // Default dark background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme?.backgroundColor ?: Color(0xFF000211),
                    (theme?.backgroundColor ?: Color(0xFF00212A)).copy(alpha = 0.85f + 0.1f * angle),
                    (theme?.backgroundColor ?: Color(0xFF0D233A)).copy(alpha = 0.8f * (1f - angle) + 0.6f * angle)
                ),
                startY = 0f,
                endY = height
            ),
            size = size
        )
    }

    // Particles if enabled
    if (theme == null || theme.showParticles) {
        particles.forEach { particle ->
            val pulseFactor = (sin(time * 2f + particle.x * 0.01f) + 1f) / 2f
            val particleColor = theme?.primaryColor ?: Color.Cyan
            drawCircle(
                color = particleColor.copy(alpha = (0.15f * particle.alpha + 0.05f) * pulseFactor),
                center = Offset(particle.x % width, particle.y % height),
                radius = particle.size * (3f + pulseFactor * 2f)
            )

            // Add inner glow
            drawCircle(
                color = Color.White.copy(alpha = 0.08f * particle.alpha * pulseFactor),
                center = Offset(particle.x % width, particle.y % height),
                radius = particle.size * 1.5f
            )
        }
    }

    // Matrix rain if enabled
    if (theme?.showMatrixRain == true) {
        matrixDrops.forEach { drop ->
            val dropAlpha = (drop.alpha * 0.6f + 0.15f).coerceIn(0f, 1f)
            val dropWidth = 8f
            val dropHeight = 12f
            val matrixColor = theme.primaryColor

            drawRect(
                color = matrixColor.copy(alpha = dropAlpha),
                topLeft = Offset((drop.x % width) - dropWidth / 2, (drop.y % height) - dropHeight / 2),
                size = Size(dropWidth, dropHeight)
            )

            drawRect(
                color = matrixColor.copy(alpha = dropAlpha * 0.3f),
                topLeft = Offset((drop.x % width) - dropWidth, (drop.y % height) - dropHeight),
                size = Size(dropWidth * 2, dropHeight * 2)
            )
        }
    }

    // Add subtle grid overlay that pulses (always shown but subtle)
    val gridAlpha = (sin(time * 1.2f) + 1f) / 2f * 0.05f
    val gridColor = theme?.primaryColor ?: Color.Cyan
    for (i in 0 until width.toInt() step 40) {
        drawLine(
            color = gridColor.copy(alpha = gridAlpha),
            start = Offset(i.toFloat(), 0f),
            end = Offset(i.toFloat(), height),
            strokeWidth = 1f
        )
    }
    for (i in 0 until height.toInt() step 40) {
        drawLine(
            color = gridColor.copy(alpha = gridAlpha),
            start = Offset(0f, i.toFloat()),
            end = Offset(width, i.toFloat()),
            strokeWidth = 1f
        )
    }
}


private fun generateMatrixDrops(count: Int = 18): List<MatrixDrop> {
    return List(count) {
        MatrixDrop(
            x = Random.nextFloat() * 400f,
            y = Random.nextFloat() * 800f,
            speed = Random.nextFloat() * 3f + 1f,
            character = listOf("0", "1", "テ", "ト", "リ", "ス")[Random.nextInt(6)]
        )
    }
}

private fun generateParticles(count: Int = 24): List<Particle> {
    return List(count) {
        Particle(
            x = Random.nextFloat() * 400f,
            y = Random.nextFloat() * 800f,
            velocityX = Random.nextFloat() * 2f - 1f,
            velocityY = Random.nextFloat() * 2f - 1f,
            alpha = Random.nextFloat().coerceIn(0.3f, 1f),
            size = Random.nextFloat() * 3f + 1f
        )
    }
}