package com.example.feature.security.domain

import com.example.core.common.Resource
import com.example.feature.security.domain.models.*
import kotlinx.coroutines.flow.Flow

interface ISecurityRepository {
    fun getRoles(companyId: String, forceRefresh: Boolean): Flow<Resource<List<Role>>>
    suspend fun getRoleById(id: String): Role?
    suspend fun createRole(role: Role): Boolean
    suspend fun updateRole(role: Role): Boolean
    suspend fun deleteRole(role: Role): Boolean

    fun getAssignments(companyId: String, forceRefresh: Boolean): Flow<Resource<List<UserAssignment>>>
    suspend fun assignUser(assignment: UserAssignment): Boolean
    suspend fun removeUserAssignment(assignmentId: String): Boolean

    fun getAuditLogs(companyId: String, forceRefresh: Boolean): Flow<Resource<List<SecurityAuditRecord>>>
    suspend fun logAuditRecord(record: SecurityAuditRecord): Boolean
}
