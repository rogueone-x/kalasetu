package com.example.kalasetu.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SubtitleGray = Color(0xFFCCCCCC)
val SelectedPurple = Color(0xFF836AE0)
val UnselectedBorder = Color(0xFFCCCCCC)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7466F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    secondary = Color(0xFFD4A843),
    surface = Color.White,
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1C1B1F),
)

@Composable
fun KalasetuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}