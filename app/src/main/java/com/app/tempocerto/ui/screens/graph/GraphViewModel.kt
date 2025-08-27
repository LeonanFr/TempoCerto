package com.app.tempocerto.ui.screens.graph

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tempocerto.data.model.SoureLog
import com.app.tempocerto.data.model.SoureParameters
import com.app.tempocerto.data.model.GraphScreenUiState
import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.CurucaParameters
import com.app.tempocerto.data.repository.DataResult
import com.app.tempocerto.data.repository.MonitoringRepository
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
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
                    val lastLog = result.data
                    val lastDate = when (lastLog) {
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

                    when (subSystem) {
                        SubSystems.Curuca -> loadCurucaData(initialDate)
                        SubSystems.Soure -> loadSoureData(initialDate)
                    }
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
            .filter { log -> log.getRawValue(parameter) != -10f }
            .mapNotNull { log ->
                try {
                    val date = log.getLocalDate()
                    val time = log.getTime()
                    if (date != null && time.isNotBlank()) {
                        val localTime = java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"))
                        val dateTime = date.atTime(localTime)
                        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC).toFloat()
                        val value = log.getRawValue(parameter)
                        Entry(timestamp, value)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("GraphViewModel", "Falha ao mapear entrada Curuca para o gráfico: ${log.date}", e)
                    null
                }
            }
    }

    private fun mapSoureToEntries(logs: List<SoureLog>, parameter: SoureParameters): List<Entry> {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
        return logs
            .filter { log -> log.getRawValue(parameter) != -10f }
            .mapNotNull { log ->
                try {
                    val dateTime = LocalDateTime.parse(log.dt, formatter)
                    val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC).toFloat()
                    val value = log.getRawValue(parameter)
                    Entry(timestamp, value)
                } catch (e: Exception) {
                    Log.e("GraphViewModel", "Falha ao converter data de Soure: '${log.dt}'", e)
                    null
                }
            }
    }
}
