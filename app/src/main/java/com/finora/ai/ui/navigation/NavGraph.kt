package com.finora.ai.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.finora.ai.ui.screens.splash.SplashScreen
import com.finora.ai.ui.screens.login.LoginScreen
import com.finora.ai.ui.screens.home.HomeScreen
import com.finora.ai.ui.screens.analysis.SourceSelectionScreen
import com.finora.ai.ui.screens.warroom.WarRoomScreen
import com.finora.ai.ui.screens.insight.InsightReportScreen
import com.finora.ai.ui.screens.actionchain.ActionChainScreen
import com.finora.ai.ui.screens.execution.ExecutionTrackerScreen
import com.finora.ai.ui.screens.ghostledger.GhostLedgerCameraScreen
import com.finora.ai.ui.screens.ghostledger.GhostLedgerReviewScreen
import com.finora.ai.ui.screens.simulator.SimulatorScreen
import com.finora.ai.ui.screens.simulator.ScenarioBattleScreen
import com.finora.ai.ui.screens.audit.AuditTrailScreen
import com.finora.ai.ui.screens.audit.AuditDetailScreen
import com.finora.ai.viewmodel.FinoraViewModel

// ═══════════════════════════════════════════════════════════════
// FinoraAI Navigation Graph — Smooth animated transitions
// ═══════════════════════════════════════════════════════════════

@Composable
fun FinoraNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Shared ViewModel scoped to the NavGraph (activity-level)
    val finoraViewModel: FinoraViewModel = viewModel()

    // Check backend health on first composition
    LaunchedEffect(Unit) {
        finoraViewModel.checkBackendHealth()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
        },
        exitTransition = {
            fadeOut(tween(300, easing = FastOutLinearInEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                )
        },
        popEnterTransition = {
            fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
        },
        popExitTransition = {
            fadeOut(tween(300, easing = FastOutLinearInEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                )
        },
    ) {
        // ── Splash ──────────────────────────────────────────
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = {
                fadeOut(tween(500)) + scaleOut(
                    targetScale = 1.2f,
                    animationSpec = tween(500)
                )
            },
        ) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ───────────────────────────────────────────
        composable(
            route = Screen.Login.route,
            enterTransition = { FinoraTransitions.slideUpEnter },
            exitTransition = {
                fadeOut(tween(400)) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(400)
                )
            },
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home Dashboard ──────────────────────────────────
        composable(route = Screen.Home.route) {
            HomeScreen(
                viewModel = finoraViewModel,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onNewAnalysis = { navController.navigate(Screen.SourceSelection.route) },
                onScanLedger = { navController.navigate(Screen.GhostLedgerCamera.route) },
                onSimulate = { navController.navigate(Screen.Simulator.route) },
                onAuditTrail = { navController.navigate(Screen.AuditTrail.route) },
            )
        }

        // ── Source Selection (wired to backend) ─────────────
        composable(route = Screen.SourceSelection.route) {
            SourceSelectionScreen(
                viewModel = finoraViewModel,
                onStartAnalysis = { sessionId ->
                    navController.navigate(Screen.WarRoom.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── War Room (wired to backend) ─────────────────────
        composable(
            route = Screen.WarRoom.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
            enterTransition = { FinoraTransitions.warRoomEnter },
            exitTransition = { FinoraTransitions.warRoomExit },
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            WarRoomScreen(
                sessionId = sessionId,
                viewModel = finoraViewModel,
                onComplete = {
                    navController.navigate(Screen.InsightReport.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Insight Report (wired to backend) ───────────────
        composable(
            route = Screen.InsightReport.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            InsightReportScreen(
                sessionId = sessionId,
                viewModel = finoraViewModel,
                onGenerateActionPlan = {
                    navController.navigate(Screen.ActionChain.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Action Chain (wired to backend) ─────────────────
        composable(
            route = Screen.ActionChain.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ActionChainScreen(
                sessionId = sessionId,
                viewModel = finoraViewModel,
                onExecuteChain = {
                    navController.navigate(Screen.ExecutionTracker.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Execution Tracker (wired to backend) ────────────
        composable(
            route = Screen.ExecutionTracker.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ExecutionTrackerScreen(
                sessionId = sessionId,
                viewModel = finoraViewModel,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Ghost Ledger Camera ─────────────────────────────
        composable(
            route = Screen.GhostLedgerCamera.route,
            enterTransition = { FinoraTransitions.slideUpEnter },
            exitTransition = { FinoraTransitions.slideDownExit },
        ) {
            GhostLedgerCameraScreen(
                onPhotoCaptured = { extractionId ->
                    navController.navigate(Screen.GhostLedgerReview.createRoute(extractionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Ghost Ledger Review ─────────────────────────────
        composable(
            route = Screen.GhostLedgerReview.route,
            arguments = listOf(navArgument("extractionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val extractionId = backStackEntry.arguments?.getString("extractionId") ?: ""
            GhostLedgerReviewScreen(
                extractionId = extractionId,
                onConfirm = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Simulator ───────────────────────────────────────
        composable(route = Screen.Simulator.route) {
            SimulatorScreen(
                onStartBattle = { navController.navigate(Screen.ScenarioBattle.route) },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Scenario Battle ─────────────────────────────────
        composable(
            route = Screen.ScenarioBattle.route,
            enterTransition = { FinoraTransitions.warRoomEnter },
            exitTransition = { FinoraTransitions.warRoomExit },
        ) {
            ScenarioBattleScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Audit Trail ─────────────────────────────────────
        composable(route = Screen.AuditTrail.route) {
            AuditTrailScreen(
                viewModel = finoraViewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Screen.AuditDetail.createRoute(entryId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Audit Detail ────────────────────────────────────
        composable(
            route = Screen.AuditDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            AuditDetailScreen(
                entryId = entryId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
