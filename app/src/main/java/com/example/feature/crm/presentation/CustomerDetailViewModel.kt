package com.example.feature.crm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.crm.domain.CrmCustomerDetail
import com.example.feature.crm.domain.ICrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerDetailViewModel(
    private val crmRepository: ICrmRepository,
    private val customerId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<CrmCustomerDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<CrmCustomerDetail>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadCustomerDetail()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadCustomerDetail()
            _isRefreshing.value = false
        }
    }

    fun loadCustomerDetail() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val detail = crmRepository.getCustomerDetail(customerId)
                _uiState.value = UiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Could not fetch client 360 details.")
            }
        }
    }
}
