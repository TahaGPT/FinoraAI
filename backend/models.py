# backend/models.py

import uuid
from datetime import datetime
from sqlalchemy import Column, String, Float, DateTime, JSON, Integer, Boolean, ForeignKey, Text
from sqlalchemy.orm import relationship
from .database import Base

class AnalysisSession(Base):
    __tablename__ = "analysis_sessions"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, index=True)
    status = Column(String, default="CREATED")
    current_step = Column(String)
    insight_report = Column(JSON)  # Stores the full InsightReport object
    action_plan = Column(JSON)      # Stores the list of ActionItems
    created_at = Column(DateTime, default=datetime.utcnow)
    completed_at = Column(DateTime)

    documents = relationship("Document", back_populates="session", cascade="all, delete-orphan")

class Document(Base):
    __tablename__ = "documents"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    session_id = Column(String, ForeignKey("analysis_sessions.id"))
    source_type = Column(String)
    raw_content = Column(Text)
    normalized_content = Column(JSON)
    credibility_score = Column(Float, default=1.0)
    document_timestamp = Column(String)
    staleness_score = Column(Float, default=0.0)
    ingested_at = Column(DateTime, default=datetime.utcnow)

    session = relationship("AnalysisSession", back_populates="documents")

class AuditEntry(Base):
    __tablename__ = "audit_entries"

    id = Column(Integer, primary_key=True, autoincrement=True)
    tx_hash = Column(String)
    document_hash = Column(String)
    action_type = Column(String)
    description = Column(String)
    metadata_json = Column(JSON)
    created_at = Column(DateTime, default=datetime.utcnow)

class AgentTrace(Base):
    __tablename__ = "agent_traces"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String, index=True)
    agent_name = Column(String)
    step = Column(String)
    input_json = Column(JSON)
    output_json = Column(JSON)
    duration_ms = Column(Integer)
    created_at = Column(DateTime, default=datetime.utcnow)

class FinancialMetric(Base):
    """Stores the latest KPIs for the dashboard."""
    __tablename__ = "financial_metrics"

    id = Column(Integer, primary_key=True, autoincrement=True)
    metric_name = Column(String, unique=True) # runway_days, current_balance, etc.
    value = Column(Float)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
