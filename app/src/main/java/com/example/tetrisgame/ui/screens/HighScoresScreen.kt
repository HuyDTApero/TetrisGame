package com.example.tetrisgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.data.models.HighScoreEntry
import com.example.tetrisgame.data.managers.HighScoreManager

@Composable
fun HighScoresScreen(
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val highScoreManager = remember { HighScoreManager(context) }
    val topScores by highScoreManager.topScores.collectAsState(initial = emptyList())
    val totalGames by highScoreManager.totalGames.collectAsState(initial = 0)
    val totalLines by highScoreManager.totalLines.collectAsState(initial = 0)
    val maxLevel by highScoreManager.maxLevel.collectAsState(initial = 1)

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
                    text = "🏆 HIGH SCORES",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Games", totalGames.toString(), Color(0xFF00D4FF))
                    StatItem("Lines", totalLines.toString(), Color(0xFF00FF41))
                    StatItem("Max Level", maxLevel.toString(), Color(0xFFFF006E))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leaderboard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (topScores.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎮",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No scores yet",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Play a game to set a record!",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(topScores) { index, entry ->
                            LeaderboardEntry(
                                rank = index + 1,
                                entry = entry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 24.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LeaderboardEntry(
    rank: Int,
    entry: HighScoreEntry
) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Gold
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Silver
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
        else -> Color(0xFF2A2A3E).copy(alpha = 0.5f)
    }

    val borderColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF00D4FF).copy(alpha = 0.3f)
    }

    val medalEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(borderColor.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (medalEmoji.isNotEmpty()) medalEmoji else "#$rank",
                    fontSize = if (medalEmoji.isNotEmpty()) 24.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Score Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.score} pts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lv.${entry.level}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF006E)
                    )
                    Text(
                        text = "${entry.lines} lines",
                        fontSize = 12.sp,
                        color = Color(0xFF00FF41)
                    )
                }
                Text(
                    text = entry.date,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
