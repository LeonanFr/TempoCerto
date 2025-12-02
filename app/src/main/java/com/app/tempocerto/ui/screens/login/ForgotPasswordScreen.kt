package com.app.tempocerto.ui.screens.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.screens.register.MethodSelectionButton
import com.app.tempocerto.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var contact by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(ContactMethod.SMS) }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isStepTwo by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordUiState.CodeSent -> isStepTwo = true
            is ForgotPasswordUiState.Success -> {}
            else -> {}
        }
    }

    BackHandler(enabled = isStepTwo && uiState !is ForgotPasswordUiState.Success) {
        isStepTwo = false
        viewModel.resetState()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BlobBackground()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isStepTwo && uiState !is ForgotPasswordUiState.Success) {
                                isStepTwo = false
                                viewModel.resetState()
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Spacer(modifier = Modifier.weight(0.25f))

                Surface(
                    modifier = Modifier
                        .weight(0.75f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Recuperar Senha",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Teal
                        )
                        Spacer(Modifier.height(24.dp))

                        if (uiState is ForgotPasswordUiState.Success) {
                            Icon(Icons.Default.CheckCircle, null, tint = Teal, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Senha alterada com sucesso!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Fazer Login")
                            }

                        } else if (isStepTwo) {
                            Text(
                                text = "Código enviado para:",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = contact,
                                color = Teal,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                                label = { Text("Código (6 dígitos)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 4.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nova Senha") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                    }
                                },
                                isError = newPassword.isNotEmpty() && newPassword.length < 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.LightGray,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                                Text(
                                    "Mínimo de 6 caracteres",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(start = 8.dp, top = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(32.dp))

                            if (uiState is ForgotPasswordUiState.Error) {
                                Text(
                                    text = (uiState as ForgotPasswordUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                            }

                            Button(
                                onClick = { viewModel.resetPassword(otpCode, newPassword) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = otpCode.length == 6 && newPassword.length >= 6 && uiState !is ForgotPasswordUiState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal)
                            ) {
                                if (uiState is ForgotPasswordUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Redefinir Senha")
                                }
                            }

                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MethodSelectionButton("Celular", selectedMethod == ContactMethod.SMS) {
                                    selectedMethod = ContactMethod.SMS
                                }
                                MethodSelectionButton("E-mail", selectedMethod == ContactMethod.EMAIL) {
                                    selectedMethod = ContactMethod.EMAIL
                                }
                            }
                            Spacer(Modifier.height(24.dp))

                            val (label, icon, keyType) =
                                if (selectedMethod == ContactMethod.SMS)
                                    Triple("Telefone (ex: 9198888...)", Icons.Default.Phone, KeyboardType.Phone)
                                else Triple("E-mail", Icons.Default.Email, KeyboardType.Email)

                            OutlinedTextField(
                                value = contact,
                                onValueChange = { contact = it },
                                label = { Text(label) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = keyType),
                                leadingIcon = { Icon(icon, null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(Modifier.height(32.dp))

                            if (uiState is ForgotPasswordUiState.Error) {
                                Text(
                                    text = (uiState as ForgotPasswordUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                            }

                            Button(
                                onClick = { viewModel.sendCode(contact, selectedMethod) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = contact.isNotBlank() && uiState !is ForgotPasswordUiState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal)
                            ) {
                                if (uiState is ForgotPasswordUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Enviar Código")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
