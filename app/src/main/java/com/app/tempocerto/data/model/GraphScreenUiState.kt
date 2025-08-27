package com.app.tempocerto.data.model

import com.github.mikephil.charting.data.Entry
import java.time.LocalDate

data class GraphScreenUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val chartEntries: List<Entry> = emptyList(),
    val availableParameters: List<Enum<*>> = emptyList(),
    val selectedParameter: Enum<*>? = null,
    val lastLogValue: String = "--",
    val lastLogTime: String = "",
    val lastAvailableDate: LocalDate? = null,
    val canNavigateForward: Boolean = false
)