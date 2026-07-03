package com.example.feature.outstanding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.session.SessionManager
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.outstanding.domain.OutstandingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OutstandingListViewModel(
    private val repository: IOutstandingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<OutstandingItem>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<OutstandingItem>>> = _uiState.asStateFlow()

    // Query states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("Receivable") // "Receivable" or "Payable"
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _selectedGroup = MutableStateFlow("All") // "All", "Sundry Debtors", "Sundry Creditors"
    val selectedGroup: StateFlow<String> = _selectedGroup.asStateFlow()

    private val _selectedSort = MutableStateFlow("overdue desc") // "outstanding desc", "outstanding asc", "overdue desc", "party name"
    val selectedSort: StateFlow<String> = _selectedSort.asStateFlow()

    private val _page = MutableStateFlow(1)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val userRole: StateFlow<String?> = sessionManager.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sales Executive")

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _uiState.value = UiState.Loading
            }
            try {
                val data = repository.getOutstandingItems(
                    query = _searchQuery.value,
                    type = _selectedType.value,
                    group = _selectedGroup.value,
                    sort = _selectedSort.value,
                    page = _page.value,
                    pageSize = 20
                )
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to retrieve outstanding lists.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _page.value = 1
        loadItems()
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
        _selectedGroup.value = "All" // Reset group on type switch
        _page.value = 1
        loadItems()
    }

    fun updateSelectedGroup(group: String) {
        _selectedGroup.value = group
        _page.value = 1
        loadItems()
    }

    fun updateSelectedSort(sort: String) {
        _selectedSort.value = sort
        _page.value = 1
        loadItems()
    }

    fun incrementPage() {
        _page.value += 1
        loadItems()
    }

    fun refresh() {
        _isRefreshing.value = true
        _page.value = 1
        loadItems()
    }
}
