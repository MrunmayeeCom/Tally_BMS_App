package com.example.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.example.core.common.BaseViewModel
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.feature.auth.domain.CompanyDomain
import com.example.feature.auth.domain.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CompanySwitcherViewModel(
    private val authRepository: IAuthRepository
) : BaseViewModel<List<CompanyDomain>>() {

    val activeCompanyId: StateFlow<String?> = authRepository.activeCompanyId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeTenantId: StateFlow<String?> = authRepository.activeTenantId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var _activeCompanyGuid = MutableStateFlow<String?>(null)
    val activeCompanyGuid: StateFlow<String?> = _activeCompanyGuid.asStateFlow()

    init {
        fetchCompanies()
        fetchActiveCompany()
    }

    private fun fetchActiveCompany() {
        viewModelScope.launch {
            authRepository.getActiveCompany().collectLatest { resource ->
                if (resource is Resource.Success) {
                    _activeCompanyGuid.value = resource.data?.companyGuid
                }
            }
        }
    }

    fun fetchCompanies() {
        launchSafe {
            authRepository.getCompanies().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateState(UiState.Loading)
                    }
                    is Resource.Success -> {
                        updateState(UiState.Success(resource.data ?: emptyList()))
                    }
                    is Resource.Error -> {
                        updateState(UiState.Error(resource.message ?: "Failed to fetch companies."))
                    }
                }
            }
        }
    }

    fun switchCompany(companyGuid: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.setActiveCompany(companyGuid)
            _activeCompanyGuid.value = companyGuid
            onComplete()
        }
    }
}
