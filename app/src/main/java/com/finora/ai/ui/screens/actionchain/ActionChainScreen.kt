package com.finora.ai.ui.screens.actionchain

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.*
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionChainScreen(sessionId: String, onExecuteChain: () -> Unit, onBack: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    val actions = remember { listOf(
        ActionItem(type=ActionType.UPDATE_RECORD, title="Validate Stock Count", description="Cross-reference Ghost Ledger count with warehouse records", status=ActionStatus.APPROVED),
        ActionItem(type=ActionType.NOTIFY, title="Notify Procurement Manager", description="Draft and send WhatsApp/email to procurement team about critical shortage", status=ActionStatus.APPROVED, requiresApproval=true),
        ActionItem(type=ActionType.SIMULATE, title="Emergency Order Simulation", description="Simulate emergency order within budget constraints", status=ActionStatus.APPROVED, constraints=ActionConstraints(estimatedCost=350000.0, requiresBudget=500000.0, budgetExceeded=true)),
        ActionItem(type=ActionType.UPDATE_RECORD, title="Update Delivery Estimates", description="Extend customer delivery timelines by +5 days", status=ActionStatus.APPROVED),
        ActionItem(type=ActionType.SCHEDULE, title="24-Hour Monitoring Alert", description="Schedule automated monitoring for inventory and cashflow", status=ActionStatus.APPROVED),
    )}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Action Chain", style=MaterialTheme.typography.titleLarge.copy(fontWeight=FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick=onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color=MaterialTheme.colorScheme.surface, shadowElevation=8.dp) {
                Button(
                    onClick=onExecuteChain,
                    modifier=Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding().height(54.dp),
                    shape=RoundedCornerShape(16.dp),
                    colors=ButtonDefaults.buttonColors(containerColor=MintLeaf, contentColor=InkBlack),
                ) { Text("Execute Chain", style=MaterialTheme.typography.labelLarge.copy(fontWeight=FontWeight.Bold, fontSize=16.sp)) }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding=PaddingValues(horizontal=20.dp, vertical=12.dp),
            verticalArrangement=Arrangement.spacedBy(0.dp),
        ) {
            item {
                AnimatedVisibility(showContent, enter=fadeIn(tween(400))+expandVertically()) {
                    Column {
                        Text("${actions.size} Actions Planned", style=MaterialTheme.typography.headlineSmall.copy(fontWeight=FontWeight.Bold), color=MaterialTheme.colorScheme.onBackground)
                        Text("Review and approve before execution", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
            itemsIndexed(actions) { index, action ->
                AnimatedVisibility(showContent, enter=fadeIn(tween(300, delayMillis=150+index*100))+slideInVertically(initialOffsetY={it/3}, animationSpec=spring(dampingRatio=Spring.DampingRatioLowBouncy))) {
                    ActionStepItem(action=action, index=index, isLast=index==actions.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun ActionStepItem(action: ActionItem, index: Int, isLast: Boolean) {
    val actionIcon = when(action.type) {
        ActionType.NOTIFY -> Icons.Filled.Email; ActionType.UPDATE_RECORD -> Icons.Filled.Description; ActionType.SIMULATE -> Icons.Filled.AutoAwesome
        ActionType.FETCH_DATA -> Icons.Filled.CellTower; ActionType.ALERT -> Icons.Filled.NotificationsActive; ActionType.SCHEDULE -> Icons.Filled.Schedule
    }
    Row(modifier=Modifier.fillMaxWidth()) {
        // Stepper line + circle
        Column(horizontalAlignment=Alignment.CenterHorizontally, modifier=Modifier.width(40.dp)) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(MintLeaf.copy(alpha=0.15f)), contentAlignment=Alignment.Center) {
                Text("${index+1}", style=MaterialTheme.typography.labelMedium.copy(fontWeight=FontWeight.Bold), color=MintLeaf)
            }
            if (!isLast) {
                Box(Modifier.width(2.dp).height(80.dp).background(MaterialTheme.colorScheme.outline.copy(alpha=0.2f)))
            }
        }
        Spacer(Modifier.width(12.dp))
        // Card
        Card(
            shape=RoundedCornerShape(14.dp),
            colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f)),
            modifier=Modifier.weight(1f).padding(bottom=if(isLast) 0.dp else 12.dp),
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Icon(actionIcon, contentDescription=null, modifier=Modifier.size(20.dp), tint=MintLeaf)
                    Spacer(Modifier.width(8.dp))
                    Text(action.title, style=MaterialTheme.typography.titleSmall.copy(fontWeight=FontWeight.SemiBold), color=MaterialTheme.colorScheme.onSurface, modifier=Modifier.weight(1f))
                    if (action.requiresApproval) {
                        AssistChip(onClick={}, label={ Text("Approval", style=MaterialTheme.typography.labelSmall) }, colors=AssistChipDefaults.assistChipColors(containerColor=WarningAmber.copy(alpha=0.15f), labelColor=WarningAmber), shape=RoundedCornerShape(8.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(action.description, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                if (action.constraints.budgetExceeded) {
                    Spacer(Modifier.height(8.dp))
                    Card(shape=RoundedCornerShape(8.dp), colors=CardDefaults.cardColors(DangerRed.copy(alpha=0.1f))) {
                        Row(Modifier.padding(8.dp), verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription=null, modifier=Modifier.size(14.dp), tint=DangerRed)
                            Spacer(Modifier.width(6.dp))
                            Text("Budget exceeded — auto-adjusted ₨500k → ₨350k", style=MaterialTheme.typography.labelSmall, color=DangerRed)
                        }
                    }
                }
            }
        }
    }
}
