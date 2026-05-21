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
    
    # Seed initial metrics if table is empty
    async with AsyncSessionLocal() as db:
        from . import models
        from sqlalchemy import select
        result = await db.execute(select(models.FinancialMetric))
        if not result.scalars().first():
            db.add_all([
                models.FinancialMetric(metric_name="runway_days", value=142),
                models.FinancialMetric(metric_name="current_balance", value=4350000.0),
                models.FinancialMetric(metric_name="monthly_burn", value=780000.0),
                models.FinancialMetric(metric_name="mom_change", value=-3.2),
                models.FinancialMetric(metric_name="health_score", value=72)
            ])
            await db.commit()
            print("Database seeded with initial metrics.")
