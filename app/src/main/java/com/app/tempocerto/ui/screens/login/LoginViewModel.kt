package com.app.tempocerto.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.TokenManager
import com.app.tempocerto.data.model.RegisterRequest
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object OtpRequired : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

enum class ContactMethod {
    SMS, EMAIL
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private var tempRegisterData: RegisterRequest? = null
    private var currentContactType: String = "SMS"

    fun initiateRegistration(name: String, username: String, contact: String, method: ContactMethod, password: String) {
        val type = if (method == ContactMethod.SMS) "SMS" else "EMAIL"
        currentContactType = type

        var finalContact = contact
        if (method == ContactMethod.SMS) {
            val digitsOnly = contact.filter { it.isDigit() || it == '+' }

            finalContact = if (digitsOnly.startsWith("+")) {
                digitsOnly
            } else {
                if (digitsOnly.length <= 11) {
                    "+55$digitsOnly"
                } else {
                    "+$digitsOnly"
                }
            }
        }

        val phone = if (method == ContactMethod.SMS) finalContact else null
        val email = if (method == ContactMethod.EMAIL) finalContact else null

        tempRegisterData = RegisterRequest(
            name = name,
            username = username,
            email = email,
            phone = phone,
            password = password,
            otpCode = null
        )

        viewModelScope.launch {
            _registerUiState.value = RegisterUiState.Loading
            repository.sendOtp(finalContact, type, "REGISTER")
                .onSuccess {
                    _registerUiState.value = RegisterUiState.OtpRequired
                }
                .onFailure { error ->
                    _registerUiState.value = RegisterUiState.Error(error.message ?: "Erro ao enviar código")                }
        }
    }

    fun confirmRegistration(otpCode: String) {
        val data = tempRegisterData ?: return

        viewModelScope.launch {
            _registerUiState.value = RegisterUiState.Loading

            val emailToSend = data.email ?: ""
            val phoneToSend = data.phone ?: ""

            repository.register(data.name, data.username, emailToSend, phoneToSend, data.password, otpCode)
                .onSuccess {
                    repository.login(data.username, data.password)
                        .onSuccess { loginResp ->
                            tokenManager.saveToken(loginResp.token)
                            _registerUiState.value = RegisterUiState.Success
                        }
                        .onFailure {
                            _registerUiState.value = RegisterUiState.Error("Conta criada, mas falha ao logar.")
                        }
                }
                .onFailure {
                    _registerUiState.value = RegisterUiState.Error(it.message ?: "Erro no cadastro")
                }
        }
    }
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            repository.login(username, password)
                .onSuccess {
                    tokenManager.saveToken(it.token)
                    _loginUiState.value = LoginUiState.Success
                }
                .onFailure {
                    _loginUiState.value = LoginUiState.Error(it.message ?: "Erro inesperado")
                }
        }
    }

    fun resetLoginState() {
        _loginUiState.value = LoginUiState.Idle
    }

    fun resetRegisterState() {
        _registerUiState.value = RegisterUiState.Idle
        tempRegisterData = null
    }
}
