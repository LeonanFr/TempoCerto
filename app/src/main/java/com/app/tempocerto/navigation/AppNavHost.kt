package com.app.tempocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.tempocerto.ui.screens.Home
import com.app.tempocerto.ui.screens.login.LoginScreen
import com.app.tempocerto.ui.screens.profile.ProfileScreen
import com.app.tempocerto.ui.screens.ParameterSelectionScreen
import com.app.tempocerto.ui.screens.graph.GraphScreen
import com.app.tempocerto.ui.screens.list.ListScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("profile_screen") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = "profile_screen") {
            ProfileScreen(navController = navController)
        }

        composable(route = "home") {
            Home(navController = navController)
        }

        composable(
            route = "parameter_selection/{subSystem}",
            arguments = listOf(navArgument("subSystem") { type = NavType.StringType })
        ) {
            ParameterSelectionScreen(navController = navController)
        }

        composable(
            route = "graph_screen/{subSystem}/{parameter}",
            arguments = listOf(
                navArgument("subSystem") { type = NavType.StringType },
                navArgument("parameter") { type = NavType.StringType }
            )
        ) {
            GraphScreen(navController = navController)
        }

        composable(
            route = "list_screen/{subSystem}/{parameter}",
            arguments = listOf(
                navArgument("subSystem") { type = NavType.StringType },
                navArgument("parameter") { type = NavType.StringType }
            )
        ) {
            ListScreen(navController = navController)
        }
    }
}
