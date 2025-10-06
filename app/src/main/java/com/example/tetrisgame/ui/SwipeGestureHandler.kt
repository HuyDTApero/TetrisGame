package com.example.tetrisgame.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Gesture types for Tetris controls
 */
enum class GestureType {
    TAP,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
    SWIPE_DOWN
}

/**
 * Add swipe gesture detection to a composable
 */
fun Modifier.swipeGestures(
    onGesture: (GestureType) -> Unit,
    swipeThreshold: Float = 50f
): Modifier = this
    .pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                onGesture(GestureType.TAP)
            }
        )
    }
    .pointerInput(Unit) {
        var dragStart = Offset.Zero
        var dragEnd = Offset.Zero

        detectDragGestures(
            onDragStart = { offset ->
                dragStart = offset
            },
            onDragEnd = {
                val deltaX = dragEnd.x - dragStart.x
                val deltaY = dragEnd.y - dragStart.y

                // Determine which direction has more movement
                if (abs(deltaX) > abs(deltaY)) {
                    // Horizontal swipe
                    if (abs(deltaX) > swipeThreshold) {
                        if (deltaX > 0) {
                            onGesture(GestureType.SWIPE_RIGHT)
                        } else {
                            onGesture(GestureType.SWIPE_LEFT)
                        }
                    }
                } else {
                    // Vertical swipe
                    if (abs(deltaY) > swipeThreshold) {
                        if (deltaY > 0) {
                            onGesture(GestureType.SWIPE_DOWN)
                        } else {
                            onGesture(GestureType.SWIPE_UP)
                        }
                    }
                }
            },
            onDrag = { change, _ ->
                dragEnd = change.position
            }
        )
    }
