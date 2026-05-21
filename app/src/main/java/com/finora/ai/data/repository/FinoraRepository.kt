package com.finora.ai.data.repository

import com.finora.ai.data.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository layer — wraps all FinoraAI backend API calls.
 * Converts Retrofit responses into sealed Result types.
 */
class FinoraRepository(
    private val api: FinoraApiService = ApiConfig.apiService
) {

    // ── Health Check ────────────────────────────────────────────

    suspend fun checkHealth(): Result<HealthResponse> = safeApiCall {
        api.getHealth()
    }

    suspend fun checkRoot(): Result<RootResponse> = safeApiCall {
        api.getRoot()
    }

    // ── Analysis Pipeline ───────────────────────────────────────

    /**
     * Sends documents to the backend and starts the agent pipeline.
     * Returns the session with insight report + action plan (paused before execution).
     */
    suspend fun startAnalysis(
        documents: List<DocumentInput>,
        userQuery: String = "Analyze my financial situation"
    ): Result<AnalysisSessionResponse> = safeApiCall {
        api.startAnalysis(
            AnalysisRequest(
                documents = documents,
                userQuery = userQuery
            )
        )
    }

    /**
     * Polls the status of an existing analysis session.
     */
    suspend fun getSessionStatus(sessionId: String): Result<SessionStatusResponse> = safeApiCall {
        api.getSessionStatus(sessionId)
    }

    // ── Approval & Execution ────────────────────────────────────

    /**
     * Approves an action plan, which triggers the executor agent.
     */
    suspend fun approveSession(
        sessionId: String,
        userComment: String = ""
    ): Result<ApprovalResponse> = safeApiCall {
        api.approveAndExecute(
            ApprovalRequest(
                sessionId = sessionId,
                approved = true,
                userComment = userComment
            )
        )
    }

    /**
     * Rejects an action plan — no actions are executed.
     */
    suspend fun rejectSession(
        sessionId: String,
        reason: String = ""
    ): Result<ApprovalResponse> = safeApiCall {
        api.approveAndExecute(
            ApprovalRequest(
                sessionId = sessionId,
                approved = false,
                userComment = reason
            )
        )
    }

    // ── Helper ──────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(
                    Exception("API error ${response.code()}: ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
