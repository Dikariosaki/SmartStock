package com.example.smartstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartstock.ui.screens.auth.LoginScreen
import com.example.smartstock.ui.screens.home.HomeScreen
import com.example.smartstock.ui.screens.inventory.InventoryScreen
import com.example.smartstock.ui.screens.movements.MovementsScreen
import com.example.smartstock.ui.screens.products.ProductsScreen
import com.example.smartstock.ui.screens.profile.ProfileScreen
import com.example.smartstock.ui.screens.reports.ReportsScreen
import com.example.smartstock.ui.screens.tasks.TasksScreen
import com.example.smartstock.ui.screens.users.UsersScreen
import com.example.smartstock.ui.session.SessionViewModel

object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val USERS = "users"
    const val PRODUCTS = "products"
    const val TASKS = "tasks"
    const val INVENTORY = "inventory"
    const val MOVEMENTS = "movements"
    const val REPORTS = "reports"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val sessionState by sessionViewModel.uiState.collectAsState()
    val currentSession = sessionState.session
    val roleName = currentSession?.roleName

    val navigateTo: (String) -> Unit = { route ->
        val targetRoute =
            if (currentSession == null) {
                AppRoutes.LOGIN
            } else if (canAccessRoute(roleName, route)) {
                route
            } else {
                AppRoutes.HOME
            }

        if (targetRoute != navController.currentDestination?.route) {
            navController.navigate(targetRoute) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentSession) {
        if (currentSession == null) {
            if (navController.currentDestination?.route != AppRoutes.LOGIN) {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == null || currentRoute == AppRoutes.LOGIN) {
                navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentSession == null) AppRoutes.LOGIN else AppRoutes.HOME,
        modifier = modifier,
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                sessionState = sessionState,
                onLogin = sessionViewModel::login,
                onClearError = sessionViewModel::clearLoginError,
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                currentRoute = AppRoutes.HOME,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.USERS) {
            UsersScreen(
                currentRoute = AppRoutes.USERS,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.PRODUCTS) {
            ProductsScreen(
                currentRoute = AppRoutes.PRODUCTS,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.TASKS) {
            TasksScreen(
                currentRoute = AppRoutes.TASKS,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.INVENTORY) {
            InventoryScreen(
                currentRoute = AppRoutes.INVENTORY,
                roleName = roleName,
                currentSession = currentSession,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.MOVEMENTS) {
            MovementsScreen(
                currentRoute = AppRoutes.MOVEMENTS,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.REPORTS) {
            ReportsScreen(
                currentRoute = AppRoutes.REPORTS,
                roleName = roleName,
                onNavigate = navigateTo,
            )
        }
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                currentRoute = AppRoutes.PROFILE,
                roleName = roleName,
                currentSession = currentSession,
                onLogout = sessionViewModel::logout,
                onNavigate = navigateTo,
            )
        }
    }
}

private fun canAccessRoute(roleName: String?, route: String): Boolean {
    val role = roleName?.trim()?.lowercase()

    return when (route) {
        AppRoutes.LOGIN -> true
        AppRoutes.HOME, AppRoutes.PROFILE -> role != null
        AppRoutes.USERS -> role == "administrador"
        AppRoutes.PRODUCTS,
        AppRoutes.TASKS,
        -> role in setOf("administrador", "supervisor", "auxiliar")
        AppRoutes.INVENTORY,
        AppRoutes.MOVEMENTS,
        -> role in setOf("administrador", "supervisor")
        AppRoutes.REPORTS -> role in setOf("administrador", "supervisor", "auxiliar")
        else -> false
    }
}
