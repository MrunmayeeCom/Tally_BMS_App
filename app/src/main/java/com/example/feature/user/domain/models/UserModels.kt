package com.example.feature.user.domain.models

import java.io.Serializable

enum class UserStatus {
    ACTIVE, INACTIVE, LOCKED
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val assignedCompany: String,
    val assignedTerritory: String,
    val assignedRoles: List<String>, // List of Role IDs
    val status: UserStatus,
    val lastLogin: String?,
    val companyId: String,
    val tenantId: String
) : Serializable

data class UserActivity(
    val id: String,
    val userId: String,
    val timestamp: String,
    val eventName: String, // e.g. "Login Success", "Login Failure", "Password Reset", "Locked", "Roles Modified"
    val deviceInfo: String,
    val ipAddress: String,
    val location: String?,
    val status: String // "SUCCESS", "FAILURE", "INFO"
) : Serializable
