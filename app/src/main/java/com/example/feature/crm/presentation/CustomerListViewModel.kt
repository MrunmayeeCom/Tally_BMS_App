package com.example.feature.crm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.session.SessionManager
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.ICrmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerListViewModel(
    private val crmRepository: ICrmRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CrmCustomer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CrmCustomer>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow("Name (A-Z)")
    val selectedSort = _selectedSort.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20
    private var isEndReached = false

    val companyName: StateFlow<String?> = sessionManager.companyId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Active Company")

    val tenantId: StateFlow<String?> = sessionManager.tenantId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Tenant Node")

    init {
        loadCustomers(forceRefresh = true)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        loadCustomers(forceRefresh = true)
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        loadCustomers(forceRefresh = true)
    }

    fun onSortSelected(sort: String) {
        _selectedSort.value = sort
        loadCustomers(forceRefresh = true)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadCustomers(forceRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (_uiState.value !is UiState.Success || isEndReached || _isRefreshing.value) return

        viewModelScope.launch {
            currentPage++
            try {
                val newCustomers = crmRepository.getCustomers(
                    query = _searchQuery.value,
                    filter = _selectedFilter.value,
                    sort = _selectedSort.value,
                    page = currentPage,
                    pageSize = pageSize
                )
                if (newCustomers.isEmpty()) {
                    isEndReached = true
                } else {
                    val currentList = (_uiState.value as UiState.Success).data
                    _uiState.value = UiState.Success(currentList + newCustomers)
                }
            } catch (e: Exception) {
                // Fail gracefully, keep previous entries but don't crash
                currentPage--
            }
        }
    }

    private fun loadCustomers(forceRefresh: Boolean) {
        if (forceRefresh) {
            currentPage = 1
            isEndReached = false
        }

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = UiState.Loading
            }
            try {
                val customers = crmRepository.getCustomers(
                    query = _searchQuery.value,
                    filter = _selectedFilter.value,
                    sort = _selectedSort.value,
                    page = currentPage,
                    pageSize = pageSize
                )
                if (customers.isEmpty() && currentPage == 1) {
                    _uiState.value = UiState.Success(emptyList())
                } else {
                    _uiState.value = UiState.Success(customers)
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "Failed to acquire client rosters.")
                }
            }
        }
    }
}
