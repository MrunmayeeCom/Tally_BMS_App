package com.example.feature.user.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.feature.user.domain.models.UserStatus

@Entity(tableName = "users_profile")
data class LocalUser(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val assignedCompany: String,
    val assignedTerritory: String,
    val assignedRoles: String, // Stored as semicolon-separated role IDs
    val status: String, // ACTIVE, INACTIVE, LOCKED
    val lastLogin: String?,
    val companyId: String,
    val tenantId: String
)

@Entity(tableName = "users_activity")
data class LocalUserActivity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: String,
    val eventName: String,
    val deviceInfo: String,
    val ipAddress: String,
    val location: String?,
    val status: String
)

class UserTypeConverters {
    @TypeConverter
    fun fromRolesList(roles: List<String>?): String? {
        return roles?.joinToString(";")
    }

    @TypeConverter
    fun toRolesList(data: String?): List<String>? {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(";")
    }
}
