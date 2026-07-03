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

class VoucherViewModel(
    private val accountingRepository: AccountingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _vouchersState = MutableStateFlow<UiState<List<LocalVoucher>>>(UiState.Idle)
    val vouchersState: StateFlow<UiState<List<LocalVoucher>>> = _vouchersState.asStateFlow()

    private val _voucherDetailState = MutableStateFlow<UiState<LocalVoucher>>(UiState.Idle)
    val voucherDetailState: StateFlow<UiState<LocalVoucher>> = _voucherDetailState.asStateFlow()

    private val _createState = MutableStateFlow<UiState<LocalVoucher>>(UiState.Idle)
    val createState: StateFlow<UiState<LocalVoucher>> = _createState.asStateFlow()

    val companyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    fun loadVouchers(companyId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _vouchersState.value = UiState.Loading
            accountingRepository.getVouchers(companyId, forceRefresh).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> _vouchersState.value = UiState.Loading
                    is Resource.Success -> _vouchersState.value = UiState.Success(resource.data)
                    is Resource.Error -> _vouchersState.value = UiState.Error(resource.message ?: "Failed to load vouchers")
                }
            }
        }
    }

    fun loadVoucherDetail(voucherId: String, companyId: String) {
        viewModelScope.launch {
            _voucherDetailState.value = UiState.Loading
            accountingRepository.getVouchers(companyId, false).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val found = resource.data.find { it.voucherId == voucherId }
                        if (found != null) {
                            _voucherDetailState.value = UiState.Success(found)
                        } else {
                            _voucherDetailState.value = UiState.Error("Voucher statement with ID $voucherId not found")
                        }
                    }
                    is Resource.Error -> _voucherDetailState.value = UiState.Error(resource.message ?: "Failed to find voucher")
                }
            }
        }
    }

    fun createVoucher(
        companyId: String,
        type: String,
        partyId: String,
        partyName: String,
        amount: Double,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            try {
                val bigDecimalAmount = java.math.BigDecimal(amount.toString())
                val voucher = accountingRepository.createLocalVoucher(companyId, type, partyId, partyName, bigDecimalAmount, date)
                _createState.value = UiState.Success(voucher)
                loadVouchers(companyId, true)
                onSuccess()
            } catch (e: Exception) {
                _createState.value = UiState.Error(e.message ?: "Failed to create voucher transaction")
            }
        }
    }
}
