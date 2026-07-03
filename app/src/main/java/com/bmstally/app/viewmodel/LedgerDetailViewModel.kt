package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LedgerDetailState(
    val ledger: Ledger? = null,
    val vouchers: List<Voucher> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val bills: List<Bill> = emptyList(),
    val ageing: List<Ageing> = emptyList(),
    val activeTab: Int = 0,
    val loading: Boolean = true
) {
    val totalDebit: Double get() = vouchers.sumOf { it.debit }
    val totalCredit: Double get() = vouchers.sumOf { it.credit }
}

@HiltViewModel
class LedgerDetailViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LedgerDetailState())
    val state: StateFlow<LedgerDetailState> = _state.asStateFlow()

    fun loadLedgerDetail(ledgerGuid: String) {
        _state.value = _state.value.copy(loading = true)
        _state.value = _state.value.copy(
            ledger = repository.getLedgerDetail(ledgerGuid),
            vouchers = repository.getLedgerVouchers(ledgerGuid),
            invoices = repository.getLedgerInvoices(ledgerGuid),
            bills = repository.getLedgerBills(ledgerGuid),
            ageing = repository.getLedgerAgeing(ledgerGuid),
            loading = false
        )
    }

    fun setActiveTab(tab: Int) {
        _state.value = _state.value.copy(activeTab = tab)
    }
}
