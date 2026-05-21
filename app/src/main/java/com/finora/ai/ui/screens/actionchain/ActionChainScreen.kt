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
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════
// Action Chain Screen — Shows real action plan from backend
// Falls back to demo actions if backend data unavailable
// ═══════════════════════════════════════════════════════════════

data class ActionDisplayItem(
    val type: ActionType,
    val title: String,
    val description: String,
    val status: ActionStatus = ActionStatus.APPROVED,
    val requiresApproval: Boolean = false,
    val estimatedCost: Double? = null,
    val budgetExceeded: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionChainScreen(
    sessionId: String,
    viewModel: FinoraViewModel,
    onExecuteChain: () -> Unit,
    onBack: () -> Unit,
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    // Build action items from real backend data or fallback
    val backendActions = viewModel.analysisResult?.actionPlan
    val actions = remember(backendActions) {
        if (!backendActions.isNullOrEmpty()) {
            backendActions.map { apiAction ->
                val actionType = when (apiAction.type?.uppercase()) {
                    "NOTIFY" -> ActionType.NOTIFY
                    "UPDATE_RECORD" -> ActionType.UPDATE_RECORD
                    "SIMULATE" -> ActionType.SIMULATE
                    "FETCH_DATA" -> ActionType.FETCH_DATA
                    "ALERT" -> ActionType.ALERT
                    "SCHEDULE" -> ActionType.SCHEDULE
                    else -> ActionType.UPDATE_RECORD
                }
                val cost = apiAction.estimatedCost ?: 0.0
                ActionDisplayItem(
                    type = actionType,
                    title = apiAction.actionId?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
                        ?: apiAction.description?.take(40) ?: "Action",
                    description = apiAction.description ?: "",
                    status = if (apiAction.requiresApproval == true) ActionStatus.PENDING else ActionStatus.APPROVED,
                    requiresApproval = apiAction.requiresApproval == true,
                    estimatedCost = cost,
                    budgetExceeded = cost > 50000.0,
                )
            }
        } else {
            // Fallback demo actions
            listOf(
                ActionDisplayItem(type=ActionType.UPDATE_RECORD, title="Validate Stock Count", description="Cross-reference Ghost Ledger count with warehouse records"),
                ActionDisplayItem(type=ActionType.NOTIFY, title="Notify Procurement Manager", description="Draft and send WhatsApp/email to procurement team about critical shortage", requiresApproval=true),
                ActionDisplayItem(type=ActionType.SIMULATE, title="Emergency Order Simulation", description="Simulate emergency order within budget constraints", estimatedCost=350000.0, budgetExceeded=true),
                ActionDisplayItem(type=ActionType.UPDATE_RECORD, title="Update Delivery Estimates", description="Extend customer delivery timelines by +5 days"),
                ActionDisplayItem(type=ActionType.SCHEDULE, title="24-Hour Monitoring Alert", description="Schedule automated monitoring for inventory and cashflow"),
            )
        }
    }

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
                        Text(
                            if (backendActions != null) "Generated by Finora AI agents" else "Review and approve before execution",
                            style=MaterialTheme.typography.bodySmall,
                            color=MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
private fun ActionStepItem(action: ActionDisplayItem, index: Int, isLast: Boolean) {
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

                // Cost badge
                action.estimatedCost?.let { cost ->
                    if (cost > 0) {
                        Spacer(Modifier.height(8.dp))
                        Card(shape=RoundedCornerShape(8.dp), colors=CardDefaults.cardColors(
                            if (action.budgetExceeded) DangerRed.copy(alpha=0.1f) else MintLeaf.copy(alpha=0.1f)
                        )) {
                            Row(Modifier.padding(8.dp), verticalAlignment=Alignment.CenterVertically) {
                                Icon(
                                    if (action.budgetExceeded) Icons.Filled.Warning else Icons.Filled.AccountBalance,
                                    contentDescription=null,
                                    modifier=Modifier.size(14.dp),
                                    tint=if (action.budgetExceeded) DangerRed else MintLeaf
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (action.budgetExceeded) "Budget exceeded — cost: ₨${"%,.0f".format(cost)}"
                                    else "Estimated cost: ₨${"%,.0f".format(cost)}",
                                    style=MaterialTheme.typography.labelSmall,
                                    color=if (action.budgetExceeded) DangerRed else MintLeaf
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
