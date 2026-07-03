package com.example.feature.security.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.session.SessionManager
import com.example.feature.security.domain.ISecurityRepository
import com.example.feature.security.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SecurityUiState(
    val isLoading: Boolean = false,
    val roles: List<Role> = emptyList(),
    val assignments: List<UserAssignment> = emptyList(),
    val auditLogs: List<SecurityAuditRecord> = emptyList(),
    val error: String? = null,

    // Form inputs for Role Create/Edit/Clone
    val editingRole: Role? = null, // Non-null if editing or cloning
    val isCloneMode: Boolean = false,
    val formRoleName: String = "",
    val formRoleDescription: String = "",
    val formRolePermissions: Map<SecurityModule, Set<PermissionAction>> = emptyMap(),

    // Form inputs for Assignment
    val formAssignUserId: String = "",
    val formAssignUserName: String = "",
    val formAssignUserEmail: String = "",
    val formAssignRoleId: String = "",

    // Preview state
    val previewingUserId: String? = null,

    // Filter metrics
    val searchAuditQuery: String = "",
    val searchRolesQuery: String = "",
    val searchAssignmentsQuery: String = ""
)

class SecurityViewModel(
    private val repository: ISecurityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    private val _companyId = MutableStateFlow("")
    private val _tenantId = MutableStateFlow("")
    private val _userRoleName = MutableStateFlow("")
    private val _userName = MutableStateFlow("")
    private val _userId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            sessionManager.companyId.collectLatest { id ->
                val cid = id ?: "comp_01"
                _companyId.value = cid
                loadModuleData(cid)
            }
        }
        viewModelScope.launch {
            sessionManager.tenantId.collectLatest { id ->
                _tenantId.value = id ?: "tenant_global"
            }
        }
        viewModelScope.launch {
            sessionManager.userRole.collectLatest { role ->
                _userRoleName.value = role ?: "Company Admin"
            }
        }
        viewModelScope.launch {
            sessionManager.userName.collectLatest { name ->
                _userName.value = name ?: "Manager"
            }
        }
        _userId.value = "user_default"
    }

    fun refreshAll() {
        val cid = _companyId.value.ifBlank { "comp_01" }
        loadModuleData(cid, forceRefresh = true)
    }

    private fun loadModuleData(cid: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Fetch dynamic roles
            repository.getRoles(cid, forceRefresh).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(roles = resource.data, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = resource.exception.message, isLoading = false) }
                    }
                }
            }

            // 2. Fetch assignments
            repository.getAssignments(cid, forceRefresh).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(assignments = resource.data) }
                }
            }

            // 3. Fetch audit records
            repository.getAuditLogs(cid, forceRefresh).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(auditLogs = resource.data) }
                }
            }
        }
    }

    // === Role Form Management ===
    fun startCreateRole() {
        _uiState.update {
            it.copy(
                editingRole = null,
                isCloneMode = false,
                formRoleName = "",
                formRoleDescription = "",
                formRolePermissions = SecurityModule.values().associateWith { emptySet<PermissionAction>() }
            )
        }
    }

    fun startEditRole(role: Role) {
        val permissionsMap = SecurityModule.values().associateWith { module ->
            role.permissions.firstOrNull { it.module == module }?.actions ?: emptySet()
        }
        _uiState.update {
            it.copy(
                editingRole = role,
                isCloneMode = false,
                formRoleName = role.name,
                formRoleDescription = role.description,
                formRolePermissions = permissionsMap
            )
        }
    }

    fun startCloneRole(role: Role) {
        val permissionsMap = SecurityModule.values().associateWith { module ->
            role.permissions.firstOrNull { it.module == module }?.actions ?: emptySet()
        }
        _uiState.update {
            it.copy(
                editingRole = role,
                isCloneMode = true,
                formRoleName = "${role.name} (Copy)",
                formRoleDescription = "Cloned from ${role.name}. ${role.description}",
                formRolePermissions = permissionsMap
            )
        }
    }

    fun updateRoleName(name: String) {
        _uiState.update { it.copy(formRoleName = name) }
    }

    fun updateRoleDescription(desc: String) {
        _uiState.update { it.copy(formRoleDescription = desc) }
    }

    fun togglePermission(module: SecurityModule, action: PermissionAction) {
        _uiState.update { state ->
            val modulePermissions = state.formRolePermissions[module] ?: emptySet()
            val updatedActions = if (modulePermissions.contains(action)) {
                modulePermissions - action
            } else {
                modulePermissions + action
            }
            val updatedMap = state.formRolePermissions.toMutableMap()
            updatedMap[module] = updatedActions
            state.copy(formRolePermissions = updatedMap)
        }
    }

    fun setAllPermissionsForModule(module: SecurityModule, enableAll: Boolean) {
        _uiState.update { state ->
            val updatedMap = state.formRolePermissions.toMutableMap()
            updatedMap[module] = if (enableAll) PermissionAction.values().toSet() else emptySet()
            state.copy(formRolePermissions = updatedMap)
        }
    }

    fun saveRole(onSuccess: () -> Unit) {
        val state = _uiState.value
        val cid = _companyId.value.ifBlank { "comp_01" }
        val tid = _tenantId.value.ifBlank { "tenant_global" }

        val isEditing = state.editingRole != null && !state.isCloneMode
        val roleId = if (isEditing) state.editingRole!!.id else "role_${UUID.randomUUID().toString().take(6)}"

        val mappedModulePermissions = state.formRolePermissions.map { (module, actions) ->
            ModulePermission(module, actions)
        }

        val role = Role(
            id = roleId,
            name = state.formRoleName.trim(),
            description = state.formRoleDescription.trim(),
            companyId = cid,
            tenantId = tid,
            isSystem = false,
            permissions = mappedModulePermissions
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val completed = if (isEditing) {
                repository.updateRole(role)
            } else {
                repository.createRole(role)
            }

            if (completed) {
                // Log Audit Record
                val actionLabel = if (isEditing) "ROLE_UPDATED" else if (state.isCloneMode) "ROLE_CLONED" else "ROLE_CREATED"
                val auditText = if (isEditing) {
                    "Updated role custom parameters and granular permissions matrix for visual operations."
                } else if (state.isCloneMode) {
                    "Cloned and instantiated role vectors from predecessor visual role ${state.editingRole?.name}."
                } else {
                    "Constructed secure modular credential permissions vector."
                }

                repository.logAuditRecord(
                    SecurityAuditRecord(
                        id = "aud_${UUID.randomUUID().toString().take(6)}",
                        companyId = cid,
                        tenantId = tid,
                        timestamp = getCurrentFormattedTime(),
                        actionType = actionLabel,
                        actorId = _userId.value,
                        actorName = _userName.value,
                        actorRole = _userRoleName.value,
                        targetName = role.name,
                        details = auditText
                    )
                )

                loadModuleData(cid, forceRefresh = false)
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to synchronize role updates with security master nodes.") }
            }
        }
    }

    fun deleteRole(role: Role) {
        val cid = _companyId.value.ifBlank { "comp_01" }
        val tid = _tenantId.value.ifBlank { "tenant_global" }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val completed = repository.deleteRole(role)
            if (completed) {
                // Log audit trail
                repository.logAuditRecord(
                    SecurityAuditRecord(
                        id = "aud_${UUID.randomUUID().toString().take(6)}",
                        companyId = cid,
                        tenantId = tid,
                        timestamp = getCurrentFormattedTime(),
                        actionType = "ROLE_DELETED",
                        actorId = _userId.value,
                        actorName = _userName.value,
                        actorRole = _userRoleName.value,
                        targetName = role.name,
                        details = "Permanently deleted role permissions schema. All active assignments were revoked."
                    )
                )
                loadModuleData(cid, forceRefresh = false)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to purge database metadata for target role.") }
            }
        }
    }

    // === User Assignments Management ===
    fun updateAssignForm(userId: String, userName: String, userEmail: String, roleId: String) {
        _uiState.update {
            it.copy(
                formAssignUserId = userId,
                formAssignUserName = userName,
                formAssignUserEmail = userEmail,
                formAssignRoleId = roleId
            )
        }
    }

    fun assignUserRole() {
        val state = _uiState.value
        val cid = _companyId.value.ifBlank { "comp_01" }
        val tid = _tenantId.value.ifBlank { "tenant_global" }

        val selectedRoleName = state.roles.firstOrNull { it.id == state.formAssignRoleId }?.name ?: "Unknown Role"

        val assignment = UserAssignment(
            id = "asg_${UUID.randomUUID().toString().take(6)}",
            roleId = state.formAssignRoleId,
            roleName = selectedRoleName,
            userId = state.formAssignUserId.trim(),
            userName = state.formAssignUserName.trim(),
            userEmail = state.formAssignUserEmail.trim(),
            companyId = cid,
            tenantId = tid,
            assignedAt = getCurrentFormattedDateOnly()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val completed = repository.assignUser(assignment)
            if (completed) {
                // Log audit trail
                repository.logAuditRecord(
                    SecurityAuditRecord(
                        id = "aud_${UUID.randomUUID().toString().take(6)}",
                        companyId = cid,
                        tenantId = tid,
                        timestamp = getCurrentFormattedTime(),
                        actionType = "USER_ASSIGNED",
                        actorId = _userId.value,
                        actorName = _userName.value,
                        actorRole = _userRoleName.value,
                        targetName = assignment.userName,
                        details = "Assigned user to '${assignment.roleName}' credential permission matrix."
                    )
                )
                _uiState.update {
                    it.copy(
                        formAssignUserId = "",
                        formAssignUserName = "",
                        formAssignUserEmail = "",
                        formAssignRoleId = ""
                    )
                }
                loadModuleData(cid, forceRefresh = false)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to synchronize user credentials linkage with secure core.") }
            }
        }
    }

    fun revokeAssignment(assignment: UserAssignment) {
        val cid = _companyId.value.ifBlank { "comp_01" }
        val tid = _tenantId.value.ifBlank { "tenant_global" }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val completed = repository.removeUserAssignment(assignment.id)
            if (completed) {
                repository.logAuditRecord(
                    SecurityAuditRecord(
                        id = "aud_${UUID.randomUUID().toString().take(6)}",
                        companyId = cid,
                        tenantId = tid,
                        timestamp = getCurrentFormattedTime(),
                        actionType = "USER_REMOVED",
                        actorId = _userId.value,
                        actorName = _userName.value,
                        actorRole = _userRoleName.value,
                        targetName = assignment.userName,
                        details = "Revoked user access assignment from '${assignment.roleName}' clearance level."
                    )
                )
                loadModuleData(cid, forceRefresh = false)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to sever user assignment.") }
            }
        }
    }

    fun setPreviewUser(userId: String?) {
        _uiState.update { it.copy(previewingUserId = userId) }
    }

    // Search input updates
    fun updateSearchAuditQuery(q: String) {
        _uiState.update { it.copy(searchAuditQuery = q) }
    }

    fun updateSearchRolesQuery(q: String) {
        _uiState.update { it.copy(searchRolesQuery = q) }
    }

    fun updateSearchAssignmentsQuery(q: String) {
        _uiState.update { it.copy(searchAssignmentsQuery = q) }
    }

    // Helper functions
    private fun getCurrentFormattedTime(): String {
        val sdf = SimpleDateFormat("dd-Jun-yyyy hh:mm a", Locale.ENGLISH)
        return sdf.format(Date())
    }

    private fun getCurrentFormattedDateOnly(): String {
        val sdf = SimpleDateFormat("dd-Jun-yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }
}

// Extension to compute effective permissions
fun SecurityUiState.getEffectivePermissionsForUser(userId: String): Map<SecurityModule, Set<PermissionAction>> {
    val userAssignedRoleIds = assignments.filter { it.userId == userId }.map { it.roleId }
    val matchingRoles = roles.filter { it.id in userAssignedRoleIds }
    
    val effectiveMap = SecurityModule.values().associateWith { mutableSetOf<PermissionAction>() }
    for (role in matchingRoles) {
        for (modulePerm in role.permissions) {
            effectiveMap[modulePerm.module]?.addAll(modulePerm.actions)
        }
    }
    return effectiveMap
}
