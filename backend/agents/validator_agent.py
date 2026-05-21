# backend/agents/validator_agent.py

import os
import json
import asyncio
import logging
from typing import List
from pydantic import BaseModel
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage

from .state import ActionItem

logger = logging.getLogger(__name__)


class CorporateLimits(BaseModel):
    available_budget: float
    current_cashflow: float
    monthly_runway_days: int
    rate_limit_remaining: int
    approval_authority_threshold: float


class ValidatedActionList(BaseModel):
    validated_actions: List[ActionItem]
    total_approved: int
    total_rejected: int
    modification_notes: List[str]


def _get_llm():
    return ChatGoogleGenerativeAI(
        model=os.getenv("GEMINI_MODEL", "gemini-1.5-flash"),
        google_api_key=os.environ["GOOGLE_API_KEY"],
        temperature=0.2,
    )


async def run_constraint_validator(
    action_plan: List[ActionItem],
    limits: CorporateLimits
) -> List[ActionItem]:
    """
    Checks each action against budget, time, rate limits, and approval requirements.
    Marks infeasible actions as REJECTED.
    Tries to modify parameters to find feasible alternatives.
    Returns the action list with updated status fields.
    """
    if not action_plan:
        return []

    llm = _get_llm()
    structured_llm = llm.with_structured_output(ValidatedActionList)

    actions_data = [a.model_dump() for a in action_plan]

    messages = [
        SystemMessage(content=f"""You are a financial constraint validator.
Check each action against these corporate limits:

LIMITS:
- Available budget: PKR {limits.available_budget:,.0f}
- Current cashflow: PKR {limits.current_cashflow:,.0f}
- Monthly runway: {limits.monthly_runway_days} days
- API calls remaining: {limits.rate_limit_remaining}
- Approval threshold: PKR {limits.approval_authority_threshold:,.0f}

VALIDATION RULES:
1. If action.estimated_cost > available_budget → status: REJECTED, reason_for_rejection: "Exceeds budget"
2. If action.estimated_cost > approval_authority_threshold AND requires_approval is false → set requires_approval: true
3. If action needs API calls and rate_limit_remaining < 10 → status: REJECTED, reason_for_rejection: "Rate limit exceeded"
4. Try to MODIFY the action's estimated_cost or parameters to make it feasible before rejecting (e.g., reduce cost to fit budget)
5. If modified, set status to APPROVED, set requires_approval to true if needed, and add a note to modification_notes explaining what changed.
6. Make sure to populate the status field (either APPROVED or REJECTED) and reason_for_rejection (if status is REJECTED).

Return ALL actions with their final status and reasons."""),

        HumanMessage(content=f"""Validate these actions:

{json.dumps(actions_data, indent=2)}

Check each one and return the full list with updated status and reason_for_rejection fields.""")
    ]

    result = await structured_llm.ainvoke(messages)
    return result.validated_actions


async def constraint_validator_node(state: dict) -> dict:
    """
    LangGraph node that validates each action against corporate budget and operational limits.
    Falls back to approving all actions if the LLM call fails.
    """
    try:
        limits = CorporateLimits(
            available_budget=500000.0,
            current_cashflow=120000.0,
            monthly_runway_days=45,
            rate_limit_remaining=95,
            approval_authority_threshold=100000.0
        )

        validated = await run_constraint_validator(
            action_plan=state.get("action_plan", []),
            limits=limits
        )

        approved = [a for a in validated if a.status == "APPROVED"]
        rejected = [a for a in validated if a.status == "REJECTED"]

        return {
            "action_plan": validated,
            "current_step": "VALIDATION_COMPLETE",
            "execution_log": [{
                "step": "constraint_validation",
                "status": "success",
                "approved": len(approved),
                "rejected": len(rejected)
            }]
        }

    except Exception as e:
        logger.warning("[Validator Agent] LLM call failed: %s. Approving all actions as fallback.", str(e))
        validated_actions = []
        for action in state.get("action_plan", []):
            if hasattr(action, "model_dump"):
                action = action.model_dump()
            action_dict = action if isinstance(action, dict) else {}
            action_dict["status"] = "APPROVED"
            validated_actions.append(action_dict)

        return {
            "action_plan": validated_actions,
            "current_step": "VALIDATION_COMPLETE",
            "execution_log": [{
                "step": "constraint_validation",
                "status": "success (fallback)",
                "approved": len(validated_actions),
                "rejected": 0
            }]
        }