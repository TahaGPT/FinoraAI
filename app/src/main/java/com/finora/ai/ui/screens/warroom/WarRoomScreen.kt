package com.finora.ai.ui.screens.warroom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.*
import com.finora.ai.ui.components.PulsingDot
import com.finora.ai.ui.components.StressOMeter
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WarRoomScreen(
    sessionId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val traceEvents = remember { mutableStateListOf<AgentTraceEvent>() }
    var currentStep by remember { mutableIntStateOf(0) }
    var stressScore by remember { mutableIntStateOf(85) }
    var contradictionFlash by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val steps = listOf(
            AgentTraceEvent(agentName="Orchestrator", step="1/6", type=TraceEventType.STEP_START, message="Initializing analysis session..."),
            AgentTraceEvent(agentName="Ingestion Agent", step="2/6", type=TraceEventType.TOOL_CALL, message="Processing 6 data sources in parallel..."),
            AgentTraceEvent(agentName="Ingestion Agent", step="2/6", type=TraceEventType.STEP_COMPLETE, message="All sources ingested. 287 records extracted.", durationMs=1840),
            AgentTraceEvent(agentName="Insight Analyst", step="3/6", type=TraceEventType.INSIGHT_FOUND, message="Revenue declining 4.2% MoM"),
            AgentTraceEvent(agentName="Contradiction Detector", step="4/6", type=TraceEventType.CONTRADICTION_DETECTED, message="CONFLICT: CSV (500 units) vs Ledger (287 units)"),
            AgentTraceEvent(agentName="Contradiction Detector", step="4/6", type=TraceEventType.STEP_COMPLETE, message="Resolution: Ghost Ledger wins (recency)", durationMs=920),
            AgentTraceEvent(agentName="Action Planner", step="5/6", type=TraceEventType.STEP_COMPLETE, message="5 actions planned, 1 budget-adjusted", durationMs=680),
            AgentTraceEvent(agentName="Session", step="6/6", type=TraceEventType.SESSION_COMPLETE, message="Analysis complete — ready for review"),
        )
        for (step in steps) {
            delay(if (step.type == TraceEventType.CONTRADICTION_DETECTED) 800 else 1200)
            traceEvents.add(step)
            currentStep++
            if (step.type == TraceEventType.CONTRADICTION_DETECTED) { contradictionFlash = true; stressScore = 45; delay(1500); contradictionFlash = false }
            if (step.type == TraceEventType.SESSION_COMPLETE) { stressScore = 72; isComplete = true }
        }
        delay(1500); onComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wr")
    val bgPulse by infiniteTransition.animateFloat(0f, 2*PI.toFloat(), infiniteRepeatable(tween(6000, easing=LinearEasing)), label="bg")
    val flashAlpha by animateFloatAsState(if (contradictionFlash) 0.3f else 0f, tween(if (contradictionFlash) 200 else 800), label="flash")

    Box(modifier = Modifier.fillMaxSize().background(InkBlack)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..15) {
                val angle = bgPulse + i * (2*PI.toFloat()/16)
                val x = size.width*0.5f + sin(angle)*size.width*0.35f
                val y = size.height*0.3f + sin(angle*1.5f+i)*size.height*0.15f
                drawCircle(NeonMint.copy(alpha=0.15f+sin(angle)*0.1f), 2f+sin(angle*2)*1.5f, Offset(x,y))
            }
        }
        if (flashAlpha > 0f) Box(Modifier.fillMaxSize().background(DangerRed.copy(alpha=flashAlpha)))

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(if (isComplete) SuccessGreen else NeonMint, 8.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("WAR ROOM", style=MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.Bold, letterSpacing=3.sp), color=NeonMint)
                    }
                    Text("Session: $sessionId", style=MaterialTheme.typography.labelSmall, color=DarkTextTertiary)
                }
                IconButton(onClick=onBack) { Icon(Icons.Filled.Close, "Close", tint=DarkTextSecondary) }
            }
            Box(Modifier.fillMaxWidth().padding(vertical=8.dp), contentAlignment=Alignment.Center) { StressOMeter(stressScore, size=140.dp) }

            AnimatedVisibility(contradictionFlash, enter=fadeIn(tween(100))+slideInVertically{-it}, exit=fadeOut(tween(600))) {
                Card(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=8.dp), shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(DangerRed.copy(alpha=0.2f))) {
                    Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bolt, contentDescription=null, modifier=Modifier.size(24.dp), tint=DangerRed)
                        Spacer(Modifier.width(12.dp))
                        Text("CONFLICT DETECTED", style=MaterialTheme.typography.titleSmall.copy(fontWeight=FontWeight.Bold, letterSpacing=2.sp), color=DangerRed)
                    }
                }
            }

            Text("Agent Reasoning", style=MaterialTheme.typography.labelMedium.copy(letterSpacing=1.sp), color=DarkTextTertiary, modifier=Modifier.padding(horizontal=20.dp, vertical=8.dp))

            LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding=PaddingValues(horizontal=16.dp, vertical=4.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                itemsIndexed(traceEvents) { _, event ->
                    val accent = when(event.type) {
                        TraceEventType.STEP_START -> Turquoise; TraceEventType.STEP_COMPLETE -> SuccessGreen
                        TraceEventType.TOOL_CALL -> PearlAqua; TraceEventType.INSIGHT_FOUND -> MintLeaf
                        TraceEventType.CONTRADICTION_DETECTED -> DangerRed; TraceEventType.ACTION_EXECUTING -> WarningAmber
                        TraceEventType.ACTION_FAILED -> DangerRed; TraceEventType.SESSION_COMPLETE -> NeonMint
                    }
                    val traceIcon: ImageVector = when(event.type) {
                        TraceEventType.STEP_START -> Icons.Filled.PlayCircle
                        TraceEventType.STEP_COMPLETE -> Icons.Filled.CheckCircle
                        TraceEventType.TOOL_CALL -> Icons.Filled.Build
                        TraceEventType.INSIGHT_FOUND -> Icons.Filled.Lightbulb
                        TraceEventType.CONTRADICTION_DETECTED -> Icons.Filled.Bolt
                        TraceEventType.ACTION_EXECUTING -> Icons.Filled.Sync
                        TraceEventType.ACTION_FAILED -> Icons.Filled.Error
                        TraceEventType.SESSION_COMPLETE -> Icons.Filled.TaskAlt
                    }
                    Card(shape=RoundedCornerShape(12.dp), colors=CardDefaults.cardColors(DarkSurfaceVariant.copy(alpha=0.7f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment=Alignment.Top) {
                            Icon(traceIcon, contentDescription=null, modifier=Modifier.size(16.dp), tint=accent)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(event.agentName, style=MaterialTheme.typography.labelMedium.copy(fontWeight=FontWeight.Bold), color=accent)
                                    Text(event.step, style=MaterialTheme.typography.labelSmall, color=DarkTextTertiary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(event.message, style=MaterialTheme.typography.bodySmall, color=DarkTextSecondary)
                                event.durationMs?.let {
                                    Row(Modifier.padding(top=4.dp), verticalAlignment=Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Timer, contentDescription=null, modifier=Modifier.size(10.dp), tint=DarkTextTertiary)
                                        Spacer(Modifier.width(4.dp))
                                        Text("${it}ms", style=MaterialTheme.typography.labelSmall, color=DarkTextTertiary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val progress by animateFloatAsState(currentStep/8f, tween(500, easing=FastOutSlowInEasing), label="p")
            Column(Modifier.padding(horizontal=20.dp, vertical=12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        if (isComplete) Icon(Icons.Filled.CheckCircle, contentDescription=null, modifier=Modifier.size(14.dp), tint=SuccessGreen)
                        else Icon(Icons.Filled.Sync, contentDescription=null, modifier=Modifier.size(14.dp), tint=DarkTextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(if(isComplete) "Complete" else "Processing...", style=MaterialTheme.typography.labelSmall, color=if(isComplete) SuccessGreen else DarkTextSecondary)
                    }
                    Text("${(progress*100).toInt()}%", style=MaterialTheme.typography.labelSmall.copy(fontWeight=FontWeight.Bold), color=NeonMint)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress={progress}, Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color=NeonMint, trackColor=DarkBorder)
            }
        }
    }
}
