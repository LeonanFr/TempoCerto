package com.app.tempocerto.ui.screens.register

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.screens.login.LoginViewModel
import com.app.tempocerto.ui.screens.login.RegisterUiState
import com.app.tempocerto.ui.theme.Teal
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onSignInClicked: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val registerUiState by viewModel.registerUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun validateFields(): Boolean {
        nameError = if (name.isBlank()) "Nome não pode estar em branco" else null
        usernameError = if (username.isBlank()) "Usuário não pode estar em branco" else null
        passwordError = if (password.length < 4) "Senha deve ter 4+ caracteres" else null
        emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Formato de e-mail inválido" else null

        return nameError == null && usernameError == null && passwordError == null && emailError == null
    }

    LaunchedEffect(registerUiState) {
        when (val state = registerUiState) {
            is RegisterUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetRegisterState()
            }
            is RegisterUiState.Success -> {
                onRegisterSuccess()
                viewModel.resetRegisterState()
            }
            else -> {}
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        BlobBackground()
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 180.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Criar Conta",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; nameError = null },
                                label = { Text("Nome completo") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = nameError != null,
                                supportingText = { if (nameError != null) Text(nameError!!) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it; usernameError = null },
                                label = { Text("Nome de usuário") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = usernameError != null,
                                supportingText = { if (usernameError != null) Text(usernameError!!) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; emailError = null },
                                label = { Text("E-mail") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = emailError != null,
                                supportingText = { if (emailError != null) Text(emailError!!) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; passwordError = null },
                                label = { Text("Senha") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = passwordError != null,
                                supportingText = { if (passwordError != null) Text(passwordError!!) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(icon, contentDescription = "Alternar visibilidade da senha")
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (validateFields()) {
                                        viewModel.register(name, username, email, password)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = registerUiState !is RegisterUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (registerUiState is RegisterUiState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("Cadastrar", color = Color.White)
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            val annotatedText = buildAnnotatedString {
                                append("Já tem uma conta? ")
                                pushStringAnnotation(tag = "SignIn", annotation = "SignIn")
                                withStyle(style = SpanStyle(color = Teal, fontWeight = FontWeight.Bold)) {
                                    append("Entre")
                                }
                                pop()
                            }

                            Text(
                                text = annotatedText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    annotatedText
                                        .getStringAnnotations("SignIn", 0, annotatedText.length)
                                        .firstOrNull()
                                        ?.let { onSignInClicked() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

