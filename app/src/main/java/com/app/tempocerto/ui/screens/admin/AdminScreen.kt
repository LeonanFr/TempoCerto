package com.app.tempocerto.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tempocerto.data.model.AccessRequestResponse
import com.app.tempocerto.ui.theme.Teal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedRequest by remember { mutableStateOf<AccessRequestResponse?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitações Pendentes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is AdminUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AdminUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AdminUiState.Success -> {
                    if (state.requests.isEmpty()) {
                        Text(
                            text = "Nenhuma solicitação pendente.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(state.requests) { request ->
                                val formattedDate = try {
                                    val zdt = java.time.ZonedDateTime.parse(request.requestDate)
                                    zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                } catch (_: Exception) {
                                    request.requestDate
                                }

                                RequestCard(
                                    name = request.name,
                                    username = request.username,
                                    days = request.requestedDays,
                                    date = formattedDate,
                                    onApproveClick = { selectedRequest = request }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        if (selectedRequest != null) {
            ApproveDialog(
                request = selectedRequest!!,
                onDismiss = { selectedRequest = null },
                onConfirm = { days ->
                    viewModel.approveRequest(selectedRequest!!.id, days)
                    selectedRequest = null
                }
            )
        }
    }
}

@Composable
fun RequestCard(
    name: String,
    username: String,
    days: Int,
    date: String,
    onApproveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(text = "@$username", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Pediu: $days dias", color = Teal, fontWeight = FontWeight.SemiBold)
                Text(text = "Em: $date", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = onApproveClick,
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Revisar")
            }
        }
    }
}

@Composable
fun ApproveDialog(
    request: AccessRequestResponse,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var daysText by remember { mutableStateOf(request.requestedDays.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aprovar Acesso") },
        text = {
            Column {
                Text("Defina por quanto tempo o usuário terá acesso aos dados históricos.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) daysText = it },
                    label = { Text("Dias de Acesso") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Usuário pediu: ${request.requestedDays} dias",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysText.toIntOrNull() ?: request.requestedDays
                    onConfirm(days)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("Confirmar Aprovação")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}