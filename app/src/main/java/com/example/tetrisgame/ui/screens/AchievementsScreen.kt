package com.example.tetrisgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.data.models.Achievement
import com.example.tetrisgame.data.models.AchievementCategory
import com.example.tetrisgame.data.models.AchievementRarity
import com.example.tetrisgame.data.managers.AchievementManager

@Composable
fun AchievementsScreen(
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val achievementManager = remember { AchievementManager(context) }

    val achievements by achievementManager.achievements.collectAsState(initial = emptyList())
    val unlockedCount by achievementManager.unlockedCount.collectAsState(initial = 0)
    val totalCount by achievementManager.totalCount.collectAsState(initial = 0)
    val completionPercentage by achievementManager.completionPercentage.collectAsState(initial = 0)

    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

    val filteredAchievements = if (selectedCategory != null) {
        achievements.filter { it.category == selectedCategory }
    } else {
        achievements
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMenu) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00D4FF),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "🏆 ACHIEVEMENTS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D4FF)
                )

                Text(
                    text = "$unlockedCount/$totalCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERALL PROGRESS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D4FF),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = completionPercentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF00FF41),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$completionPercentage% Complete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF41)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00D4FF),
                        selectedLabelColor = Color.Black
                    )
                )

                AchievementCategory.values().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = getCategoryIcon(category),
                                fontSize = 16.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00D4FF),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Achievements Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val isUnlocked = achievement.isUnlocked
    val shouldShowDetails = !achievement.isSecret || isUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .border(
                width = if (isUnlocked) 2.dp else 1.dp,
                color = if (isUnlocked) getRarityColor(achievement.rarity) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                Color(0xFF1A1A2E).copy(alpha = 0.9f)
            else
                Color(0xFF0A0A0A).copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Text(
                text = if (shouldShowDetails) achievement.icon else "🔒",
                fontSize = 48.sp,
                modifier = Modifier.padding(vertical = 8.dp),
                color = if (isUnlocked) Color.White else Color.Gray
            )

            // Name
            Text(
                text = if (shouldShowDetails) achievement.name else "???",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Description
            Text(
                text = if (shouldShowDetails) achievement.description else "Secret Achievement",
                fontSize = 10.sp,
                color = if (isUnlocked) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress or Status
            if (isUnlocked) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ UNLOCKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF41)
                    )
                }
            } else if (shouldShowDetails && achievement.targetValue > 1) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = achievement.progressPercentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color(0xFF00D4FF),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${achievement.currentProgress}/${achievement.targetValue}",
                        fontSize = 9.sp,
                        color = Color(0xFF00D4FF)
                    )
                }
            } else {
                Text(
                    text = "🔒 LOCKED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            // Rarity Stars
            Text(
                text = "⭐".repeat(achievement.rarity.stars),
                fontSize = 10.sp,
                color = getRarityColor(achievement.rarity)
            )
        }
    }
}

private fun getRarityColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> Color(0xFF888888)
        AchievementRarity.RARE -> Color(0xFF00D4FF)
        AchievementRarity.EPIC -> Color(0xFFBF40BF)
        AchievementRarity.LEGENDARY -> Color(0xFFFFD700)
    }
}

private fun getCategoryIcon(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.SCORE -> "🎯"
        AchievementCategory.LEVEL -> "📊"
        AchievementCategory.LINES -> "🧱"
        AchievementCategory.SPEED -> "⏱️"
        AchievementCategory.SKILL -> "🎮"
        AchievementCategory.SURVIVAL -> "💀"
    }
}
