package ru.kram.pagingsample.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun PagingSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkAppColorScheme()
    } else {
        LightAppColorScheme()
    }

    CompositionLocalProvider(
        LocalColorScheme provides colorScheme
    ) {
        MaterialTheme(
            typography = Typography,
            content = content
        )
    }
}