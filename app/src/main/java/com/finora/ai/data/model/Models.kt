package com.finora.ai.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

// ═══════════════════════════════════════════════════════════════
// FinoraAI Data Models — Core domain entities
// ═══════════════════════════════════════════════════════════════

// ── Source & Ingestion ──────────────────────────────────────────

enum class SourceType(val label: String, val icon: ImageVector) {
    PDF("PDF / Report", Icons.Filled.PictureAsPdf),
    URL("Website / Article", Icons.Filled.Language),
    CSV("CSV / JSON", Icons.Filled.TableChart),
    REAL_TIME("Live Feed", Icons.Filled.CellTower),
    GHOST_LEDGER("Handwritten Ledger", Icons.Filled.CameraAlt),
    GOOGLE_SHEETS("Google Sheets", Icons.Filled.GridOn),
}

data class FinancialDocument(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val sourceType: SourceType,
    val title: String,
    val contentSummary: String = "",
    val credibilityScore: Float = 0.8f,
    val stalenessScore: Float = 0f,
    val documentTimestamp: Long = System.currentTimeMillis(),
    val ingestedAt: Long = System.currentTimeMillis(),
)

// ── Analysis Session ────────────────────────────────────────────

enum class SessionStatus {
    CREATED, INGESTING, ANALYZING, CONTRADICTIONS_FOUND,
    PLANNING_ACTIONS, EXECUTING, COMPLETED, FAILED
}

data class AnalysisSession(
    val id: String = UUID.randomUUID().toString(),
    val status: SessionStatus = SessionStatus.CREATED,
    val documents: List<FinancialDocument> = emptyList(),
    val insightReport: InsightReport? = null,
    val actionPlan: ActionPlan? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)

// ── Insights ────────────────────────────────────────────────────

data class InsightReport(
    val keySignals: List<Signal> = emptyList(),
    val risks: List<Risk> = emptyList(),
    val opportunities: List<Opportunity> = emptyList(),
    val contradictions: List<Contradiction> = emptyList(),
)

data class Signal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val severity: Severity = Severity.INFO,
    val sourceDocs: List<String> = emptyList(),
)

data class Risk(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val probability: Float = 0.5f,
    val impact: Impact = Impact.MEDIUM,
)

data class Opportunity(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val potentialValue: String = "",
    val confidence: Float = 0.7f,
)

data class Contradiction(
    val id: String = UUID.randomUUID().toString(),
    val sourceA: String,
    val sourceB: String,
    val field: String,
    val valueA: String,
    val valueB: String,
    val resolution: ContradictionResolution? = null,
)

data class ContradictionResolution(
    val winner: String,
    val reason: String,
    val confidence: Float,
)

enum class Severity { INFO, WARNING, CRITICAL }
enum class Impact { LOW, MEDIUM, HIGH, CRITICAL }

// ── Action Chain ────────────────────────────────────────────────

data class ActionPlan(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val actions: List<ActionItem> = emptyList(),
)

data class ActionItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ActionType,
    val title: String,
    val description: String,
    val dependencies: List<String> = emptyList(),
    val constraints: ActionConstraints = ActionConstraints(),
    val status: ActionStatus = ActionStatus.PENDING,
    val rollbackAction: String? = null,
    val requiresApproval: Boolean = false,
    val retryCount: Int = 0,
    val result: String? = null,
    val errorMessage: String? = null,
)

enum class ActionType { NOTIFY, UPDATE_RECORD, SIMULATE, FETCH_DATA, ALERT, SCHEDULE }
enum class ActionStatus { PENDING, APPROVED, QUEUED, RUNNING, SUCCESS, FAILED, REJECTED, ROLLED_BACK }

data class ActionConstraints(
    val estimatedCost: Double? = null,
    val estimatedDuration: String? = null,
    val requiresBudget: Double? = null,
    val budgetExceeded: Boolean = false,
)

// ── Simulation ──────────────────────────────────────────────────

data class SimulationScenario(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val revenueChange: Float = 0f,
    val costChange: Float = 0f,
    val hireCount: Int = 0,
    val horizonMonths: Int = 12,
    val isStressTest: Boolean = false,
)

data class SimulationResult(
    val scenarioId: String,
    val scenarioName: String,
    val p10Runway: Int = 0,
    val p50Runway: Int = 0,
    val p90Runway: Int = 0,
    val finalBalance: Double = 0.0,
    val defaultProbability: Float = 0f,
    val riskScore: Float = 0f,
    val cashflowCurve: List<Double> = emptyList(),
    val isWinner: Boolean = false,
    val confidencePercent: Int = 0,
)

// ── Audit Trail ─────────────────────────────────────────────────

data class AuditEntry(
    val id: String = UUID.randomUUID().toString(),
    val txHash: String,
    val documentHash: String,
    val actionType: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val etherscanUrl: String? = null,
    val reasoningTrace: String? = null,
)

// ── Agent Trace (War Room) ──────────────────────────────────────

data class AgentTraceEvent(
    val id: String = UUID.randomUUID().toString(),
    val agentName: String,
    val step: String,
    val type: TraceEventType,
    val message: String,
    val durationMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class TraceEventType {
    STEP_START, STEP_COMPLETE, TOOL_CALL,
    INSIGHT_FOUND, CONTRADICTION_DETECTED,
    ACTION_EXECUTING, ACTION_FAILED,
    SESSION_COMPLETE
}

// ── KPI Data ────────────────────────────────────────────────────

data class KPIData(
    val runwayDays: Int = 0,
    val currentBalance: Double = 0.0,
    val monthlyBurn: Double = 0.0,
    val momChange: Float = 0f,
    val healthScore: Int = 72,
)

// ── Ghost Ledger OCR ────────────────────────────────────────────

data class LedgerCell(
    val row: Int,
    val col: Int,
    val value: String,
    val confidence: Float,
    val needsReview: Boolean = false,
    val correctedValue: String? = null,
)

data class LedgerExtraction(
    val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val cells: List<LedgerCell> = emptyList(),
    val overallConfidence: Float = 0f,
    val headers: List<String> = emptyList(),
    val documentHash: String? = null,
    val blockchainTxHash: String? = null,
)

// ── Alert Feed ──────────────────────────────────────────────────

data class AlertItem(
    val id: String = UUID.randomUUID().toString(),
    val type: AlertType,
    val title: String,
    val message: String,
    val icon: ImageVector = Icons.Filled.Info,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
)

enum class AlertType {
    CONTRADICTION, ACTION_EXECUTED, ACTION_FAILED,
    MARKET_ALERT, CASH_DANGER, LEDGER_REVIEW, INSIGHT
}
