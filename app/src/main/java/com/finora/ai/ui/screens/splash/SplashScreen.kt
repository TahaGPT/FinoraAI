package com.finora.ai.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════
// Splash Screen — Cinematic brand reveal with animated logo
// ═══════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    var animationPhase by remember { mutableStateOf(0) }

    // Staggered animation phases
    LaunchedEffect(Unit) {
        delay(300)
        animationPhase = 1 // Logo appears
        delay(600)
        animationPhase = 2 // Title appears
        delay(500)
        animationPhase = 3 // Subtitle appears
        delay(400)
        animationPhase = 4 // Particles burst
        delay(800)
        // TODO: Check auth state — for now go to login
        onNavigateToLogin()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
        ),
        label = "particles"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DarkTeal.copy(alpha = 0.3f),
                        JetBlack,
                        InkBlack,
                    ),
                    radius = 1200f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Ambient particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val particleCount = 30

            for (i in 0 until particleCount) {
                val angle = (i * 2 * PI / particleCount).toFloat() + particleOffset
                val radius = 150f + sin(angle * 2 + i) * 80f
                val x = centerX + kotlin.math.cos(angle) * radius
                val y = centerY + sin(angle) * radius
                val alpha = if (animationPhase >= 4) 0.6f else 0.1f
                val particleSize = 2f + sin(angle * 3) * 1.5f

                drawCircle(
                    color = MintLeaf.copy(alpha = alpha * (0.3f + sin(particleOffset + i) * 0.3f)),
                    radius = particleSize,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Logo Mark ───────────────────────────────────
            val logoScale by animateFloatAsState(
                targetValue = if (animationPhase >= 1) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "logoScale"
            )
            val logoAlpha by animateFloatAsState(
                targetValue = if (animationPhase >= 1) 1f else 0f,
                animationSpec = tween(600),
                label = "logoAlpha"
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val w = size.width
                    val h = size.height
                    // Stylized "F" mark with flowing lines
                    val path = Path().apply {
                        moveTo(w * 0.2f, h * 0.9f)
                        lineTo(w * 0.2f, h * 0.1f)
                        lineTo(w * 0.8f, h * 0.1f)
                        moveTo(w * 0.2f, h * 0.5f)
                        lineTo(w * 0.65f, h * 0.5f)
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(MintLeaf, PearlAqua)
                        ),
                        style = Stroke(width = 5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    // Accent dot
                    drawCircle(
                        color = Turquoise,
                        radius = 6.dp.toPx(),
                        center = Offset(w * 0.75f, h * 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ───────────────────────────────────────
            AnimatedVisibility(
                visible = animationPhase >= 2,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                ),
            ) {
                Text(
                    text = "FinoraAI",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                    ),
                    color = DarkTextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtitle ────────────────────────────────────
            AnimatedVisibility(
                visible = animationPhase >= 3,
                enter = fadeIn(tween(400, delayMillis = 100)) + expandVertically(),
            ) {
                Text(
                    text = "Your CFO Twin · Powered by AI",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 2.sp,
                    ),
                    color = Turquoise.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Loading indicator ────────────────────────────
            AnimatedVisibility(
                visible = animationPhase >= 3,
                enter = fadeIn(tween(400)),
            ) {
                val loadingAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "loading"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "dot$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .alpha(dotAlpha)
                                .background(MintLeaf, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }
            }
        }
    }
}
