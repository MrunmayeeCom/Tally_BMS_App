package com.example.feature.crm.domain

data class CrmCustomer(
    val id: String,
    val name: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val statusBadge: String, // "Active", "Cr Overdue", "Dormant", "Risky"
    val stateCode: String,
    val salesRepName: String
)

data class ContactInfo(
    val phone: String,
    val email: String,
    val address: String,
    val keyContactPerson: String
)

data class LedgerSummary(
    val creditLimit: Double,
    val openingBalance: Double,
    val closingBalance: Double,
    val lastPaymentAmount: Double,
    val lastPaymentDate: String
)

data class OutstandingSummary(
    val totalOutstanding: Double,
    val overdue30Days: Double,
    val overdue60Days: Double,
    val overdue90Days: Double,
    val overdueOver90Days: Double
) {
    fun getOverdueTotal(): Double = overdue30Days + overdue60Days + overdue90Days + overdueOver90Days
}

data class Transaction(
    val id: String,
    val date: String,
    val type: String, // Invoice, Payment, Journal
    val amount: Double,
    val status: String
)

data class CrmCustomerDetail(
    val id: String,
    val name: String,
    val contactInfo: ContactInfo,
    val ledgerSummary: LedgerSummary,
    val outstandingSummary: OutstandingSummary,
    val lastTransaction: Transaction,
    val statusBadge: String,
    val salesRepName: String
)

data class CrmTimelineEvent(
    val id: String,
    val type: String, // "Follow-up", "Visit", "Reminder", "Note", "Collection"
    val date: String,
    val description: String,
    val performedBy: String,
    val outcomeStatus: String?
)
