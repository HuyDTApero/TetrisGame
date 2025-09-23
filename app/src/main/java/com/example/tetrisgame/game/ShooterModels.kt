package com.example.tetrisgame.game

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt
import kotlin.random.Random

// Space Shooter game board dimensions
const val SHOOTER_BOARD_WIDTH = 12
const val SHOOTER_BOARD_HEIGHT = 20

// Game entities
enum class EntityType {
    PLAYER, ENEMY, BULLET, POWER_UP
}

// Enemy types with different shapes and behaviors
enum class EnemyType {
    SCOUT, FIGHTER, CRUISER, MOTHERSHIP
}

// Player spaceship
data class Player(
    val x: Float,
    val y: Float,
    val health: Int = 3,
    val maxHealth: Int = 3,
    val fireRate: Long = 500L, // milliseconds between shots
    val lastShotTime: Long = 0L
) {
    fun moveLeft(): Player = copy(x = maxOf(0f, x - 1f))
    fun moveRight(): Player = copy(x = minOf(SHOOTER_BOARD_WIDTH - 1f, x + 1f))
    fun moveUp(): Player = copy(y = maxOf(SHOOTER_BOARD_HEIGHT * 0.8f, y - 1f))
    fun moveDown(): Player = copy(y = minOf(SHOOTER_BOARD_HEIGHT - 1f, y + 1f))
    fun takeDamage(): Player = copy(health = maxOf(0, health - 1))
    fun heal(): Player = copy(health = minOf(maxHealth, health + 1))
    fun canShoot(currentTime: Long): Boolean = currentTime - lastShotTime >= fireRate
    fun shoot(currentTime: Long): Player = copy(lastShotTime = currentTime)
}

// Enemy spaceship
data class Enemy(
    val id: Int,
    val type: EnemyType,
    val x: Float,
    val y: Float,
    val health: Int,
    val maxHealth: Int,
    val speed: Float,
    val color: Color,
    val lastShotTime: Long = 0L,
    val fireRate: Long = 1000L
) {
    fun moveDown(): Enemy = copy(y = y + speed)
    fun moveLeft(): Enemy = copy(x = maxOf(0f, x - speed))
    fun moveRight(): Enemy = copy(x = minOf(SHOOTER_BOARD_WIDTH - 1f, x + speed))
    fun takeDamage(): Enemy = copy(health = maxOf(0, health - 1))
    fun canShoot(currentTime: Long): Boolean = currentTime - lastShotTime >= fireRate
    fun shoot(currentTime: Long): Enemy = copy(lastShotTime = currentTime)
    
    companion object {
        fun createEnemy(id: Int, type: EnemyType, x: Float, y: Float): Enemy {
            return when (type) {
                EnemyType.SCOUT -> Enemy(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    health = 1,
                    maxHealth = 1,
                    speed = 0.15f, // Chậm hơn nhiều
                    color = Color(0xFF00FF88), // Xanh lá neon
                    fireRate = 2000L
                )
                EnemyType.FIGHTER -> Enemy(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    health = 2,
                    maxHealth = 2,
                    speed = 0.12f,
                    color = Color(0xFFFF6B35), // Cam
                    fireRate = 1500L
                )
                EnemyType.CRUISER -> Enemy(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    health = 4,
                    maxHealth = 4,
                    speed = 0.08f,
                    color = Color(0xFF8A2BE2), // Tím
                    fireRate = 2500L
                )
                EnemyType.MOTHERSHIP -> Enemy(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    health = 8,
                    maxHealth = 8,
                    speed = 0.05f,
                    color = Color(0xFFDC143C), // Đỏ đậm
                    fireRate = 1000L
                )
            }
        }
    }
}

// Bullet projectile
data class Bullet(
    val id: Int,
    val x: Float,
    val y: Float,
    val isPlayerBullet: Boolean,
    val speed: Float,
    val damage: Int = 1,
    val color: Color
) {
    fun moveUp(): Bullet = copy(y = y - speed)
    fun moveDown(): Bullet = copy(y = y + speed)
    
    companion object {
        fun createPlayerBullet(id: Int, x: Float, y: Float): Bullet {
            return Bullet(
                id = id,
                x = x,
                y = y,
                isPlayerBullet = true,
                speed = 0.8f, // Chậm hơn
                damage = 1,
                color = Color(0xFF00FFFF) // Cyan sáng
            )
        }
        
        fun createEnemyBullet(id: Int, x: Float, y: Float, enemyType: EnemyType): Bullet {
            val (speed, damage, color) = when (enemyType) {
                EnemyType.SCOUT -> Triple(0.3f, 1, Color(0xFFFF6B6B))
                EnemyType.FIGHTER -> Triple(0.4f, 1, Color(0xFFFFB347))
                EnemyType.CRUISER -> Triple(0.25f, 2, Color(0xFFB19CD9))
                EnemyType.MOTHERSHIP -> Triple(0.5f, 3, Color(0xFFDC143C))
            }
            
            return Bullet(
                id = id,
                x = x,
                y = y,
                isPlayerBullet = false,
                speed = speed,
                damage = damage,
                color = color
            )
        }
    }
}

// Power-up items
data class PowerUp(
    val id: Int,
    val type: PowerUpType,
    val x: Float,
    val y: Float,
    val color: Color
) {
    fun moveDown(): PowerUp = copy(y = y + 0.08f) // Chậm hơn
    
    enum class PowerUpType {
        HEALTH, RAPID_FIRE, SHIELD, MULTI_SHOT
    }
    
    companion object {
        fun createRandomPowerUp(id: Int, x: Float, y: Float): PowerUp {
            val type = PowerUpType.values().random()
            val color = when (type) {
                PowerUpType.HEALTH -> Color.Green
                PowerUpType.RAPID_FIRE -> Color.Cyan
                PowerUpType.SHIELD -> Color.Blue
                PowerUpType.MULTI_SHOT -> Color.Magenta
            }
            return PowerUp(id, type, x, y, color)
        }
    }
}

// Particle effect for explosions
data class ShooterParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val life: Float,
    val maxLife: Float,
    val color: Color
) {
    fun update(): ShooterParticle {
        return copy(
            x = x + velocityX,
            y = y + velocityY,
            life = life - 1f
        )
    }
    
    val isAlive: Boolean get() = life > 0f
    val alpha: Float get() = life / maxLife
}

// Collision detection utilities
object CollisionUtils {
    fun checkCollision(bullet: Bullet, enemy: Enemy): Boolean {
        return bullet.x >= enemy.x - 0.7f && 
               bullet.x <= enemy.x + 0.7f &&
               bullet.y >= enemy.y - 0.7f && 
               bullet.y <= enemy.y + 0.7f
    }
    
    fun checkCollision(bullet: Bullet, player: Player): Boolean {
        return bullet.x >= player.x - 0.7f && 
               bullet.x <= player.x + 0.7f &&
               bullet.y >= player.y - 0.7f && 
               bullet.y <= player.y + 0.7f
    }
    
    fun checkCollision(enemy: Enemy, player: Player): Boolean {
        return enemy.x >= player.x - 1.5f && 
               enemy.x <= player.x + 1.5f &&
               enemy.y >= player.y - 1.5f && 
               enemy.y <= player.y + 1.5f
    }
    
    fun checkCollision(powerUp: PowerUp, player: Player): Boolean {
        return powerUp.x >= player.x - 0.7f && 
               powerUp.x <= player.x + 0.7f &&
               powerUp.y >= player.y - 0.7f && 
               powerUp.y <= player.y + 0.7f
    }
}

// Main game state
data class ShooterGameState(
    val player: Player = Player(
        x = SHOOTER_BOARD_WIDTH / 2f,
        y = SHOOTER_BOARD_HEIGHT - 2f
    ),
    val enemies: List<Enemy> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val powerUps: List<PowerUp> = emptyList(),
    val particles: List<ShooterParticle> = emptyList(),
    val score: Int = 0,
    val level: Int = 1,
    val wave: Int = 1,
    val enemiesDestroyed: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val gameStartTime: Long = System.currentTimeMillis(),
    val lastEnemySpawnTime: Long = 0L,
    val lastPowerUpSpawnTime: Long = 0L,
    val nextId: Int = 0,
    val playerFireRate: Long = 500L,
    val hasShield: Boolean = false,
    val shieldTime: Long = 0L,
    val rapidFireActive: Boolean = false,
    val rapidFireTime: Long = 0L,
    val multiShotActive: Boolean = false,
    val multiShotTime: Long = 0L
) {
    val currentTime: Long get() = System.currentTimeMillis()
    val gameTime: Long get() = currentTime - gameStartTime
    
    fun calculateLevel(): Int {
        return (score / 1000) + 1
    }
    
    fun shouldSpawnEnemy(): Boolean {
        val spawnInterval = maxOf(2000L, 5000L - (level * 300L)) // Chậm hơn
        return currentTime - lastEnemySpawnTime >= spawnInterval
    }
    
    fun shouldSpawnPowerUp(): Boolean {
        return currentTime - lastPowerUpSpawnTime >= 10000L && Random.nextFloat() < 0.4f
    }
    
    fun isShieldActive(): Boolean {
        return hasShield && currentTime - shieldTime < 10000L // 10 seconds
    }
    
    fun isRapidFireActive(): Boolean {
        return rapidFireActive && currentTime - rapidFireTime < 8000L // 8 seconds
    }
    
    fun isMultiShotActive(): Boolean {
        return multiShotActive && currentTime - multiShotTime < 6000L // 6 seconds
    }
    
    fun generateNextId(): Int = nextId + 1
}
