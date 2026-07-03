package com.example.feature.outstanding.domain

data class OutstandingDashboardData(
    val totalReceivables: Double,
    val totalPayables: Double,
    val overdueAmount: Double,
    val collectionsThisMonth: Double
)

data class OutstandingItem(
    val partyId: String,
    val partyName: String,
    val type: String, // "Receivable" or "Payable"
    val groupName: String, // e.g., "Sundry Debtors", "Sundry Creditors"
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val lastContactDate: String?,
    val assignedExecutive: String?,
    val billingState: String? // "Open", "Overdue", "Under collection", "Paid"
)

data class OutstandingInvoice(
    val billId: String,
    val date: String,
    val amount: Double,
    val pendingAmount: Double,
    val dueDate: String,
    val daysOverdue: Int,
    val status: String // "Pending", "Overdue", "Partial"
)

data class OutstandingPayment(
    val paymentId: String,
    val date: String,
    val amount: Double,
    val mode: String // "Bank", "Cash", "Cheque"
)

data class RecoveryTrackData(
    val recoveryStatus: String, // "Normal", "Reminder Sent", "Promised", "Paid"
    val assignedExecutive: String,
    val followUpCount: Int,
    val lastContactDate: String?,
    val nextActionDate: String?
)

data class OutstandingDetail(
    val partyId: String,
    val partyName: String,
    val type: String, // "Receivable" or "Payable"
    val phone: String,
    val email: String,
    val ledgerClosingBalance: Double,
    val creditLimit: Double,
    val outstandingAmount: Double,
    val billingHistory: List<OutstandingInvoice>,
    val paymentHistory: List<OutstandingPayment>,
    val recoveryTracking: RecoveryTrackData
)

data class AgeingBucket(
    val range: String, // "Current", "0-30 Days", "31-60 Days", "61-90 Days", "90+ Days"
    val amount: Double,
    val count: Int,
    val percentage: Double
)

data class AgeingReportData(
    val totalAmount: Double,
    val buckets: List<AgeingBucket>,
    val criticalAccounts: List<OutstandingItem>
)
