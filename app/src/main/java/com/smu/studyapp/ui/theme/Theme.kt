package com.smu.studyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF3B5998)
val PrimaryContainer = Color(0xFFDDE3F5)
val Secondary = Color(0xFF5C6BC0)
val Background = Color(0xFFF8F9FF)
val Surface = Color(0xFFFFFFFF)
val OnPrimary = Color(0xFFFFFFFF)
val Error = Color(0xFFB00020)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    error = Error,
)

@Composable
fun SMUStudyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
