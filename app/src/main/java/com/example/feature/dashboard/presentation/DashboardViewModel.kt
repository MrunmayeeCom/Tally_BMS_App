package com.example.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.session.SessionManager
import com.example.feature.dashboard.domain.DashboardData
import com.example.feature.dashboard.domain.IDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: IDashboardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardData>> = _uiState.asStateFlow()

    val userNameFlow: StateFlow<String?> = sessionManager.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Administrator")

    private var currentCompanyGuid: String? = null

    init {
        observeCompanyAndFetch()
    }

    private fun observeCompanyAndFetch() {
        viewModelScope.launch {
            sessionManager.companyId.collectLatest { guid ->
                currentCompanyGuid = guid
                if (!guid.isNullOrEmpty()) {
                    fetchDashboardData(guid)
                }
            }
        }
    }

    fun fetchDashboardData(companyGuid: String? = null) {
        val guid = companyGuid ?: currentCompanyGuid
        if (guid.isNullOrEmpty()) {
            _uiState.value = UiState.Error("No active company selected.")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = dashboardRepository.getDashboardData(guid)
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Failed to load dashboard metrics.")
            }
        }
    }
}
