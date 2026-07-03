package com.example.feature.user.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val assignedCompany: String,
    val assignedTerritory: String,
    val assignedRoles: List<String>,
    val status: String, // ACTIVE, INACTIVE, LOCKED
    val lastLogin: String?,
    val companyId: String,
    val tenantId: String
)

@JsonClass(generateAdapter = true)
data class UserActivityDto(
    val id: String,
    val userId: String,
    val timestamp: String,
    val eventName: String,
    val deviceInfo: String,
    val ipAddress: String,
    val location: String?,
    val status: String
)

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    val name: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val assignedCompany: String,
    val assignedTerritory: String,
    val assignedRoles: List<String>,
    val status: String,
    val companyId: String,
    val tenantId: String
)

@JsonClass(generateAdapter = true)
data class InviteUserRequest(
    val email: String,
    val phone: String,
    val assignedRoles: List<String>,
    val invitationType: String // "EMAIL" or "SMS"
)

@JsonClass(generateAdapter = true)
data class InviteUserResponse(
    val success: Boolean,
    val message: String,
    val invitationLink: String,
    val tempPassword: String
)

@JsonClass(generateAdapter = true)
data class StatusUpdateResponse(
    val success: Boolean,
    val message: String
)
