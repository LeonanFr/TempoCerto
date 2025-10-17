package com.app.tempocerto.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.TokenManager
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
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
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

    fun register(name: String, username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerUiState.value = RegisterUiState.Loading
            repository.register(name, username, email, password)
                .onSuccess {
                    repository.login(username, password)
                        .onSuccess {
                            tokenManager.saveToken(it.token)
                            _registerUiState.value = RegisterUiState.Success
                        }
                        .onFailure {
                            _registerUiState.value = RegisterUiState.Error("Cadastro criado, mas falha ao logar.")
                        }
                }
                .onFailure {
                    _registerUiState.value = RegisterUiState.Error(it.message ?: "Erro inesperado")
                }
        }
    }

    fun resetLoginState() {
        _loginUiState.value = LoginUiState.Idle
    }

    fun resetRegisterState() {
        _registerUiState.value = RegisterUiState.Idle
    }
}
