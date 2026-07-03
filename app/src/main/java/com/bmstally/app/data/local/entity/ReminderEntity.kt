package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.data.MockDataService.Reminder

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val title: String,
    val description: String,
    val time: String,
    val enabled: Boolean
) {
    fun toModel() = Reminder(id, title, description, time, enabled)
}
