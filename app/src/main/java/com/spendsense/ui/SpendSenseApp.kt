package com.spendsense.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendsense.ui.screens.*
import com.spendsense.ui.viewmodel.AuthState
import com.spendsense.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Analytics : Screen("analytics", "Analysis", Icons.Default.PieChart)
    object History : Screen("history", "History", Icons.Default.History)
    object Targets : Screen("targets", "Targets", Icons.Default.Flag)
    object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
}

@Composable
fun SpendSenseApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    var showSplash by remember { mutableStateOf(true) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash || authState is AuthState.Loading) {
        SplashScreen()
        return
    }

    if (authState is AuthState.Unauthenticated) {
        LoginScreen()
        return
    }

    val items = listOf(
        Screen.Home,
        Screen.Analytics,
        Screen.History,
        Screen.Targets,
        Screen.Reports
    )

    Scaffold(
        bottomBar = {
            if (items.any { it.route == currentDestination?.route }) {
                // Interactive Floating Navigation Bar
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(72.dp)
                    ) {
                        items.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            
                            NavigationBarItem(
                                icon = { 
                                    AnimatedContent(
                                        targetState = selected,
                                        transitionSpec = {
                                            (scaleIn(initialScale = 0.7f) + fadeIn()) togetherWith 
                                            (scaleOut(targetScale = 0.7f) + fadeOut())
                                        },
                                        label = "navIconAnimation"
                                    ) { isSelected ->
                                        Icon(
                                            imageVector = screen.icon, 
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                                        )
                                    }
                                },
                                label = { 
                                    Text(
                                        text = screen.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        fontSize = if (selected) 11.sp else 10.sp
                                    ) 
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                DashboardScreen(
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Targets.route) {
                TargetsScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable("addTransaction") {
                AddTransactionScreen(onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
