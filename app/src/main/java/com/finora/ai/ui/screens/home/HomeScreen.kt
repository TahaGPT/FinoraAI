package com.finora.ai.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.*
import com.finora.ai.ui.components.*
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════
// Home Dashboard Screen — Financial command center
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinoraViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNewAnalysis: () -> Unit,
    onScanLedger: () -> Unit,
    onSimulate: () -> Unit,
    onAuditTrail: () -> Unit,
) {
    var fabExpanded by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
        viewModel.fetchDashboardData()
    }

    // Use real data from ViewModel
    val kpiData = viewModel.kpiData
    val realAlerts = viewModel.alerts

    Scaffold(
        floatingActionButton = {
            FinoraFAB(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onNewAnalysis = onNewAnalysis,
                onScanLedger = onScanLedger,
                onSimulate = onSimulate,
                onAuditTrail = onAuditTrail,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // ── Top Bar ─────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -it }),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.background,
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Good morning, Taha",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PulsingDot(color = MintLeaf, size = 6.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Finora is watching",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = onToggleTheme) {
                                    Icon(
                                        if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                        contentDescription = "Toggle theme",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { /* TODO: Notifications */ }) {
                                    Badge(
                                        containerColor = DangerRed,
                                    ) {
                                        Text("3", fontSize = 10.sp)
                                    }
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Cashflow River Card ─────────────────────────
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 100)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        ),
                ) {
                    CashflowRiverCard(
                        healthScore = kpiData.healthScore,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }

            // ── KPI Strip ───────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            )
                        ),
                ) {
                    KPIStrip(
                        runwayDays = kpiData.runwayDays,
                        currentBalance = "₨${String.format("%,.0f", kpiData.currentBalance / 1000)}k",
                        monthlyBurn = "₨${String.format("%,.0f", kpiData.monthlyBurn / 1000)}k",
                        momChange = kpiData.momChange,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }

            // ── Quick Actions Row ───────────────────────────
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 300)) + expandVertically(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            QuickActionCard(Icons.Filled.Analytics, "New\nAnalysis", onNewAnalysis, Modifier.weight(1f))
                            QuickActionCard(Icons.Filled.CameraAlt, "Scan\nLedger", onScanLedger, Modifier.weight(1f))
                            QuickActionCard(Icons.Filled.AutoAwesome, "What-If\nSimulate", onSimulate, Modifier.weight(1f))
                            QuickActionCard(Icons.Filled.Link, "Audit\nTrail", onAuditTrail, Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── Recent Alerts Feed ──────────────────────────
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 400)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Recent Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        TextButton(onClick = { /* View all */ }) {
                            Text("View all", color = MintLeaf)
                        }
                    }
                }
            }

            itemsIndexed(realAlerts) { index, alert ->
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400, delayMillis = 500 + index * 80)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            )
                        ),
                ) {
                    AlertFeedCard(alert = alert, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                }
            }
        }
    }
}

// ── Quick Action Card ───────────────────────────────────────────

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(28.dp), tint = MintLeaf)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// ── Alert Feed Card ─────────────────────────────────────────────

@Composable
private fun AlertFeedCard(
    alert: AlertItem,
    modifier: Modifier = Modifier,
) {
    val accentColor = when (alert.type) {
        AlertType.CONTRADICTION -> DangerRed
        AlertType.ACTION_EXECUTED -> SuccessGreen
        AlertType.ACTION_FAILED -> DangerRed
        AlertType.MARKET_ALERT -> WarningAmber
        AlertType.CASH_DANGER -> CriticalPulse
        AlertType.LEDGER_REVIEW -> WarningAmber
        AlertType.INSIGHT -> Turquoise
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Icon indicator
            Icon(
                imageVector = alert.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            PulsingDot(color = accentColor, size = 6.dp)
        }
    }
}
