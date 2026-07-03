package com.example.feature.accounting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import com.example.feature.accounting.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinancialSummaryViewModel(
    private val accountingRepository: AccountingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _trialBalanceState = MutableStateFlow<UiState<TrialBalanceSummary>>(UiState.Idle)
    val trialBalanceState: StateFlow<UiState<TrialBalanceSummary>> = _trialBalanceState.asStateFlow()

    private val _profitLossState = MutableStateFlow<UiState<ProfitLossSummary>>(UiState.Idle)
    val profitLossState: StateFlow<UiState<ProfitLossSummary>> = _profitLossState.asStateFlow()

    private val _balanceSheetState = MutableStateFlow<UiState<BalanceSheetSummary>>(UiState.Idle)
    val balanceSheetState: StateFlow<UiState<BalanceSheetSummary>> = _balanceSheetState.asStateFlow()

    private val _cashFlowState = MutableStateFlow<UiState<CashFlowSnapshot>>(UiState.Idle)
    val cashFlowState: StateFlow<UiState<CashFlowSnapshot>> = _cashFlowState.asStateFlow()

    val companyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    fun loadAllFinancials(companyId: String) {
        viewModelScope.launch {
            loadTrialBalance(companyId)
            loadProfitLoss(companyId)
            loadBalanceSheet(companyId)
            loadCashFlow(companyId)
        }
    }

    suspend fun loadTrialBalance(companyId: String) {
        _trialBalanceState.value = UiState.Loading
        try {
            val res = accountingRepository.getTrialBalance(companyId)
            _trialBalanceState.value = UiState.Success(res)
        } catch (e: Exception) {
            _trialBalanceState.value = UiState.Error(e.message ?: "Failed to calculate Trial Balance")
        }
    }

    suspend fun loadProfitLoss(companyId: String) {
        _profitLossState.value = UiState.Loading
        try {
            val res = accountingRepository.getProfitLossSummary(companyId)
            _profitLossState.value = UiState.Success(res)
        } catch (e: Exception) {
            _profitLossState.value = UiState.Error(e.message ?: "Failed to calculate P&L summary")
        }
    }

    suspend fun loadBalanceSheet(companyId: String) {
        _balanceSheetState.value = UiState.Loading
        try {
            val res = accountingRepository.getBalanceSheetSummary(companyId)
            _balanceSheetState.value = UiState.Success(res)
        } catch (e: Exception) {
            _balanceSheetState.value = UiState.Error(e.message ?: "Failed to calculated Balance Sheet")
        }
    }

    suspend fun loadCashFlow(companyId: String) {
        _cashFlowState.value = UiState.Loading
        try {
            val res = accountingRepository.getCashFlowSnapshot(companyId)
            _cashFlowState.value = UiState.Success(res)
        } catch (e: Exception) {
            _cashFlowState.value = UiState.Error(e.message ?: "Failed to calculate Cash Flow")
        }
    }
}
