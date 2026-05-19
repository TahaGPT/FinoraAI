package com.finora.ai.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
// FinoraAI Color System — derived from palette.scss
// ═══════════════════════════════════════════════════════════════

// Primary Palette (from Coolors)
val InkBlack = Color(0xFF060B0F)
val JetBlack = Color(0xFF03272B)
val DarkTeal = Color(0xFF004346)
val MintLeaf = Color(0xFF09BC8A)
val Turquoise = Color(0xFF3FCDB4)
val PearlAqua = Color(0xFF75DDDD)

// Extended Dark Theme Colors
val DarkSurface = Color(0xFF0A1419)
val DarkSurfaceVariant = Color(0xFF0F1E23)
val DarkCard = Color(0xFF0D1B20)
val DarkCardElevated = Color(0xFF122429)
val DarkBorder = Color(0xFF1A3038)
val DarkTextPrimary = Color(0xFFE8F5F2)
val DarkTextSecondary = Color(0xFF8FB8B0)
val DarkTextTertiary = Color(0xFF5A8A80)

// Extended Light Theme Colors
val LightBackground = Color(0xFFF5FAFA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBF5F3)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFF0F9F7)
val LightBorder = Color(0xFFD0E8E3)
val LightTextPrimary = Color(0xFF060B0F)
val LightTextSecondary = Color(0xFF3D5A54)
val LightTextTertiary = Color(0xFF6B8F87)

// Semantic Colors
val SuccessGreen = Color(0xFF09BC8A)
val WarningAmber = Color(0xFFF5A623)
val DangerRed = Color(0xFFE74C5E)
val InfoBlue = Color(0xFF3FCDB4)
val CriticalPulse = Color(0xFFFF3B5C)

// Confidence Halo Colors (Ghost Ledger)
val ConfidenceHigh = Color(0xFF09BC8A)   // >= 0.9
val ConfidenceMedium = Color(0xFFF5A623) // 0.7 - 0.9
val ConfidenceLow = Color(0xFFE74C5E)    // < 0.7

// War Room Neon Accents
val NeonMint = Color(0xFF00FFB3)
val NeonCyan = Color(0xFF00F0FF)
val NeonPulse = Color(0xFF09BC8A)

// Gradient color stops
val GradientDarkStart = InkBlack
val GradientDarkMid = JetBlack
val GradientDarkEnd = DarkTeal
val GradientAccentStart = MintLeaf
val GradientAccentEnd = PearlAqua
