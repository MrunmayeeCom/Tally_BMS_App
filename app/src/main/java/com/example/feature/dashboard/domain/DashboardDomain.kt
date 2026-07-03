package com.example.feature.dashboard.domain

data class DashboardSummary(
    val receivables: Double,
    val payables: Double,
    val pendingBills: Int,
    val clearedBills: Int
)

data class IncomeExpenseEntry(
    val monthName: String,
    val income: Double,
    val expense: Double
)

data class OutstandingTrend(
    val partyName: String,
    val amount: Double,
    val dueDate: String,
    val daysLeft: Int
)

data class DashboardData(
    val summary: DashboardSummary,
    val incomeExpense: List<IncomeExpenseEntry>,
    val outstandingTrends: List<OutstandingTrend>,
    val upcomingDues: List<OutstandingTrend>
)
