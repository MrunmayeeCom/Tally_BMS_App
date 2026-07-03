package com.example.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.reports.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ==========================================
// UI STATE REPRESENTATIONS
// ==========================================
sealed interface ExecutiveUiState {
    object Loading : ExecutiveUiState
    data class Success(val report: ExecutiveDashboardReport) : ExecutiveUiState
    data class Error(val message: String) : ExecutiveUiState
}

sealed interface OutstandingUiState {
    object Loading : OutstandingUiState
    data class Success(val report: OutstandingReport) : OutstandingUiState
    data class Error(val message: String) : OutstandingUiState
}

sealed interface ReminderUiState {
    object Loading : ReminderUiState
    data class Success(val report: ReminderEffectivenessReport) : ReminderUiState
    data class Error(val message: String) : ReminderUiState
}

sealed interface FollowUpUiState {
    object Loading : FollowUpUiState
    data class Success(val report: FollowUpReport) : FollowUpUiState
    data class Error(val message: String) : FollowUpUiState
}

sealed interface SalesTeamUiState {
    object Loading : SalesTeamUiState
    data class Success(val report: SalesTeamReport) : SalesTeamUiState
    data class Error(val message: String) : SalesTeamUiState
}

sealed interface CustomReportUiState {
    object Idle : CustomReportUiState
    object Loading : CustomReportUiState
    data class Success(val spec: CustomReportSpec) : CustomReportUiState
    data class Error(val message: String) : CustomReportUiState
}

sealed interface ExportHistoryUiState {
    object Loading : ExportHistoryUiState
    data class Success(val exports: List<ExportRecord>) : ExportHistoryUiState
    data class Error(val message: String) : ExportHistoryUiState
}

// ==========================================
// VIEWMODEL IMPLEMENTATION
// ==========================================
class ReportsViewModel(
    private val repository: IReportsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Global session information for multi-tenant isolation
    val companyId: StateFlow<String> = sessionManager.companyId
        .map { it ?: "default_company_id" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default_company_id")

    val userRole: StateFlow<String> = sessionManager.userRole
        .map { it ?: "Super Admin" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Super Admin")

    val userName: StateFlow<String> = sessionManager.userName
        .map { it ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    // Dynamic Filter states
    private val _startDate = MutableStateFlow<String>("2026-06-01")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<String>("2026-06-30")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    private val _selectedUserId = MutableStateFlow<String?>(null)
    val selectedUserId: StateFlow<String?> = _selectedUserId.asStateFlow()

    private val _selectedTerritory = MutableStateFlow<String?>(null)
    val selectedTerritory: StateFlow<String?> = _selectedTerritory.asStateFlow()

    // Screen Sub-states
    private val _executiveState = MutableStateFlow<ExecutiveUiState>(ExecutiveUiState.Loading)
    val executiveState: StateFlow<ExecutiveUiState> = _executiveState.asStateFlow()

    private val _outstandingState = MutableStateFlow<OutstandingUiState>(OutstandingUiState.Loading)
    val outstandingState: StateFlow<OutstandingUiState> = _outstandingState.asStateFlow()

    private val _reminderState = MutableStateFlow<ReminderUiState>(ReminderUiState.Loading)
    val reminderState: StateFlow<ReminderUiState> = _reminderState.asStateFlow()

    private val _followUpState = MutableStateFlow<FollowUpUiState>(FollowUpUiState.Loading)
    val followUpState: StateFlow<FollowUpUiState> = _followUpState.asStateFlow()

    private val _salesTeamState = MutableStateFlow<SalesTeamUiState>(SalesTeamUiState.Loading)
    val salesTeamState: StateFlow<SalesTeamUiState> = _salesTeamState.asStateFlow()

    private val _customReportState = MutableStateFlow<CustomReportUiState>(CustomReportUiState.Idle)
    val customReportState: StateFlow<CustomReportUiState> = _customReportState.asStateFlow()

    private val _exportHistoryState = MutableStateFlow<ExportHistoryUiState>(ExportHistoryUiState.Loading)
    val exportHistoryState: StateFlow<ExportHistoryUiState> = _exportHistoryState.asStateFlow()

    init {
        // Observe company changes to reload reports dynamically
        viewModelScope.launch {
            combine(
                combine(companyId, startDate, endDate) { cid, s, e -> Triple(cid, s, e) },
                combine(selectedCustomerId, selectedUserId, selectedTerritory) { cust, usr, terr -> Triple(cust, usr, terr) }
            ) { first, second ->
                SessionFilters(
                    companyId = first.first,
                    startDate = first.second,
                    endDate = first.third,
                    customerId = second.first,
                    userId = second.second,
                    territory = second.third
                )
            }.collectLatest { filters ->
                loadAllReports(filters)
            }
        }
    }

    private fun loadAllReports(filters: SessionFilters) {
        loadExecutiveReport(filters.companyId, filters.startDate, filters.endDate)
        loadOutstandingReport(filters.companyId, filters.customerId, filters.territory)
        loadReminderAnalytics(filters.companyId, filters.startDate, filters.endDate)
        loadFollowUpReport(filters.companyId, filters.userId, filters.startDate, filters.endDate)
        loadSalesTeamReport(filters.companyId, filters.territory, filters.userId, filters.startDate, filters.endDate)
        loadExportHistory(filters.companyId)
    }

    // Role-based verification check helper
    fun isAuthorizedFor(allowedRoles: List<String>): Boolean {
        return allowedRoles.contains(userRole.value)
    }

    // ==========================================
    // ACTION TRIGGERS
    // ==========================================

    fun setDateRange(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
    }

    fun setCustomerFilter(customerId: String?) {
        _selectedCustomerId.value = customerId
    }

    fun setUserFilter(userId: String?) {
        _selectedUserId.value = userId
    }

    fun setTerritoryFilter(territory: String?) {
        _selectedTerritory.value = territory
    }

    fun loadExecutiveReport(companyId: String, start: String?, end: String?) {
        viewModelScope.launch {
            _executiveState.value = ExecutiveUiState.Loading
            repository.getExecutiveDashboardReport(companyId, start, end)
                .catch { e -> _executiveState.value = ExecutiveUiState.Error(e.message ?: "Unknown Error occurred") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _executiveState.value = ExecutiveUiState.Success(it) },
                        onFailure = { _executiveState.value = ExecutiveUiState.Error(it.message ?: "Failed to fetch executive summaries") }
                    )
                }
        }
    }

    fun loadOutstandingReport(companyId: String, customerId: String?, territory: String?) {
        viewModelScope.launch {
            _outstandingState.value = OutstandingUiState.Loading
            repository.getOutstandingReport(companyId, customerId, territory)
                .catch { e -> _outstandingState.value = OutstandingUiState.Error(e.message ?: "Unknown Error occurred") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _outstandingState.value = OutstandingUiState.Success(it) },
                        onFailure = { _outstandingState.value = OutstandingUiState.Error(it.message ?: "Failed to load receivables overview") }
                    )
                }
        }
    }

    fun loadReminderAnalytics(companyId: String, start: String?, end: String?) {
        viewModelScope.launch {
            _reminderState.value = ReminderUiState.Loading
            repository.getReminderEffectivenessReport(companyId, start, end)
                .catch { e -> _reminderState.value = ReminderUiState.Error(e.message ?: "Unknown Error occurred") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _reminderState.value = ReminderUiState.Success(it) },
                        onFailure = { _reminderState.value = ReminderUiState.Error(it.message ?: "Failed loading notification effectiveness") }
                    )
                }
        }
    }

    fun loadFollowUpReport(companyId: String, userId: String?, start: String?, end: String?) {
        viewModelScope.launch {
            _followUpState.value = FollowUpUiState.Loading
            repository.getFollowUpReport(companyId, userId, start, end)
                .catch { e -> _followUpState.value = FollowUpUiState.Error(e.message ?: "Unknown Error") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _followUpState.value = FollowUpUiState.Success(it) },
                        onFailure = { _followUpState.value = FollowUpUiState.Error(it.message ?: "Failed loading compliance statistics") }
                    )
                }
        }
    }

    fun loadSalesTeamReport(companyId: String, territory: String?, userId: String?, start: String?, end: String?) {
        viewModelScope.launch {
            _salesTeamState.value = SalesTeamUiState.Loading
            repository.getSalesTeamReport(companyId, territory, userId, start, end)
                .catch { e -> _salesTeamState.value = SalesTeamUiState.Error(e.message ?: "Unknown error") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _salesTeamState.value = SalesTeamUiState.Success(it) },
                        onFailure = { _salesTeamState.value = SalesTeamUiState.Error(it.message ?: "Failed loading representative metrics") }
                    )
                }
        }
    }

    fun executeCustomReport(sourceModule: String, columns: List<String>) {
        viewModelScope.launch {
            _customReportState.value = CustomReportUiState.Loading
            val spec = CustomReportSpec(
                title = "Ad-Hoc Custom Extraction: $sourceModule",
                sourceModule = sourceModule,
                measureColumns = columns,
                startDate = startDate.value,
                endDate = endDate.value,
                customerId = selectedCustomerId.value,
                userId = selectedUserId.value,
                territory = selectedTerritory.value
            )
            repository.runCustomReport(companyId.value, spec)
                .catch { e -> _customReportState.value = CustomReportUiState.Error(e.message ?: "Generation Failed") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _customReportState.value = CustomReportUiState.Success(it) },
                        onFailure = { _customReportState.value = CustomReportUiState.Error(it.message ?: "Extraction failed") }
                    )
                }
        }
    }

    fun clearCustomReport() {
        _customReportState.value = CustomReportUiState.Idle
    }

    fun loadExportHistory(companyId: String) {
        viewModelScope.launch {
            repository.getExportHistory(companyId)
                .catch { e -> _exportHistoryState.value = ExportHistoryUiState.Error(e.message ?: "Unknown error") }
                .collect { result ->
                    result.fold(
                        onSuccess = { _exportHistoryState.value = ExportHistoryUiState.Success(it) },
                        onFailure = { _exportHistoryState.value = ExportHistoryUiState.Error(it.message ?: "Failed listing past exports") }
                    )
                }
        }
    }

    fun triggerExportReport(reportType: String, format: String, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            _exportHistoryState.value = ExportHistoryUiState.Loading
            val filtersMap = mutableMapOf<String, String>().apply {
                put("startDate", startDate.value)
                put("endDate", endDate.value)
                selectedCustomerId.value?.let { put("customerId", it) }
                selectedUserId.value?.let { put("userId", it) }
                selectedTerritory.value?.let { put("territory", it) }
            }
            val result = repository.triggerExport(companyId.value, reportType, format, filtersMap)
            result.fold(
                onSuccess = { record ->
                    loadExportHistory(companyId.value)
                    onComplete(record.fileName)
                },
                onFailure = {
                    loadExportHistory(companyId.value)
                    onComplete("Error compiling document: ${it.message}")
                }
            )
        }
    }

    private data class SessionFilters(
        val companyId: String,
        val startDate: String,
        val endDate: String,
        val customerId: String?,
        val userId: String?,
        val territory: String?
    )
}
