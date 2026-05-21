package com.finora.ai.data.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for the FinoraAI FastAPI backend.
 * Maps 1:1 with the backend endpoints in backend/main.py.
 */
interface FinoraApiService {

    // ── Health & Status ─────────────────────────────────────────

    @GET("/")
    suspend fun getRoot(): Response<RootResponse>

    @GET("/health")
    suspend fun getHealth(): Response<HealthResponse>

    // ── Analysis Pipeline ───────────────────────────────────────

    /**
     * POST /analyze/session
     * Sends documents to the backend, triggers the full agent pipeline.
     * Returns session_id + insight report + action plan (paused before execution).
     */
    @POST("/analyze/session")
    suspend fun startAnalysis(@Body request: AnalysisRequest): Response<AnalysisSessionResponse>

    /**
     * GET /analyze/session/{session_id}
     * Polls the current status of any analysis session.
     */
    @GET("/analyze/session/{session_id}")
    suspend fun getSessionStatus(
        @Path("session_id") sessionId: String
    ): Response<SessionStatusResponse>

    // ── Approval & Execution ────────────────────────────────────

    /**
     * POST /analyze/approve
     * Sends user approval or rejection. If approved, resumes the graph
     * and runs the action executor agent.
     */
    @POST("/analyze/approve")
    suspend fun approveAndExecute(@Body request: ApprovalRequest): Response<ApprovalResponse>

    // ── Dashboard & Audit ───────────────────────────────────────

    @GET("/dashboard/kpis")
    suspend fun getDashboardKpis(): Response<Map<String, Double>>

    @GET("/dashboard/alerts")
    suspend fun getDashboardAlerts(): Response<List<Map<String, String>>>

    @GET("/audit/trail")
    suspend fun getAuditTrail(): Response<List<Map<String, Any>>>
}
