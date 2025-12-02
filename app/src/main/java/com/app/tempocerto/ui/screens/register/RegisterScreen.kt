package com.app.tempocerto.ui.screens.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.screens.login.ContactMethod
import com.app.tempocerto.ui.screens.login.LoginViewModel
import com.app.tempocerto.ui.screens.login.RegisterUiState
import com.app.tempocerto.ui.theme.Teal

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onSignInClicked: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val registerState by viewModel.registerUiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var contactValue by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(ContactMethod.SMS) }

    var isVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterUiState.OtpRequired -> {
                isVerifying = true
            }
            is RegisterUiState.Success -> {
                isVerifying = false
                onRegisterSuccess()
                viewModel.resetRegisterState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BlobBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(0.25f))

            Surface(
                modifier = Modifier
                    .weight(0.75f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                if (isVerifying) {
                    BackHandler {
                        isVerifying = false
                        viewModel.resetRegisterState()
                    }
                    VerificationContent(
                        contact = contactValue,
                        method = selectedMethod,
                        isLoading = registerState is RegisterUiState.Loading,
                        errorMessage = (registerState as? RegisterUiState.Error)?.message,
                        onConfirm = { code -> viewModel.confirmRegistration(code) },
                        onBack = {
                            isVerifying = false
                            viewModel.resetRegisterState()
                        }
                    )
                } else {
                    RegistrationFormContent(
                        name = name, onNameChange = { name = it },
                        username = username, onUsernameChange = { username = it },
                        contact = contactValue, onContactChange = { contactValue = it },
                        password = password, onPasswordChange = { password = it },
                        passwordVisible = passwordVisible, onPasswordVisibilityChange = { passwordVisible = it },
                        selectedMethod = selectedMethod, onMethodSelected = { selectedMethod = it },
                        isLoading = registerState is RegisterUiState.Loading,
                        errorMessage = (registerState as? RegisterUiState.Error)?.message,
                        onSubmit = {
                            viewModel.initiateRegistration(name, username, contactValue, selectedMethod, password)
                        },
                        onSignInClicked = onSignInClicked
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationFormContent(
    name: String, onNameChange: (String) -> Unit,
    username: String, onUsernameChange: (String) -> Unit,
    contact: String, onContactChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean, onPasswordVisibilityChange: (Boolean) -> Unit,
    selectedMethod: ContactMethod, onMethodSelected: (ContactMethod) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onSignInClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Criar Conta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Teal
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MethodSelectionButton(
                text = "Celular (SMS)",
                isSelected = selectedMethod == ContactMethod.SMS,
                onClick = { onMethodSelected(ContactMethod.SMS) }
            )
            MethodSelectionButton(
                text = "E-mail",
                isSelected = selectedMethod == ContactMethod.EMAIL,
                onClick = { onMethodSelected(ContactMethod.EMAIL) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray
            )
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = username, onValueChange = onUsernameChange,
            label = { Text("Nome de Usuário") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray
            )
        )
        Spacer(Modifier.height(12.dp))

        val (label, icon, keyType) = if (selectedMethod == ContactMethod.SMS) {
            Triple("Telefone (ex: 9198888...)", Icons.Default.Phone, KeyboardType.Phone)
        } else {
            Triple("Endereço de E-mail", Icons.Default.Email, KeyboardType.Email)
        }

        OutlinedTextField(
            value = contact, onValueChange = onContactChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyType),
            leadingIcon = { Icon(icon, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray
            )
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            isError = password.isNotEmpty() && password.length < 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        if (password.isNotEmpty() && password.length < 6) {
            Text(
                text = "A senha deve ter no mínimo 6 caracteres",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        } else {
            Text(
                text = "Mínimo de 6 caracteres",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            enabled = !isLoading && contact.isNotBlank() && username.isNotBlank() && password.length >= 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Enviar Código de Verificação")
            }
        }

        Spacer(Modifier.weight(1f))

        val annotatedText = buildAnnotatedString {
            append("Já tem uma conta? ")
            pushStringAnnotation(tag = "SignIn", annotation = "SignIn")
            withStyle(style = SpanStyle(color = Teal, fontWeight = FontWeight.Bold)) { append("Entre") }
            pop()
        }
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable {
                    annotatedText.getStringAnnotations("SignIn", 0, annotatedText.length)
                        .firstOrNull()?.let { onSignInClicked() }
                }
        )
    }
}

@Composable
fun VerificationContent(
    contact: String,
    method: ContactMethod,
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verificação",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Teal
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Digite o código enviado via ${if (method == ContactMethod.SMS) "SMS" else "E-mail"} para:",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Text(
            text = contact,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Teal,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otpCode = it },
            label = { Text("Código (6 dígitos)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirm(otpCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            enabled = otpCode.length == 6 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirmar Cadastro", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RowScope.MethodSelectionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Teal else Color.Gray
    val shadow = if (isSelected) 4.dp else 0.dp

    Surface(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        shadowElevation = shadow
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
