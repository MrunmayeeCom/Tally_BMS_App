package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmstally.app.data.auth.TokenManager
import com.bmstally.app.data.repository.AppResult
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AppState(
    val isLoggedIn: Boolean = false,
    val currentTenant: Tenant? = null,
    val selectedCompany: Company? = null,
    val companies: List<Company> = emptyList(),
    val userName: String = "",
    val userEmail: String = "",
    val tabIndex: Int = 0,
    val tenantId: String = ""
)

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val tenant: Tenant) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: BmsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        if (tokenManager.isLoggedIn) {
            val tenantId = tokenManager.tenantId ?: return
            repository.setTenantId(tenantId)
            val companies = repository.getCompanies()
            _state.value = AppState(
                isLoggedIn = true,
                userName = tokenManager.userName ?: "",
                userEmail = tokenManager.userEmail ?: "",
                companies = companies,
                selectedCompany = companies.firstOrNull(),
                tenantId = tenantId,
                currentTenant = null
            )
            Timber.i("Session restored for ${tokenManager.userName}")
        }
    }

    fun setTab(index: Int) {
        _state.value = _state.value.copy(tabIndex = index)
    }

    fun selectCompany(company: Company) {
        _state.value = _state.value.copy(selectedCompany = company)
    }

    fun login(tenantCode: String, username: String, password: String) {
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = repository.login(tenantCode, username, password)) {
                is AppResult.Success -> {
                    val tenant = result.data
                    val companies = repository.getCompanies()
                    _state.value = AppState(
                        isLoggedIn = true,
                        currentTenant = tenant,
                        userName = username,
                        userEmail = "$username@${tenantCode.lowercase()}.com",
                        companies = companies,
                        selectedCompany = companies.firstOrNull(),
                        tenantId = tokenManager.tenantId ?: ""
                    )
                    _loginState.value = LoginUiState.Success(tenant)
                    Timber.i("Login successful for $username @ $tenantCode")
                }
                is AppResult.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
                    Timber.w("Login failed: ${result.message}")
                }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun logout() {
        repository.logout()
        _state.value = AppState()
        _loginState.value = LoginUiState.Idle
        Timber.i("User logged out")
    }

    val dashboardStats: List<DashboardStat>
        get() = repository.getDashboardStats()

    val outstandingSummary: String
        get() = repository.getOutstandingSummary()

    val outstandingLedgers: List<OutstandingItem>
        get() = repository.getOutstandingLedgers()

    val outstandingGroups: List<Pair<String, String>>
        get() = repository.getOutstandingGroups()

    val salesEntries: List<SalesEntry> get() = repository.getSalesEntries()
    val reports: List<ReportItem> get() = repository.getReports()
    val entryTypes: List<String> get() = repository.getEntryTypes()
    val parties: List<String> get() = repository.getParties()
}
