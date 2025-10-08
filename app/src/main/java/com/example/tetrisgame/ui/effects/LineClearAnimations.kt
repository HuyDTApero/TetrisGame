package com.example.tetrisgame.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Optimized Animation state for line clears with background processing
 */
data class LineClearAnimation(
    val lineIndices: List<Int>,
    val startTime: Long = System.currentTimeMillis(),
    val duration: Long = 200 // Reduced from 300ms to 200ms for faster gameplay
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
 * Optimized Particle system with object pooling
 */
class OptimizedParticleSystem {
    private val activeParticles = mutableListOf<ExplosionParticle>()
    private val particlePool = mutableListOf<ExplosionParticle>()
    private var maxParticles = 100 // Limit for performance

    fun emit(x: Float, y: Float, color: Color, count: Int = 15) { // Reduced particle count
        val actualCount = minOf(count, maxParticles - activeParticles.size)
        repeat(actualCount) {
            val particle = getPooledParticle() ?: return@repeat
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 150 + 75 // Reduced speed for better performance

            resetParticle(particle, x, y, angle, speed, color)
            activeParticles.add(
                particle.copy(
                    x = x,
                    y = y,
                    velocityX = kotlin.math.cos(angle) * speed,
                    velocityY = kotlin.math.sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 3 + 1.5f, // Smaller particles
                    lifetime = 0.8f // Shorter lifetime
                )
            )
        }
    }

    private fun getPooledParticle(): ExplosionParticle? {
        return if (particlePool.isNotEmpty()) {
            particlePool.removeAt(particlePool.size - 1)
        } else {
            ExplosionParticle(0f, 0f, 0f, 0f, Color.White, 2f, 1f)
        }
    }

    private fun resetParticle(
        particle: ExplosionParticle,
        x: Float, y: Float, angle: Float, speed: Float, color: Color
    ): ExplosionParticle {
        return particle.copy(
            x = x,
            y = y,
            velocityX = kotlin.math.cos(angle) * speed,
            velocityY = kotlin.math.sin(angle) * speed,
            color = color,
            size = Random.nextFloat() * 3 + 1.5f,
            lifetime = 0.8f
        )
    }

    suspend fun updateAsync(deltaTime: Float) = withContext(Dispatchers.Default) {
        // Move expired particles back to pool
        val expiredParticles = activeParticles.filter { it.lifetime <= 0 }
        expiredParticles.forEach { particle ->
            particlePool.add(particle)
        }
        activeParticles.removeAll(expiredParticles)

        // Update remaining particles
        for (i in activeParticles.indices) {
            val particle = activeParticles[i]
            activeParticles[i] = particle.copy(
                x = particle.x + particle.velocityX * deltaTime,
                y = particle.y + particle.velocityY * deltaTime,
                velocityY = particle.velocityY + 400 * deltaTime, // Reduced gravity
                lifetime = particle.lifetime - deltaTime
            )
        }
    }

    fun update(deltaTime: Float) {
        // Synchronous version for immediate updates
        activeParticles.removeAll { particle ->
            if (particle.lifetime <= 0) {
                particlePool.add(particle)
                true
            } else false
        }

        activeParticles.forEachIndexed { index, particle ->
            activeParticles[index] = particle.copy(
                x = particle.x + particle.velocityX * deltaTime,
                y = particle.y + particle.velocityY * deltaTime,
                velocityY = particle.velocityY + 400 * deltaTime,
                lifetime = particle.lifetime - deltaTime
            )
        }
    }

    fun draw(drawScope: DrawScope) {
        // Optimized drawing - batch similar operations
        activeParticles.forEach { particle ->
            val alpha = particle.lifetime.coerceIn(0f, 1f)
            drawScope.drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }

    fun clear() {
        // Move all active particles back to pool
        particlePool.addAll(activeParticles)
        activeParticles.clear()
    }

    fun hasParticles() = activeParticles.isNotEmpty()
}

/**
 * Optimized line clear flash effect with reduced overdraw
 */
fun DrawScope.drawLineClearFlashOptimized(
    lineIndices: List<Int>,
    cellSize: Float,
    boardWidth: Int,
    progress: Float
) {
    if (lineIndices.isEmpty()) return

    val alpha = (1f - progress) * 0.6f // Reduced opacity for better performance

    // Group consecutive lines for batch drawing
    val lineGroups = groupConsecutiveLines(lineIndices)

    lineGroups.forEach { group ->
        val startY = group.first() * cellSize
        val height = group.size * cellSize

        // Single flash rect for consecutive lines
        drawRect(
            color = Color.White.copy(alpha = alpha * 0.3f), // Reduced alpha
            topLeft = Offset(0f, startY),
            size = Size(boardWidth * cellSize, height)
        )

        // Colored border based on line count
        val lineColor = when {
            lineIndices.size >= 4 -> Color(0xFFFF00FF) // Magenta for Tetris!
            lineIndices.size >= 3 -> Color(0xFF00FF00) // Green for triple
            lineIndices.size >= 2 -> Color(0xFF00FFFF) // Cyan for double
            else -> Color(0xFFFFFF00) // Yellow for single
        }

        // Simplified border (top and bottom only)
        val borderWidth = (1f - progress) * 2f // Thinner border
        drawRect(
            color = lineColor.copy(alpha = alpha),
            topLeft = Offset(0f, startY),
            size = Size(boardWidth * cellSize, borderWidth)
        )
        drawRect(
            color = lineColor.copy(alpha = alpha),
            topLeft = Offset(0f, startY + height - borderWidth),
            size = Size(boardWidth * cellSize, borderWidth)
        )
    }
}

// Helper function to group consecutive line indices
private fun groupConsecutiveLines(indices: List<Int>): List<List<Int>> {
    if (indices.isEmpty()) return emptyList()

    val sorted = indices.sorted()
    val groups = mutableListOf<MutableList<Int>>()
    var currentGroup = mutableListOf(sorted[0])

    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1] + 1) {
            currentGroup.add(sorted[i])
        } else {
            groups.add(currentGroup)
            currentGroup = mutableListOf(sorted[i])
        }
    }
    groups.add(currentGroup)

    return groups
}

/**
 * Optimized shake effect with reduced calculations
 */
@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

class ShakeController {
    private var shakeOffset by mutableStateOf(Offset.Zero)
    private var isShaking by mutableStateOf(false)

    fun getOffset() = shakeOffset

    suspend fun shake(intensity: Float = 8f, duration: Long = 80) { // Reduced values
        if (isShaking) return
        isShaking = true

        val startTime = System.currentTimeMillis()
        val frameDelay = 20L // ~50 FPS for shake (reduced from 60)

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
            val magnitude = intensity * (1f - progress)

            shakeOffset = Offset(
                x = (Random.nextFloat() - 0.5f) * magnitude,
                y = (Random.nextFloat() - 0.5f) * magnitude
            )
            delay(frameDelay)
        }

        shakeOffset = Offset.Zero
        isShaking = false
    }
}
