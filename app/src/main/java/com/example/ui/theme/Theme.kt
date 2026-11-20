package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val CyberTacticalColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TacticalObsidian,
    primaryContainer = TacticalCardSurface,
    onPrimaryContainer = NeonCyan,
    secondary = NeonEmerald,
    onSecondary = TacticalObsidian,
    secondaryContainer = TacticalGlassSurface,
    onSecondaryContainer = NeonEmerald,
    tertiary = TacticalGold,
    onTertiary = TacticalObsidian,
    background = TacticalObsidian,
    onBackground = TacticalTextPrimary,
    surface = TacticalDeepNavy,
    onSurface = TacticalTextPrimary,
    surfaceVariant = TacticalGlassSurface,
    onSurfaceVariant = TacticalTextSecondary,
    error = TacticalCrimson,
    onError = TacticalTextPrimary,
    outline = TacticalBorderCyan
)

@Composable
fun WajehniTacticalTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = CyberTacticalColorScheme,
            typography = Typography,
            content = content
        )
    }
}
