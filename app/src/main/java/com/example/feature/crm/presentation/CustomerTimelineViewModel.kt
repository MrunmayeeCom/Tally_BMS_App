package com.example.feature.crm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.crm.domain.CrmTimelineEvent
import com.example.feature.crm.domain.ICrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerTimelineViewModel(
    private val crmRepository: ICrmRepository,
    private val customerId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CrmTimelineEvent>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CrmTimelineEvent>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadTimeline()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadTimeline()
            _isRefreshing.value = false
        }
    }

    fun loadTimeline() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val timeline = crmRepository.getCustomerTimeline(customerId)
                _uiState.value = UiState.Success(timeline)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Could not retrieve activity logs.")
            }
        }
    }
}
