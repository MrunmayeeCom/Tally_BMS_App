package com.example.feature.outstanding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.outstanding.domain.OutstandingDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutstandingDetailViewModel(
    private val repository: IOutstandingRepository,
    private val partyId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<OutstandingDetail>>(UiState.Idle)
    val uiState: StateFlow<UiState<OutstandingDetail>> = _uiState.asStateFlow()

    private val _isActionSubmitting = MutableStateFlow(false)
    val isActionSubmitting: StateFlow<Boolean> = _isActionSubmitting.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val detail = repository.getOutstandingDetail(partyId)
                _uiState.value = UiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to retrieve account detail log.")
            }
        }
    }

    fun promoteRecovery(status: String, nextAction: String?, assignedRep: String?) {
        viewModelScope.launch {
            _isActionSubmitting.value = true
            try {
                val success = repository.updateRecoveryStatus(partyId, status, nextAction, assignedRep)
                if (success) {
                    // Force refresh to grab updated recovery status state
                    val detail = repository.getOutstandingDetail(partyId)
                    _uiState.value = UiState.Success(detail)
                }
            } catch (e: Exception) {
                // Keep current state but log error
            } finally {
                _isActionSubmitting.value = false
            }
        }
    }
}
