package com.finora.ai.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════
// FinoraAI Theme — Dark / Light with smooth animated transitions
// ═══════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = MintLeaf,
    onPrimary = InkBlack,
    primaryContainer = DarkTeal,
    onPrimaryContainer = PearlAqua,
    secondary = Turquoise,
    onSecondary = InkBlack,
    secondaryContainer = JetBlack,
    onSecondaryContainer = PearlAqua,
    tertiary = PearlAqua,
    onTertiary = InkBlack,
    background = InkBlack,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f),
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRed.copy(alpha = 0.15f),
    onErrorContainer = DangerRed,
    inverseSurface = LightSurface,
    inverseOnSurface = LightTextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = DarkTeal,
    onPrimary = Color.White,
    primaryContainer = MintLeaf.copy(alpha = 0.15f),
    onPrimaryContainer = DarkTeal,
    secondary = MintLeaf,
    onSecondary = Color.White,
    secondaryContainer = Turquoise.copy(alpha = 0.15f),
    onSecondaryContainer = DarkTeal,
    tertiary = Turquoise,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.5f),
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRed.copy(alpha = 0.08f),
    onErrorContainer = DangerRed,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkTextPrimary,
)

// Extended color properties accessible from theme
data class FinoraExtendedColors(
    val cardBackground: Color,
    val cardElevated: Color,
    val textTertiary: Color,
    val accentGlow: Color,
    val successGreen: Color,
    val warningAmber: Color,
    val dangerRed: Color,
    val neonMint: Color,
    val neonCyan: Color,
    val confidenceHigh: Color,
    val confidenceMedium: Color,
    val confidenceLow: Color,
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
)

val LocalFinoraColors = staticCompositionLocalOf {
    FinoraExtendedColors(
        cardBackground = DarkCard,
        cardElevated = DarkCardElevated,
        textTertiary = DarkTextTertiary,
        accentGlow = MintLeaf,
        successGreen = SuccessGreen,
        warningAmber = WarningAmber,
        dangerRed = DangerRed,
        neonMint = NeonMint,
        neonCyan = NeonCyan,
        confidenceHigh = ConfidenceHigh,
        confidenceMedium = ConfidenceMedium,
        confidenceLow = ConfidenceLow,
        gradientStart = GradientDarkStart,
        gradientMid = GradientDarkMid,
        gradientEnd = GradientDarkEnd,
    )
}

private val DarkExtendedColors = FinoraExtendedColors(
    cardBackground = DarkCard,
    cardElevated = DarkCardElevated,
    textTertiary = DarkTextTertiary,
    accentGlow = NeonMint,
    successGreen = SuccessGreen,
    warningAmber = WarningAmber,
    dangerRed = DangerRed,
    neonMint = NeonMint,
    neonCyan = NeonCyan,
    confidenceHigh = ConfidenceHigh,
    confidenceMedium = ConfidenceMedium,
    confidenceLow = ConfidenceLow,
    gradientStart = GradientDarkStart,
    gradientMid = GradientDarkMid,
    gradientEnd = GradientDarkEnd,
)

private val LightExtendedColors = FinoraExtendedColors(
    cardBackground = LightCard,
    cardElevated = LightCardElevated,
    textTertiary = LightTextTertiary,
    accentGlow = MintLeaf,
    successGreen = SuccessGreen,
    warningAmber = WarningAmber,
    dangerRed = DangerRed,
    neonMint = MintLeaf,
    neonCyan = Turquoise,
    confidenceHigh = ConfidenceHigh,
    confidenceMedium = ConfidenceMedium,
    confidenceLow = ConfidenceLow,
    gradientStart = LightBackground,
    gradientMid = LightSurfaceVariant,
    gradientEnd = LightCard,
)

@Composable
fun FinoraAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFinoraColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinoraTypography,
            shapes = FinoraShapes,
            content = content
        )
    }
}

// Convenience accessor
object FinoraTheme {
    val extendedColors: FinoraExtendedColors
        @Composable
        get() = LocalFinoraColors.current
}
