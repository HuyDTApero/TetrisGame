package com.example.tetrisgame

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.tetrisgame.ui.Screen
import com.example.tetrisgame.ui.TetrisGame
import com.example.tetrisgame.ui.TetrisMenuGame
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

    // Persistent audio preferences
    val isSoundEnabled = remember { mutableStateOf(true) }
    val isMusicEnabled = remember { mutableStateOf(true) }

    when (currentScreen) {
        Screen.TETRIS_MENU -> TetrisMenuGame(
            onStartGame = { currentScreen = Screen.TETRIS_GAME },
            isSoundEnabled = isSoundEnabled,
            isMusicEnabled = isMusicEnabled
        )
        Screen.TETRIS_GAME -> TetrisGame(
            onBackToMenu = { currentScreen = Screen.TETRIS_MENU },
            isSoundEnabled = isSoundEnabled.value,
            isMusicEnabled = isMusicEnabled.value
        )

        Screen.SHOOTER_MENU -> {}
        Screen.SHOOTER_GAME -> {}
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