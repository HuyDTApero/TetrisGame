package com.example.tetrisgame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.game.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun ShooterGameBoard(
    gameState: ShooterGameState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cellWidth = size.width / SHOOTER_BOARD_WIDTH
        val cellHeight = size.height / SHOOTER_BOARD_HEIGHT

        // Draw background grid
        drawGameGrid(cellWidth, cellHeight)

        // Draw player
        drawPlayer(gameState.player, cellWidth, cellHeight)

        // Draw enemies
        gameState.enemies.forEach { enemy ->
            drawEnemy(enemy, cellWidth, cellHeight)
        }

        // Draw bullets
        gameState.bullets.forEach { bullet ->
            drawBullet(bullet, cellWidth, cellHeight)
        }

        // Draw power-ups
        gameState.powerUps.forEach { powerUp ->
            drawPowerUp(powerUp, cellWidth, cellHeight)
        }

        // Draw particles
        gameState.particles.forEach { particle ->
            drawParticle(particle, cellWidth, cellHeight)
        }
    }
}

private fun DrawScope.drawGameGrid(cellWidth: Float, cellHeight: Float) {
    // Draw vertical lines
    for (i in 0..SHOOTER_BOARD_WIDTH) {
        val x = i * cellWidth
        drawLine(
            color = Color.Cyan.copy(alpha = 0.1f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Draw horizontal lines
    for (i in 0..SHOOTER_BOARD_HEIGHT) {
        val y = i * cellHeight
        drawLine(
            color = Color.Cyan.copy(alpha = 0.1f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawPlayer(player: Player, cellWidth: Float, cellHeight: Float) {
    val centerX = player.x * cellWidth + cellWidth / 2
    val centerY = player.y * cellHeight + cellHeight / 2

    // Draw player ship - sleek fighter design
    val shipSize = minOf(cellWidth, cellHeight) * 0.8f

    // Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = shipSize * 0.6f,
        center = Offset(centerX + 2, centerY + 2)
    )

    // Main ship body - sleek fighter shape
    val path = Path().apply {
        moveTo(centerX, centerY - shipSize * 0.6f) // Nose
        lineTo(centerX - shipSize * 0.3f, centerY - shipSize * 0.2f) // Left wing front
        lineTo(centerX - shipSize * 0.5f, centerY + shipSize * 0.1f) // Left wing middle
        lineTo(centerX - shipSize * 0.4f, centerY + shipSize * 0.3f) // Left engine
        lineTo(centerX, centerY + shipSize * 0.4f) // Center back
        lineTo(centerX + shipSize * 0.4f, centerY + shipSize * 0.3f) // Right engine
        lineTo(centerX + shipSize * 0.5f, centerY + shipSize * 0.1f) // Right wing middle
        lineTo(centerX + shipSize * 0.3f, centerY - shipSize * 0.2f) // Right wing front
        close()
    }
    drawPath(path, color = Color(0xFF00FFFF)) // Bright cyan

    // Cockpit
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = shipSize * 0.15f,
        center = Offset(centerX, centerY - shipSize * 0.1f)
    )

    // Engine glow
    drawCircle(
        color = Color(0xFF00FFFF).copy(alpha = 0.4f),
        radius = shipSize * 0.3f,
        center = Offset(centerX - shipSize * 0.35f, centerY + shipSize * 0.35f)
    )
    drawCircle(
        color = Color(0xFF00FFFF).copy(alpha = 0.4f),
        radius = shipSize * 0.3f,
        center = Offset(centerX + shipSize * 0.35f, centerY + shipSize * 0.35f)
    )

    // Wing tips glow
    drawCircle(
        color = Color(0xFF00FFFF).copy(alpha = 0.6f),
        radius = shipSize * 0.1f,
        center = Offset(centerX - shipSize * 0.5f, centerY + shipSize * 0.1f)
    )
    drawCircle(
        color = Color(0xFF00FFFF).copy(alpha = 0.6f),
        radius = shipSize * 0.1f,
        center = Offset(centerX + shipSize * 0.5f, centerY + shipSize * 0.1f)
    )
}

private fun DrawScope.drawEnemy(enemy: Enemy, cellWidth: Float, cellHeight: Float) {
    val centerX = enemy.x * cellWidth + cellWidth / 2
    val centerY = enemy.y * cellHeight + cellHeight / 2
    val size = minOf(cellWidth, cellHeight) * 0.7f

    // Enemy spaceship design based on type
    when (enemy.type) {
        EnemyType.SCOUT -> {
            // Small agile fighter - pointed nose
            val path = Path().apply {
                moveTo(centerX, centerY - size * 0.6f) // Nose
                lineTo(centerX - size * 0.3f, centerY - size * 0.2f) // Left wing front
                lineTo(centerX - size * 0.4f, centerY + size * 0.3f) // Left wing back
                lineTo(centerX - size * 0.2f, centerY + size * 0.4f) // Left engine
                lineTo(centerX, centerY + size * 0.5f) // Center back
                lineTo(centerX + size * 0.2f, centerY + size * 0.4f) // Right engine
                lineTo(centerX + size * 0.4f, centerY + size * 0.3f) // Right wing back
                lineTo(centerX + size * 0.3f, centerY - size * 0.2f) // Right wing front
                close()
            }
            drawPath(path, color = enemy.color)
            // Engine glow
            drawCircle(
                color = enemy.color.copy(alpha = 0.4f),
                radius = size * 0.3f,
                center = Offset(centerX, centerY + size * 0.4f)
            )
        }
        EnemyType.FIGHTER -> {
            // Medium fighter - angular design
            val path = Path().apply {
                moveTo(centerX, centerY - size * 0.5f) // Nose
                lineTo(centerX - size * 0.4f, centerY - size * 0.1f) // Left wing front
                lineTo(centerX - size * 0.5f, centerY + size * 0.2f) // Left wing middle
                lineTo(centerX - size * 0.3f, centerY + size * 0.4f) // Left engine
                lineTo(centerX, centerY + size * 0.5f) // Center back
                lineTo(centerX + size * 0.3f, centerY + size * 0.4f) // Right engine
                lineTo(centerX + size * 0.5f, centerY + size * 0.2f) // Right wing middle
                lineTo(centerX + size * 0.4f, centerY - size * 0.1f) // Right wing front
                close()
            }
            drawPath(path, color = enemy.color)
            // Cockpit
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = size * 0.15f,
                center = Offset(centerX, centerY - size * 0.2f)
            )
        }
        EnemyType.CRUISER -> {
            // Large cruiser - wide and imposing
            val path = Path().apply {
                moveTo(centerX, centerY - size * 0.4f) // Nose
                lineTo(centerX - size * 0.6f, centerY - size * 0.1f) // Left front wing
                lineTo(centerX - size * 0.7f, centerY + size * 0.1f) // Left wing tip
                lineTo(centerX - size * 0.5f, centerY + size * 0.3f) // Left engine
                lineTo(centerX - size * 0.3f, centerY + size * 0.4f) // Left back
                lineTo(centerX, centerY + size * 0.5f) // Center back
                lineTo(centerX + size * 0.3f, centerY + size * 0.4f) // Right back
                lineTo(centerX + size * 0.5f, centerY + size * 0.3f) // Right engine
                lineTo(centerX + size * 0.7f, centerY + size * 0.1f) // Right wing tip
                lineTo(centerX + size * 0.6f, centerY - size * 0.1f) // Right front wing
                close()
            }
            drawPath(path, color = enemy.color)
            // Engine glow
            drawCircle(
                color = enemy.color.copy(alpha = 0.3f),
                radius = size * 0.4f,
                center = Offset(centerX - size * 0.4f, centerY + size * 0.35f)
            )
            drawCircle(
                color = enemy.color.copy(alpha = 0.3f),
                radius = size * 0.4f,
                center = Offset(centerX + size * 0.4f, centerY + size * 0.35f)
            )
        }
        EnemyType.MOTHERSHIP -> {
            // Massive mothership - complex design
            val path = Path().apply {
                moveTo(centerX, centerY - size * 0.3f) // Front
                lineTo(centerX - size * 0.8f, centerY - size * 0.1f) // Left front
                lineTo(centerX - size * 0.9f, centerY + size * 0.2f) // Left side
                lineTo(centerX - size * 0.7f, centerY + size * 0.4f) // Left engine
                lineTo(centerX - size * 0.4f, centerY + size * 0.5f) // Left back
                lineTo(centerX, centerY + size * 0.6f) // Center back
                lineTo(centerX + size * 0.4f, centerY + size * 0.5f) // Right back
                lineTo(centerX + size * 0.7f, centerY + size * 0.4f) // Right engine
                lineTo(centerX + size * 0.9f, centerY + size * 0.2f) // Right side
                lineTo(centerX + size * 0.8f, centerY - size * 0.1f) // Right front
                close()
            }
            drawPath(path, color = enemy.color)
            // Central command center
            drawCircle(
                color = Color.Yellow.copy(alpha = 0.8f),
                radius = size * 0.2f,
                center = Offset(centerX, centerY + size * 0.1f)
            )
            // Engine glow
            drawCircle(
                color = enemy.color.copy(alpha = 0.2f),
                radius = size * 0.6f,
                center = Offset(centerX - size * 0.5f, centerY + size * 0.45f)
            )
            drawCircle(
                color = enemy.color.copy(alpha = 0.2f),
                radius = size * 0.6f,
                center = Offset(centerX + size * 0.5f, centerY + size * 0.45f)
            )
        }
    }

    // Health indicator for damaged enemies
    if (enemy.health < enemy.maxHealth) {
        val healthWidth = size * (enemy.health.toFloat() / enemy.maxHealth)
        drawRect(
            color = Color.Red,
            topLeft = Offset(centerX - size * 0.4f, centerY - size * 0.6f),
            size = androidx.compose.ui.geometry.Size(healthWidth, 3.dp.toPx())
        )
    }
}

private fun DrawScope.drawBullet(bullet: Bullet, cellWidth: Float, cellHeight: Float) {
    val centerX = bullet.x * cellWidth + cellWidth / 2
    val centerY = bullet.y * cellHeight + cellHeight / 2
    val size = minOf(cellWidth, cellHeight) * 0.2f

    if (bullet.isPlayerBullet) {
        // Player bullet - elongated with glow
        drawRect(
            color = bullet.color,
            topLeft = Offset(centerX - size * 0.1f, centerY - size * 0.8f),
            size = androidx.compose.ui.geometry.Size(size * 0.2f, size * 1.6f)
        )
        drawCircle(
            color = bullet.color.copy(alpha = 0.6f),
            radius = size * 0.8f,
            center = Offset(centerX, centerY)
        )
    } else {
        // Enemy bullet - round with trail
        drawCircle(
            color = bullet.color,
            radius = size * 0.4f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = bullet.color.copy(alpha = 0.4f),
            radius = size * 0.8f,
            center = Offset(centerX, centerY)
        )
    }
}

private fun DrawScope.drawPowerUp(powerUp: PowerUp, cellWidth: Float, cellHeight: Float) {
    val centerX = powerUp.x * cellWidth + cellWidth / 2
    val centerY = powerUp.y * cellHeight + cellHeight / 2
    val size = minOf(cellWidth, cellHeight) * 0.6f

    // Pulsing effect
    val pulseAlpha = (sin(System.currentTimeMillis() * 0.01f) * 0.3f + 0.7f).coerceIn(0f, 1f)

    when (powerUp.type) {
        PowerUp.PowerUpType.HEALTH -> {
            // Heart shape
            drawCircle(
                color = powerUp.color.copy(alpha = pulseAlpha),
                radius = size * 0.4f,
                center = Offset(centerX, centerY)
            )
        }
        PowerUp.PowerUpType.RAPID_FIRE -> {
            // Lightning bolt
            drawRect(
                color = powerUp.color.copy(alpha = pulseAlpha),
                topLeft = Offset(centerX - size * 0.1f, centerY - size * 0.4f),
                size = androidx.compose.ui.geometry.Size(size * 0.2f, size * 0.8f)
            )
        }
        PowerUp.PowerUpType.SHIELD -> {
            // Shield shape
            drawCircle(
                color = powerUp.color.copy(alpha = pulseAlpha),
                radius = size * 0.5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        PowerUp.PowerUpType.MULTI_SHOT -> {
            // Star shape
            drawCircle(
                color = powerUp.color.copy(alpha = pulseAlpha),
                radius = size * 0.3f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

private fun DrawScope.drawParticle(particle: ShooterParticle, cellWidth: Float, cellHeight: Float) {
    val centerX = particle.x * cellWidth + cellWidth / 2
    val centerY = particle.y * cellHeight + cellHeight / 2
    val size = minOf(cellWidth, cellHeight) * 0.1f * particle.alpha

    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = size,
        center = Offset(centerX, centerY)
    )
}

@Composable
fun ShooterGameControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onShoot: () -> Unit,
    onPause: () -> Unit,
    isPaused: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Movement controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Up button
            Button(
                onClick = onMoveUp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Text("↑", fontSize = 20.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Left button
                Button(
                    onClick = onMoveLeft,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(60.dp)
                ) {
                    Text("←", fontSize = 20.sp)
                }

                // Right button
                Button(
                    onClick = onMoveRight,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(60.dp)
                ) {
                    Text("→", fontSize = 20.sp)
                }
            }

            // Down button
            Button(
                onClick = onMoveDown,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Text("↓", fontSize = 20.sp)
            }
        }

        // Action controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shoot button
            Button(
                onClick = onShoot,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Text("FIRE", fontSize = 12.sp, color = Color.White)
            }

            // Pause button
            Button(
                onClick = onPause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) Color.Green else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Text(
                    if (isPaused) "RESUME" else "PAUSE",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }

        }
    }
}

@Composable
fun ShooterGameOverDialog(
    score: Int,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color.Red,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Final Score: $score",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESTART", color = Color.White)
                    }

                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("MENU", color = Color.White)
                    }
                }
            }
        }
    }
}
