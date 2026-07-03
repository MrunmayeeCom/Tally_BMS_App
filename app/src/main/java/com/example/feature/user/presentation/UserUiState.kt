package com.example.feature.user.presentation

import com.example.feature.user.domain.models.User
import com.example.feature.user.domain.models.UserActivity
import com.example.feature.user.domain.models.UserStatus
import com.example.feature.security.domain.models.Role

data class UserUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    
    // Directory Data
    val users: List<User> = emptyList(),
    val roles: List<Role> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: UserStatus? = null,
    
    // Active detail/profile operations
    val selectedUser: User? = null,
    val activities: List<UserActivity> = emptyList(),
    
    // Creation/Invite Form fields
    val formName: String = "",
    val formEmail: String = "",
    val formPhone: String = "",
    val formEmployeeCode: String = "",
    val formCompany: String = "",
    val formTerritory: String = "",
    val formRoles: Set<String> = emptySet(),
    val formStatus: UserStatus = UserStatus.ACTIVE,
    
    // Generated invitation results
    val invitationLink: String? = null,
    val tempPassword: String? = null,
    
    // Permissions preview map of the selected (or edited) user
    // resolved across each SecurityModule to its set of clearances
    val resolvedPermissions: Map<com.example.feature.security.domain.models.SecurityModule, Set<com.example.feature.security.domain.models.PermissionAction>> = emptyMap()
)
