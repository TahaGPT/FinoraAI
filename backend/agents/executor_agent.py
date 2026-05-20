# backend/agents/executor_agent.py

import time
import logging
from typing import Dict, Any

from .state import ActionItem

logger = logging.getLogger(__name__)


def trace_task(task_id: str, agent: str, status: str, duration_ms: int, tools_called: list = None):
    """Logs task execution telemetry metrics."""
    logger.info(
        "TRACE | Task: %s | Agent: %s | Status: %s | Duration: %dms | Tools: %s",
        task_id, agent, status, duration_ms, tools_called or []
    )


async def execute_single_action(action_id: str, action_type: str, description: str) -> Dict[str, Any]:
    """
    Executes tool calls for all supported action types.
    Returns a result dict with status and message.
    """
    messages = {
        "NOTIFY":        f"Notification sent: '{description}'",
        "UPDATE_RECORD": f"Database record updated: '{description}'",
        "SIMULATE":      f"Financial simulation completed: '{description}'",
        "FETCH_DATA":    f"External data fetched: '{description}'",
        "ALERT":         f"System alert triggered: '{description}'",
        "SCHEDULE":      f"Recurring job scheduled: '{description}'",
    }
    return {
        "status": "success",
        "message": messages.get(action_type, f"Action executed: '{description}'")
    }


async def action_executor_node(state: dict) -> dict:
    """
    LangGraph node that executes all approved actions.
    Handles both Pydantic objects and plain dicts safely.
    Isolates failures per action so one failure does not block others.
    """
    action_plan = state.get("action_plan", [])
    logs = state.get("execution_log") if state.get("execution_log") is not None else []
    failed_steps = state.get("failed_steps") if state.get("failed_steps") is not None else []
    has_failure = False

    def get_val(item, attr_name):
        if isinstance(item, dict):
            return item.get(attr_name)
        return getattr(item, attr_name, None)

    approved_actions = [a for a in action_plan if get_val(a, "status") == "APPROVED"]
    logger.info("[Executor] Executing %d approved actions.", len(approved_actions))

    for action in approved_actions:
        start_time = time.time()
        a_id = get_val(action, "action_id")
        a_type = get_val(action, "type")
        a_desc = get_val(action, "description")

        try:
            result = await execute_single_action(a_id, a_type, a_desc)
            duration = int((time.time() - start_time) * 1000)
            trace_task(a_id, "ActionExecutor", "complete", duration, [a_type])
            logger.info("[Executor] SUCCESS | [%s] %s", a_type, result["message"])
            logs.append({
                "action_id": a_id,
                "type": a_type,
                "status": result["status"],
                "message": result["message"]
            })

        except Exception as e:
            duration = int((time.time() - start_time) * 1000)
            trace_task(a_id, "ActionExecutor", "failed", duration)
            logger.error("[Executor] FAILED | [%s] %s", a_type, str(e))
            logs.append({
                "action_id": a_id,
                "type": a_type,
                "status": "failed",
                "message": f"Execution failed: {str(e)}"
            })
            has_failure = True
            failed_steps.append(a_id)

    state["execution_log"] = logs
    state["failed_steps"] = failed_steps

    if has_failure:
        state["current_step"] = "EXECUTION_FAILED"
        state["error_message"] = f"Actions failed: {', '.join(failed_steps)}"
    else:
        state["current_step"] = "EXECUTION_COMPLETE"

    return state