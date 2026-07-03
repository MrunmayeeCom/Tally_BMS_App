package com.example.feature.security.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.security.domain.models.ModulePermission

@Entity(tableName = "security_roles")
data class LocalSecurityRole(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val companyId: String,
    val tenantId: String,
    val isSystem: Boolean,
    val permissions: List<ModulePermission>
)

@Entity(tableName = "user_role_assignments")
data class LocalUserAssignment(
    @PrimaryKey val id: String,
    val roleId: String,
    val roleName: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val companyId: String,
    val tenantId: String,
    val assignedAt: String
)

@Entity(tableName = "security_audit_records")
data class LocalSecurityAuditRecord(
    @PrimaryKey val id: String,
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
