package com.app.tempocerto.ui.screens.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.model.SoureLog
import com.app.tempocerto.data.model.SoureParameters
import com.app.tempocerto.data.model.ListScreenUiState
import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.CurucaParameters
import com.app.tempocerto.data.network.DataResult
import com.app.tempocerto.data.network.MonitoringRepository
import com.app.tempocerto.util.SubSystems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: MonitoringRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListScreenUiState())
    val uiState: StateFlow<ListScreenUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val subSystem: SubSystems


    init {
        val systemName: String = checkNotNull(savedStateHandle["subSystem"])
        val parameterName: String = checkNotNull(savedStateHandle["parameter"])
        subSystem = SubSystems.valueOf(systemName)

        setupInitialState(parameterName)
        loadInitialData()
    }

    fun requestAccessToData(days: Int) {
        viewModelScope.launch {
            val result = repository.requestAccess(days)
            if (result.isSuccess) {
                _events.emit("Solicitação enviada com sucesso!")
            } else {
                _events.emit("Erro ao enviar solicitação")
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val lastDate = _uiState.value.lastAvailableDate
        if (date.isAfter(lastDate ?: LocalDate.now())) return
        loadDataForDate(date)
    }

    fun fetchLogsByDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            if (subSystem == SubSystems.Soure) {
                repository.getSoureLogsForDateRange(startDate, endDate).collect { result ->
                    handleListResult(result)
                }
            } else {
                repository.getCurucaLogsForDateRange(startDate, endDate).collect { result ->
                    handleListResult(result)
                }
            }
        }
    }

    fun selectParameter(parameter: Enum<*>) {
        _uiState.update { it.copy(selectedParameter = parameter) }
        mapSourceLogsToDailyLogs()
    }

    fun refresh() {
        loadLastLog()
        loadDataForDate(uiState.value.selectedDate)
    }

    private fun setupInitialState(parameterName: String) {
        val (params, initialParam) = when (subSystem) {
            SubSystems.Curuca -> CurucaParameters.entries.toList() to CurucaParameters.valueOf(parameterName)
            SubSystems.Soure -> SoureParameters.entries.toList() to SoureParameters.valueOf(parameterName)
        }
        _uiState.update {
            it.copy(
                availableParameters = params,
                selectedParameter = initialParam
            )
        }
    }

    private fun loadInitialData() {
        loadLastLog()

        val repositoryCall = when (subSystem) {
            SubSystems.Curuca -> repository.getLastCurucaLog()
            SubSystems.Soure -> repository.getLastSoureLog()
        }

        repositoryCall.onEach { result ->
            when (result) {
                is DataResult.Success -> {
                    val lastDate = when (val lastLog = result.data) {
                        is CurucaLog -> lastLog.getLocalDate()
                        is SoureLog -> lastLog.getLocalDate()
                        else -> null
                    }
                    loadDataForDate(lastDate ?: LocalDate.now())
                }
                is DataResult.Error -> {
                    loadDataForDate(LocalDate.now())
                }
                is DataResult.Restricted -> {
                    _uiState.update { it.copy(isRestricted = true) }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun loadDataForDate(date: LocalDate) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = date,
                canNavigateForward = currentState.lastAvailableDate != null && date.isBefore(currentState.lastAvailableDate)
            )
        }

        when (subSystem) {
            SubSystems.Curuca -> loadCurucaData(date)
            SubSystems.Soure -> loadSoureData(date)
        }
    }

    private fun loadCurucaData(date: LocalDate) {
        repository.getCurucaLogsForDate(date).onEach { result ->
            handleListResult(result)
        }.launchIn(viewModelScope)
    }

    private fun loadSoureData(date: LocalDate) {
        repository.getSoureLogsForDate(date).onEach { result ->
            handleListResult(result)
        }.launchIn(viewModelScope)
    }

    private fun <T> handleListResult(result: DataResult<List<T>>) {
        when (result) {
            is DataResult.Loading -> _uiState.update {
                it.copy(isLoading = true, sourceLogs = emptyList(), isRestricted = false)
            }
            is DataResult.Error -> _uiState.update {
                it.copy(isLoading = false, error = result.message, isRestricted = false)
            }
            is DataResult.Success -> {
                _uiState.update {
                    it.copy(isLoading = false, sourceLogs = result.data, isRestricted = false)
                }
                mapSourceLogsToDailyLogs()
            }
            is DataResult.Restricted -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRestricted = true,
                    sourceLogs = emptyList(),
                    dailyLogs = emptyList()
                )
            }
        }
    }

    private fun mapSourceLogsToDailyLogs() {
        val selectedParam = _uiState.value.selectedParameter ?: return
        val sourceLogs = _uiState.value.sourceLogs

        if (sourceLogs.isEmpty()) {
            _uiState.update { it.copy(dailyLogs = emptyList()) }
            return
        }

        val mappedLogs = when (subSystem) {
            SubSystems.Curuca -> {
                val param = selectedParam as CurucaParameters
                _uiState.update { it.copy(listTitle = param.toListString()) }
                val logs = sourceLogs.mapNotNull { it as? CurucaLog }
                logs
                    .filter { log -> (log.getRawValue(param) ?: -10f) != -10f }
                    .map { log ->
                        Pair(log.getValue(param), log.getTime())
                    }
            }
            SubSystems.Soure -> {
                val param = selectedParam as SoureParameters
                _uiState.update { it.copy(listTitle = param.toListString()) }
                val logs = sourceLogs.mapNotNull { it as? SoureLog }
                logs
                    .filter { log -> (log.getRawValue(param) ?: -10f) != -10f }
                    .map { log ->
                        Pair(log.getValue(param), log.getTime())
                    }
            }
        }
        _uiState.update { it.copy(dailyLogs = mappedLogs) }
    }

    private fun loadLastLog() {
        val repositoryCall = when (subSystem) {
            SubSystems.Curuca -> repository.getLastCurucaLog()
            SubSystems.Soure -> repository.getLastSoureLog()
        }

        repositoryCall.onEach { result ->
            when (result) {
                is DataResult.Success -> {
                    val logMap: Map<String, String>
                    val lastDate: LocalDate?

                    when (val lastLog = result.data) {
                        is CurucaLog -> {
                            logMap = lastLog.toMap()
                            lastDate = lastLog.getLocalDate()
                        }
                        is SoureLog -> {
                            logMap = lastLog.toMap()
                            lastDate = lastLog.getLocalDate()
                        }
                        else -> {
                            logMap = mapOf("Status" to "Nenhum dado atual.")
                            lastDate = null
                        }
                    }

                    _uiState.update {
                        it.copy(
                            lastLogData = logMap,
                            lastAvailableDate = lastDate,
                            canNavigateForward = lastDate != null && it.selectedDate.isBefore(lastDate)
                        )
                    }
                }
                is DataResult.Error -> {
                    val errorMap = mapOf("Status" to "Falha ao carregar.")
                    _uiState.update { it.copy(lastLogData = errorMap) }
                }
                is DataResult.Loading -> {  }
                is DataResult.Restricted -> {
                    _uiState.update { it.copy(isRestricted = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}
