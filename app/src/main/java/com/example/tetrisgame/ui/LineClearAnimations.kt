package com.example.tetrisgame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Animation state for line clears
 */
data class LineClearAnimation(
    val lineIndices: List<Int>,
    val startTime: Long = System.currentTimeMillis(),
    val duration: Long = 300
) {
    fun isFinished(): Boolean {
        return System.currentTimeMillis() - startTime >= duration
    }

    fun getProgress(): Float {
        val elapsed = System.currentTimeMillis() - startTime
        return (elapsed.toFloat() / duration).coerceIn(0f, 1f)
    }
}

/**
 * Particle for explosion effect
 */
data class ExplosionParticle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val lifetime: Float = 1f
)

/**
 * Particle system for line clear effects
 */
class ParticleSystem {
    private val particles = mutableListOf<ExplosionParticle>()

    fun emit(x: Float, y: Float, color: Color, count: Int = 20) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 200 + 100
            particles.add(
                ExplosionParticle(
                    x = x,
                    y = y,
                    velocityX = kotlin.math.cos(angle) * speed,
                    velocityY = kotlin.math.sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 4 + 2
                )
            )
        }
    }

    fun update(deltaTime: Float) {
        particles.removeAll { particle ->
            particle.lifetime <= 0
        }

        particles.forEachIndexed { index, particle ->
            particles[index] = particle.copy(
                x = particle.x + particle.velocityX * deltaTime,
                y = particle.y + particle.velocityY * deltaTime,
                velocityY = particle.velocityY + 500 * deltaTime, // Gravity
                lifetime = particle.lifetime - deltaTime
            )
        }
    }

    fun draw(drawScope: DrawScope) {
        particles.forEach { particle ->
            val alpha = particle.lifetime.coerceIn(0f, 1f)
            drawScope.drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }

    fun clear() {
        particles.clear()
    }

    fun hasParticles() = particles.isNotEmpty()
}

/**
 * Draw line clear flash effect
 */
fun DrawScope.drawLineClearFlash(
    lineIndices: List<Int>,
    cellSize: Float,
    boardWidth: Int,
    progress: Float
) {
    lineIndices.forEach { lineIndex ->
        // Flash effect (fade out)
        val alpha = 1f - progress
        val y = lineIndex * cellSize

        // White flash
        drawRect(
            color = Color.White.copy(alpha = alpha * 0.5f),
            topLeft = Offset(0f, y),
            size = Size(boardWidth * cellSize, cellSize)
        )

        // Colored outline
        val lineColor = when {
            lineIndices.size >= 4 -> Color(0xFFFF00FF) // Magenta for Tetris!
            lineIndices.size >= 3 -> Color(0xFF00FF00) // Green for triple
            lineIndices.size >= 2 -> Color(0xFF00FFFF) // Cyan for double
            else -> Color(0xFFFFFF00) // Yellow for single
        }

        // Pulsing border
        val borderWidth = (1f - progress) * 4f
        drawRect(
            color = lineColor.copy(alpha = alpha),
            topLeft = Offset(0f, y),
            size = Size(boardWidth * cellSize, borderWidth)
        )
        drawRect(
            color = lineColor.copy(alpha = alpha),
            topLeft = Offset(0f, y + cellSize - borderWidth),
            size = Size(boardWidth * cellSize, borderWidth)
        )
    }
}

/**
 * Shake effect composable
 */
@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

class ShakeController {
    private var shakeOffset by mutableStateOf(Offset.Zero)
    private var isShaking by mutableStateOf(false)

    fun getOffset() = shakeOffset

    suspend fun shake(intensity: Float = 10f, duration: Long = 100) {
        if (isShaking) return
        isShaking = true

        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
            val magnitude = intensity * (1f - progress)

            shakeOffset = Offset(
                x = (Random.nextFloat() - 0.5f) * magnitude * 2,
                y = (Random.nextFloat() - 0.5f) * magnitude * 2
            )
            delay(16) // ~60 FPS
        }

        shakeOffset = Offset.Zero
        isShaking = false
    }
}
