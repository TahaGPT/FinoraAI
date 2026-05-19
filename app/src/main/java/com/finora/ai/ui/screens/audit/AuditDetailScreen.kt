package com.finora.ai.ui.screens.audit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDetailScreen(entryId: String, onBack: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); showContent = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Entry", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header card
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MintLeaf.copy(alpha = 0.08f))) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = SuccessGreen)
                                Spacer(Modifier.width(8.dp))
                                Text("ACTION EXECUTED", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = SuccessGreen)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Emergency procurement order placed — ₨350k", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Details
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 100)) + expandVertically()) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow("Transaction Hash", "0x2b5a...9e1d")
                            DetailRow("Document Hash", "sha256:f7e8d...")
                            DetailRow("Block Number", "#42,891,023")
                            DetailRow("Network", "Polygon Mumbai Testnet")
                            DetailRow("Timestamp", "May 19, 2026 · 14:32:18 UTC")
                            DetailRow("Gas Used", "0.00021 MATIC")
                        }
                    }
                }
            }

            // Reasoning trace
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 200)) + expandVertically()) {
                    Column {
                        Text("Reasoning Trace", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(8.dp))
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TraceStep("1", "Insight Analyst detected critical inventory shortage (287 units, 1.6d runway)")
                                TraceStep("2", "Action Planner proposed emergency order: 500 units @ ₨500k")
                                TraceStep("3", "Constraint Validator: BUDGET EXCEEDED — auto-adjusted to ₨350k (70% of original)")
                                TraceStep("4", "Action Executor: API call to procurement system — success after 1 retry")
                                TraceStep("5", "Blockchain audit logger: TX confirmed on block #42,891,023")
                            }
                        }
                    }
                }
            }

            // Etherscan button
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 300))) {
                    Button(
                        onClick = { /* Open Etherscan */ },
                        Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open", Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View on PolygonScan", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TraceStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(MintLeaf.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Text(number, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MintLeaf)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
