package com.delta.harmonica.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DeltaHarmonicaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(0xFF101010),
            primary = Color(0xFF42A5F5),
            secondary = Color(0xFFFFD58A),
            surface = Color(0xFF121212)
        ),
        content = content
    )
}
