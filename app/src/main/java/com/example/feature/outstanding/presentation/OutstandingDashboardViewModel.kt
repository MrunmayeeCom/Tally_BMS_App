package com.example.feature.outstanding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.session.SessionManager
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.outstanding.domain.OutstandingDashboardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OutstandingDashboardViewModel(
    private val repository: IOutstandingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<OutstandingDashboardData>>(UiState.Idle)
    val uiState: StateFlow<UiState<OutstandingDashboardData>> = _uiState.asStateFlow()

    // Access states for RBAC & tenants
    val userRole: StateFlow<String?> = sessionManager.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sales Executive")

    val activeCompany: StateFlow<String?> = sessionManager.companyId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = repository.getDashboardData()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to retrieve outstanding stats.")
            }
        }
    }
}
