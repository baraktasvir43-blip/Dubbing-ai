package com.example.ui.theme

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
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainerDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = SecondaryCyan,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryCyanContainerDark,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = TertiaryViolet,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryVioletContainerDark,
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigoDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = SecondaryCyanDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryCyanContainer,
    onSecondaryContainer = Color(0xFF083344),
    tertiary = TertiaryVioletDark,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryVioletContainer,
    onTertiaryContainer = Color(0xFF2E1065),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
