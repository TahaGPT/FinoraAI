# backend/agents/state.py

from typing import TypedDict, List, Optional, Annotated, Dict, Any
import operator
from pydantic import BaseModel, Field


class IngestedDocument(BaseModel):
    id: str
    source_type: str
    raw_content: str
    normalized_content: Dict[str, Any] = Field(default_factory=dict)
    credibility_score: float = 1.0
    document_timestamp: str
    staleness_score: float = 0.0


class KeySignal(BaseModel):
    metric: str
    value: str


class InsightReport(BaseModel):
    key_signals: List[KeySignal]
    risks: List[str]
    opportunities: List[str]
    contradictions: List[dict]   # each dict: {source_a, source_b, field, value_a, value_b}


class ContradictionResolution(BaseModel):
    contradiction: str
    winning_source_id: str
    winning_value: str
    confidence_score: float
    resolution_rationale: str
    investigation_path: Optional[List[str]] = None


class ActionItem(BaseModel):
    action_id: str
    type: str            # NOTIFY | UPDATE_RECORD | SIMULATE | FETCH_DATA | ALERT | SCHEDULE
    description: str
    dependencies: List[str] = Field(default_factory=list)
    constraints: Dict[str, Any] = Field(default_factory=dict)
    rollback_action: str
    estimated_cost: float
    estimated_duration: int
    requires_approval: bool
    status: str = "PENDING"  # PENDING | APPROVED | REJECTED
    reason_for_rejection: Optional[str] = None


class FinoraState(TypedDict):
    """
    The shared state object that flows through every node in the LangGraph.
    """
    session_id: str
    query_type: Optional[str]                               # "analysis" | "simulation" | "action" | "alert"
    documents: List[IngestedDocument]                       # ingested documents
    insight_report: Optional[InsightReport]                 # output of InsightAnalyst
    resolved_contradictions: Optional[List[ContradictionResolution]]  # output of ContradictionDetector
    action_plan: Optional[List[ActionItem]]                 # list of ActionItems (planned & validated)
    execution_log: Annotated[List[dict], operator.add]     # appends each step's log
    failed_steps: Annotated[List[str], operator.add]       # appends names of failed nodes
    current_step: str                                       # tracks which node ran last
    error_message: Optional[str]                            # last error if any