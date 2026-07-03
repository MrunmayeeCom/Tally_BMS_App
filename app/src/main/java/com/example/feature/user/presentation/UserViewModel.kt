package com.example.feature.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.session.SessionManager
import com.example.feature.security.domain.ISecurityRepository
import com.example.feature.security.domain.models.PermissionAction
import com.example.feature.security.domain.models.Role
import com.example.feature.security.domain.models.SecurityModule
import com.example.feature.user.domain.models.User
import com.example.feature.user.domain.models.UserActivity
import com.example.feature.user.domain.models.UserStatus
import com.example.feature.user.domain.repository.IUserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserViewModel(
    private val userRepository: IUserRepository,
    private val securityRepository: ISecurityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private var activeCompanyId: String = "company_default"
    private var activeTenantId: String = "tenant_default"
    private var activeUserName: String = "Administrator"

    init {
        // Collect session scope for Multi-Tenant Isolation
        viewModelScope.launch {
            sessionManager.companyId.collectLatest { cid ->
                if (cid != null) {
                    activeCompanyId = cid
                    refreshAll()
                }
            }
        }
        viewModelScope.launch {
            sessionManager.tenantId.collectLatest { tid ->
                if (tid != null) activeTenantId = tid
            }
        }
        viewModelScope.launch {
            sessionManager.userName.collectLatest { name ->
                if (name != null) activeUserName = name
            }
        }
    }

    fun refreshAll() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // 1. Fetch Users
            userRepository.getUsers(activeCompanyId, forceRefresh = true).collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(users = res.data, isLoading = false) }
                        // If a user was selected, refresh their details too
                        val currentSelected = _uiState.value.selectedUser
                        if (currentSelected != null) {
                            val updatedUser = res.data.firstOrNull { it.id == currentSelected.id }
                            if (updatedUser != null) {
                                selectUser(updatedUser)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = res.message, isLoading = false) }
                    }
                    Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }

        viewModelScope.launch {
            // 2. Fetch Policies Roles
            securityRepository.getRoles(activeCompanyId, forceRefresh = true).collectLatest { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(roles = res.data) }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateStatusFilter(filter: UserStatus?) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun selectUser(user: User) {
        _uiState.update { it.copy(selectedUser = user, isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            // Load user activities & security logs
            userRepository.getUserActivities(user.id, forceRefresh = true).collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                activities = res.data,
                                isLoading = false,
                                resolvedPermissions = calculateEffectivePermissions(it.roles, user.assignedRoles)
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                resolvedPermissions = calculateEffectivePermissions(it.roles, user.assignedRoles),
                                isLoading = false
                            )
                        }
                    }
                    Resource.Loading -> {}
                }
            }
        }
    }

    fun calculateEffectivePermissions(roles: List<Role>, assignedRoleIds: Collection<String>): Map<SecurityModule, Set<PermissionAction>> {
        val result = mutableMapOf<SecurityModule, Set<PermissionAction>>()
        SecurityModule.values().forEach { module ->
            result[module] = emptySet()
        }
        val matchingRoles = roles.filter { assignedRoleIds.contains(it.id) }
        matchingRoles.forEach { role ->
            role.permissions.forEach { modulePerm ->
                val currentSet = result[modulePerm.module] ?: emptySet()
                result[modulePerm.module] = currentSet + modulePerm.actions
            }
        }
        return result
    }

    // --- Form Management ---
    fun updateFormName(value: String) { _uiState.update { it.copy(formName = value) } }
    fun updateFormEmail(value: String) { _uiState.update { it.copy(formEmail = value) } }
    fun updateFormPhone(value: String) { _uiState.update { it.copy(formPhone = value) } }
    fun updateFormEmployeeCode(value: String) { _uiState.update { it.copy(formEmployeeCode = value) } }
    fun updateFormCompany(value: String) { _uiState.update { it.copy(formCompany = value) } }
    fun updateFormTerritory(value: String) { _uiState.update { it.copy(formTerritory = value) } }
    
    fun toggleFormRole(roleId: String) {
        _uiState.update { state ->
            val roles = state.formRoles.toMutableSet()
            if (roles.contains(roleId)) roles.remove(roleId) else roles.add(roleId)
            val updatedPermissions = calculateEffectivePermissions(state.roles, roles)
            state.copy(formRoles = roles, resolvedPermissions = updatedPermissions)
        }
    }

    fun clearForm() {
        _uiState.update { 
            it.copy(
                formName = "",
                formEmail = "",
                formPhone = "",
                formEmployeeCode = "",
                formCompany = "",
                formTerritory = "",
                formRoles = emptySet(),
                formStatus = UserStatus.ACTIVE,
                invitationLink = null,
                tempPassword = null
            )
        }
    }

    // --- Lifecycle Actions ---
    fun createUser(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.formName.isBlank() || state.formEmail.isBlank()) return

        val newUser = User(
            id = "user_" + UUID.randomUUID().toString().take(6),
            name = state.formName,
            email = state.formEmail,
            phone = state.formPhone,
            employeeCode = state.formEmployeeCode,
            assignedCompany = state.formCompany.ifBlank { "Tally Corporate" },
            assignedTerritory = state.formTerritory.ifBlank { "Unassigned" },
            assignedRoles = state.formRoles.toList(),
            status = UserStatus.ACTIVE,
            lastLogin = null,
            companyId = activeCompanyId,
            tenantId = activeTenantId
        )

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            userRepository.createUser(newUser).collectLatest { res ->
                if (res is Resource.Success) {
                    val created = res.data
                    logActivity(created.id, "Direct Account Created", "System administrative creation of profile", "SUCCESS")
                    refreshAll()
                    onSuccess()
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun inviteUser(type: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.formEmail.isBlank() || state.formPhonesValid().not()) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            userRepository.inviteUser(
                email = state.formEmail,
                phone = state.formPhone,
                assignedRoles = state.formRoles.toList(),
                invitationType = type
            ).collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        val response = res.data
                        val tempPass = response.tempPassword
                        val inviteLink = response.invitationLink

                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                invitationLink = inviteLink,
                                tempPassword = tempPass,
                                infoMessage = "Invitation successfully triggered via $type."
                            )
                        }

                        // Also create user profile immediately in database
                        val userMock = User(
                            id = "invited_" + UUID.randomUUID().toString().take(6),
                            name = state.formName.ifBlank { state.formEmail.substringBefore("@") },
                            email = state.formEmail,
                            phone = state.formPhone,
                            employeeCode = state.formEmployeeCode.ifBlank { "INVITED" },
                            assignedCompany = state.formCompany.ifBlank { "Tally Partner Domain" },
                            assignedTerritory = state.formTerritory.ifBlank { "Unassigned" },
                            assignedRoles = state.formRoles.toList(),
                            status = UserStatus.INACTIVE,
                            lastLogin = null,
                            companyId = activeCompanyId,
                            tenantId = activeTenantId
                        )
                        userRepository.createUser(userMock).collectLatest { r ->
                            if (r is Resource.Success) {
                                logActivity(userMock.id, "Invitation Issued", "Credentials profile requested via $type", "SUCCESS")
                                refreshAll()
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = res.message) }
                    }
                    Resource.Loading -> {}
                }
            }
        }
    }

    fun activateUser(userId: String) {
        _uiState.update { it.copy(isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, UserStatus.ACTIVE).collectLatest { res ->
                if (res is Resource.Success) {
                    logActivity(userId, "Account Activated", "Unlocked and enabled worker login keys", "SUCCESS")
                    _uiState.update { it.copy(infoMessage = "User status set to ACTIVE successfully.") }
                    refreshAll()
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun deactivateUser(userId: String) {
        _uiState.update { it.copy(isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, UserStatus.INACTIVE).collectLatest { res ->
                if (res is Resource.Success) {
                    logActivity(userId, "Account Suspended", "Disabled worker system access clearances", "INFO")
                    _uiState.update { it.copy(infoMessage = "User status set to INACTIVE successfully.") }
                    refreshAll()
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun lockUser(userId: String) {
        _uiState.update { it.copy(isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, UserStatus.LOCKED).collectLatest { res ->
                if (res is Resource.Success) {
                    logActivity(userId, "Credentials Locked", "Security alert freeze on authorization credentials", "FAILURE")
                    _uiState.update { it.copy(infoMessage = "User profile set to LOCKED instantly.") }
                    refreshAll()
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun resetPassword(userId: String) {
        _uiState.update { it.copy(isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            userRepository.resetUserPassword(userId).collectLatest { res ->
                if (res is Resource.Success) {
                    logActivity(userId, "Password Rotated", "Credential reset requested. Temp token compiled.", "SUCCESS")
                    _uiState.update { it.copy(isLoading = false, infoMessage = res.data) }
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun deleteUser(userId: String, onDeleted: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, infoMessage = null) }
        viewModelScope.launch {
            userRepository.deleteUser(userId).collectLatest { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(isLoading = false, selectedUser = null, infoMessage = "Teammate reference destroyed completely.") }
                    logActivity(userId, "Account Erased", "Destroyed profile registries and associated cache files", "INFO")
                    refreshAll()
                    onDeleted()
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun updateUserRoles(userId: String, roles: List<String>) {
        val user = _uiState.value.users.firstOrNull { it.id == userId } ?: return
        _uiState.update { it.copy(isLoading = true) }
        
        val updatedUser = user.copy(assignedRoles = roles)
        viewModelScope.launch {
            userRepository.createUser(updatedUser).collectLatest { res ->
                if (res is Resource.Success) {
                    logActivity(userId, "Roles Vector Modified", "Security levels assigned: ${roles.joinToString(", ")}", "SUCCESS")
                    _uiState.update { it.copy(infoMessage = "Permissions matrix assignments re-routed.") }
                    refreshAll()
                }
            }
        }
    }

    private fun logActivity(targetUid: String, eventName: String, desc: String, status: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val act = UserActivity(
                id = UUID.randomUUID().toString(),
                userId = targetUid,
                timestamp = dateStr,
                eventName = eventName,
                deviceInfo = "Android App (Device: " + android.os.Build.MODEL + ", OS: " + android.os.Build.VERSION.RELEASE + ")",
                ipAddress = "192.168.1.104 (Local Gateway)",
                location = "Active Office Workspace, IN",
                status = status
            )
            userRepository.saveLocalActivity(act)
        }
    }

    private fun UserUiState.formPhonesValid(): Boolean {
        // Simple sanity check for simulation
        return this.formPhone.isNotBlank() || this.formEmail.isNotBlank()
    }
}
