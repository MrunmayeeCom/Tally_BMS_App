package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.model.DashboardStat

@Entity(tableName = "dashboard_stats")
data class DashboardStatEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val label: String,
    val amount: String
) {
    fun toModel() = DashboardStat(label, amount)
}
