package com.app.tempocerto.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.app.tempocerto.ui.theme.Teal

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val loginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            BlobBackground()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp)
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
                        .imePadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Entrar",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nome de usuário") },

                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Teal)
                            )
                            Text("Lembre de mim")
                        }
                        TextButton(onClick = { onForgotPasswordClicked() }) {
                            Text("Esqueceu a senha?", color = Teal)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = loginUiState !is LoginUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Entrar", color = Color.White)
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        when (loginUiState) {
                            is LoginUiState.Loading -> {
                                CircularProgressIndicator(color = Teal)
                            }
                            is LoginUiState.Error -> {
                                Text(
                                    text = (loginUiState as LoginUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            is LoginUiState.Success -> {
                                LaunchedEffect(Unit) {
                                    onLoginSuccess()
                                    viewModel.resetLoginState()
                                }
                            }
                            else -> {}
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    val annotatedText = buildAnnotatedString {
                        append("Não tem uma conta? ")
                        pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
                        withStyle(style = SpanStyle(color = Teal, fontWeight = FontWeight.Bold)) {
                            append("Cadastre-se")
                        }
                        pop()
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            annotatedText.getStringAnnotations(tag = "SignUp", start = 0, end = annotatedText.length)
                                .firstOrNull()?.let {
                                    onSignUpClicked()
                                }
                        }
                    )
                }
            }
        }
    }
}