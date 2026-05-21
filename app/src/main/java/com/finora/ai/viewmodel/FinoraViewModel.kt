package com.finora.ai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.ai.data.model.*
import com.finora.ai.data.network.*
import com.finora.ai.data.repository.FinoraRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the full FinoraAI analysis pipeline.
 * Scoped to the activity so it persists across navigation destinations.
 *
 * Flow: SourceSelection → WarRoom → InsightReport → ActionChain → ExecutionTracker
 */
class FinoraViewModel : ViewModel() {

    private val repository = FinoraRepository()

    // ── Pipeline State ──────────────────────────────────────────

    var pipelineState by mutableStateOf<PipelineState>(PipelineState.Idle)
        private set

    var executionState by mutableStateOf<ExecutionState>(ExecutionState.Idle)
        private set

    var isBackendReachable by mutableStateOf(false)
        private set

    var userEmail by mutableStateOf("taha@finora.ai") // Default simulation user
        private set

    // ── Stored Results ──────────────────────────────────────────

    var currentSessionId by mutableStateOf<String?>(null)
        private set

    var analysisResult by mutableStateOf<AnalysisSessionResponse?>(null)
        private set

    var approvalResult by mutableStateOf<ApprovalResponse?>(null)
        private set

    var kpiData by mutableStateOf(KPIData(healthScore = 72)) // Default
        private set

    var alerts by mutableStateOf<List<AlertItem>>(emptyList())
        private set

    var auditEntries by mutableStateOf<List<AuditEntry>>(emptyList())
        private set

    // ── Actions ─────────────────────────────────────────────────

    /**
     * Check backend health and fetch initial dashboard data.
     */
    fun checkBackendHealth() {
        viewModelScope.launch {
            repository.checkHealth()
                .onSuccess { 
                    isBackendReachable = it.status == "healthy"
                    if (isBackendReachable) {
                        fetchDashboardData()
                        fetchAuditTrail()
                    }
                }
                .onFailure { isBackendReachable = false }
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            repository.getDashboardKpis(userEmail).onSuccess { data ->
                kpiData = KPIData(
                    runwayDays = data["runway_days"]?.toInt() ?: 142,
                    currentBalance = data["current_balance"] ?: 4350000.0,
                    monthlyBurn = data["monthly_burn"] ?: 780000.0,
                    momChange = data["mom_change"]?.toFloat() ?: -3.2f,
                    healthScore = data["health_score"]?.toInt() ?: 72
                )
            }
            repository.getDashboardAlerts().onSuccess { data ->
                alerts = data.map { alert ->
                    AlertItem(
                        type = when(alert["type"]) {
                            "risk" -> AlertType.CASH_DANGER
                            "insight" -> AlertType.INSIGHT
                            else -> AlertType.INSIGHT
                        },
                        title = alert["title"] ?: "Alert",
                        message = alert["message"] ?: ""
                    )
                }
            }
        }
    }

    fun fetchAuditTrail() {
        viewModelScope.launch {
            repository.getAuditTrail().onSuccess { data ->
                auditEntries = data.map { entry ->
                    AuditEntry(
                        id = entry["id"].toString(),
                        txHash = entry["tx_hash"].toString(),
                        documentHash = entry["document_hash"]?.toString() ?: "",
                        actionType = entry["action_type"].toString(),
                        description = entry["description"].toString(),
                        timestamp = System.currentTimeMillis() // Ideally parse from timestamp string
                    )
                }
            }
        }
    }

    /**
     * Called from SourceSelectionScreen.
     * Sends documents to the backend, triggers the full agent pipeline.
     * The WarRoom screen observes pipelineState to animate progress.
     */
    fun startAnalysis(selectedSourceTypes: List<String>) {
        viewModelScope.launch {
            pipelineState = PipelineState.Running(step = "INITIATING", progress = 0.1f)
            analysisResult = null
            approvalResult = null
            executionState = ExecutionState.Idle

            val documents = buildDemoDocuments(selectedSourceTypes)

            repository.startAnalysis(
                documents = documents,
                userQuery = "Analyze the financial health of my textile business. Detect contradictions, assess risks, and suggest actions."
            )
                .onSuccess { response ->
                    val sessionId = response.sessionId
                    currentSessionId = sessionId
                    
                    // Start polling for results
                    pollAnalysisStatus(sessionId)
                }
                .onFailure { error ->
                    pipelineState = PipelineState.Error(
                        error.localizedMessage ?: "Failed to start analysis"
                    )
                }
        }
    }

    private fun pollAnalysisStatus(sessionId: String) {
        viewModelScope.launch {
            var isComplete = false
            var attempts = 0
            val maxAttempts = 60 // 2 minutes max polling

            while (!isComplete && attempts < maxAttempts) {
                attempts++
                delay(2000) // Poll every 2 seconds

                repository.getSessionStatus(sessionId).onSuccess { status ->
                    val currentStep = status.currentStep ?: "PROCESSING"
                    pipelineState = PipelineState.Running(
                        step = currentStep,
                        progress = 0.3f + (attempts.toFloat() / maxAttempts.toFloat()) * 0.6f
                    )

                    // Check if analysis is actually done (we need the full report)
                    // The getSessionStatus endpoint currently only returns basic stats.
                    // We need to re-fetch the full session once it moves past planning.
                    if (currentStep == "VALIDATION_COMPLETE" || currentStep == "PLANNING_COMPLETE") {
                        // Re-fetch full session to get insight report and action plan
                        // We use the startAnalysis success response structure
                        // Actually, let's update the backend to return full data in getSessionStatus 
                        // or add a GET /analyze/session/{id} that returns everything.
                        
                        // For now, let's assume we can re-query a "final" state
                        isComplete = true
                        fetchFinalAnalysisResult(sessionId)
                    }
                }.onFailure {
                    // Ignore transient errors while polling
                }
            }
            
            if (!isComplete && attempts >= maxAttempts) {
                pipelineState = PipelineState.Error("Analysis timed out. Please try again.")
            }
        }
    }

    private fun fetchFinalAnalysisResult(sessionId: String) {
        viewModelScope.launch {
            repository.getFullSessionResults(sessionId).onSuccess { response ->
                analysisResult = response
                pipelineState = PipelineState.Complete(response)
            }.onFailure { error ->
                pipelineState = PipelineState.Error("Failed to fetch analysis results: ${error.localizedMessage}")
            }
        }
    }

    /**
     * Called from ExecutionTrackerScreen.
     * Approves the action plan and triggers the executor agent.
     */
    fun approveAndExecute(comment: String = "Approved from Android app") {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            executionState = ExecutionState.Running

            repository.approveSession(sessionId, comment)
                .onSuccess { response ->
                    approvalResult = response
                    executionState = if (response.success == true) {
                        ExecutionState.Complete(response)
                    } else {
                        ExecutionState.PartialFailure(response)
                    }
                }
                .onFailure { error ->
                    executionState = ExecutionState.Error(
                        error.localizedMessage ?: "Execution failed"
                    )
                }
        }
    }

    /**
     * Called from ExecutionTrackerScreen if user rejects the plan.
     */
    fun rejectPlan(reason: String = "Rejected from Android app") {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            repository.rejectSession(sessionId, reason)
                .onSuccess { approvalResult = it }
                .onFailure { /* silently fail */ }
        }
    }

    /**
     * Reset for a new analysis session.
     */
    fun resetSession() {
        pipelineState = PipelineState.Idle
        executionState = ExecutionState.Idle
        currentSessionId = null
        analysisResult = null
        approvalResult = null
    }

    // ── Demo Documents ──────────────────────────────────────────

    /**
     * Builds the "Inventory Crisis at Finora Textiles" demo documents
     * based on the user's selected source types.
     */
    private fun buildDemoDocuments(sources: List<String>): List<DocumentInput> {
        val docs = mutableListOf<DocumentInput>()

        // Always include core docs for the demo scenario
        docs.add(
            DocumentInput(
                id = "doc_warehouse_csv",
                sourceType = "CSV",
                rawContent = """
                    |item,stock_count,unit_price,last_audit
                    |cotton_yarn,500,2500,2026-04-30
                    |polyester_blend,300,1800,2026-04-30
                    |silk_raw,50,8000,2026-04-30
                    |Total inventory value: PKR 2,090,000
                """.trimMargin(),
                documentTimestamp = "2026-04-30",
                credibilityScore = 0.8,
                stalenessScore = 0.7
            )
        )

        docs.add(
            DocumentInput(
                id = "doc_supplier_email",
                sourceType = "PDF",
                rawContent = """
                    |Subject: URGENT - Supply Chain Disruption Notice
                    |From: logistics@karachi-textiles.pk
                    |Date: 2026-05-20
                    |
                    |Dear Finora Textiles,
                    |Due to ongoing port strikes at Karachi port, we are unable to fulfill
                    |your standing order of 1000 units cotton yarn. Expected delay: 2-4 weeks.
                    |Alternative suppliers in Faisalabad may have limited stock at 15% premium.
                    |
                    |Regards,
                    |Karachi Textile Suppliers
                """.trimMargin(),
                documentTimestamp = "2026-05-20",
                credibilityScore = 0.9,
                stalenessScore = 0.0
            )
        )

        docs.add(
            DocumentInput(
                id = "doc_sales_dashboard",
                sourceType = "CSV",
                rawContent = """
                    |date,units_sold,revenue_pkr,channel
                    |2026-05-14,185,462500,wholesale
                    |2026-05-15,178,445000,wholesale
                    |2026-05-16,192,480000,retail
                    |2026-05-17,168,420000,wholesale
                    |2026-05-18,180,450000,mixed
                    |2026-05-19,175,437500,wholesale
                    |2026-05-20,190,475000,retail
                    |Average velocity: 180 units/day
                """.trimMargin(),
                documentTimestamp = "2026-05-20",
                credibilityScore = 0.95,
                stalenessScore = 0.0
            )
        )

        if (sources.any { it in listOf("GHOST_LEDGER", "CSV", "PDF") }) {
            docs.add(
                DocumentInput(
                    id = "doc_ghost_ledger_scan",
                    sourceType = "GHOST_LEDGER",
                    rawContent = """
                        |=== Ghost Ledger OCR Extraction ===
                        |Handwritten ledger page, Urdu/Roman Urdu headers
                        |
                        |Row 1: Cotton yarn (kacha dhaga) — 287 units — checked physically
                        |Row 2: Polyester — 145 units — partial count
                        |Row 3: Silk — 32 units — verified
                        |
                        |Note written in margin: "Godown B locked, count incomplete"
                        |Verified by: Ahmad (warehouse supervisor)
                        |Date: 19 May 2026
                    """.trimMargin(),
                    documentTimestamp = "2026-05-19",
                    credibilityScore = 0.85,
                    stalenessScore = 0.05
                )
            )
        }

        docs.add(
            DocumentInput(
                id = "doc_complaints",
                sourceType = "CSV",
                rawContent = """
                    |complaint_id,date,customer,issue,status
                    |C001,2026-05-18,Lahore Retail Hub,Delayed delivery 5 days,open
                    |C002,2026-05-18,Faisalabad Wholesale,Short shipment 50 units,open
                    |C003,2026-05-19,Multan Textiles,Quality variance in batch,investigating
                    |C004,2026-05-19,Islamabad Fashion,Order cancelled due to delay,closed
                    |Total open complaints: 23 in last 48 hours
                """.trimMargin(),
                documentTimestamp = "2026-05-20",
                credibilityScore = 0.9,
                stalenessScore = 0.0
            )
        )

        return docs
    }
}

// ── State sealed classes ────────────────────────────────────────

sealed class PipelineState {
    object Idle : PipelineState()
    data class Running(val step: String, val progress: Float) : PipelineState()
    data class Complete(val response: AnalysisSessionResponse) : PipelineState()
    data class Error(val message: String) : PipelineState()
}

sealed class ExecutionState {
    object Idle : ExecutionState()
    object Running : ExecutionState()
    data class Complete(val response: ApprovalResponse) : ExecutionState()
    data class PartialFailure(val response: ApprovalResponse) : ExecutionState()
    data class Error(val message: String) : ExecutionState()
}
