package com.app.tempocerto.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.tempocerto.ui.theme.Teal

private val mainNavItems = listOf(
    BottomNavItem(
        title = "Home",
        route = "home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "Perfil",
        route = "profile_screen",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

private val listNavItem = BottomNavItem("Lista", "list_screen",
    Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List
)
private val graphNavItem = BottomNavItem("Gráfico", "graph_screen", Icons.Filled.BarChart, Icons.Outlined.BarChart)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    onSearchClicked: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDataScreen = currentRoute?.startsWith("list_screen") == true ||
            currentRoute?.startsWith("graph_screen") == true

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDataScreen) {
                BottomBarItem(
                    item = mainNavItems.first { it.route == "home" },
                    isSelected = false,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )

                BottomBarItem(
                    item = BottomNavItem("Busca", "", Icons.Filled.Search, Icons.Outlined.Search),
                    isSelected = false,
                    onClick = onSearchClicked
                )

                val isListScreen = currentRoute.startsWith("list_screen")
                val toggleItem = if (isListScreen) graphNavItem else listNavItem

                BottomBarItem(
                    item = toggleItem,
                    isSelected = false,
                    onClick = {
                        val subSystem = navBackStackEntry?.arguments?.getString("subSystem")
                        val parameter = navBackStackEntry?.arguments?.getString("parameter")

                        if (subSystem != null && parameter != null) {
                            val newRoute = if (isListScreen) {
                                "graph_screen/$subSystem/$parameter"
                            } else {
                                "list_screen/$subSystem/$parameter"
                            }
                            navController.navigate(newRoute) {
                                popUpTo(currentRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )

            } else {
                mainNavItems.forEach { item ->
                    val isSelected = currentRoute?.startsWith(item.route) == true ||
                            (item.route == "home" && currentRoute?.startsWith("parameter_selection") == true)

                    BottomBarItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
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

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Teal else Color.Gray,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            tint = animatedColor,
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Teal)
            )
        }
    }
}

