package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.model.OutstandingItem

@Entity(tableName = "outstanding_items")
data class OutstandingItemEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val partyName: String,
    val creditInfo: String,
    val amount: String,
    val isCredit: Boolean,
    val meta: String,
    val paymentInfo: String
) {
    fun toModel() = OutstandingItem(partyName, creditInfo, amount, isCredit, meta, paymentInfo)
}
