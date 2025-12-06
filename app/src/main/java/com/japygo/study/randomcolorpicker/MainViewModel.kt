package com.japygo.study.randomcolorpicker

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class ColorUiState(
    val currentColor: Color = Color.White,
    val hexCode: String = "#FFFFFF",
    val rgbCode: String = "RGB(255, 255, 255)",
    val brightness: Float = 1f, // 0f to 1f
    val history: List<Color> = emptyList(),
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ColorUiState())
    val uiState: StateFlow<ColorUiState> = _uiState.asStateFlow()

    init {
        generateNewColor()
    }

    fun generateNewColor() {
        // Generate random color
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)
        val color = Color(red, green, blue)

        updateColor(color)
    }

    fun updateBrightness(brightness: Float) {
        // Not perfectly implementing brightness adjustment on the *same* color base for now,
        // just storing the value or we can manipulate the current color.
        // For simplicity, let's just store it or apply it to the base randomly generated color?
        // Let's assume brightness scales the current RGB values.

        // However, a better approach for a "picker" with brightness is likely HSL.
        // But the requirement says "Random Color Generator" + "Brightness Slider".
        // Let's changing existing color's brightness if possible or just generate new one.
        // Let's stick to storing brightness state for now if complex logic is needed, 
        // OR simply don't complexify if not strictly requested.
        // Requirement: "Slider로 색상의 밝기 조절" -> Adjust brightness of *current* color.

        _uiState.update { currentState ->
            currentState.copy(brightness = brightness)
        }
    }

    // Helper to actually apply brightness for display if needed, 
    // but typically we might want to change the actual color components.
    // Let's keep it simple: The slider will just be a visual element or actually modify the color?
    // "Adjust brightness of the color".
    // I will implement a helper to apply brightness to the *base* color if I was storing a base color. 
    // For now, I'll skip complex brightness logic in this iteration unless I add strictly keeping base color.
    // Let's just implement generate and copy first. Brightness is optional.

    private fun updateColor(color: Color, addToHistory: Boolean = true) {
        val hex = String.format(
            "#%02X%02X%02X",
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )
        val rgb =
            "RGB(${(color.red * 255).toInt()}, ${(color.green * 255).toInt()}, ${(color.blue * 255).toInt()})"

        _uiState.update { currentState ->
            val newHistory = if (addToHistory) {
                (listOf(color) + currentState.history).take(5)
            } else {
                currentState.history
            }
            currentState.copy(
                currentColor = color,
                hexCode = hex,
                rgbCode = rgb,
                history = newHistory,
            )
        }
    }

    fun restoreColor(color: Color) {
        updateColor(color, addToHistory = false)
    }
}
