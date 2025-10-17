package com.app.tempocerto.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.TokenManager
import com.app.tempocerto.data.model.UserProfile
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Loaded(val user: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    open val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val token = tokenManager.getToken().first()

            if (token == null) {
                _uiState.value = ProfileUiState.Error("Usuário não autenticado")
                return@launch
            }

            repository.getMe()
                .onSuccess { _uiState.value = ProfileUiState.Loaded(it) }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Erro inesperado") }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}
