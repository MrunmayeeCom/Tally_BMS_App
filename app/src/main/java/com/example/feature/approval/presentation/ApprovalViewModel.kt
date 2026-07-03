package com.example.feature.approval.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.session.SessionManager
import com.example.feature.approval.domain.IApprovalRepository
import com.example.feature.approval.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ApprovalUiState(
    val isLoading: Boolean = false,
    val approvalsCount: Int = 0,
    val pendingApprovals: List<ApprovalRequest> = emptyList(),
    val approvedApprovals: List<ApprovalRequest> = emptyList(),
    val rejectedApprovals: List<ApprovalRequest> = emptyList(),
    val escalatedApprovals: List<ApprovalRequest> = emptyList(),
    val allApprovals: List<ApprovalRequest> = emptyList(),
    val selectedApproval: ApprovalRequest? = null,
    val analytics: ApprovalAnalytics? = null,
    val message: String? = null,
    val error: String? = null
)

class ApprovalViewModel(
    private val repository: IApprovalRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApprovalUiState())
    val uiState: StateFlow<ApprovalUiState> = _uiState.asStateFlow()

    private val _companyId = MutableStateFlow("")
    private val _tenantId = MutableStateFlow("")
    private val _userRole = MutableStateFlow("")
    private val _userName = MutableStateFlow("")

    init {
        viewModelScope.launch {
            sessionManager.companyId.collectLatest { id ->
                val cid = id ?: "comp_01"
                _companyId.value = cid
                loadDashboardData(cid)
            }
        }
        viewModelScope.launch {
            sessionManager.tenantId.collectLatest { id ->
                _tenantId.value = id ?: "tenant_global"
            }
        }
        viewModelScope.launch {
            sessionManager.userRole.collectLatest { role ->
                _userRole.value = role ?: "Company Admin"
            }
        }
        viewModelScope.launch {
            sessionManager.userName.collectLatest { name ->
                _userName.value = name ?: "Manager"
            }
        }
    }

    fun loadDashboardData(forceRefresh: Boolean = false) {
        val cid = _companyId.value.ifBlank { "comp_01" }
        loadDashboardData(cid, forceRefresh)
    }

    private fun loadDashboardData(cid: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observe approvals flow
            repository.getApprovals(cid, forceRefresh).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val approvals = resource.data
                        val pending = approvals.filter { it.status == ApprovalStatus.PENDING }
                        val approved = approvals.filter { it.status == ApprovalStatus.APPROVED }
                        val rejected = approvals.filter { it.status == ApprovalStatus.REJECTED }
                        val escalated = approvals.filter { it.status == ApprovalStatus.ESCALATED || it.status == ApprovalStatus.HELD }
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                allApprovals = approvals,
                                pendingApprovals = pending,
                                approvedApprovals = approved,
                                rejectedApprovals = rejected,
                                escalatedApprovals = escalated,
                                approvalsCount = approvals.size
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.exception.message) }
                    }
                }
            }

            // Fetch analytics
            repository.getAnalytics(cid, forceRefresh).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(analytics = resource.data) }
                }
            }
        }
    }

    fun loadApprovalDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val approval = repository.getApprovalById(id)
            if (approval != null) {
                _uiState.update { it.copy(isLoading = false, selectedApproval = approval, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Approval request $id not found.") }
            }
        }
    }

    fun performAction(
        id: String,
        status: ApprovalStatus,
        remarks: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = "user_default"
            val userName = _userName.value
            val userRole = _userRole.value
            
            val result = repository.performAction(
                id = id,
                status = status,
                remarks = remarks,
                operatorId = userId,
                operatorName = userName,
                operatorRole = userRole
            )

            if (result) {
                _uiState.update { it.copy(isLoading = false, message = "Approval updated successfully.") }
                // Reload lists
                loadDashboardData(forceRefresh = true)
                // Reload detail
                loadApprovalDetail(id)
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to update approval action.") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
