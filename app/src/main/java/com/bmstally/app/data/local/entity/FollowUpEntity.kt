package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.data.MockDataService.FollowUp

@Entity(tableName = "follow_ups")
data class FollowUpEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val party: String,
    val purpose: String,
    val date: String,
    val status: String,
    val notes: String
) {
    fun toModel() = FollowUp(id, party, purpose, date, status, notes)
}
