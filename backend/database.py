# backend/database.py

import os
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase
from dotenv import load_dotenv

load_dotenv()

# Railway or local PostgreSQL URL
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql+asyncpg://postgres:postgres@localhost:5432/finora")

# Railway provides "postgres://", but asyncpg requires "postgresql+asyncpg://"
if DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql+asyncpg://", 1)
elif DATABASE_URL.startswith("postgresql://"):
    DATABASE_URL = DATABASE_URL.replace("postgresql://", "postgresql+asyncpg://", 1)

engine = create_async_engine(DATABASE_URL, echo=False)

AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
)

class Base(DeclarativeBase):
    pass

async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()

async def init_db():
    async with engine.begin() as conn:
        # Import models here to ensure they are registered with Base
        from . import models
        await conn.run_sync(Base.metadata.create_all)
    
    # Seed initial data if tables are empty
    async with AsyncSessionLocal() as db:
        from . import models
        from sqlalchemy import select
        
        # 1. Seed Financial Metrics (KPIs) for different user contexts
        result = await db.execute(select(models.FinancialMetric))
        if not result.scalars().first():
            db.add_all([
                # Taha (Healthy)
                models.FinancialMetric(metric_name="taha@finora.ai:runway_days", value=142),
                models.FinancialMetric(metric_name="taha@finora.ai:current_balance", value=4350000.0),
                models.FinancialMetric(metric_name="taha@finora.ai:monthly_burn", value=780000.0),
                models.FinancialMetric(metric_name="taha@finora.ai:mom_change", value=-3.2),
                models.FinancialMetric(metric_name="taha@finora.ai:health_score", value=88),
                
                # Founder (Struggling)
                models.FinancialMetric(metric_name="founder@demo.pk:runway_days", value=42),
                models.FinancialMetric(metric_name="founder@demo.pk:current_balance", value=850000.0),
                models.FinancialMetric(metric_name="founder@demo.pk:monthly_burn", value=1200000.0),
                models.FinancialMetric(metric_name="founder@demo.pk:mom_change", value=-12.5),
                models.FinancialMetric(metric_name="founder@demo.pk:health_score", value=35),
            ])
            await db.commit()

        # 2. Seed Initial Analysis Session for Alerts
        result = await db.execute(select(models.AnalysisSession))
        if not result.scalars().first():
            db.add(models.AnalysisSession(
                id="initial_demo_session",
                user_id="default_user",
                status="COMPLETED",
                insight_report={
                    "risks": [
                        "Stockout imminent — 1.6 days until zero inventory at current velocity",
                        "USD/PKR rate spike increased COGS by 3.2% in last 24h"
                    ],
                    "opportunities": [
                        "Bulk discount available — supplier offering 8% on 1000+ units"
                    ],
                    "key_signals": [
                        {"metric": "inventory_runway", "value": "1.6 days"}
                    ]
                }
            ))
            await db.commit()
            print("Database seeded with metrics and initial session.")

        # 2. Seed some initial audit entries to verify agent history
        result = await db.execute(select(models.AuditEntry))
        if not result.scalars().first():
            db.add_all([
                models.AuditEntry(
                    tx_hash="0x7a3f4b8c9d2e1f0a",
                    action_type="CONTRADICTION_RESOLVED",
                    description="Conflict between Bank Statement and CSV resolved (Credibility: 0.95)",
                    metadata_json={"user": "taha@finora.ai", "agent": "ContradictionAgent"}
                ),
                models.AuditEntry(
                    tx_hash="0x9c1e5d3f7a2b6e4d",
                    action_type="ACTION_EXECUTED",
                    description="Emergency order for 500 units Cotton Yarn placed via Supplier API",
                    metadata_json={"user": "founder@demo.pk", "agent": "ExecutorAgent"}
                )
            ])
            await db.commit()
            print("Database seeded with audit history.")
