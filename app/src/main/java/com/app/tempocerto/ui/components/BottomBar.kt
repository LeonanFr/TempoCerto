package com.app.tempocerto.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.app.tempocerto.R

@Composable
fun BottomBar(
    navController: NavHostController,
    onSearchClicked: () -> Unit,
    showSearchButton: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF006D77)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(iconRes = R.drawable.ic_home, label = "Início") {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }

        if (showSearchButton) {
            BottomBarItem(iconRes = R.drawable.ic_search, label = "Pesquisa", onClick = onSearchClicked)
        }

        BottomBarItem(iconRes = R.drawable.ic_person, label = "Perfil") {
            navController.navigate("profile_screen")
        }
    }
}

@Composable
private fun BottomBarItem(
    iconRes: Int,
    label: String,
    size: Dp = 30.dp,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(size).padding(bottom = 4.dp)
            )
            Text(text = label, color = Color.White)
        }
    }
}
