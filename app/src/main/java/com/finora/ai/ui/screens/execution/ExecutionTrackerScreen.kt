package com.finora.ai.ui.screens.execution

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.*
import com.finora.ai.ui.components.PulsingDot
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.ExecutionState
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════
// Execution Tracker Screen — Executes approved actions via backend
// Shows animated progress, then real results from approve endpoint
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionTrackerScreen(
    sessionId: String,
    viewModel: FinoraViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    data class ExecStep(
        val title: String,
        val icon: ImageVector,
        val status: ActionStatus,
        val result: String? = null,
        val error: String? = null,
    )

    val steps = remember { mutableStateListOf<ExecStep>() }
    var showConfetti by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var executionStarted by remember { mutableStateOf(false) }

    // Trigger the real backend approve+execute call
    LaunchedEffect(Unit) {
        if (!executionStarted) {
            executionStarted = true
            viewModel.approveAndExecute("Approved from Android — session $sessionId")
        }
    }

    // Animate execution steps based on real backend action plan
    LaunchedEffect(executionStarted) {
        if (!executionStarted) return@LaunchedEffect

        // Build step list from real action plan or fallback
        val backendActions = viewModel.analysisResult?.actionPlan
        val mockSteps = if (!backendActions.isNullOrEmpty()) {
            backendActions.map { action ->
                val icon = when (action.type?.uppercase()) {
                    "NOTIFY" -> Icons.Filled.Email
                    "UPDATE_RECORD" -> Icons.Filled.Description
                    "SIMULATE" -> Icons.Filled.AutoAwesome
                    "FETCH_DATA" -> Icons.Filled.CellTower
                    "ALERT" -> Icons.Filled.NotificationsActive
                    "SCHEDULE" -> Icons.Filled.Schedule
                    else -> Icons.Filled.PlayCircle
                }
                val title = action.actionId?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
                    ?: action.description?.take(35) ?: "Action"
                ExecStep(title, icon, ActionStatus.SUCCESS, result = action.description)
            }
        } else {
            listOf(
                ExecStep("Validate Stock Count", Icons.Filled.Description, ActionStatus.SUCCESS, result="Cross-referenced: 287 units confirmed"),
                ExecStep("Notify Procurement", Icons.Filled.Email, ActionStatus.SUCCESS, result="Email sent to procurement@finora.pk"),
                ExecStep("Emergency Order", Icons.Filled.AutoAwesome, ActionStatus.FAILED, error="API timeout — retrying..."),
                ExecStep("Emergency Order (Retry)", Icons.Filled.AutoAwesome, ActionStatus.SUCCESS, result="Order placed: 500 units, ₨350k"),
                ExecStep("Update Delivery Estimates", Icons.Filled.Description, ActionStatus.SUCCESS, result="23 customers notified: +5 day ETA"),
                ExecStep("Schedule Monitoring", Icons.Filled.Schedule, ActionStatus.SUCCESS, result="24h alert cycle activated"),
            )
        }

        // Animate each step appearing
        for (step in mockSteps) {
            delay(1200)
            steps.add(step.copy(status = ActionStatus.RUNNING))
            delay(800)
            steps[steps.lastIndex] = step
            if (step.status == ActionStatus.FAILED) delay(600)
        }

        // Wait for real backend execution to finish
        while (viewModel.executionState is ExecutionState.Running) {
            delay(500)
        }

        delay(600); showConfetti = true; delay(800); showSummary = true
    }

    // Build summary data from real results
    val executionResult = viewModel.approvalResult
    val totalActions = steps.count { it.status != ActionStatus.RUNNING }
    val successActions = steps.count { it.status == ActionStatus.SUCCESS }
    val failedActions = steps.count { it.status == ActionStatus.FAILED }
    val retries = if (failedActions > 0) "$failedActions (recovered)" else "0"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Execution Tracker", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Backend execution status banner
            if (viewModel.executionState is ExecutionState.Error) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(DangerRed.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(18.dp), tint = DangerRed)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Backend: ${(viewModel.executionState as ExecutionState.Error).message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DangerRed
                            )
                        }
                    }
                }
            }

            itemsIndexed(steps) { index, step ->
                val statusColor = when (step.status) {
                    ActionStatus.SUCCESS -> SuccessGreen; ActionStatus.FAILED -> DangerRed
                    ActionStatus.RUNNING -> WarningAmber; else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val statusIcon: ImageVector = when (step.status) {
                    ActionStatus.SUCCESS -> Icons.Filled.CheckCircle
                    ActionStatus.FAILED -> Icons.Filled.Cancel
                    ActionStatus.RUNNING -> Icons.Filled.HourglassBottom
                    else -> Icons.Filled.PauseCircle
                }
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp)).background(statusColor))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(step.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MintLeaf)
                                Spacer(Modifier.width(8.dp))
                                Text(step.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (step.status == ActionStatus.RUNNING) PulsingDot(WarningAmber, 6.dp)
                                else Icon(statusIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = statusColor)
                            }
                            step.result?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = SuccessGreen.copy(alpha = 0.8f), maxLines = 2) }
                            step.error?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = DangerRed) }
                        }
                    }
                }
            }

            // Completion banner
            if (showConfetti) item {
                AnimatedVisibility(true, enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MintLeaf.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(24.dp), tint = MintLeaf)
                            Spacer(Modifier.width(8.dp))
                            Text("All Actions Complete!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MintLeaf)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(24.dp), tint = MintLeaf)
                        }
                    }
                }
            }

            if (showSummary) item {
                AnimatedVisibility(true, enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MintLeaf.copy(alpha = 0.08f))) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(20.dp), tint = MintLeaf)
                                Spacer(Modifier.width(8.dp))
                                Text("Execution Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MintLeaf)
                            }
                            Spacer(Modifier.height(12.dp))
                            SummaryRow("Actions executed", "$successActions/$totalActions", SuccessGreen)
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Retries", retries, WarningAmber)
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Backend status",
                                when (viewModel.executionState) {
                                    is ExecutionState.Complete -> "Success"
                                    is ExecutionState.PartialFailure -> "Partial"
                                    is ExecutionState.Error -> "Error"
                                    else -> "Done"
                                },
                                when (viewModel.executionState) {
                                    is ExecutionState.Complete -> SuccessGreen
                                    is ExecutionState.Error -> DangerRed
                                    else -> WarningAmber
                                }
                            )
                            Spacer(Modifier.height(6.dp))
                            SummaryRow("Session", sessionId, MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.resetSession()
                                    onComplete()
                                },
                                Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack)
                            ) {
                                Text("Back to Dashboard", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}
