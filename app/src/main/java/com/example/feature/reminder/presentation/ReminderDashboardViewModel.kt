package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderDashboardData
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderDashboardViewModel(
    private val repository: IReminderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ReminderDashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ReminderDashboardData>> = _uiState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _companyId = MutableStateFlow<String?>(null)
    val companyId: StateFlow<String?> = _companyId.asStateFlow()

    init {
        loadSessionAndDashboard()
    }

    fun loadSessionAndDashboard() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Collect session variables
                _userRole.value = sessionManager.userRole.firstOrNull() ?: "Super Admin"
                _companyId.value = sessionManager.companyId.firstOrNull() ?: "comp_01"

                val data = repository.getReminderDashboard()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load reminder statistics.")
            }
        }
    }
}
