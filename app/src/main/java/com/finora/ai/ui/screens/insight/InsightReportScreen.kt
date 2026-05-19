package com.finora.ai.ui.screens.insight

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InsightReportScreen(
    sessionId: String,
    onGenerateActionPlan: () -> Unit,
    onBack: () -> Unit,
) {
    val tabs = listOf("Insights", "Contradictions", "Risks", "Opportunities")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

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
                    0 -> InsightsTab(showContent)
                    1 -> ContradictionsTab(showContent)
                    2 -> RisksTab(showContent)
                    3 -> OpportunitiesTab(showContent)
                }
            }
        }
    }
}

@Composable
private fun InsightsTab(show: Boolean) {
    data class InsightItem(val icon: ImageVector, val title: String, val desc: String)
    val insights = listOf(
        InsightItem(Icons.Filled.ArrowDownward, "Revenue Declining", "Revenue down 4.2% MoM — seasonal Q3 pattern detected across 3 years of data"),
        InsightItem(Icons.Filled.Whatshot, "Burn Rate Warning", "Monthly burn (₨780k) exceeds revenue by 12%. Projected break-even pushed to Q1 2027"),
        InsightItem(Icons.Filled.Warning, "Inventory Critical", "At 180 units/day velocity, stockout in 1.6 days. Immediate restocking required"),
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(insights) { i, item ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(item.icon, item.title, item.desc, MintLeaf)
            }
        }
    }
}

@Composable
private fun ContradictionsTab(show: Boolean) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun RisksTab(show: Boolean) {
    data class RiskItem(val icon: ImageVector, val title: String, val desc: String)
    val risks = listOf(
        RiskItem(Icons.Filled.Warning, "Stockout Imminent", "1.6 days until zero inventory at current velocity. Revenue loss: ₨3.2M/week"),
        RiskItem(Icons.Filled.AccountBalance, "Cashflow Squeeze", "60-day runway if current burn continues. Seasonal dip amplifies risk"),
        RiskItem(Icons.Filled.Language, "Supply Chain Disruption", "Port strike affecting textile imports — 2-4 week delay expected"),
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(risks) { i, item ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(item.icon, item.title, item.desc, DangerRed)
            }
        }
    }
}

@Composable
private fun OpportunitiesTab(show: Boolean) {
    data class OppItem(val icon: ImageVector, val title: String, val desc: String)
    val opps = listOf(
        OppItem(Icons.Filled.Star, "Bulk Discount Available", "Supplier offering 8% discount on 1000+ unit orders — saves ₨280k"),
        OppItem(Icons.Filled.ArrowUpward, "New Market Segment", "Competitor exit in Lahore region — potential 15% revenue increase"),
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(opps) { i, item ->
            AnimatedVisibility(show, enter = fadeIn(tween(300, delayMillis = i * 100)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                InsightCard(item.icon, item.title, item.desc, Turquoise)
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
