package com.app.tempocerto.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.model.AccessRequestResponse
import com.app.tempocerto.data.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(val requests: List<AccessRequestResponse>) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            repository.getPendingRequests()
                .onSuccess {
                    _uiState.value = AdminUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = AdminUiState.Error(it.message ?: "Erro ao carregar")
                }
        }
    }

    fun approveRequest(id: Int, days: Int) {
        viewModelScope.launch {
            repository.approveRequest(id, days)
                .onSuccess {
                    loadRequests()
                }
                .onFailure {
                    _events.emit("Erro ao aprovar solicitação.")
                }
        }
    }
}