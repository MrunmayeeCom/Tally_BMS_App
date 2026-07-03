package com.example.feature.security.data.repository

import com.example.core.common.Resource
import com.example.feature.security.data.api.SecurityApiService
import com.example.feature.security.data.db.*
import com.example.feature.security.data.dto.*
import com.example.feature.security.domain.ISecurityRepository
import com.example.feature.security.domain.models.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class SecurityRepositoryImpl(
    private val apiService: SecurityApiService,
    private val securityDao: SecurityDao
) : ISecurityRepository {

    override fun getRoles(companyId: String, forceRefresh: Boolean): Flow<Resource<List<Role>>> = flow {
        emit(Resource.Loading)

        // 1. Fetch from database first
        val cached = securityDao.getRolesByCompany(companyId).firstOrNull() ?: emptyList()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        // Seeding default system roles on first run
        if (cached.isEmpty()) {
            val systemRoles = getDefaultSystemRoles(companyId)
            securityDao.insertRoles(systemRoles.map { it.toLocal() })
            emit(Resource.Success(systemRoles))
        }

        // 2. Fetch from cloud
        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getRoles(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val entityModels = dtos.map { it.toLocal() }
                    securityDao.insertRoles(entityModels)

                    val updated = securityDao.getRolesByCompany(companyId).first()
                    emit(Resource.Success(updated.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                // Return cache silently
                val finalCached = securityDao.getRolesByCompany(companyId).first()
                emit(Resource.Success(finalCached.map { it.toDomain() }))
            }
        }
    }

    override suspend fun getRoleById(id: String): Role? {
        return securityDao.getRoleById(id)?.toDomain()
    }

    override suspend fun createRole(role: Role): Boolean {
        securityDao.insertRole(role.toLocal())
        try {
            val response = apiService.createRole(role.toDto())
            if (response.isSuccessful && response.body() != null) {
                securityDao.insertRole(response.body()!!.toLocal())
                return true
            }
        } catch (_: Exception) {}
        return true
    }

    override suspend fun updateRole(role: Role): Boolean {
        securityDao.insertRole(role.toLocal())
        try {
            val response = apiService.updateRole(role.id, role.toDto())
            if (response.isSuccessful && response.body() != null) {
                securityDao.insertRole(response.body()!!.toLocal())
                return true
            }
        } catch (_: Exception) {}
        return true
    }

    override suspend fun deleteRole(role: Role): Boolean {
        securityDao.deleteRole(role.toLocal())
        securityDao.deleteAssignmentsByRole(role.id)
        try {
            apiService.deleteRole(role.id)
        } catch (_: Exception) {}
        return true
    }

    override fun getAssignments(companyId: String, forceRefresh: Boolean): Flow<Resource<List<UserAssignment>>> = flow {
        emit(Resource.Loading)

        val cached = securityDao.getUserAssignmentsByCompany(companyId).firstOrNull() ?: emptyList()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        // Seeding default mock user assignments
        if (cached.isEmpty()) {
            val defaultAssignments = getDefaultAssignments(companyId)
            securityDao.insertUserAssignments(defaultAssignments.map { it.toLocal() })
            emit(Resource.Success(defaultAssignments))
        }

        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getAssignments(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    securityDao.insertUserAssignments(dtos.map { it.toLocal() })

                    val updated = securityDao.getUserAssignmentsByCompany(companyId).first()
                    emit(Resource.Success(updated.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                val finalCached = securityDao.getUserAssignmentsByCompany(companyId).first()
                emit(Resource.Success(finalCached.map { it.toDomain() }))
            }
        }
    }

    override suspend fun assignUser(assignment: UserAssignment): Boolean {
        securityDao.insertUserAssignment(assignment.toLocal())
        try {
            val response = apiService.createAssignment(assignment.toDto())
            if (response.isSuccessful && response.body() != null) {
                securityDao.insertUserAssignment(response.body()!!.toLocal())
                return true
            }
        } catch (_: Exception) {}
        return true
    }

    override suspend fun removeUserAssignment(assignmentId: String): Boolean {
        securityDao.deleteUserAssignment(assignmentId)
        try {
            apiService.deleteAssignment(assignmentId)
        } catch (_: Exception) {}
        return true
    }

    override fun getAuditLogs(companyId: String, forceRefresh: Boolean): Flow<Resource<List<SecurityAuditRecord>>> = flow {
        emit(Resource.Loading)

        val cached = securityDao.getAuditRecordsByCompany(companyId).firstOrNull() ?: emptyList()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        if (cached.isEmpty()) {
            val seedAudits = getSeedAuditLogs(companyId)
            securityDao.insertAuditRecords(seedAudits.map { it.toLocal() })
            emit(Resource.Success(seedAudits))
        }

        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getAuditLogs(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    securityDao.insertAuditRecords(dtos.map { it.toLocal() })

                    val updated = securityDao.getAuditRecordsByCompany(companyId).first()
                    emit(Resource.Success(updated.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                val finalCached = securityDao.getAuditRecordsByCompany(companyId).first()
                emit(Resource.Success(finalCached.map { it.toDomain() }))
            }
        }
    }

    override suspend fun logAuditRecord(record: SecurityAuditRecord): Boolean {
        securityDao.insertAuditRecord(record.toLocal())
        try {
            apiService.createAuditLog(record.toDto())
        } catch (_: Exception) {}
        return true
    }

    // === Converters ===
    private fun LocalSecurityRole.toDomain() = Role(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        tenantId = tenantId,
        isSystem = isSystem,
        permissions = permissions
    )

    private fun Role.toLocal() = LocalSecurityRole(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        tenantId = tenantId,
        isSystem = isSystem,
        permissions = permissions
    )

    private fun SecurityRoleDto.toLocal() = LocalSecurityRole(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        tenantId = tenantId,
        isSystem = isSystem,
        permissions = permissions.map { dto ->
            ModulePermission(
                module = SecurityModule.valueOf(dto.module),
                actions = dto.actions.map { PermissionAction.valueOf(it) }.toSet()
            )
        }
    )

    private fun Role.toDto() = SecurityRoleDto(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        tenantId = tenantId,
        isSystem = isSystem,
        permissions = permissions.map { item ->
            ModulePermissionDto(
                module = item.module.name,
                actions = item.actions.map { it.name }
            )
        }
    )

    private fun LocalUserAssignment.toDomain() = UserAssignment(
        id = id,
        roleId = roleId,
        roleName = roleName,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        companyId = companyId,
        tenantId = tenantId,
        assignedAt = assignedAt
    )

    private fun UserAssignment.toLocal() = LocalUserAssignment(
        id = id,
        roleId = roleId,
        roleName = roleName,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        companyId = companyId,
        tenantId = tenantId,
        assignedAt = assignedAt
    )

    private fun UserAssignmentDto.toLocal() = LocalUserAssignment(
        id = id,
        roleId = roleId,
        roleName = roleName,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        companyId = companyId,
        tenantId = tenantId,
        assignedAt = assignedAt
    )

    private fun UserAssignment.toDto() = UserAssignmentDto(
        id = id,
        roleId = roleId,
        roleName = roleName,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        companyId = companyId,
        tenantId = tenantId,
        assignedAt = assignedAt
    )

    private fun LocalSecurityAuditRecord.toDomain() = SecurityAuditRecord(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        timestamp = timestamp,
        actionType = actionType,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        targetName = targetName,
        details = details
    )

    private fun SecurityAuditRecord.toLocal() = LocalSecurityAuditRecord(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        timestamp = timestamp,
        actionType = actionType,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        targetName = targetName,
        details = details
    )

    private fun SecurityAuditRecordDto.toLocal() = LocalSecurityAuditRecord(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        timestamp = timestamp,
        actionType = actionType,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        targetName = targetName,
        details = details
    )

    private fun SecurityAuditRecord.toDto() = SecurityAuditRecordDto(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        timestamp = timestamp,
        actionType = actionType,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        targetName = targetName,
        details = details
    )

    // === System Defaults & Mock Providers ===
    private fun getDefaultSystemRoles(companyId: String): List<Role> {
        val modules = SecurityModule.values()
        val allActions = PermissionAction.values().toSet()
        val limitedActions = setOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.EDIT)
        val viewOnlyActions = setOf(PermissionAction.VIEW)

        return listOf(
            Role(
                id = "role_super_admin",
                name = "Super Admin",
                description = "Complete authority over system configurations, licensing, and security infrastructure.",
                companyId = companyId,
                tenantId = "tenant_global",
                isSystem = true,
                permissions = modules.map { ModulePermission(it, allActions) }
            ),
            Role(
                id = "role_company_admin",
                name = "Company Admin",
                description = "Total regulatory manager of company operations, including core approval flows and user sign-offs.",
                companyId = companyId,
                tenantId = "tenant_global",
                isSystem = true,
                permissions = modules.map { ModulePermission(it, allActions) }
            ),
            Role(
                id = "role_accountant",
                name = "Accountant",
                description = "Handles accounting sheets, voucher approvals, and client outstanding limits.",
                companyId = companyId,
                tenantId = "tenant_global",
                isSystem = true,
                permissions = modules.map { m ->
                    val actions = when (m) {
                        SecurityModule.ACCOUNTING, SecurityModule.OUTSTANDING, SecurityModule.REMINDERS -> allActions
                        SecurityModule.SETTINGS, SecurityModule.SYNC -> viewOnlyActions
                        else -> limitedActions
                    }
                    ModulePermission(m, actions)
                }
            ),
            Role(
                id = "role_sales_manager",
                name = "Sales Manager",
                description = "Coordinates client portfolios, quotation exceptions, territory mapping, and executive workflows.",
                companyId = companyId,
                tenantId = "tenant_global",
                isSystem = true,
                permissions = modules.map { m ->
                    val actions = when (m) {
                        SecurityModule.CRM, SecurityModule.QUOTATIONS, SecurityModule.ORDERS, SecurityModule.SALES_TEAM, SecurityModule.TERRITORY_BEAT -> allActions
                        SecurityModule.ACCOUNTING, SecurityModule.SETTINGS -> viewOnlyActions
                        else -> limitedActions
                    }
                    ModulePermission(m, actions)
                }
            ),
            Role(
                id = "role_sales_executive",
                name = "Sales Executive",
                description = "Client-facing agent for placing orders, reporting activities, mapping beats, and field surveys.",
                companyId = companyId,
                tenantId = "tenant_global",
                isSystem = true,
                permissions = modules.map { m ->
                    val actions = when (m) {
                        SecurityModule.CRM, SecurityModule.QUOTATIONS, SecurityModule.ORDERS, SecurityModule.FOLLOW_UPS -> limitedActions
                        SecurityModule.DASHBOARD, SecurityModule.INVENTORY, SecurityModule.TERRITORY_BEAT, SecurityModule.NOTIFICATIONS -> setOf(PermissionAction.VIEW)
                        else -> emptySet()
                    }
                    ModulePermission(m, actions)
                }
            )
        )
    }

    private fun getDefaultAssignments(companyId: String): List<UserAssignment> {
        return listOf(
            UserAssignment(
                id = "asg_1",
                roleId = "role_super_admin",
                roleName = "Super Admin",
                userId = "user_default",
                userName = "Mrunmayee Erande",
                userEmail = "mrunmayeeerande2714@gmail.com",
                companyId = companyId,
                tenantId = "tenant_global",
                assignedAt = "23-Jun-2026"
            ),
            UserAssignment(
                id = "asg_2",
                roleId = "role_accountant",
                roleName = "Accountant",
                userId = "user_acct2",
                userName = "Anjali Goel",
                userEmail = "anjali.goel@tallybms.com",
                companyId = companyId,
                tenantId = "tenant_global",
                assignedAt = "22-Jun-2026"
            ),
            UserAssignment(
                id = "asg_3",
                roleId = "role_sales_manager",
                roleName = "Sales Manager",
                userId = "user_sale1",
                userName = "Rohan Sharma",
                userEmail = "rohan.sharma@tallybms.com",
                companyId = companyId,
                tenantId = "tenant_global",
                assignedAt = "21-Jun-2026"
            ),
            UserAssignment(
                id = "asg_4",
                roleId = "role_sales_executive",
                roleName = "Sales Executive",
                userId = "user_exec4",
                userName = "Amit Verma",
                userEmail = "amit.verma@tallybms.com",
                companyId = companyId,
                tenantId = "tenant_global",
                assignedAt = "21-Jun-2026"
            )
        )
    }

    private fun getSeedAuditLogs(companyId: String): List<SecurityAuditRecord> {
        return listOf(
            SecurityAuditRecord(
                id = "aud_1",
                companyId = companyId,
                tenantId = "tenant_global",
                timestamp = "23-Jun-2026 10:15 AM",
                actionType = "ROLE_UPDATED",
                actorId = "user_default",
                actorName = "Mrunmayee Erande",
                actorRole = "Super Admin",
                targetName = "Sales Executive",
                details = "Granted VIEW permission on INVENTORY godowns to support field deliveries."
            ),
            SecurityAuditRecord(
                id = "aud_2",
                companyId = companyId,
                tenantId = "tenant_global",
                timestamp = "22-Jun-2026 04:30 PM",
                actionType = "USER_ASSIGNED",
                actorId = "user_default",
                actorName = "Mrunmayee Erande",
                actorRole = "Super Admin",
                targetName = "Anjali Goel",
                details = "Assigned user to 'Accountant' role for ledger audits."
            ),
            SecurityAuditRecord(
                id = "aud_3",
                companyId = companyId,
                tenantId = "tenant_global",
                timestamp = "21-Jun-2026 09:00 AM",
                actionType = "ROLE_CREATED",
                actorId = "user_default",
                actorName = "Mrunmayee Erande",
                actorRole = "Super Admin",
                targetName = "Sales Executive",
                details = "Created and initialized default module permission vectors."
            )
        )
    }
}
