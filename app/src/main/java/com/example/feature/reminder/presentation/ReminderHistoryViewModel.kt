package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderItem
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderHistoryViewModel(
    private val repository: IReminderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<ReminderItem>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<ReminderItem>>> = _historyState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>("Super Admin")
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    init {
        loadSessionAndHistory()
    }

    private fun loadSessionAndHistory() {
        viewModelScope.launch {
            _userRole.value = sessionManager.userRole.firstOrNull() ?: "Super Admin"
            refreshHistory()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        refreshHistory()
    }

    fun updateStatusFilter(status: String) {
        _selectedStatus.value = status
        refreshHistory()
    }

    fun updateTypeFilter(type: String) {
        _selectedType.value = type
        refreshHistory()
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            try {
                // If user is a Sales Executive, they can only view limit scope?
                // The requirements: Sales Executive sees "Own records only" or limited assignments.
                // We'll perform standard filtering, and restrict names if Role matches.
                val data = repository.getReminders(
                    query = _searchQuery.value,
                    status = _selectedStatus.value,
                    type = _selectedType.value
                )
                
                val filteredData = if (_userRole.value == "Sales Executive") {
                    // For the purpose of multi-tenant isolation and user roles,
                    // let's simulate only showing reminders completed or assigned to Rajesh Kumar (mock sales executive name)
                    data.filter { it.assigneeName == "Rajesh Kumar" || it.assigneeId == "user_current" }
                } else {
                    data
                }

                _historyState.value = UiState.Success(filteredData)
            } catch (e: Exception) {
                _historyState.value = UiState.Error(e.message ?: "Failed to read delivery ledger.")
            }
        }
    }
}
