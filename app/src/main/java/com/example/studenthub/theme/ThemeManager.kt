package com.example.studenthub.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "student_hub_theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_DYNAMIC_COLORS = "dynamic_colors"

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(false)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
        _useDynamicColors.value = prefs.getBoolean(KEY_DYNAMIC_COLORS, false)
    }

    fun toggleDarkMode(context: Context) {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, newValue)
            .apply()
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        _isDarkMode.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
    }

    fun toggleDynamicColors(context: Context) {
        val newValue = !_useDynamicColors.value
        _useDynamicColors.value = newValue
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DYNAMIC_COLORS, newValue)
            .apply()
    }
}

@Composable
fun rememberThemeState(): ThemeState {
    val context = LocalContext.current
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val useDynamicColors by ThemeManager.useDynamicColors.collectAsState()

    return remember(isDarkMode, useDynamicColors) {
        ThemeState(
            isDarkMode = isDarkMode,
            useDynamicColors = useDynamicColors,
            toggleDarkMode = { ThemeManager.toggleDarkMode(context) },
            setDarkMode = { enabled -> ThemeManager.setDarkMode(context, enabled) },
            toggleDynamicColors = { ThemeManager.toggleDynamicColors(context) }
        )
    }
}

data class ThemeState(
    val isDarkMode: Boolean,
    val useDynamicColors: Boolean,
    val toggleDarkMode: () -> Unit,
    val setDarkMode: (Boolean) -> Unit,
    val toggleDynamicColors: () -> Unit
)