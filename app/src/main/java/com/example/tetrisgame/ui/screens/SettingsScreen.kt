package com.example.tetrisgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetrisgame.data.models.GameTheme
import com.example.tetrisgame.data.managers.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Collect all settings
    val isSfxEnabled by settingsManager.isSfxEnabled.collectAsState(initial = true)
    val isMusicEnabled by settingsManager.isMusicEnabled.collectAsState(initial = true)
    val sfxVolume by settingsManager.sfxVolume.collectAsState(initial = 0.7f)
    val musicVolume by settingsManager.musicVolume.collectAsState(initial = 0.5f)
    val isHapticEnabled by settingsManager.isHapticEnabled.collectAsState(initial = true)
    val gestureSensitivity by settingsManager.gestureSensitivity.collectAsState(initial = 50f)
    val currentTheme by settingsManager.theme.collectAsState(initial = GameTheme.NEON)

    // AI Assistant settings
    val isAIAssistantEnabled by settingsManager.isAIAssistantEnabled.collectAsState(initial = false)

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
                    text = "⚙️ SETTINGS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D4FF)
                )

                Spacer(modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI Assistant Section
                item {
                    SettingsSectionHeader("🤖 AI ASSISTANT")
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // AI Assistant Toggle
                            SettingsToggle(
                                label = "Enable AI Assistant",
                                checked = isAIAssistantEnabled,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        settingsManager.setAIAssistantEnabled(it)
                                    }
                                }
                            )
                        }
                    }
                }

                // Audio Section
                item {
                    SettingsSectionHeader("🔊 AUDIO")
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // SFX Toggle
                            SettingsToggle(
                                label = "Sound Effects",
                                checked = isSfxEnabled,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        settingsManager.setSfxEnabled(it)
                                    }
                                }
                            )

                            // SFX Volume
                            if (isSfxEnabled) {
                                SettingsSlider(
                                    label = "SFX Volume",
                                    value = sfxVolume,
                                    onValueChange = {
                                        coroutineScope.launch {
                                            settingsManager.setSfxVolume(it)
                                        }
                                    },
                                    valueDisplay = "${(sfxVolume * 100).toInt()}%"
                                )
                            }

                            Divider(color = Color.Gray.copy(alpha = 0.3f))

                            // Music Toggle
                            SettingsToggle(
                                label = "Music",
                                checked = isMusicEnabled,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        settingsManager.setMusicEnabled(it)
                                    }
                                }
                            )

                            // Music Volume
                            if (isMusicEnabled) {
                                SettingsSlider(
                                    label = "Music Volume",
                                    value = musicVolume,
                                    onValueChange = {
                                        coroutineScope.launch {
                                            settingsManager.setMusicVolume(it)
                                        }
                                    },
                                    valueDisplay = "${(musicVolume * 100).toInt()}%"
                                )
                            }
                        }
                    }
                }

                // Controls Section
                item {
                    SettingsSectionHeader("🎮 CONTROLS")
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsSlider(
                                label = "Gesture Sensitivity",
                                value = gestureSensitivity / 100f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        settingsManager.setGestureSensitivity(it * 100f)
                                    }
                                },
                                valueDisplay = when {
                                    gestureSensitivity < 40f -> "Low"
                                    gestureSensitivity < 70f -> "Medium"
                                    else -> "High"
                                }
                            )
                        }
                    }
                }

                // Haptic Section
                item {
                    SettingsSectionHeader("📳 HAPTIC FEEDBACK")
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsToggle(
                                label = "Vibration",
                                checked = isHapticEnabled,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        settingsManager.setHapticEnabled(it)
                                    }
                                }
                            )
                        }
                    }
                }

                // Theme Section
                item {
                    SettingsSectionHeader("🎨 THEME")
                }

                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GameTheme.values().forEach { theme ->
                                ThemeOption(
                                    theme = theme,
                                    isSelected = currentTheme == theme,
                                    onSelect = {
                                        coroutineScope.launch {
                                            settingsManager.setTheme(theme)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Reset Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsManager.resetToDefaults()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF006E).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔄 RESET TO DEFAULTS",
                            color = Color(0xFFFF006E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFFD700),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D4FF),
                checkedTrackColor = Color(0xFF00D4FF).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueDisplay: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = valueDisplay,
                fontSize = 14.sp,
                color = Color(0xFF00FF41),
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00D4FF),
                activeTrackColor = Color(0xFF00D4FF),
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ThemeOption(
    theme: GameTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val themeColors = when (theme) {
        GameTheme.NEON -> listOf(Color(0xFF00FFFF), Color(0xFFFF006E), Color(0xFFBF40BF))
        GameTheme.CLASSIC -> listOf(Color.Red, Color.Green, Color.Blue)
        GameTheme.DARK -> listOf(Color(0xFF333333), Color(0xFF666666), Color(0xFF999999))
        GameTheme.PASTEL -> listOf(Color(0xFFFFB3BA), Color(0xFFBAE1FF), Color(0xFFFFFACD))
    }

    val themeDescription = when (theme) {
        GameTheme.NEON -> "Neon - Vibrant colors"
        GameTheme.CLASSIC -> "Classic - Traditional Tetris"
        GameTheme.DARK -> "Dark - Easy on eyes"
        GameTheme.PASTEL -> "Pastel - Soft & gentle"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color(0xFF00D4FF),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF00D4FF).copy(alpha = 0.2f)
            else
                Color(0xFF2A2A3E).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = themeDescription,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                themeColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}