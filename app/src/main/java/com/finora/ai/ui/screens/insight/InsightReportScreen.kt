package com.finora.ai.ui.screens.insight

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.finora.ai.data.network.KeySignalResponse
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.FinoraViewModel
import com.finora.ai.viewmodel.PipelineState
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════
// Insight Report Screen — Shows real analysis results from backend
// Falls back to demo data if backend response is unavailable
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightReportScreen(
    sessionId: String,
    viewModel: FinoraViewModel,
    onGenerateActionPlan: () -> Unit,
    onBack: () -> Unit,
) {
    val tabs = listOf("Insights", "Contradictions", "Risks", "Opportunities")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    // Extract real data from ViewModel (or use fallback)
    val insightReport = viewModel.analysisResult?.insightReport
    val keySignals = insightReport?.keySignals ?: emptyList()
    val risks = insightReport?.risks ?: listOf(
        "Stockout imminent — 1.6 days until zero inventory at current velocity",
        "Cashflow squeeze — 60-day runway if current burn continues",
        "Supply chain disruption — port strike affecting textile imports"
    )
    val opportunities = insightReport?.opportunities ?: listOf(
        "Bulk discount available — supplier offering 8% on 1000+ units",
        "New market segment — competitor exit in Lahore region"
    )
    val contradictions = insightReport?.contradictions ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insight Report", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Button(
                    onClick = onGenerateActionPlan,
                    modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack),
                ) { Text("Generate Action Plan", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)) }
            }
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MintLeaf,
                edgePadding = 16.dp,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = MintLeaf,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Content based on selected tab
            AnimatedContent(targetState = selectedTab, label = "tabContent") { page ->
                when (page) {
                    0 -> InsightsTab(showContent, keySignals)
                    1 -> ContradictionsTab(showContent, contradictions)
                    2 -> RisksTab(showContent, risks)
                    3 -> OpportunitiesTab(showContent, opportunities)
                }
            }
        }
    }
}

@Composable
private fun InsightsTab(show: Boolean, keySignals: List<KeySignalResponse>) {
    data class InsightItem(val icon: ImageVector, val title: String, val desc: String)

    // Build insight items from real backend signals or use fallback
    val insights = if (keySignals.isNotEmpty()) {
        keySignals.mapIndexed { index, signal ->
            val metric = signal.metric
            val value = signal.value
            val icon = when {
                metric.contains("stock", true) || metric.contains("inventory", true) -> Icons.Filled.Warning
                metric.contains("revenue", true) || metric.contains("sales", true) -> Icons.Filled.ArrowDownward
                metric.contains("burn", true) || metric.contains("cost", true) -> Icons.Filled.Whatshot
                else -> Icons.Filled.Lightbulb
            }
            InsightItem(icon, metric.replace("_", " ").replaceFirstChar { it.uppercase() }, "Current value: $value")
        }
    } else {
        listOf(
            InsightItem(Icons.Filled.ArrowDownward, "Revenue Declining", "Revenue down 4.2% MoM — seasonal Q3 pattern detected across 3 years of data"),
            InsightItem(Icons.Filled.Whatshot, "Burn Rate Warning", "Monthly burn (₨780k) exceeds revenue by 12%. Projected break-even pushed to Q1 2027"),
            InsightItem(Icons.Filled.Warning, "Inventory Critical", "At 180 units/day velocity, stockout in 1.6 days. Immediate restocking required"),
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(insights) { i, item ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(item.icon, item.title, item.desc, MintLeaf)
            }
        }
    }
}

@Composable
private fun ContradictionsTab(show: Boolean, contradictions: List<Map<String, Any?>>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (contradictions.isNotEmpty()) {
            // Show real contradictions from backend
            itemsIndexed(contradictions) { i, c ->
                val sourceA = c["source_a"]?.toString() ?: "Source A"
                val sourceB = c["source_b"]?.toString() ?: "Source B"
                val field = c["field"]?.toString() ?: "Unknown"
                val valueA = c["value_a"]?.toString() ?: "—"
                val valueB = c["value_b"]?.toString() ?: "—"

                AnimatedVisibility(show, enter = fadeIn(tween(400, delayMillis = i * 150)) + expandVertically()) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(DangerRed.copy(alpha = 0.08f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                field.replace("_", " ").replaceFirstChar { it.uppercase() } + " Mismatch",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DangerRed
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(sourceA.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(valueA, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(SuccessGreen.copy(alpha = 0.1f))) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sourceB.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = SuccessGreen)
                                        }
                                        Text(valueB, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Fallback demo contradiction
            item {
                AnimatedVisibility(show, enter = fadeIn(tween(400)) + expandVertically()) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(DangerRed.copy(alpha = 0.08f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Stock Count Mismatch", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Warehouse CSV", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("500 units", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                        Text("3 weeks old", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                                    }
                                }
                                Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(SuccessGreen.copy(alpha = 0.1f))) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Ghost Ledger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = SuccessGreen)
                                        }
                                        Text("287 units", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                                        Text("1 day old", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Resolution: Ghost Ledger wins — higher recency (1d vs 21d) + manual verification. Confidence: 87%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RisksTab(show: Boolean, risks: List<String>) {
    val riskIcons = listOf(Icons.Filled.Warning, Icons.Filled.AccountBalance, Icons.Filled.Language, Icons.Filled.Error, Icons.Filled.LocalFireDepartment)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(risks) { i, risk ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(riskIcons.getOrElse(i) { Icons.Filled.Warning }, "Risk ${i + 1}", risk, DangerRed)
            }
        }
    }
}

@Composable
private fun OpportunitiesTab(show: Boolean, opportunities: List<String>) {
    val oppIcons = listOf(Icons.Filled.Star, Icons.Filled.ArrowUpward, Icons.Filled.EmojiEvents, Icons.Filled.TrendingUp)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(opportunities) { i, opp ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(oppIcons.getOrElse(i) { Icons.Filled.Star }, "Opportunity ${i + 1}", opp, Turquoise)
            }
        }
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, desc: String, accent: Color) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.width(4.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(accent))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = accent)
                    Spacer(Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(6.dp))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
