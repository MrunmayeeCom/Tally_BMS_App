package com.example.feature.dashboard.data

import com.example.core.network.ApiService
import com.example.feature.dashboard.domain.*

class DashboardRepositoryImpl(
    private val apiService: ApiService
) : IDashboardRepository {

    override suspend fun getDashboardData(companyGuid: String): DashboardData {
        val summaryRes = apiService.getDashboardSummary(companyGuid)
        val incomeExpenseRes = apiService.getDashboardIncomeExpense(companyGuid)
        val billsRes = apiService.getDashboardBills(companyGuid)

        if (!summaryRes.isSuccessful) throw Exception("Dashboard summary API failed: ${summaryRes.code()}")
        if (!incomeExpenseRes.isSuccessful) throw Exception("Income/expense API failed: ${incomeExpenseRes.code()}")
        if (!billsRes.isSuccessful) throw Exception("Bills API failed: ${billsRes.code()}")

        val summaryDto = summaryRes.body()!!
        val incomeExpenseDto = incomeExpenseRes.body()!!
        val billsDto = billsRes.body()!!

        return DashboardData(
            summary = DashboardSummary(
                receivables = summaryDto.receivables,
                payables = summaryDto.payables,
                pendingBills = summaryDto.pending_bills,
                clearedBills = summaryDto.cleared_bills
            ),
            incomeExpense = incomeExpenseDto.map {
                IncomeExpenseEntry(
                    monthName = it.month,
                    income = it.income,
                    expense = it.expense
                )
            },
            outstandingTrends = billsDto.map {
                OutstandingTrend(
                    partyName = it.ledger_name,
                    amount = it.pending_amount,
                    dueDate = it.due_date,
                    daysLeft = calculateDaysLeft(it.due_date)
                )
            },
            upcomingDues = billsDto.map {
                OutstandingTrend(
                    partyName = it.ledger_name,
                    amount = it.pending_amount,
                    dueDate = it.due_date,
                    daysLeft = calculateDaysLeft(it.due_date)
                )
            }
        )
    }

    private fun calculateDaysLeft(dueDate: String): Int {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val due = sdf.parse(dueDate) ?: return 0
            val diff = due.time - System.currentTimeMillis()
            kotlin.math.max(0, (diff / 86400000).toInt())
        } catch (_: Exception) {
            0
        }
    }
}
