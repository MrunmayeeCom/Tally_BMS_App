package com.example.feature.security.domain.models

enum class PermissionAction {
    VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, SYNC, ASSIGN, MANAGE
}

enum class SecurityModule {
    DASHBOARD, CRM, QUOTATIONS, ORDERS, ACCOUNTING, INVENTORY, OUTSTANDING,
    REMINDERS, FOLLOW_UPS, SALES_TEAM, TERRITORY_BEAT, REPORTS, APPROVALS,
    SYNC, NOTIFICATIONS, SETTINGS
}

data class ModulePermission(
    val module: SecurityModule,
    val actions: Set<PermissionAction>
)

data class Role(
    val id: String,
    val name: String,
    val description: String,
    val companyId: String,
    val tenantId: String,
    val isSystem: Boolean,
    val permissions: List<ModulePermission>
)

data class UserAssignment(
    val id: String,
    val roleId: String,
    val roleName: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val companyId: String,
    val tenantId: String,
    val assignedAt: String
)

data class SecurityAuditRecord(
    val id: String,
    val companyId: String,
    val tenantId: String,
    val timestamp: String,
    val actionType: String, // "ROLE_CREATED", "ROLE_UPDATED", "ROLE_DELETED", "ROLE_CLONED", "USER_ASSIGNED", "USER_REMOVED"
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val targetName: String,
    val details: String
)
