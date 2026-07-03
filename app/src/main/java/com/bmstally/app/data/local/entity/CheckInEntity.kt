package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.data.MockDataService.CheckIn

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val user: String,
    val time: String,
    val location: String,
    val status: String
) {
    fun toModel() = CheckIn(id, user, time, location, status)
}
