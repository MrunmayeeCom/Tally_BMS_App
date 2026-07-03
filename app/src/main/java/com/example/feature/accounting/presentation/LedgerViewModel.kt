package com.example.feature.accounting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.database.LocalLedger
import com.example.core.database.LocalVoucher
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LedgerViewModel(
    private val accountingRepository: AccountingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _ledgersState = MutableStateFlow<UiState<List<LocalLedger>>>(UiState.Idle)
    val ledgersState: StateFlow<UiState<List<LocalLedger>>> = _ledgersState.asStateFlow()

    private val _ledgerDetailState = MutableStateFlow<UiState<LocalLedger>>(UiState.Idle)
    val ledgerDetailState: StateFlow<UiState<LocalLedger>> = _ledgerDetailState.asStateFlow()

    private val _ledgerVouchersState = MutableStateFlow<UiState<List<LocalVoucher>>>(UiState.Idle)
    val ledgerVouchersState: StateFlow<UiState<List<LocalVoucher>>> = _ledgerVouchersState.asStateFlow()

    private val _createLedgerState = MutableStateFlow<UiState<LocalLedger>>(UiState.Idle)
    val createLedgerState: StateFlow<UiState<LocalLedger>> = _createLedgerState.asStateFlow()

    val companyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    fun loadLedgers(companyId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _ledgersState.value = UiState.Loading
            accountingRepository.getLedgers(companyId, forceRefresh).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> _ledgersState.value = UiState.Loading
                    is Resource.Success -> _ledgersState.value = UiState.Success(resource.data)
                    is Resource.Error -> _ledgersState.value = UiState.Error(resource.message ?: "Failed to load ledgers")
                }
            }
        }
    }

    fun loadLedgerDetail(ledgerId: String, companyId: String) {
        viewModelScope.launch {
            _ledgerDetailState.value = UiState.Loading
            val ledger = accountingRepository.getLedgerDetails(ledgerId)
            if (ledger != null) {
                _ledgerDetailState.value = UiState.Success(ledger)
                loadLedgerVouchers(companyId, ledgerId)
            } else {
                _ledgerDetailState.value = UiState.Error("Ledger not found")
            }
        }
    }

    private fun loadLedgerVouchers(companyId: String, ledgerId: String) {
        viewModelScope.launch {
            _ledgerVouchersState.value = UiState.Loading
            accountingRepository.getVouchers(companyId, false).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val filtered = resource.data.filter { it.partyLedgerId == ledgerId }
                        _ledgerVouchersState.value = UiState.Success(filtered)
                    }
                    is Resource.Error -> _ledgerVouchersState.value = UiState.Error(resource.message ?: "Failed to load statement")
                }
            }
        }
    }

    fun createLedger(companyId: String, name: String, group: String, openingBalance: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _createLedgerState.value = UiState.Loading
            try {
                val bigDecimalBalance = java.math.BigDecimal(openingBalance.toString())
                val newLedger = accountingRepository.createLedger(companyId, name, group, bigDecimalBalance)
                _createLedgerState.value = UiState.Success(newLedger)
                loadLedgers(companyId, true)
                onSuccess()
            } catch (e: Exception) {
                _createLedgerState.value = UiState.Error(e.message ?: "Failed to create ledger")
            }
        }
    }
}
