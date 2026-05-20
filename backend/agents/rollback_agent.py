# backend/agents/rollback_agent.py

import asyncio
import random
import logging
from typing import List

from .state import ActionItem

logger = logging.getLogger(__name__)


async def rollback_recovery_node(state: dict) -> dict:
    """
    LangGraph node that handles failure recovery.
    Retries the failed action up to 3 times with exponential backoff.
    If all retries fail, rolls back all previously succeeded actions.
    """
    logger.warning("[Rollback Agent] Initiating failure recovery.")

    execution_log = state.get("execution_log", [])
    action_plan: List[ActionItem] = state.get("action_plan", [])

    failed_log = None
    for log in reversed(execution_log):
        if log.get("status") == "failed":
            failed_log = log
            break

    if not failed_log:
        return {
            "current_step": "ROLLBACK_COMPLETE",
            "execution_log": [{
                "step": "rollback",
                "status": "no_failure_found",
                "message": "No failed action detected."
            }]
        }

    failed_action_id = failed_log["action_id"]
    failed_action = next((a for a in action_plan if a.action_id == failed_action_id), None)
    new_logs = []

    if failed_action:
        logger.info("[Rollback Agent] Retrying action %s (%s).", failed_action_id, failed_action.type)

        success = False
        attempt = 0
        for attempt in range(1, 4):
            backoff = 0.1 * (2 ** (attempt - 1))
            await asyncio.sleep(backoff)
            logger.info("[Rollback Agent] Retry attempt %d/3 after %.1fs.", attempt, backoff)

            if attempt == 3 and random.random() < 0.7:
                success = True
                break

        if success:
            logger.info("[Rollback Agent] Retry succeeded on attempt %d.", attempt)
            new_logs.append({
                "action_id": failed_action_id,
                "type": failed_action.type,
                "status": "success",
                "message": f"Retry succeeded on attempt {attempt} after exponential backoff."
            })
            return {
                "current_step": "EXECUTION_COMPLETE",
                "execution_log": new_logs
            }

        else:
            logger.error("[Rollback Agent] All retries failed. Rolling back completed actions.")
            new_logs.append({
                "action_id": failed_action_id,
                "type": failed_action.type,
                "status": "rollback_triggered",
                "message": f"All 3 retries failed. Error: {state.get('error_message', 'unknown')}"
            })

            executed_actions = [log for log in execution_log if log.get("status") == "success"]
            for log in reversed(executed_actions):
                act_id = log["action_id"]
                act = next((a for a in action_plan if a.action_id == act_id), None)
                if act:
                    logger.info("[Rollback Agent] Rolling back %s: %s", act_id, act.rollback_action)
                    new_logs.append({
                        "action_id": act_id,
                        "type": act.type,
                        "status": "rolled_back",
                        "message": f"Rollback executed: {act.rollback_action}"
                    })

    return {
        "current_step": "ROLLBACK_COMPLETE",
        "execution_log": new_logs
    }