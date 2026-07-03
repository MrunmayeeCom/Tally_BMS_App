package com.example.feature.security.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModulePermissionDto(
    val module: String,
    val actions: List<String>
)

@JsonClass(generateAdapter = true)
data class SecurityRoleDto(
    val id: String,
    val name: String,
    val description: String,
    val companyId: String,
    val tenantId: String,
    val isSystem: Boolean,
    val permissions: List<ModulePermissionDto>
)

@JsonClass(generateAdapter = true)
data class UserAssignmentDto(
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

@JsonClass(generateAdapter = true)
data class SecurityAuditRecordDto(
    val id: String,
    val companyId: String,
    val tenantId: String,
    val timestamp: String,
    val actionType: String,
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val targetName: String,
    val details: String
)
