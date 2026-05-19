package com.finora.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finora.ai.ui.components.FinoraBottomNavBar
import com.finora.ai.ui.navigation.FinoraNavGraph
import com.finora.ai.ui.navigation.Screen
import com.finora.ai.ui.theme.FinoraAITheme

// ═══════════════════════════════════════════════════════════════
// FinoraAI — Agentic Financial Intelligence Platform
// Main Activity — Entry point with theme toggle & bottom nav
// ═══════════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinoraApp()
        }
    }
}

@Composable
fun FinoraApp() {
    var isDarkTheme by rememberSaveable { mutableStateOf(true) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that show the bottom navigation bar
    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Simulator.route,
        Screen.GhostLedgerCamera.route,
        Screen.AuditTrail.route,
    )
    val showBottomNav = currentRoute in bottomNavRoutes

    // Screens that are full-screen (no system chrome)
    val immersiveRoutes = setOf(
        Screen.Splash.route,
        Screen.WarRoom.route,
        Screen.ScenarioBattle.route,
    )

    FinoraAITheme(darkTheme = isDarkTheme) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                ) {
                    FinoraBottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            FinoraNavGraph(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = !isDarkTheme },
                modifier = Modifier.padding(
                    bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp
                ),
            )
        }
    }
}
