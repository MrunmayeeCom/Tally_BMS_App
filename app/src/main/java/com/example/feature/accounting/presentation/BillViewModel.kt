package com.example.feature.accounting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.database.LocalBill
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BillViewModel(
    private val accountingRepository: AccountingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _billsState = MutableStateFlow<UiState<List<LocalBill>>>(UiState.Idle)
    val billsState: StateFlow<UiState<List<LocalBill>>> = _billsState.asStateFlow()

    private val _billDetailState = MutableStateFlow<UiState<LocalBill>>(UiState.Idle)
    val billDetailState: StateFlow<UiState<LocalBill>> = _billDetailState.asStateFlow()

    val companyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    fun loadBills(companyId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _billsState.value = UiState.Loading
            accountingRepository.getBills(companyId, forceRefresh).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> _billsState.value = UiState.Loading
                    is Resource.Success -> _billsState.value = UiState.Success(resource.data)
                    is Resource.Error -> _billsState.value = UiState.Error(resource.message ?: "Failed to load bills list")
                }
            }
        }
    }

    fun loadBillDetail(billId: String, companyId: String) {
        viewModelScope.launch {
            _billDetailState.value = UiState.Loading
            accountingRepository.getBills(companyId, false).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val found = resource.data.find { it.billId == billId }
                        if (found != null) {
                            _billDetailState.value = UiState.Success(found)
                        } else {
                            _billDetailState.value = UiState.Error("Bill record with ID $billId not found")
                        }
                    }
                    is Resource.Error -> _billDetailState.value = UiState.Error(resource.message ?: "Failed directly")
                }
            }
        }
    }

    fun updateBillRecoveryState(billId: String, state: String, promisedDate: String?, companyId: String) {
        viewModelScope.launch {
            try {
                accountingRepository.updateRecoveryStateLocal(billId, state, promisedDate)
                loadBillDetail(billId, companyId)
                loadBills(companyId, true)
            } catch (e: Exception) {
                // Keep local state
            }
        }
    }
}
