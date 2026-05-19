package com.finora.ai.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════
// FinoraAI Reusable Components
// ═══════════════════════════════════════════════════════════════

// ── Glowing Card ────────────────────────────────────────────────

@Composable
fun GlowingCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MintLeaf,
    glowRadius: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = glowColor.copy(alpha = glowAlpha),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        content()
    }
}

// ── Cashflow River Card ─────────────────────────────────────────

@Composable
fun CashflowRiverCard(
    healthScore: Int, // 0-100
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "river")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (healthScore > 60) 2000 else 4000,
                easing = LinearEasing
            ),
        ),
        label = "waveOffset"
    )

    val riverColor = when {
        healthScore >= 70 -> MintLeaf
        healthScore >= 40 -> WarningAmber
        else -> DangerRed
    }

    val animatedColor by animateColorAsState(
        targetValue = riverColor,
        animationSpec = tween(800),
        label = "riverColor"
    )

    GlowingCard(
        modifier = modifier.fillMaxWidth().height(180.dp),
        glowColor = animatedColor,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated wave background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val wavePath = Path().apply {
                    moveTo(0f, height * 0.6f)
                    for (x in 0..width.toInt() step 4) {
                        val y = height * 0.6f +
                            sin(x * 0.02f + waveOffset) * 20f +
                            sin(x * 0.01f + waveOffset * 0.7f) * 15f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.6f),
                            animatedColor.copy(alpha = 0.2f),
                        )
                    )
                )

                // Second wave layer
                val wavePath2 = Path().apply {
                    moveTo(0f, height * 0.7f)
                    for (x in 0..width.toInt() step 4) {
                        val y = height * 0.7f +
                            sin(x * 0.015f + waveOffset + 1f) * 15f +
                            cos(x * 0.008f + waveOffset * 0.5f) * 10f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = wavePath2,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0.3f),
                            animatedColor.copy(alpha = 0.1f),
                        )
                    )
                )
            }

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Text(
                    text = "Cashflow Health",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = animatedColor,
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            healthScore >= 70 -> Icons.Filled.FitnessCenter
                            healthScore >= 40 -> Icons.Filled.Warning
                            else -> Icons.Filled.ErrorOutline
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = animatedColor.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            healthScore >= 70 -> "Strong cashflow — rivers flowing"
                            healthScore >= 40 -> "Moderate — watch the currents"
                            else -> "Critical — danger zone"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── KPI Strip ───────────────────────────────────────────────────

@Composable
fun KPIStrip(
    runwayDays: Int,
    currentBalance: String,
    monthlyBurn: String,
    momChange: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KPIChip("Runway", "$runwayDays days", Modifier.weight(1f))
        KPIChip("Balance", currentBalance, Modifier.weight(1f))
        KPIChip("Burn", monthlyBurn, Modifier.weight(1f))
        KPIChip(
            "MoM",
            "${if (momChange >= 0) "+" else ""}${String.format("%.1f", momChange)}%",
            Modifier.weight(1f),
            valueColor = if (momChange >= 0) SuccessGreen else DangerRed,
        )
    }
}

@Composable
private fun KPIChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Stress-O-Meter Gauge ────────────────────────────────────────

@Composable
fun StressOMeter(
    score: Int, // 0-100
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "stressScore"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "stressPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (score < 40) 0.8f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (score < 40) 600 else 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val gaugeColor = when {
        score >= 70 -> SuccessGreen
        score >= 40 -> WarningAmber
        else -> DangerRed
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 12.dp.toPx()
            val arcSize = Size(
                this.size.width - strokeWidth,
                this.size.height - strokeWidth
            )
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(
                color = gaugeColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Value arc
            val sweepAngle = (animatedScore / 100f) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(gaugeColor, gaugeColor.copy(alpha = pulseAlpha))
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glow effect
            drawArc(
                color = gaugeColor.copy(alpha = pulseAlpha * 0.3f),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = gaugeColor,
            )
            Text(
                text = when {
                    score >= 70 -> "Healthy"
                    score >= 40 -> "Moderate"
                    else -> "Critical"
                },
                style = MaterialTheme.typography.labelSmall,
                color = gaugeColor.copy(alpha = 0.8f),
            )
        }
    }
}

// ── Animated Floating Action Button ─────────────────────────────

@Composable
fun FinoraFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNewAnalysis: () -> Unit,
    onScanLedger: () -> Unit,
    onSimulate: () -> Unit,
    onAuditTrail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Sub-actions with staggered animation
        data class FabAction(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val onClick: () -> Unit, val delay: Int)
        val actions = listOf(
            FabAction(Icons.Filled.Analytics, "New Analysis", onNewAnalysis, 0),
            FabAction(Icons.Filled.CameraAlt, "Scan Ledger", onScanLedger, 50),
            FabAction(Icons.Filled.AutoAwesome, "What-If", onSimulate, 100),
            FabAction(Icons.Filled.Link, "Audit Trail", onAuditTrail, 150),
        )

        actions.forEach { action ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200, delayMillis = action.delay)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + scaleIn(
                        initialScale = 0.6f,
                        animationSpec = tween(250, delayMillis = action.delay)
                    ),
                exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.6f),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        action.onClick()
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(action.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = action.label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Main FAB with rotation animation
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "fabRotation"
        )

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MintLeaf,
            contentColor = InkBlack,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Actions",
                modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotation },
            )
        }
    }
}

// ── Pulsing Dot Indicator ───────────────────────────────────────

@Composable
fun PulsingDot(
    color: Color = MintLeaf,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.size(size * 2), contentAlignment = Alignment.Center) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.3f))
        )
        // Inner dot
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ── Confidence Halo Border ──────────────────────────────────────

@Composable
fun ConfidenceHaloCell(
    confidence: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val haloColor = when {
        confidence >= 0.9f -> ConfidenceHigh
        confidence >= 0.7f -> ConfidenceMedium
        else -> ConfidenceLow
    }

    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = if (confidence < 0.7f) 0.4f else 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (confidence < 0.7f) 800 else 2000,
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = modifier
            .border(
                width = if (confidence < 0.7f) 2.5.dp else 1.5.dp,
                color = haloColor.copy(alpha = haloAlpha),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(2.dp),
        content = content,
    )
}

// ── Shimmer Loading Effect ──────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    start = Offset(shimmerOffset * 300f, 0f),
                    end = Offset(shimmerOffset * 300f + 300f, 0f),
                )
            )
    )
}

// ── Bottom Navigation Bar ───────────────────────────────────────

@Composable
fun FinoraBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
    ) {
        data class NavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
        val items = listOf(
            NavItem("Dashboard", "home", Icons.Filled.Dashboard),
            NavItem("Simulate", "simulator", Icons.Filled.AutoAwesome),
            NavItem("Scan", "ghost_ledger_camera", Icons.Filled.CameraAlt),
            NavItem("Audit", "audit_trail", Icons.Filled.Link),
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (selected) 26.dp else 24.dp),
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MintLeaf,
                    selectedTextColor = MintLeaf,
                    indicatorColor = MintLeaf.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
