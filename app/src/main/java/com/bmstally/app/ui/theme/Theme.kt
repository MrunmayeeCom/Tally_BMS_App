package com.bmstally.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5CAE9),
    secondary = Color(0xFFFF9800),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF212121),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    error = Color(0xFFD32F2F)
)

@Composable
fun BmsTallyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
