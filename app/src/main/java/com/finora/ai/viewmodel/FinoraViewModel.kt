package com.finora.ai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.ai.data.network.*
import com.finora.ai.data.repository.FinoraRepository
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the FinoraAI app.
 * Drives the analysis pipeline: submit docs → get insights → approve/reject → execute.
 */
class FinoraViewModel : ViewModel() {

    private val repository = FinoraRepository()

    // ── UI State ────────────────────────────────────────────────

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isBackendConnected by mutableStateOf(false)
        private set

    // ── Session State ───────────────────────────────────────────

    var currentSessionId by mutableStateOf<String?>(null)
        private set

    var analysisResult by mutableStateOf<AnalysisSessionResponse?>(null)
        private set

    var approvalResult by mutableStateOf<ApprovalResponse?>(null)
        private set

    var sessionStatus by mutableStateOf<SessionStatusResponse?>(null)
        private set

    // ── Actions ─────────────────────────────────────────────────

    /**
     * Check if the backend is reachable.
     */
    fun checkBackendHealth() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.checkHealth()
                .onSuccess { isBackendConnected = it.status == "healthy" }
                .onFailure {
                    isBackendConnected = false
                    errorMessage = "Backend unreachable: ${it.localizedMessage}"
                }
            isLoading = false
        }
    }

    /**
     * Start a new analysis session by sending documents to the backend.
     * The agent pipeline runs and pauses before the action executor.
     */
    fun startAnalysis(
        documents: List<DocumentInput>,
        userQuery: String = "Analyze my financial situation"
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            analysisResult = null
            approvalResult = null

            repository.startAnalysis(documents, userQuery)
                .onSuccess { response ->
                    currentSessionId = response.sessionId
                    analysisResult = response
                }
                .onFailure {
                    errorMessage = "Analysis failed: ${it.localizedMessage}"
                }

            isLoading = false
        }
    }

    /**
     * Approve the current session's action plan → triggers execution.
     */
    fun approveCurrentSession(comment: String = "") {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.approveSession(sessionId, comment)
                .onSuccess { approvalResult = it }
                .onFailure { errorMessage = "Approval failed: ${it.localizedMessage}" }

            isLoading = false
        }
    }

    /**
     * Reject the current session's action plan.
     */
    fun rejectCurrentSession(reason: String = "") {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.rejectSession(sessionId, reason)
                .onSuccess { approvalResult = it }
                .onFailure { errorMessage = "Rejection failed: ${it.localizedMessage}" }

            isLoading = false
        }
    }

    /**
     * Poll the status of the current session.
     */
    fun refreshSessionStatus() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            repository.getSessionStatus(sessionId)
                .onSuccess { sessionStatus = it }
                .onFailure { errorMessage = "Status check failed: ${it.localizedMessage}" }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
