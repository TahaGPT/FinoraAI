package com.finora.ai.data.network

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════
// API Request / Response DTOs — matches backend Pydantic models
// ═══════════════════════════════════════════════════════════════

// ── Requests ────────────────────────────────────────────────────

data class DocumentInput(
    val id: String,
    @SerializedName("source_type") val sourceType: String,
    @SerializedName("raw_content") val rawContent: String,
    @SerializedName("normalized_content") val normalizedContent: Map<String, Any> = emptyMap(),
    @SerializedName("credibility_score") val credibilityScore: Double = 0.8,
    @SerializedName("document_timestamp") val documentTimestamp: String,
    @SerializedName("staleness_score") val stalenessScore: Double = 0.0,
)

data class AnalysisRequest(
    val documents: List<DocumentInput>,
    @SerializedName("user_query") val userQuery: String = "Analyze my financial situation",
)

data class ApprovalRequest(
    @SerializedName("session_id") val sessionId: String,
    val approved: Boolean,
    @SerializedName("user_comment") val userComment: String = "",
)

// ── Responses ───────────────────────────────────────────────────

data class HealthResponse(
    val status: String,
    val pipeline: String,
)

data class RootResponse(
    val status: String,
    val version: String,
    val agents: String,
)

data class KeySignalResponse(
    val metric: String,
    val value: String,
)

data class InsightReportResponse(
    val risks: List<String> = emptyList(),
    val opportunities: List<String> = emptyList(),
    val contradictions: List<String> = emptyList(),
    @SerializedName("key_signals") val keySignals: List<KeySignalResponse> = emptyList(),
)

data class ActionItemResponse(
    @SerializedName("action_id") val actionId: String,
    val type: String,
    val description: String,
    val status: String,
    val dependencies: List<String> = emptyList(),
    @SerializedName("rollback_action") val rollbackAction: String? = null,
)

data class AnalysisSessionResponse(
    @SerializedName("session_id") val sessionId: String,
    val status: String,
    @SerializedName("current_step") val currentStep: String?,
    @SerializedName("insight_report") val insightReport: InsightReportResponse?,
    @SerializedName("action_plan") val actionPlan: List<ActionItemResponse> = emptyList(),
    val message: String?,
)

data class ApprovalResponse(
    @SerializedName("session_id") val sessionId: String,
    val status: String,
    @SerializedName("current_step") val currentStep: String? = null,
    @SerializedName("execution_logs") val executionLogs: List<Map<String, Any>> = emptyList(),
    @SerializedName("failed_steps") val failedSteps: List<String> = emptyList(),
    val success: Boolean? = null,
    val message: String?,
    val comment: String? = null,
)

data class SessionStatusResponse(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("current_step") val currentStep: String?,
    @SerializedName("risks_count") val risksCount: Int = 0,
    @SerializedName("actions_count") val actionsCount: Int = 0,
    @SerializedName("failed_steps") val failedSteps: List<String> = emptyList(),
    @SerializedName("execution_log") val executionLog: List<Map<String, Any>> = emptyList(),
)
