package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

data class VoucherPosting(
    val ledgerId: String,
    val ledgerName: String,
    val isDebit: Boolean,
    val amount: BigDecimal
)

@Entity(tableName = "ledgers")
data class LocalLedger(
    @PrimaryKey val ledgerId: String,
    val companyId: String,
    val tallyGuid: String,
    val name: String,
    val group: String,
    val balance: BigDecimal,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "vouchers")
data class LocalVoucher(
    @PrimaryKey val voucherId: String,
    val companyId: String,
    val type: String,
    val partyLedgerId: String,
    val partyName: String,
    val amount: BigDecimal,
    val date: String,
    val source: String,
    val pendingSync: Boolean = false,
    val postings: List<VoucherPosting> = emptyList()
)

@Entity(tableName = "bills")
data class LocalBill(
    @PrimaryKey val billId: String,
    val companyId: String,
    val partyId: String,
    val partyName: String,
    val voucherId: String,
    val amount: BigDecimal,
    val dueDate: String,
    val status: String,
    val recoveryState: String = "Pending",
    val promisedDate: String? = null
)

@Entity(tableName = "check_ins")
data class LocalCheckIn(
    @PrimaryKey val checkInId: String,
    val customerUserId: String,
    val latitude: Double,
    val longitude: Double,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val photoUrl: String?,
    val notes: String?,
    val pendingSync: Boolean = false
)

@Entity(tableName = "follow_ups")
data class LocalFollowUp(
    @PrimaryKey val followUpId: String,
    val customerId: String,
    val customerName: String,
    val assignedTo: String,
    val dueDate: String,
    val status: String,
    val outcome: String?,
    val notes: String?,
    val pendingSync: Boolean = false
)
