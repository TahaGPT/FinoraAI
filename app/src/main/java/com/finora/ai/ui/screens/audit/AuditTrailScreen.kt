package com.finora.ai.ui.screens.audit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.AuditEntry
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditTrailScreen(
    viewModel: FinoraViewModel,
    onEntryClick: (String) -> Unit, 
    onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { 
        delay(100)
        showContent = true 
        viewModel.fetchAuditTrail()
    }

    val realEntries: List<AuditEntry> = viewModel.auditEntries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(22.dp), tint = MintLeaf)
                        Spacer(Modifier.width(8.dp))
                        Text("Audit Trail", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.FilterList, "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400)) + expandVertically()) {
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MintLeaf.copy(alpha = 0.08f))) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(28.dp), tint = MintLeaf)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Blockchain Secured", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MintLeaf)
                                Text("${realEntries.size} entries on Mumbai Testnet · Immutable audit trail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            itemsIndexed(realEntries) { index, entry ->
                AnimatedVisibility(showContent, enter = fadeIn(tween(300, delayMillis = 100 + index * 60)) + slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                    val typeColor = when (entry.actionType) {
                        "OCR_COMPLETE" -> PearlAqua; "CONTRADICTION_RESOLVED" -> WarningAmber
                        "ACTION_EXECUTED" -> SuccessGreen; "SIMULATION_RUN" -> Turquoise
                        "SESSION_COMPLETE" -> MintLeaf; else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val typeIcon: ImageVector = when (entry.actionType) {
                        "OCR_COMPLETE" -> Icons.Filled.CameraAlt
                        "CONTRADICTION_RESOLVED" -> Icons.Filled.Bolt
                        "ACTION_EXECUTED" -> Icons.Filled.CheckCircle
                        "SIMULATION_RUN" -> Icons.Filled.AutoAwesome
                        "SESSION_COMPLETE" -> Icons.Filled.Flag
                        else -> Icons.Filled.Info
                    }
                    Card(
                        onClick = { onEntryClick(entry.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp)).background(typeColor))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(typeIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = typeColor)
                                    Spacer(Modifier.width(6.dp))
                                    Text(entry.actionType.replace("_", " "), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = typeColor)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(entry.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(6.dp))
                                Text("TX: ${entry.txHash}", style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Details", modifier = Modifier.size(20.dp), tint = DarkTextTertiary)
                        }
                    }
                }
            }

            // Export button
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 600))) {
                    OutlinedButton(
                        onClick = {}, Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MintLeaf),
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export Audit PDF", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
