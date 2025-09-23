package com.example.tetrisgame.game

import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

class ShooterEngine {
    
    fun updateGameState(gameState: ShooterGameState): ShooterGameState {
        if (gameState.isGameOver || gameState.isPaused) return gameState
        
        var newState = gameState.copy()
        
        // Update player
        newState = updatePlayer(newState)
        
        // Update enemies
        newState = updateEnemies(newState)
        
        // Update bullets
        newState = updateBullets(newState)
        
        // Update power-ups
        newState = updatePowerUps(newState)
        
        // Update particles
        newState = updateParticles(newState)
        
        // Spawn new enemies
        newState = spawnEnemies(newState)
        
        // Spawn power-ups
        newState = spawnPowerUps(newState)
        
        // Check collisions
        newState = checkCollisions(newState)
        
        // Update level
        newState = newState.copy(level = newState.calculateLevel())
        
        return newState
    }
    
    private fun updatePlayer(gameState: ShooterGameState): ShooterGameState {
        var player = gameState.player
        
        // Handle power-up timeouts
        var newGameState = gameState
        if (!newGameState.isShieldActive()) {
            newGameState = newGameState.copy(hasShield = false, shieldTime = 0L)
        }
        if (!newGameState.isRapidFireActive()) {
            newGameState = newGameState.copy(rapidFireActive = false, rapidFireTime = 0L)
        }
        if (!newGameState.isMultiShotActive()) {
            newGameState = newGameState.copy(multiShotActive = false, multiShotTime = 0L)
        }
        
        return gameState.copy(player = player)
    }
    
    private fun updateEnemies(gameState: ShooterGameState): ShooterGameState {
        val updatedEnemies = gameState.enemies.mapNotNull { enemy ->
            val newEnemy = enemy.moveDown()
            
            // Remove enemies that have moved off screen
            if (newEnemy.y > SHOOTER_BOARD_HEIGHT + 1) {
                null
            } else {
                newEnemy
            }
        }
        
        return gameState.copy(enemies = updatedEnemies)
    }
    
    private fun updateBullets(gameState: ShooterGameState): ShooterGameState {
        val updatedBullets = gameState.bullets.mapNotNull { bullet ->
            val newBullet = if (bullet.isPlayerBullet) {
                bullet.moveUp()
            } else {
                bullet.moveDown()
            }
            
            // Remove bullets that have moved off screen
            if (newBullet.y < -1 || newBullet.y > SHOOTER_BOARD_HEIGHT + 1) {
                null
            } else {
                newBullet
            }
        }
        
        return gameState.copy(bullets = updatedBullets)
    }
    
    private fun updatePowerUps(gameState: ShooterGameState): ShooterGameState {
        val updatedPowerUps = gameState.powerUps.mapNotNull { powerUp ->
            val newPowerUp = powerUp.moveDown()
            
            // Remove power-ups that have moved off screen
            if (newPowerUp.y > SHOOTER_BOARD_HEIGHT + 1) {
                null
            } else {
                newPowerUp
            }
        }
        
        return gameState.copy(powerUps = updatedPowerUps)
    }
    
    private fun updateParticles(gameState: ShooterGameState): ShooterGameState {
        val updatedParticles = gameState.particles.map { it.update() }.filter { it.isAlive }
        return gameState.copy(particles = updatedParticles)
    }
    
    private fun spawnEnemies(gameState: ShooterGameState): ShooterGameState {
        if (!gameState.shouldSpawnEnemy()) return gameState
        
        val enemyTypes = when (gameState.level) {
            in 1..3 -> listOf(EnemyType.SCOUT)
            in 4..6 -> listOf(EnemyType.SCOUT, EnemyType.FIGHTER)
            in 7..10 -> listOf(EnemyType.SCOUT, EnemyType.FIGHTER, EnemyType.CRUISER)
            else -> listOf(EnemyType.SCOUT, EnemyType.FIGHTER, EnemyType.CRUISER, EnemyType.MOTHERSHIP)
        }
        
        val enemyType = enemyTypes.random()
        val x = Random.nextFloat() * (SHOOTER_BOARD_WIDTH - 2) + 1
        val enemy = Enemy.createEnemy(gameState.generateNextId(), enemyType, x, -1f)
        
        return gameState.copy(
            enemies = gameState.enemies + enemy,
            lastEnemySpawnTime = gameState.currentTime,
            nextId = gameState.generateNextId()
        )
    }
    
    private fun spawnPowerUps(gameState: ShooterGameState): ShooterGameState {
        if (!gameState.shouldSpawnPowerUp()) return gameState
        
        val x = Random.nextFloat() * (SHOOTER_BOARD_WIDTH - 1)
        val powerUp = PowerUp.createRandomPowerUp(gameState.generateNextId(), x, -1f)
        
        return gameState.copy(
            powerUps = gameState.powerUps + powerUp,
            lastPowerUpSpawnTime = gameState.currentTime,
            nextId = gameState.generateNextId()
        )
    }
    
    private fun checkCollisions(gameState: ShooterGameState): ShooterGameState {
        var newState = gameState
        
        // Player bullets vs enemies
        val playerBullets = newState.bullets.filter { it.isPlayerBullet }
        val enemies = newState.enemies.toMutableList()
        
        for (bullet in playerBullets) {
            val enemyIndex = enemies.indexOfFirst { 
                CollisionUtils.checkCollision(bullet, it) 
            }
            
            if (enemyIndex != -1) {
                val enemy = enemies[enemyIndex]
                val damagedEnemy = enemy.takeDamage()
                
                if (damagedEnemy.health <= 0) {
                    // Enemy destroyed
                    enemies.removeAt(enemyIndex)
                    newState = newState.copy(
                        bullets = newState.bullets.filter { it.id != bullet.id },
                        enemies = enemies,
                        score = newState.score + when (enemy.type) {
                            EnemyType.SCOUT -> 100
                            EnemyType.FIGHTER -> 250
                            EnemyType.CRUISER -> 500
                            EnemyType.MOTHERSHIP -> 1000
                        },
                        enemiesDestroyed = newState.enemiesDestroyed + 1
                    )
                    
                    // Create explosion particles
                    newState = addExplosionParticles(newState, enemy.x, enemy.y, enemy.color)
                } else {
                    // Enemy damaged
                    enemies[enemyIndex] = damagedEnemy
                    newState = newState.copy(
                        bullets = newState.bullets.filter { it.id != bullet.id },
                        enemies = enemies
                    )
                }
            }
        }
        
        // Enemy bullets vs player
        val enemyBullets = newState.bullets.filter { !it.isPlayerBullet }
        var player = newState.player
        
        for (bullet in enemyBullets) {
            if (CollisionUtils.checkCollision(bullet, player) && !newState.isShieldActive()) {
                println("BULLET HIT! Enemy bullet at (${bullet.x}, ${bullet.y}) vs Player at (${player.x}, ${player.y})")
                println("Player health before: ${player.health}")
                player = player.takeDamage()
                println("Player health after: ${player.health}")
                newState = newState.copy(
                    bullets = newState.bullets.filter { it.id != bullet.id },
                    player = player
                )
                
                // Create hit particles
                newState = addHitParticles(newState, bullet.x, bullet.y, Color.White)
                
                if (player.health <= 0) {
                    newState = newState.copy(isGameOver = true)
                    break
                }
            }
        }
        
        // Enemies vs player (collision damage)
        for (enemy in newState.enemies) {
            if (CollisionUtils.checkCollision(enemy, player) && !newState.isShieldActive()) {
                player = player.takeDamage()
                newState = newState.copy(
                    enemies = newState.enemies.filter { it.id != enemy.id },
                    player = player
                )
                
                // Create explosion particles
                newState = addExplosionParticles(newState, enemy.x, enemy.y, enemy.color)
                
                if (player.health <= 0) {
                    newState = newState.copy(isGameOver = true)
                    break
                }
            }
        }
        
        // Power-ups vs player
        for (powerUp in newState.powerUps) {
            if (CollisionUtils.checkCollision(powerUp, player)) {
                newState = applyPowerUp(newState, powerUp)
                newState = newState.copy(
                    powerUps = newState.powerUps.filter { it.id != powerUp.id }
                )
            }
        }
        
        return newState
    }
    
    private fun applyPowerUp(gameState: ShooterGameState, powerUp: PowerUp): ShooterGameState {
        return when (powerUp.type) {
            PowerUp.PowerUpType.HEALTH -> {
                gameState.copy(
                    player = gameState.player.heal(),
                    score = gameState.score + 50
                )
            }
            PowerUp.PowerUpType.RAPID_FIRE -> {
                gameState.copy(
                    rapidFireActive = true,
                    rapidFireTime = gameState.currentTime,
                    score = gameState.score + 100
                )
            }
            PowerUp.PowerUpType.SHIELD -> {
                gameState.copy(
                    hasShield = true,
                    shieldTime = gameState.currentTime,
                    score = gameState.score + 150
                )
            }
            PowerUp.PowerUpType.MULTI_SHOT -> {
                gameState.copy(
                    multiShotActive = true,
                    multiShotTime = gameState.currentTime,
                    score = gameState.score + 200
                )
            }
        }
    }
    
    private fun addExplosionParticles(gameState: ShooterGameState, x: Float, y: Float, color: Color): ShooterGameState {
        val particles = (0..8).map { i ->
            val angle = (i * 40f) * kotlin.math.PI.toFloat() / 180f
            val speed = Random.nextFloat() * 2f + 1f
            ShooterParticle(
                id = gameState.generateNextId() + i,
                x = x,
                y = y,
                velocityX = (cos(angle) * speed),
                velocityY = (sin(angle) * speed),
                life = 30f,
                maxLife = 30f,
                color = color
            )
        }
        
        return gameState.copy(
            particles = gameState.particles + particles,
            nextId = gameState.generateNextId() + 8
        )
    }
    
    private fun addHitParticles(gameState: ShooterGameState, x: Float, y: Float, color: Color): ShooterGameState {
        val particles = (0..4).map { i ->
            val angle = Random.nextFloat() * 360f * kotlin.math.PI.toFloat() / 180f
            val speed = Random.nextFloat() * 1.5f + 0.5f
            ShooterParticle(
                id = gameState.generateNextId() + i,
                x = x,
                y = y,
                velocityX = (cos(angle) * speed),
                velocityY = (sin(angle) * speed),
                life = 15f,
                maxLife = 15f,
                color = color
            )
        }
        
        return gameState.copy(
            particles = gameState.particles + particles,
            nextId = gameState.generateNextId() + 4
        )
    }
    
    // Player movement actions
    fun movePlayerLeft(gameState: ShooterGameState): ShooterGameState {
        return gameState.copy(player = gameState.player.moveLeft())
    }
    
    fun movePlayerRight(gameState: ShooterGameState): ShooterGameState {
        return gameState.copy(player = gameState.player.moveRight())
    }
    
    fun movePlayerUp(gameState: ShooterGameState): ShooterGameState {
        return gameState.copy(player = gameState.player.moveUp())
    }
    
    fun movePlayerDown(gameState: ShooterGameState): ShooterGameState {
        return gameState.copy(player = gameState.player.moveDown())
    }
    
    fun shootBullet(gameState: ShooterGameState): ShooterGameState {
        // Determine fire rate based on power-ups
        val currentFireRate = if (gameState.isRapidFireActive()) {
            150L // Rapid fire
        } else {
            gameState.player.fireRate
        }
        
        if (!gameState.player.canShoot(gameState.currentTime) || 
            gameState.currentTime - gameState.player.lastShotTime < currentFireRate) {
            return gameState
        }
        
        val bullets = mutableListOf<Bullet>()
        
        // Create main bullet
        val mainBullet = Bullet.createPlayerBullet(
            gameState.generateNextId(),
            gameState.player.x,
            gameState.player.y - 1f
        )
        bullets.add(mainBullet)
        
        // Multi-shot power-up effect
        if (gameState.isMultiShotActive()) {
            val leftBullet = Bullet.createPlayerBullet(
                gameState.generateNextId(),
                gameState.player.x - 0.5f,
                gameState.player.y - 1f
            )
            val rightBullet = Bullet.createPlayerBullet(
                gameState.generateNextId(),
                gameState.player.x + 0.5f,
                gameState.player.y - 1f
            )
            bullets.add(leftBullet)
            bullets.add(rightBullet)
        }
        
        return gameState.copy(
            bullets = gameState.bullets + bullets,
            player = gameState.player.shoot(gameState.currentTime),
            nextId = gameState.generateNextId() + bullets.size - 1
        )
    }
    
    fun togglePause(gameState: ShooterGameState): ShooterGameState {
        return gameState.copy(isPaused = !gameState.isPaused)
    }
    
    fun resetGame(): ShooterGameState {
        return ShooterGameState()
    }
    
    fun getGameOverScore(gameState: ShooterGameState): Int {
        return gameState.score + (gameState.enemiesDestroyed * 10)
    }
}
