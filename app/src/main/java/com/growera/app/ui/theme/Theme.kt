package com.growera.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CrystalScheme = darkColorScheme(
    primary = Color(0xFF9B8CFF),
    onPrimary = Color(0xFF100C24),
    secondary = Color(0xFF5DE4FF),
    background = Color(0xFF080D19),
    surface = Color(0xFF111A2B),
    surfaceVariant = Color(0xFF1A2540),
    onBackground = Color(0xFFF4F6FF),
    onSurface = Color(0xFFF4F6FF),
    onSurfaceVariant = Color(0xFFB8C2DA),
    error = Color(0xFFFF718B)
)

@Composable
fun GroweraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CrystalScheme, content = content)
}
