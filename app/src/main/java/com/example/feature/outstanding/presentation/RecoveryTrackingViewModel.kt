package com.example.feature.outstanding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.outstanding.domain.OutstandingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecoveryTrackingViewModel(
    private val repository: IOutstandingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<OutstandingItem>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<OutstandingItem>>> = _uiState.asStateFlow()

    init {
        loadPipeline()
    }

    fun loadPipeline() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val pipeline = repository.getRecoveryPipeline()
                _uiState.value = UiState.Success(pipeline)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to pull pipeline state.")
            }
        }
    }

    fun dragOrUpdateStatus(partyId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                // Inline update
                val success = repository.updateRecoveryStatus(
                    partyId = partyId,
                    status = newStatus,
                    nextActionDate = null,
                    executive = null
                )
                if (success) {
                    // Refresh pipeline list
                    val pipeline = repository.getRecoveryPipeline()
                    _uiState.value = UiState.Success(pipeline)
                }
            } catch (e: Exception) {
                // error
            }
        }
    }
}
