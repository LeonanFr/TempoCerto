package com.app.tempocerto.ui.screens.graph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.CurucaParameters
import com.app.tempocerto.data.model.GraphScreenUiState
import com.app.tempocerto.data.model.SoureLog
import com.app.tempocerto.data.model.SoureParameters
import com.app.tempocerto.data.network.DataResult
import com.app.tempocerto.data.network.MonitoringRepository
import com.app.tempocerto.util.SubSystems
import com.github.mikephil.charting.data.Entry
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
class GraphViewModel @Inject constructor(
    private val repository: MonitoringRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState: StateFlow<GraphScreenUiState> = _uiState.asStateFlow()

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

    fun selectDate(date: LocalDate) {
        val lastDate = _uiState.value.lastAvailableDate
        if (date.isAfter(lastDate ?: LocalDate.now())) return
        loadDataForDate(date)
    }

    fun fetchLogsByDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = startDate,
                canNavigateForward = endDate.isBefore(LocalDate.now())
            )
        }
        viewModelScope.launch {
            if (subSystem == SubSystems.Soure) {
                repository.getSoureLogsForDateRange(startDate, endDate).collect { result ->
                    handleGraphResult(result) { data -> updateGraphDataSoure(data) }
                }
            } else {
                repository.getCurucaLogsForDateRange(startDate, endDate).collect { result ->
                    handleGraphResult(result) { data -> updateGraphDataCuruca(data) }
                }
            }
        }
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

    fun selectParameter(parameter: Enum<*>) {
        _uiState.update { it.copy(selectedParameter = parameter) }
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
        _uiState.update { it.copy(isLoading = true) }
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
                    val initialDate = lastDate ?: LocalDate.now()

                    _uiState.update {
                        it.copy(
                            selectedDate = initialDate,
                            lastAvailableDate = lastDate,
                            canNavigateForward = lastDate != null && initialDate.isBefore(lastDate)
                        )
                    }
                    loadDataForDate(initialDate)
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                    loadDataForDate(LocalDate.now())
                }
                is DataResult.Loading -> {}
                is DataResult.Restricted -> {
                    _uiState.update { it.copy(isLoading = false, isRestricted = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadDataForDate(date: LocalDate) {
        val lastDate = _uiState.value.lastAvailableDate
        _uiState.update {
            it.copy(
                selectedDate = date,
                canNavigateForward = lastDate != null && date.isBefore(lastDate)
            )
        }
        when (subSystem) {
            SubSystems.Curuca -> loadCurucaData(date)
            SubSystems.Soure -> loadSoureData(date)
        }
    }

    private fun loadCurucaData(date: LocalDate) {
        repository.getCurucaLogsForDate(date).onEach { result ->
            handleGraphResult(result) { data -> updateGraphDataCuruca(data) }
        }.launchIn(viewModelScope)
    }

    private fun loadSoureData(date: LocalDate) {
        repository.getSoureLogsForDate(date).onEach { result ->
            handleGraphResult(result) { data -> updateGraphDataSoure(data) }
        }.launchIn(viewModelScope)
    }

    private fun updateGraphDataCuruca(data: List<CurucaLog>) {
        val selectedParam = uiState.value.selectedParameter as? CurucaParameters ?: CurucaParameters.Temperature
        val entries = mapCurucaToEntries(data, selectedParam)
        _uiState.update {
            it.copy(
                chartEntries = entries,
                lastLogValue = data.lastOrNull()?.getValue(selectedParam) ?: "--",
                lastLogTime = data.lastOrNull()?.getTime() ?: ""
            )
        }
    }

    private fun updateGraphDataSoure(data: List<SoureLog>) {
        val selectedParam = uiState.value.selectedParameter as? SoureParameters ?: SoureParameters.AirTemperature
        val entries = mapSoureToEntries(data, selectedParam)
        _uiState.update {
            it.copy(
                chartEntries = entries,
                lastLogValue = data.lastOrNull()?.getValue(selectedParam) ?: "--",
                lastLogTime = data.lastOrNull()?.getTime() ?: ""
            )
        }
    }

    private fun <T> handleGraphResult(result: DataResult<List<T>>, onSuccess: (List<T>) -> Unit) {
        when (result) {
            is DataResult.Loading -> _uiState.update {
                it.copy(isLoading = true, error = null, isRestricted = false)
            }
            is DataResult.Error -> _uiState.update {
                it.copy(isLoading = false, error = result.message, isRestricted = false)
            }
            is DataResult.Success -> {
                _uiState.update { it.copy(isLoading = false, error = null, isRestricted = false) }
                onSuccess(result.data)
            }
            is DataResult.Restricted -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRestricted = true,
                    error = null,
                    chartEntries = emptyList(),
                    lastLogValue = "--",
                    lastLogTime = ""
                )
            }
        }
    }

    private fun mapCurucaToEntries(logs: List<CurucaLog>, parameter: CurucaParameters): List<Entry> {
        return logs
            .asSequence()
            .filter { it.dateTime != null && it.getRawValue(parameter) != null }
            .mapNotNull { log ->
                val value = log.getRawValue(parameter)
                if (log.dateTime != null && value != null && value > 0) {
                    val timestamp = log.dateTime.toEpochSecond().toFloat()
                    Entry(timestamp, value)
                } else null
            }
            .toList()
    }

    private fun mapSoureToEntries(logs: List<SoureLog>, parameter: SoureParameters): List<Entry> {
        return logs
            .asSequence()
            .filter { it.dateTime != null && it.getRawValue(parameter) != null }
            .mapNotNull { log ->
                val value = log.getRawValue(parameter)
                if (log.dateTime != null && value != null && value > 0) {
                    val timestamp = log.dateTime.toEpochSecond().toFloat()
                    Entry(timestamp, value)
                } else null
            }
            .toList()
    }
}
