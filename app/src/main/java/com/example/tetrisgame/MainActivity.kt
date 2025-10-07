package com.example.tetrisgame

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.tetrisgame.data.managers.SettingsManager
import com.example.tetrisgame.data.managers.HighScoreManager
import com.example.tetrisgame.data.managers.ProgressManager
import com.example.tetrisgame.data.models.Achievement
import com.example.tetrisgame.data.models.GameLevel
import com.example.tetrisgame.ui.components.dialogs.AchievementUnlockDialog
import com.example.tetrisgame.ui.navigation.Screen
import com.example.tetrisgame.ui.screens.AchievementsScreen
import com.example.tetrisgame.ui.screens.HighScoresScreen
import com.example.tetrisgame.ui.screens.LevelSelectScreen
import com.example.tetrisgame.ui.screens.SettingsScreen
import com.example.tetrisgame.ui.screens.TetrisGame
import com.example.tetrisgame.ui.screens.TetrisMenuGame
import com.example.tetrisgame.ui.theme.TetrisGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        // Hide system bars (navigation and status bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Hide system UI
            HideSystemUI()

            TetrisGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.TETRIS_MENU) }
    var selectedGameLevel by remember { mutableStateOf(GameLevel.CLASSIC) }
    val context = LocalContext.current

    // Settings manager
    val settingsManager = remember { SettingsManager(context) }
    val isSfxEnabled by settingsManager.isSfxEnabled.collectAsState(initial = true)
    val isMusicEnabled by settingsManager.isMusicEnabled.collectAsState(initial = true)

    // High score manager
    val highScoreManager = remember { HighScoreManager(context) }
    val highScore by highScoreManager.highScore.collectAsState(initial = 0)

    // Progress manager
    val progressManager = remember { ProgressManager(context) }

    // Achievement queue
    var unlockedAchievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var showAchievementDialog by remember { mutableStateOf(false) }

    // Show achievements when returning to menu
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.TETRIS_MENU && unlockedAchievements.isNotEmpty()) {
            showAchievementDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.TETRIS_MENU -> TetrisMenuGame(
                onStartGame = { currentScreen = Screen.LEVEL_SELECT },
                onHighScores = { currentScreen = Screen.HIGH_SCORES },
                onSettings = { currentScreen = Screen.SETTINGS },
                onAchievements = { currentScreen = Screen.ACHIEVEMENTS },
                highScore = highScore,
                newAchievementsCount = unlockedAchievements.size
            )
            Screen.LEVEL_SELECT -> LevelSelectScreen(
                onLevelSelected = { level ->
                    selectedGameLevel = level
                    currentScreen = Screen.TETRIS_GAME
                },
                onBackToMenu = { currentScreen = Screen.TETRIS_MENU }
            )
            Screen.TETRIS_GAME -> TetrisGame(
                onBackToMenu = { currentScreen = Screen.TETRIS_MENU },
                isSoundEnabled = isSfxEnabled,
                isMusicEnabled = isMusicEnabled,
                gameLevel = selectedGameLevel,
                onAchievementUnlocked = { achievement ->
                    unlockedAchievements = unlockedAchievements + achievement
                },
                onLevelComplete = { /* Level unlocked notification */ },
                onSwitchToLevel = { newLevel ->
                    // User chose to switch to new level immediately
                    selectedGameLevel = newLevel
                    currentScreen = Screen.TETRIS_GAME
                }
            )
        Screen.HIGH_SCORES -> HighScoresScreen(
            onBackToMenu = { currentScreen = Screen.TETRIS_MENU }
        )
        Screen.SETTINGS -> SettingsScreen(
            onBackToMenu = { currentScreen = Screen.TETRIS_MENU }
        )
        Screen.ACHIEVEMENTS -> AchievementsScreen(
            onBackToMenu = { currentScreen = Screen.TETRIS_MENU }
        )

            Screen.SHOOTER_MENU -> {}
            Screen.SHOOTER_GAME -> {}
        }

        // Achievement unlock dialog (only show in menu)
        if (showAchievementDialog && unlockedAchievements.isNotEmpty() && currentScreen == Screen.TETRIS_MENU) {
            AchievementUnlockDialog(
                achievement = unlockedAchievements.first(),
                onDismiss = {
                    if (unlockedAchievements.size > 1) {
                        unlockedAchievements = unlockedAchievements.drop(1)
                    } else {
                        showAchievementDialog = false
                        unlockedAchievements = emptyList()
                    }
                }
            )
        }
    }
}

@Composable
fun HideSystemUI() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as ComponentActivity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Hide both status bar and navigation bar
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Make it immersive sticky (bars reappear on swipe but hide again)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // Show system bars when leaving
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}