package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.data.MockDataService.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val type: String,
    val party: String,
    val amount: String,
    val date: String,
    val status: String
) {
    fun toModel() = Transaction(id, type, party, amount, date, status)
}
