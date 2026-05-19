package com.finora.ai.ui.screens.ghostledger

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.LedgerCell
import com.finora.ai.ui.components.ConfidenceHaloCell
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostLedgerReviewScreen(extractionId: String, onConfirm: () -> Unit, onBack: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); showContent = true }

    val headers = listOf("Date", "Description", "Amount (₨)", "Type")
    val mockCells = remember { listOf(
        listOf(LedgerCell(0,0,"12/05",0.95f), LedgerCell(0,1,"Cloth shipment",0.88f), LedgerCell(0,2,"45,000",0.92f), LedgerCell(0,3,"Credit",0.97f)),
        listOf(LedgerCell(1,0,"13/05",0.91f), LedgerCell(1,1,"Dye chemicals",0.72f,true), LedgerCell(1,2,"12,500",0.85f), LedgerCell(1,3,"Debit",0.93f)),
        listOf(LedgerCell(2,0,"14/05",0.88f), LedgerCell(2,1,"Labour wages",0.94f), LedgerCell(2,2,"28,000",0.90f), LedgerCell(2,3,"Debit",0.96f)),
        listOf(LedgerCell(3,0,"15/05",0.60f,true), LedgerCell(3,1,"Misc supplies",0.55f,true), LedgerCell(3,2,"8,???",0.42f,true), LedgerCell(3,3,"Debit",0.65f,true)),
        listOf(LedgerCell(4,0,"16/05",0.93f), LedgerCell(4,1,"Customer payment",0.89f), LedgerCell(4,2,"92,000",0.95f), LedgerCell(4,3,"Credit",0.98f)),
    )}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Ghost Ledger Review", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)); Text("Tap red cells to correct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(Modifier.padding(20.dp).navigationBarsPadding()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Overall confidence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("78%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = WarningAmber)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("3 cells need review · SHA-256 hash will be written to blockchain", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onConfirm, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack)) {
                        Text("Confirm & Add to Session", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Legend
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400)) + expandVertically()) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), Arrangement.spacedBy(16.dp)) {
                        LegendChip("≥ 0.9", ConfidenceHigh)
                        LegendChip("0.7-0.9", ConfidenceMedium)
                        LegendChip("< 0.7", ConfidenceLow, showWarning = true)
                    }
                }
            }
            // Table header
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 100))) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 4.dp)) {
                        headers.forEach { h ->
                            Box(Modifier.width(110.dp).padding(4.dp)) {
                                Text(h, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            // Table rows
            mockCells.forEachIndexed { rowIdx, row ->
                item {
                    AnimatedVisibility(showContent, enter = fadeIn(tween(300, delayMillis = 200 + rowIdx * 80)) + slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            row.forEach { cell ->
                                ConfidenceHaloCell(confidence = cell.confidence, modifier = Modifier.width(110.dp).padding(3.dp)) {
                                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cell.value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                            if (cell.needsReview) Icon(Icons.Filled.Edit, "Edit", Modifier.size(14.dp), tint = ConfidenceLow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(label: String, color: androidx.compose.ui.graphics.Color, showWarning: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.3f)).border(1.5.dp, color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showWarning) {
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(10.dp), tint = ConfidenceLow)
        }
    }
}
