package com.finora.ai.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.ui.graphics.vector.ImageVector

// ═══════════════════════════════════════════════════════════════
// FinoraAI Navigation Routes & Bottom Nav Items
// ═══════════════════════════════════════════════════════════════

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object SourceSelection : Screen("source_selection")
    object WarRoom : Screen("war_room/{sessionId}") {
        fun createRoute(sessionId: String) = "war_room/$sessionId"
    }
    object InsightReport : Screen("insight_report/{sessionId}") {
        fun createRoute(sessionId: String) = "insight_report/$sessionId"
    }
    object ActionChain : Screen("action_chain/{sessionId}") {
        fun createRoute(sessionId: String) = "action_chain/$sessionId"
    }
    object ExecutionTracker : Screen("execution_tracker/{sessionId}") {
        fun createRoute(sessionId: String) = "execution_tracker/$sessionId"
    }
    object GhostLedgerCamera : Screen("ghost_ledger_camera")
    object GhostLedgerReview : Screen("ghost_ledger_review/{extractionId}") {
        fun createRoute(extractionId: String) = "ghost_ledger_review/$extractionId"
    }
    object Simulator : Screen("simulator")
    object ScenarioBattle : Screen("scenario_battle")
    object AuditTrail : Screen("audit_trail")
    object AuditDetail : Screen("audit_detail/{entryId}") {
        fun createRoute(entryId: String) = "audit_detail/$entryId"
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Simulate", Screen.Simulator.route, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    BottomNavItem("Scan", Screen.GhostLedgerCamera.route, Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    BottomNavItem("Audit", Screen.AuditTrail.route, Icons.Filled.Security, Icons.Outlined.Security),
)

// ── Transition Specs ────────────────────────────────────────────

object FinoraTransitions {

    val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth / 4 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        ) + slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        )
    }

    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        ) + slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth / 4 },
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        )
    }

    // Special transition for War Room (dramatic entry)
    val warRoomEnter: EnterTransition =
        fadeIn(tween(600)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )

    val warRoomExit: ExitTransition =
        fadeOut(tween(400)) + scaleOut(
            targetScale = 1.1f,
            animationSpec = tween(400, easing = FastOutLinearInEasing)
        )

    // Vertical slide for modals / overlays
    val slideUpEnter: EnterTransition =
        fadeIn(tween(350)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )

    val slideDownExit: ExitTransition =
        fadeOut(tween(250)) + slideOutVertically(
            targetOffsetY = { it / 3 },
            animationSpec = tween(250)
        )
}
