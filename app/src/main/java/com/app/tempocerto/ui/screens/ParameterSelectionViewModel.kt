package com.app.tempocerto.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.app.tempocerto.data.model.CurucaParameters
import com.app.tempocerto.data.model.SoureParameters
import com.app.tempocerto.util.SubSystems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ParameterSelectionUiState(
    val stationName: String = "",
    val parameters: List<Enum<*>> = emptyList()
)

@HiltViewModel
class ParameterSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParameterSelectionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val subSystemName: String = checkNotNull(savedStateHandle["subSystem"])
        val subSystem = enumValueOf<SubSystems>(subSystemName)

        val parameters = when (subSystem) {
            SubSystems.Curuca -> CurucaParameters.entries
            SubSystems.Soure -> SoureParameters.entries
        }

        _uiState.value = ParameterSelectionUiState(
            stationName = subSystem.name,
            parameters = parameters
        )
    }
}
