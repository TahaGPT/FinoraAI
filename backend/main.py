# backend/main.py

import os
import sys
import asyncio
import uuid
import logging
from typing import Dict, Any, List
from contextlib import asynccontextmanager
from dotenv import load_dotenv

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from backend.agents.state import FinoraState, IngestedDocument
from backend.agents.graph import finora_graph
from backend.database import init_db, get_db
from backend import models

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s"
)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize DB on startup
    try:
        await init_db()
        logger.info("Database initialized successfully.")
    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")
    yield

app = FastAPI(
    title="FinoraAI Backend",
    description="Agentic Financial Intelligence Platform API",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class DocumentInput(BaseModel):
    id: str
    source_type: str
    raw_content: str
    normalized_content: Dict[str, Any] = {}
    credibility_score: float = 0.8
    document_timestamp: str
    staleness_score: float = 0.0


class AnalysisRequest(BaseModel):
    documents: List[DocumentInput]
    user_query: str = "Analyze my financial situation"


class ApprovalRequest(BaseModel):
    session_id: str
    approved: bool
    user_comment: str = ""


@app.get("/")
async def root():
    return {
        "status": "FinoraAI is running",
        "version": "1.0.0",
        "agents": "ready"
    }


@app.get("/health")
async def health():
    return {"status": "healthy", "pipeline": "operational"}


@app.get("/dashboard/kpis")
async def get_dashboard_kpis(db: AsyncSession = Depends(get_db)):
    """
    Returns the latest financial KPIs for the Home Dashboard.
    """
    try:
        result = await db.execute(select(models.FinancialMetric))
        metrics = result.scalars().all()
        
        # If no metrics in DB, return some defaults for the demo
        if not metrics:
            return {
                "runway_days": 142,
                "current_balance": 4350000.0,
                "monthly_burn": 780000.0,
                "mom_change": -3.2,
                "health_score": 72
            }
            
        data = {m.metric_name: m.value for m in metrics}
        return data
    except Exception as e:
        logger.error(f"Error fetching KPIs: {e}")
        return {"error": str(e)}

@app.get("/dashboard/alerts")
async def get_dashboard_alerts(db: AsyncSession = Depends(get_db)):
    """
    Returns recent alerts/insights from agent analysis.
    """
    try:
        # Fetch latest analysis session and its insights
        result = await db.execute(
            select(models.AnalysisSession)
            .order_by(models.AnalysisSession.created_at.desc())
            .limit(1)
        )
        session = result.scalar_one_or_none()
        
        if not session or not session.insight_report:
            return []
            
        # Convert insights to alert format
        report = session.insight_report
        alerts = []
        
        for risk in report.get("risks", [])[:2]:
            alerts.append({"type": "risk", "title": "Financial Risk", "message": risk})
        for opp in report.get("opportunities", [])[:1]:
            alerts.append({"type": "insight", "title": "New Insight", "message": opp})
            
        return alerts
    except Exception as e:
        logger.error(f"Error fetching alerts: {e}")
        return []

@app.get("/audit/trail")
async def get_audit_trail(db: AsyncSession = Depends(get_db)):
    """
    Returns the chronological audit trail from the blockchain audit logger.
    """
    try:
        result = await db.execute(
            select(models.AuditEntry).order_by(models.AuditEntry.created_at.desc())
        )
        entries = result.scalars().all()
        return [
            {
                "id": e.id,
                "tx_hash": e.tx_hash,
                "action_type": e.action_type,
                "description": e.description,
                "timestamp": e.created_at.isoformat(),
                "metadata": e.metadata_json
            }
            for e in entries
        ]
    except Exception as e:
        logger.error(f"Error fetching audit trail: {e}")
        return []

@app.post("/analyze/session")
async def start_analysis_session(request: AnalysisRequest, db: AsyncSession = Depends(get_db)):
    """
    Receives documents from the Android app.
    Triggers the agent pipeline and returns a session_id.
    """
    session_id = f"session_{uuid.uuid4().hex[:8]}"
    
    # Save session to DB
    new_session = models.AnalysisSession(
        id=session_id,
        status="INGESTING",
        user_id="default_user" # Simplified for hackathon
    )
    db.add(new_session)
    
    documents = []
    for doc in request.documents:
        ingested_doc = IngestedDocument(
            id=doc.id,
            source_type=doc.source_type,
            raw_content=doc.raw_content,
            normalized_content=doc.normalized_content,
            credibility_score=doc.credibility_score,
            document_timestamp=doc.document_timestamp,
            staleness_score=doc.staleness_score
        )
        documents.append(ingested_doc)
        
        # Save document to DB
        db_doc = models.Document(
            id=doc.id,
            session_id=session_id,
            source_type=doc.source_type,
            raw_content=doc.raw_content,
            normalized_content=doc.normalized_content,
            credibility_score=doc.credibility_score,
            document_timestamp=doc.document_timestamp,
            staleness_score=doc.staleness_score
        )
        db.add(db_doc)

    await db.commit()

    initial_state: FinoraState = {
        "session_id": session_id,
        "documents": documents,
        "insight_report": None,
        "resolved_contradictions": [],
        "action_plan": [],
        "execution_log": [],
        "current_step": "START",
        "failed_steps": []
    }

    config = {"configurable": {"thread_id": session_id}}

    try:
        async for event in finora_graph.astream(initial_state, config=config):
            for node_name in event.keys():
                logger.info("[%s] Completed node: %s", session_id, node_name)

        current_state = finora_graph.get_state(config)
        state_values = current_state.values

        insight_report = state_values.get("insight_report")
        action_plan = state_values.get("action_plan", [])
        
        # Update session in DB with results
        insight_data = {
            "risks": insight_report.risks if insight_report else [],
            "opportunities": insight_report.opportunities if insight_report else [],
            "contradictions": insight_report.contradictions if insight_report else [],
            "key_signals": [
                {"metric": s.metric, "value": str(s.value)}
                for s in (insight_report.key_signals if insight_report else [])
            ]
        }
        
        plan_data = [
            {
                "action_id": a.action_id,
                "type": a.type,
                "description": a.description,
                "status": a.status,
                "dependencies": a.dependencies,
                "rollback_action": a.rollback_action,
                "estimated_cost": getattr(a, 'estimated_cost', 0)
            }
            for a in action_plan
        ]

        # Re-fetch session to avoid detached instance issues if needed, 
        # but since we are in same request it should be fine.
        new_session.status = "AWAITING_APPROVAL"
        new_session.current_step = state_values.get("current_step")
        new_session.insight_report = insight_data
        new_session.action_plan = plan_data
        await db.commit()

        return {
            "session_id": session_id,
            "status": "AWAITING_APPROVAL",
            "current_step": state_values.get("current_step"),
            "insight_report": insight_data,
            "action_plan": plan_data,
            "message": "Analysis complete. Review the action plan and approve to execute."
        }

    except Exception as e:
        logger.error("[%s] Pipeline error: %s", session_id, str(e))
        new_session.status = "FAILED"
        await db.commit()
        raise HTTPException(status_code=500, detail=f"Pipeline error: {str(e)}")


@app.get("/analyze/session/{session_id}/stream")
async def stream_analysis(session_id: str):
    """
    Streams each agent step in real time to the Android client.
    """
    async def event_generator():
        try:
            config = {"configurable": {"thread_id": session_id}}
            current_state = finora_graph.get_state(config)

            if not current_state.values:
                yield f"data: No session found with ID {session_id}\n\n"
                return

            state_values = current_state.values

            yield f"data: SESSION_ID: {session_id}\n\n"
            await asyncio.sleep(0.1)

            yield f"data: CURRENT_STEP: {state_values.get('current_step', 'UNKNOWN')}\n\n"
            await asyncio.sleep(0.1)

            report = state_values.get("insight_report")
            if report:
                yield f"data: RISKS_COUNT: {len(report.risks)}\n\n"
                await asyncio.sleep(0.1)

                for risk in report.risks:
                    yield f"data: RISK: {risk}\n\n"
                    await asyncio.sleep(0.1)

                for opp in report.opportunities:
                    yield f"data: OPPORTUNITY: {opp}\n\n"
                    await asyncio.sleep(0.1)

            plan = state_values.get("action_plan", [])
            yield f"data: ACTIONS_COUNT: {len(plan)}\n\n"
            await asyncio.sleep(0.1)

            for action in plan:
                yield f"data: ACTION: {action.action_id} | {action.type} | {action.status} | {action.description}\n\n"
                await asyncio.sleep(0.1)

            yield f"data: STREAM_COMPLETE\n\n"

        except Exception as e:
            yield f"data: ERROR: {str(e)}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no"
        }
    )


@app.post("/analyze/approve")
async def approve_and_execute(request: ApprovalRequest, db: AsyncSession = Depends(get_db)):
    """
    Receives approval or rejection from the Android user.
    If approved, resumes the graph and runs the action executor.
    """
    config = {"configurable": {"thread_id": request.session_id}}
    current_state = finora_graph.get_state(config)

    if not current_state.values:
        # Check if it's in the DB if memory is cleared
        result = await db.execute(select(models.AnalysisSession).where(models.AnalysisSession.id == request.session_id))
        session = result.scalar_one_or_none()
        if not session:
            raise HTTPException(status_code=404, detail=f"Session {request.session_id} not found")
        
        # If memory is cleared, we might need to recreate state, but for hackathon 
        # let's assume memory is present or session is already complete.
        if session.status == "EXECUTION_COMPLETE":
            return {"session_id": session.id, "status": "EXECUTION_COMPLETE", "message": "Already executed."}
        
        raise HTTPException(status_code=400, detail="Session state lost in memory. Cannot resume.")

    if not request.approved:
        # Update DB
        result = await db.execute(select(models.AnalysisSession).where(models.AnalysisSession.id == request.session_id))
        session = result.scalar_one_or_none()
        if session:
            session.status = "REJECTED_BY_USER"
            await db.commit()

        return {
            "session_id": request.session_id,
            "status": "REJECTED_BY_USER",
            "message": "Action plan rejected. No actions executed.",
            "comment": request.user_comment
        }

    try:
        execution_logs = []

        async for event in finora_graph.astream(None, config=config):
            for node_name, node_output in event.items():
                logger.info("[%s] Executing: %s", request.session_id, node_name)
                if "execution_log" in node_output:
                    execution_logs.extend(node_output["execution_log"])

        final_state = finora_graph.get_state(config)
        final_values = final_state.values
        failed_steps = final_values.get("failed_steps", [])

        # Update DB
        result = await db.execute(select(models.AnalysisSession).where(models.AnalysisSession.id == request.session_id))
        session = result.scalar_one_or_none()
        if session:
            session.status = "EXECUTION_COMPLETE"
            session.current_step = final_values.get("current_step")
            session.completed_at = datetime.utcnow()
            await db.commit()

        return {
            "session_id": request.session_id,
            "status": "EXECUTION_COMPLETE",
            "current_step": final_values.get("current_step"),
            "execution_logs": execution_logs,
            "failed_steps": failed_steps,
            "success": len(failed_steps) == 0,
            "message": (
                "All actions executed successfully."
                if len(failed_steps) == 0
                else f"{len(failed_steps)} action(s) failed and were rolled back."
            )
        }

    except Exception as e:
        logger.error("[%s] Execution error: %s", request.session_id, str(e))
        raise HTTPException(status_code=500, detail=f"Execution error: {str(e)}")


@app.get("/analyze/session/{session_id}")
async def get_session_status(session_id: str):
    """
    Returns the current status of any session by ID.
    """
    config = {"configurable": {"thread_id": session_id}}
    current_state = finora_graph.get_state(config)

    if not current_state.values:
        raise HTTPException(
            status_code=404,
            detail=f"Session {session_id} not found"
        )

    state_values = current_state.values
    insight_report = state_values.get("insight_report")
    action_plan = state_values.get("action_plan", [])

    return {
        "session_id": session_id,
        "current_step": state_values.get("current_step"),
        "risks_count": len(insight_report.risks) if insight_report else 0,
        "actions_count": len(action_plan),
        "failed_steps": state_values.get("failed_steps", []),
        "execution_log": state_values.get("execution_log", [])
    }