package com.app.tempocerto.ui.screens.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tempocerto.R
import com.app.tempocerto.data.model.UserProfile
import com.app.tempocerto.ui.theme.Teal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
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
                is ProfileUiState.Idle, is ProfileUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = "Erro ao carregar perfil:\n${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { viewModel.loadProfile() }, modifier = Modifier.padding(top = 100.dp)) {
                        Text("Tentar Novamente")
                    }
                }
                is ProfileUiState.Loaded -> {
                    ProfileContent(
                        user = state.user,
                        snackbarHostState = snackbarHostState,
                        onLogout = {
                            viewModel.logout()
                            onLogout()
                        },
                        onAdminClick = onAdminClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserProfile,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val onComingSoonClick: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Funcionalidade em breve!")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeaderCard(
                name = user.name,
                username = user.username
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (user.role == "admin") {
                Button(
                    onClick = onAdminClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Área Administrativa")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item { SectionTitle("Informações da Conta") }

        item {
            ProfileInfoItem(
                icon = Icons.Default.Person,
                title = "Nome completo",
                value = user.name
            )
        }

        item {
            ProfileInfoItem(
                icon = Icons.Default.AccountCircle,
                title = "Nome de usuário",
                value = user.username
            )
        }

        if (!user.email.isNullOrBlank()) {
            item {
                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    title = "E-mail",
                    value = user.email
                )
            }
        }

        if (!user.phone.isNullOrBlank()) {
            item {
                ProfileInfoItem(
                    icon = Icons.Default.Phone,
                    title = "Telefone",
                    value = user.phone
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { SectionTitle("Preferências") }
        item {
            ProfileMenuItem(
                iconRes = R.drawable.ic_water,
                title = "Dados de Maré",
                onClick = onComingSoonClick
            ) {
                Switch(checked = false, onCheckedChange = null, enabled = false)
            }
        }
        item {
            ProfileMenuItem(
                iconRes = R.drawable.ic_tide,
                title = "Dados de Salinidade",
                onClick = onComingSoonClick
            ) {
                Switch(checked = false, onCheckedChange = null, enabled = false)
            }
        }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Warning,
                title = "Gerenciar Alertas",
                onClick = onComingSoonClick
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { SectionTitle("Mais") }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Shield,
                title = "Privacidade e Segurança",
                onClick = onComingSoonClick
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Sobre o App",
                onClick = onComingSoonClick
            )
        }
        item {
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Sair",
                onClick = onLogout,
                color = MaterialTheme.colorScheme.error
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
        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
    )
}

@Composable
private fun ProfileHeaderCard(name: String, username: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Ícone de Perfil",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "@$username",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    title: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = color,
            modifier = Modifier.weight(1f)
        )

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