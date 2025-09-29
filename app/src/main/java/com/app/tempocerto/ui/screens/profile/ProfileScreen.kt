package com.app.tempocerto.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.app.tempocerto.R
import com.app.tempocerto.data.model.Preferences
import com.app.tempocerto.data.model.UserProfile
import com.app.tempocerto.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF7F7F7))
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ProfileUiState.Idle -> {
                    LaunchedEffect(Unit) {
                        viewModel.loadProfile()
                    }
                    CircularProgressIndicator()
                }
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = "Erro ao carregar perfil:\n${state.message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ProfileUiState.Loaded -> {
                    ProfileContent(
                        user = state.user,
                        onProfileChange = { updatedUser ->
                            viewModel.saveProfile(updatedUser)
                        },
                        onLogout = { /* TODO: Implement logout logic */ }
                    )
                }
                is ProfileUiState.Saved -> {
                    LaunchedEffect(Unit) {
                        viewModel.loadProfile()
                    }
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserProfile,
    onProfileChange: (UserProfile) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeaderCard(
                name = user.name,
                username = "@${user.name.lowercase().replace(" ", "")}",
                photoUrl = user.photoUrl,
                onEditClick = { /* TODO: Navigate to edit profile screen */ }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item { SectionTitle("Conta") }
        item {
            ProfileMenuItem(
                icon = Icons.Default.AccountCircle,
                title = "Minha Conta",
                subtitle = "Visualize os detalhes da sua conta",
                onClick = { /* TODO: Navigate */ }
            )
        }
        item { SectionTitle("Preferências de Dados") }
        item {
            var isTideChecked by remember { mutableStateOf(user.preferences.tide) }
            val tideIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_tide)

            ProfileMenuItem(
                icon = tideIcon,
                title = "Dados de Maré",
                onClick = {
                    isTideChecked = !isTideChecked
                    val updatedPreferences = user.preferences.copy(tide = isTideChecked)
                    onProfileChange(user.copy(preferences = updatedPreferences))
                }
            ) {
                Switch(checked = isTideChecked, onCheckedChange = {
                    isTideChecked = it
                    val updatedPreferences = user.preferences.copy(tide = it)
                    onProfileChange(user.copy(preferences = updatedPreferences))
                })
            }
        }
        item {
            var isSalinityChecked by remember { mutableStateOf(user.preferences.salinity) }
            val salinityIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_water)
            ProfileMenuItem(
                icon = salinityIcon,
                title = "Dados de Salinidade",
                onClick = {
                    isSalinityChecked = !isSalinityChecked
                    val updatedPreferences = user.preferences.copy(salinity = isSalinityChecked)
                    onProfileChange(user.copy(preferences = updatedPreferences))
                }
            ) {
                Switch(checked = isSalinityChecked, onCheckedChange = {
                    isSalinityChecked = it
                    val updatedPreferences = user.preferences.copy(salinity = it)
                    onProfileChange(user.copy(preferences = updatedPreferences))
                })
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { SectionTitle("Atividade") }
        item {
            ProfileMenuItem(
                icon = Icons.Default.DateRange,
                title = "Histórico de Consultas",
                onClick = { /* TODO: Navigate */ }
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Minhas Anotações",
                onClick = { /* TODO: Navigate */ }
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Warning,
                title = "Gerenciar Alertas",
                onClick = { /* TODO: Navigate */ }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { SectionTitle("Mais") }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Search,
                title = "Ajuda & Suporte",
                onClick = { /* TODO: Navigate */ }
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Sobre o App",
                onClick = { /* TODO: Navigate */ }
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Close,
                title = "Sair",
                onClick = onLogout
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    username: String,
    photoUrl: String?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Teal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = photoUrl,
                placeholder = painterResource(R.drawable.person),
                error = painterResource(R.drawable.person),
                contentDescription = "Foto do usuário",
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = username,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Perfil",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF2C2B47),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileContentPreview() {
    val sampleUser = UserProfile(
        id = "123",
        name = "Guilherme",
        photoUrl = null,
        preferences = Preferences(tide = true, salinity = false),
    )
    MaterialTheme {
        Box(modifier = Modifier.padding(top = 36.dp, bottom = 16.dp)) {
            ProfileContent(
                user = sampleUser,
                onProfileChange = {},
                onLogout = {}
            )
        }
    }
}