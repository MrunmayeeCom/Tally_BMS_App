package com.example.feature.outstanding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.AgeingReportData
import com.example.feature.outstanding.domain.IOutstandingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgingAnalysisViewModel(
    private val repository: IOutstandingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AgeingReportData>>(UiState.Idle)
    val uiState: StateFlow<UiState<AgeingReportData>> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow("Receivable") // "Receivable" or "Payable"
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val report = repository.getAgeingReport(_selectedType.value)
                _uiState.value = UiState.Success(report)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to generate dynamic ageing reports.")
            }
        }
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
        loadReport()
    }
}
