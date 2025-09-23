package com.example.tetrisgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tetrisgame.ui.Screen
import com.example.tetrisgame.ui.ShooterGame
import com.example.tetrisgame.ui.ShooterMenuGame
import com.example.tetrisgame.ui.theme.TetrisGameTheme
import com.example.tetrisgame.ui.TetrisMenuGame
import com.example.tetrisgame.ui.TetrisGame

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

    when (currentScreen) {
        Screen.TETRIS_MENU -> TetrisMenuGame(
            onStartGame = { currentScreen = Screen.TETRIS_GAME },
            onNavigateShooterMenu = { currentScreen = Screen.SHOOTER_MENU }
        )
        Screen.TETRIS_GAME -> TetrisGame(
            onBackToMenu = { currentScreen = Screen.TETRIS_MENU }
        )
        Screen.SHOOTER_MENU -> ShooterMenuGame(
            onStartGame = { currentScreen = Screen.SHOOTER_GAME },
            onNavigateTetrisMenu = { currentScreen = Screen.TETRIS_MENU }
        )
        Screen.SHOOTER_GAME -> ShooterGame(
            onBackToMenu = { currentScreen = Screen.SHOOTER_MENU }
        )
    }
}