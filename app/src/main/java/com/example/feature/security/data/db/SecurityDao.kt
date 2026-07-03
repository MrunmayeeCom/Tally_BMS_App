package com.example.feature.security.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityDao {
    // === Security Roles ===
    @Query("SELECT * FROM security_roles WHERE companyId = :companyId ORDER BY name ASC")
    fun getRolesByCompany(companyId: String): Flow<List<LocalSecurityRole>>

    @Query("SELECT * FROM security_roles WHERE id = :id")
    suspend fun getRoleById(id: String): LocalSecurityRole?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<LocalSecurityRole>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: LocalSecurityRole)

    @Delete
    suspend fun deleteRole(role: LocalSecurityRole)

    // === User Role Assignments ===
    @Query("SELECT * FROM user_role_assignments WHERE companyId = :companyId")
    fun getUserAssignmentsByCompany(companyId: String): Flow<List<LocalUserAssignment>>

    @Query("SELECT * FROM user_role_assignments WHERE userId = :userId AND companyId = :companyId")
    suspend fun getAssignmentsForUser(userId: String, companyId: String): List<LocalUserAssignment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAssignments(assignments: List<LocalUserAssignment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAssignment(assignment: LocalUserAssignment)

    @Query("DELETE FROM user_role_assignments WHERE id = :assignmentId")
    suspend fun deleteUserAssignment(assignmentId: String)

    @Query("DELETE FROM user_role_assignments WHERE roleId = :roleId")
    suspend fun deleteAssignmentsByRole(roleId: String)

    // === Security Audit Tracking ===
    @Query("SELECT * FROM security_audit_records WHERE companyId = :companyId ORDER BY timestamp DESC")
    fun getAuditRecordsByCompany(companyId: String): Flow<List<LocalSecurityAuditRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditRecord(record: LocalSecurityAuditRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditRecords(records: List<LocalSecurityAuditRecord>)
}
