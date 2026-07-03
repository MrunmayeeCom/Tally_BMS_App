package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.data.MockDataService.AppUser

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean
) {
    fun toModel() = AppUser(id, name, email, role, active)
}
