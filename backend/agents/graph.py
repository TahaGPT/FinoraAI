# backend/agents/graph.py

from langgraph.graph import StateGraph, END
from langgraph.checkpoint.memory import MemorySaver

from .state import FinoraState
from .insight_agent import insight_analyst_node
from .contradiction_agent import contradiction_detector_node
from .planner_agent import action_planner_node
from .validator_agent import constraint_validator_node
from .executor_agent import action_executor_node
from .rollback_agent import rollback_recovery_node


# ── Routing functions (conditional edges) ─────────────────────────────────────

def route_after_insights(state: FinoraState) -> str:
    """After insight analysis: if contradictions found, resolve them. Else go straight to planning."""
    if "insight_analysis" in state.get("failed_steps", []):
        return END   # abort if insight step failed
        
    report = state.get("insight_report")
    if report and report.contradictions:
        return "contradiction_detector"
    return "action_planner"


def route_after_planning(state: FinoraState) -> str:
    """After action planning: go to constraint validation unless planning failed."""
    if "action_planning" in state.get("failed_steps", []):
        return END
    return "constraint_validator"


def route_after_execution(state: FinoraState) -> str:
    """After action execution: if execution failed, route to rollback. Else end."""
    if "action_executor" in state.get("failed_steps", []):
        return "rollback_recovery"
    return END


# ── Build the graph ────────────────────────────────────────────────────────────

def build_finora_graph():
    workflow = StateGraph(FinoraState)

    # Register all nodes
    workflow.add_node("insight_analyst",        insight_analyst_node)
    workflow.add_node("contradiction_detector",  contradiction_detector_node)
    workflow.add_node("action_planner",          action_planner_node)
    workflow.add_node("constraint_validator",    constraint_validator_node)
    workflow.add_node("action_executor",         action_executor_node)
    workflow.add_node("rollback_recovery",       rollback_recovery_node)

    # Entry point
    workflow.set_entry_point("insight_analyst")

    # Conditional edge after insight analysis
    workflow.add_conditional_edges(
        "insight_analyst",
        route_after_insights,
        {
            "contradiction_detector": "contradiction_detector",
            "action_planner":         "action_planner",
            END:                      END,
        }
    )

    # After contradiction detection → always go to action planner
    workflow.add_edge("contradiction_detector", "action_planner")

    # Conditional edge after action planning
    workflow.add_conditional_edges(
        "action_planner",
        route_after_planning,
        {
            "constraint_validator": "constraint_validator",
            END:                    END,
        }
    )

    # Constraint validator → action executor
    workflow.add_edge("constraint_validator", "action_executor")

    # Conditional edge after execution (success vs failure rollback)
    workflow.add_conditional_edges(
        "action_executor",
        route_after_execution,
        {
            "rollback_recovery": "rollback_recovery",
            END:                 END,
        }
    )

    # Rollback is terminal node after a failure
    workflow.add_edge("rollback_recovery", END)

    # Compile with memory checkpointing (auto-saves state after every node)
    # Set interrupt_before="action_executor" for human-in-the-loop validation
    memory = MemorySaver()
    return workflow.compile(
        checkpointer=memory,
        interrupt_before=["action_executor"]
    )


# Export a single compiled instance
finora_graph = build_finora_graph()