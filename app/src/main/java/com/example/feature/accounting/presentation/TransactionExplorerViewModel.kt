package com.example.feature.accounting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.database.LocalVoucher
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionExplorerViewModel(
    private val accountingRepository: AccountingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<UiState<List<LocalVoucher>>>(UiState.Idle)
    val transactionsState: StateFlow<UiState<List<LocalVoucher>>> = _transactionsState.asStateFlow()

    // Query constraint states
    val searchQuery = MutableStateFlow("")
    val startDate = MutableStateFlow("")
    val endDate = MutableStateFlow("")
    val selectedType = MutableStateFlow("All")
    val selectedLedgerId = MutableStateFlow<String?>(null)
    val currentPage = MutableStateFlow(1)
    val totalPages = MutableStateFlow(1)

    val companyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    fun runQuery(companyId: String) {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            accountingRepository.queryTransactions(
                companyId = companyId,
                searchQuery = searchQuery.value,
                startDate = startDate.value,
                endDate = endDate.value,
                voucherType = selectedType.value,
                ledgerId = selectedLedgerId.value,
                page = currentPage.value,
                pageSize = 20
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> _transactionsState.value = UiState.Loading
                    is Resource.Success -> {
                        _transactionsState.value = UiState.Success(resource.data)
                        // Simple page calculation from full set
                        totalPages.value = maxOf(1, (resource.data.size + 19) / 20)
                    }
                    is Resource.Error -> _transactionsState.value = UiState.Error(resource.message ?: "Failed to query transactions")
                }
            }
        }
    }

    fun nextPage(companyId: String) {
        if (currentPage.value < totalPages.value) {
            currentPage.value += 1
            runQuery(companyId)
        }
    }

    fun prevPage(companyId: String) {
        if (currentPage.value > 1) {
            currentPage.value -= 1
            runQuery(companyId)
        }
    }

    fun resetFilters(companyId: String) {
        searchQuery.value = ""
        startDate.value = ""
        endDate.value = ""
        selectedType.value = "All"
        selectedLedgerId.value = null
        currentPage.value = 1
        runQuery(companyId)
    }
}
