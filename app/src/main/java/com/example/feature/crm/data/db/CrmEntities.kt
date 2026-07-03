package com.example.feature.crm.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crm_customers")
data class LocalCustomer(
    @PrimaryKey val id: String,
    val name: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val statusBadge: String,
    val stateCode: String,
    val salesRepName: String,
    // Contact Info
    val phone: String,
    val email: String,
    val address: String,
    val keyContactPerson: String,
    // Ledger Summary
    val creditLimit: Double,
    val openingBalance: Double,
    val closingBalance: Double,
    val lastPaymentAmount: Double,
    val lastPaymentDate: String,
    // Outstanding Summary detail
    val overdue30Days: Double,
    val overdue60Days: Double,
    val overdue90Days: Double,
    val overdueOver90Days: Double,
    // Last Transaction Info
    val lastTransactionId: String,
    val lastTransactionDate: String,
    val lastTransactionType: String,
    val lastTransactionAmount: Double,
    val lastTransactionStatus: String
)

@Entity(tableName = "crm_customer_timeline")
data class LocalCustomerTimeline(
    @PrimaryKey val id: String,
    val customerId: String,
    val type: String,
    val date: String,
    val description: String,
    val performedBy: String,
    val outcomeStatus: String?
)

@Entity(tableName = "crm_customer_interactions")
data class LocalCustomerInteraction(
    @PrimaryKey val id: String,
    val customerId: String,
    val interactionType: String,
    val interactionDate: String,
    val details: String,
    val outcome: String,
    val pendingSync: Boolean = false
)
