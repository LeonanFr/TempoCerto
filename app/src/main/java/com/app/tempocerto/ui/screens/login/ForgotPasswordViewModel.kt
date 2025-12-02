package com.app.tempocerto.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    object CodeSent : ForgotPasswordUiState()
    object Success : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var tempContact: String = ""
    private var tempType: String = "SMS"

    fun sendCode(contact: String, method: ContactMethod) {
        var finalContact = contact
        val type = if (method == ContactMethod.SMS) "SMS" else "EMAIL"

        if (method == ContactMethod.SMS) {
            val digitsOnly = contact.filter { it.isDigit() || it == '+' }
            finalContact = if (digitsOnly.startsWith("+")) digitsOnly
            else if (digitsOnly.length <= 11) "+55$digitsOnly"
            else "+$digitsOnly"
        }

        tempContact = finalContact
        tempType = type

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            repository.requestPasswordReset(tempContact, tempType)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.CodeSent
                }
                .onFailure {
                    _uiState.value = ForgotPasswordUiState.Error(it.message ?: "Erro ao enviar código")
                }
        }
    }

    fun resetPassword(code: String, newPass: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            repository.resetPassword(tempContact, tempType, code, newPass)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.Success
                }
                .onFailure {
                    _uiState.value = ForgotPasswordUiState.Error(it.message ?: "Erro ao alterar senha")
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
        tempContact = ""
    }
}
