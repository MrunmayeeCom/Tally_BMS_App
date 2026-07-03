package com.example.feature.security.data.api

import com.example.feature.security.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SecurityApiService {
    @GET("api/v1/security/roles")
    suspend fun getRoles(
        @Query("companyId") companyId: String
    ): Response<List<SecurityRoleDto>>

    @POST("api/v1/security/roles")
    suspend fun createRole(
        @Body role: SecurityRoleDto
    ): Response<SecurityRoleDto>

    @PUT("api/v1/security/roles/{id}")
    suspend fun updateRole(
        @Path("id") id: String,
        @Body role: SecurityRoleDto
    ): Response<SecurityRoleDto>

    @DELETE("api/v1/security/roles/{id}")
    suspend fun deleteRole(
        @Path("id") id: String
    ): Response<Unit>

    @GET("api/v1/security/assignments")
    suspend fun getAssignments(
        @Query("companyId") companyId: String
    ): Response<List<UserAssignmentDto>>

    @POST("api/v1/security/assignments")
    suspend fun createAssignment(
        @Body assignment: UserAssignmentDto
    ): Response<UserAssignmentDto>

    @DELETE("api/v1/security/assignments/{id}")
    suspend fun deleteAssignment(
        @Path("id") id: String
    ): Response<Unit>

    @GET("api/v1/security/audit")
    suspend fun getAuditLogs(
        @Query("companyId") companyId: String
    ): Response<List<SecurityAuditRecordDto>>

    @POST("api/v1/security/audit")
    suspend fun createAuditLog(
        @Body record: SecurityAuditRecordDto
    ): Response<SecurityAuditRecordDto>
}
