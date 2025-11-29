package com.app.tempocerto.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.TokenManager
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

@HiltViewModel
class AuthCheckViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = tokenManager.getToken().first()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
            } else {
                try {
                    val result = userRepository.getMe()

                    if (result.isSuccess) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        tokenManager.clearToken()
                        _authState.value = AuthState.Unauthenticated
                    }
                } catch (e: Exception) {
                    tokenManager.clearToken()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
}
