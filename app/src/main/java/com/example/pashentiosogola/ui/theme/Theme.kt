package com.example.pashentiosogola.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF042422),
    primaryContainer = TealDark,
    onPrimaryContainer = MintLight,
    secondary = MintLight,
    onSecondary = Color(0xFF042422),
    tertiary = CoralAccent,
    background = TealSurfaceDark,
    surface = TealSurfaceDark,
    onBackground = OnTealSurfaceDark,
    onSurface = OnTealSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = TealDark,
    secondary = TealDark,
    onSecondary = Color.White,
    tertiary = CoralAccent,
    background = TealSurfaceLight,
    surface = TealSurfaceLight,
    onBackground = OnTealSurface,
    onSurface = OnTealSurface
)

@Composable
fun PashentiOsogolaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic blue colors to maintain brand teal palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
