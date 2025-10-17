package com.app.tempocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.app.tempocerto.ui.screens.Home
import com.app.tempocerto.ui.screens.ParameterSelectionScreen
import com.app.tempocerto.ui.screens.SplashScreen
import com.app.tempocerto.ui.screens.graph.GraphScreen
import com.app.tempocerto.ui.screens.list.ListScreen
import com.app.tempocerto.ui.screens.login.LoginScreen
import com.app.tempocerto.ui.screens.profile.ProfileScreen
import com.app.tempocerto.ui.screens.register.RegisterScreen
import com.app.tempocerto.ui.screens.welcome.WelcomeScreen

object Routes {
    const val SPLASH_ROUTE = "splash"
    const val AUTH_GRAPH_ROUTE = "auth_graph"
    const val MAIN_GRAPH_ROUTE = "main_graph"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_ROUTE
    ) {
        composable(route = Routes.SPLASH_ROUTE) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.AUTH_GRAPH_ROUTE) {
                        popUpTo(Routes.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                        popUpTo(Routes.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        authGraph(navController)
        mainGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(startDestination = "welcome", route = Routes.AUTH_GRAPH_ROUTE) {
        composable(route = "welcome") {
            WelcomeScreen(
                onContinueClicked = { navController.navigate("login") }
            )
        }
        composable(route = "login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                        popUpTo(Routes.AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                },
                onSignUpClicked = { navController.navigate("register") }
            )
        }
        composable(route = "register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                        popUpTo(Routes.AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                },
                onSignInClicked = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = Routes.MAIN_GRAPH_ROUTE) {
        composable(route = "home") {
            Home(navController = navController)
        }
        composable(route = "profile_screen") {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.AUTH_GRAPH_ROUTE) {
                        popUpTo(Routes.MAIN_GRAPH_ROUTE) { inclusive = true }
                    }
                }
            )
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

