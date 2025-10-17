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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val repository: MonitoringRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState: StateFlow<GraphScreenUiState> = _uiState.asStateFlow()

    val subSystem: SubSystems

    init {
        val systemName: String = checkNotNull(savedStateHandle["subSystem"])
        val parameterName: String = checkNotNull(savedStateHandle["parameter"])
        subSystem = enumValueOf(systemName)

        setupInitialState(parameterName)
        loadInitialData()
    }

    fun selectDate(date: LocalDate) {
        val lastDate = _uiState.value.lastAvailableDate
        if (date.isAfter(lastDate ?: LocalDate.now())) return
        loadDataForDate(date)
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
            when (result) {
                is DataResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is DataResult.Success -> {
                    val selectedParam = uiState.value.selectedParameter as? CurucaParameters ?: CurucaParameters.Temperature
                    val entries = mapCurucaToEntries(result.data, selectedParam)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chartEntries = entries,
                            lastLogValue = result.data.lastOrNull()?.getValue(selectedParam) ?: "--",
                            lastLogTime = result.data.lastOrNull()?.getTime() ?: ""
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadSoureData(date: LocalDate) {
        repository.getSoureLogsForDate(date).onEach { result ->
            when (result) {
                is DataResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is DataResult.Success -> {
                    val selectedParam = uiState.value.selectedParameter as? SoureParameters ?: SoureParameters.AirTemperature
                    val entries = mapSoureToEntries(result.data, selectedParam)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chartEntries = entries,
                            lastLogValue = result.data.lastOrNull()?.getValue(selectedParam) ?: "--",
                            lastLogTime = result.data.lastOrNull()?.getTime() ?: ""
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
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
                } else {
                    null
                }
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
                } else {
                    null
                }
            }
            .toList()
    }
}