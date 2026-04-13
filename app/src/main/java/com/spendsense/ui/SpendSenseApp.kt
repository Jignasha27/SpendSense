package com.spendsense.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendsense.ui.screens.AddTransactionScreen
import com.spendsense.ui.screens.AnalyticsScreen
import com.spendsense.ui.screens.DashboardScreen

@Composable
fun SpendSenseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "dashboard" || currentRoute == "analytics") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Analytics") },
                        label = { Text("Analytics") },
                        selected = currentRoute == "analytics",
                        onClick = {
                            navController.navigate("analytics") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToAddTransaction = { navController.navigate("addTransaction") }
                )
            }
            composable("analytics") {
                AnalyticsScreen()
            }
            composable("addTransaction") {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
