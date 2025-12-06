package com.japygo.study.randomcolorpicker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.study.randomcolorpicker.data.ColorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ColorUiState(
    val currentColor: Color = Color.White,
    val hexCode: String = "#FFFFFF",
    val rgbCode: String = "RGB(255, 255, 255)",
    val brightness: Float = 1f, // 0f to 1f
    val history: List<Color> = emptyList(),
)

class MainViewModel(private val repository: ColorRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ColorUiState())
    val uiState: StateFlow<ColorUiState> = _uiState.asStateFlow()

    init {
        // Observe history from repository
        viewModelScope.launch {
            repository.recentColorsFlow.collect { colorLongs ->
                val colors = colorLongs.map { Color(it.toInt()) }
                _uiState.update { it.copy(history = colors) }
            }
        }
        generateNewColor(addToHistory = true)
    }

    fun generateNewColor(addToHistory: Boolean = true) {
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)
        val color = Color(red, green, blue)

        updateColor(color, addToHistory)
    }

    fun updateBrightness(brightness: Float) {
        _uiState.update { currentState ->
            currentState.copy(brightness = brightness)
        }
    }

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
            currentState.copy(
                currentColor = color,
                hexCode = hex,
                rgbCode = rgb
            )
        }
        
        if (addToHistory) {
            saveToHistory(color)
        }
    }
    
    private fun saveToHistory(color: Color) {
        viewModelScope.launch {
            repository.addRecentColor(color.toArgb().toLong())
        }
    }

    fun restoreColor(color: Color) {
        updateColor(color, addToHistory = false)
    }
}
