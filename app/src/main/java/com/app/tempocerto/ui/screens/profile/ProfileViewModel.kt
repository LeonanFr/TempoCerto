package com.app.tempocerto.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.model.UserProfile
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Loaded(val user: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object Saved : ProfileUiState()
}

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    open val uiState: StateFlow<ProfileUiState> = _uiState

    private var authToken: String? = null

    fun setToken(token: String) {
        authToken = token
    }

    fun loadProfile() {
        val token = authToken ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            repository.getProfile(token)
                .onSuccess { _uiState.value = ProfileUiState.Loaded(it) }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Erro inesperado") }
        }
    }

    fun saveProfile(profile: UserProfile) {
        val token = authToken ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            repository.saveProfile(token, profile)
                .onSuccess { _uiState.value = ProfileUiState.Saved }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Erro inesperado") }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}
