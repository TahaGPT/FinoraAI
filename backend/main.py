# backend/main.py

import os
import sys
import asyncio
import uuid
import logging
from typing import Dict, Any, List
from dotenv import load_dotenv

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from backend.agents.state import FinoraState, IngestedDocument
from backend.agents.graph import finora_graph

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s"
)
logger = logging.getLogger(__name__)


app = FastAPI(
    title="FinoraAI Backend",
    description="Agentic Financial Intelligence Platform API",
    version="1.0.0"
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


@app.post("/analyze/session")
async def start_analysis_session(request: AnalysisRequest):
    """
    Receives documents from the Android app.
    Triggers the agent pipeline and returns a session_id.
    The pipeline runs until it pauses before action_executor.
    """
    session_id = f"session_{uuid.uuid4().hex[:8]}"

    documents = [
        IngestedDocument(
            id=doc.id,
            source_type=doc.source_type,
            raw_content=doc.raw_content,
            normalized_content=doc.normalized_content,
            credibility_score=doc.credibility_score,
            document_timestamp=doc.document_timestamp,
            staleness_score=doc.staleness_score
        )
        for doc in request.documents
    ]

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

        return {
            "session_id": session_id,
            "status": "AWAITING_APPROVAL",
            "current_step": state_values.get("current_step"),
            "insight_report": {
                "risks": insight_report.risks if insight_report else [],
                "opportunities": insight_report.opportunities if insight_report else [],
                "contradictions": insight_report.contradictions if insight_report else [],
                "key_signals": [
                    {
                        "metric": s.metric,
                        "value": str(s.value)
                    }
                    for s in (insight_report.key_signals if insight_report else [])
                ]
            },
            "action_plan": [
                {
                    "action_id": a.action_id,
                    "type": a.type,
                    "description": a.description,
                    "status": a.status,
                    "dependencies": a.dependencies,
                    "rollback_action": a.rollback_action
                }
                for a in action_plan
            ],
            "message": "Analysis complete. Review the action plan and approve to execute."
        }

    except Exception as e:
        logger.error("[%s] Pipeline error: %s", session_id, str(e))
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
async def approve_and_execute(request: ApprovalRequest):
    """
    Receives approval or rejection from the Android user.
    If approved, resumes the graph and runs the action executor.
    """
    config = {"configurable": {"thread_id": request.session_id}}
    current_state = finora_graph.get_state(config)

    if not current_state.values:
        raise HTTPException(
            status_code=404,
            detail=f"Session {request.session_id} not found"
        )

    if not request.approved:
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