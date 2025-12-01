package com.hotian.ta.ui.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Lavender,
    onPrimaryContainer = Purple40,

    secondary = PurpleGrey80,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Mauve,
    onSecondaryContainer = Purple60,

    tertiary = Pink80,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Periwinkle,
    onTertiaryContainer = Purple40,

    background = BackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF2D1B3D),
    primaryContainer = Purple60,
    onPrimaryContainer = Lavender,

    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Purple40,
    onSecondaryContainer = Mauve,

    tertiary = Pink80,
    onTertiary = Color(0xFF3D2948),
    tertiaryContainer = AccentPurple,
    onTertiaryContainer = AccentPink,

    background = BackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E1E5),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun TaTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
