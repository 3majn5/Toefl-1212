package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BrandIndigoLight,
    onPrimary = Color(0xFF14134A),
    primaryContainer = Color(0xFF34348F),
    onPrimaryContainer = BrandIndigoSoft,
    secondary = WarmCoralLight,
    onSecondary = Color(0xFF4A1405),
    secondaryContainer = Color(0xFF6E2A17),
    onSecondaryContainer = WarmCoralSoft,
    tertiary = FreshMintLight,
    onTertiary = Color(0xFF00382A),
    tertiaryContainer = Color(0xFF0B5442),
    onTertiaryContainer = FreshMintSoft,
    background = NightBackground,
    onBackground = NightText,
    surface = NightCard,
    onSurface = NightText,
    surfaceVariant = NightCardRaised,
    onSurfaceVariant = NightMuted,
    surfaceContainer = NightCard,
    surfaceContainerHigh = NightCardRaised,
    error = Color(0xFFFF8A8E),
    outline = NightOutline,
    outlineVariant = NightOutline
)

private val LightColorScheme = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = BrandIndigoSoft,
    onPrimaryContainer = BrandIndigoDark,
    secondary = WarmCoral,
    onSecondary = Color.White,
    secondaryContainer = WarmCoralSoft,
    onSecondaryContainer = Color(0xFF8A2E14),
    tertiary = FreshMint,
    onTertiary = Color.White,
    tertiaryContainer = FreshMintSoft,
    onTertiaryContainer = Color(0xFF0B5442),
    background = CreamBackground,
    onBackground = InkText,
    surface = CardWhite,
    onSurface = InkText,
    surfaceVariant = LavenderMist,
    onSurfaceVariant = MutedText,
    surfaceContainer = CardWhite,
    surfaceContainerHigh = LavenderMist,
    error = SoftRose,
    outline = SoftOutline,
    outlineVariant = SoftOutline
)

// Generous, friendly corner radii throughout the app
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    // Keep status/navigation bar icons readable when the in-app theme differs from the phone's
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
