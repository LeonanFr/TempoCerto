package com.app.tempocerto.data.model

import java.time.LocalDate

data class ListScreenUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val listTitle: String = "Carregando...",
    val lastLogData: Map<String, String> = emptyMap(),
    val sourceLogs: List<Any> = emptyList(),
    val dailyLogs: List<Pair<String, String>> = emptyList(),
    val availableParameters: List<Enum<*>> = emptyList(),
    val selectedParameter: Enum<*>? = null,
    val lastAvailableDate: LocalDate? = null,
    val canNavigateForward: Boolean = false
)