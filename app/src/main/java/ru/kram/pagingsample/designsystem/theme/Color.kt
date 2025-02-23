package ru.kram.pagingsample.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

interface ColorScheme {
    val primary: Color
    val onPrimary: Color
    val secondary: Color
    val background: Color
    val onBackground: Color
    val accent: Color
    val textPrimary: Color
    val textSecondary: Color
    val textOnDisabled: Color
    val textBackground: Color
    val surface: Color
    val onSurface: Color
    val border: Color
    val disabled: Color
}

data class LightAppColorScheme(
    override val primary: Color = Color(0xFF7451D3),
    override val secondary: Color = Color.White,
    override val onPrimary: Color = Color.White,
    override val background: Color = Color(0xFFF5F5F5),
    override val onBackground: Color = Color(0xFF1C1C2E),
    override val accent: Color = Color(0xFFFFD700),
    override val textPrimary: Color = Color(0xFF1C1C2E),
    override val textSecondary: Color = Color(0xff000000),
    override val textOnDisabled: Color = Color(0xff030730),
    override val textBackground: Color = Color(0xFFEAEAEA),
    override val surface: Color = Color(0xFFEAEAEA),
    override val onSurface: Color = Color(0xFF2A1B3D),
    override val border: Color = Color(0xFF5A3E9D),
    override val disabled: Color = Color(0xff979797)
) : ColorScheme

data class DarkAppColorScheme(
    override val primary: Color = Color(0xFF240A4D),
    override val onPrimary: Color = Color.White,
    override val secondary: Color = Color(0xFF1B0066),
    override val background: Color = Color(0xFF0F0F1A),
    override val onBackground: Color = Color(0xFFF5F5F5),
    override val accent: Color = Color(0xFFFFC107),
    override val textPrimary: Color = Color(0xFFF5F5F5),
    override val textSecondary: Color = Color(0xFF9E9E9E),
    override val textOnDisabled: Color = Color(0xFF9E9E9E),
    override val textBackground: Color = Color(0xFF1A1A2E),
    override val surface: Color = Color(0xFF1A1A2E),
    override val onSurface: Color = Color(0xFFD0D0D0),
    override val border: Color = Color(0xFF3D2C8D),
    override val disabled: Color = Color(0xFF6B6B6B)
) : ColorScheme

val LocalColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No ColorPalette provided")
}

val Colors @Composable get() = LocalColorScheme.current

