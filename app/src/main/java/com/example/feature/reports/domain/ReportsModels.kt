package com.example.feature.reports.domain

// ==========================================
// 1. EXECUTIVE DASHBOARD REPORT MODEL
// ==========================================
data class ExecutiveDashboardReport(
    val totalRevenue: Double,
    val totalOutstanding: Double,
    val collectionsAchieved: Double,
    val recoverySuccessRate: Double, // in percentage, e.g. 78.5
    val teamPerformance: List<RepPerformanceSummary>
)

data class RepPerformanceSummary(
    val repId: String,
    val repName: String,
    val role: String,
    val visitsCount: Int,
    val collectionsAmount: Double,
    val recoveryRate: Double
)

// ==========================================
// 2. OUTSTANDING REPORTS MODELS
// ==========================================
data class OutstandingReport(
    val customerOutstanding: List<CustomerOutstandingSummary>,
    val ageingBuckets: List<AgeingBucket>,
    val recoveryStatusCounts: Map<String, Int>,
    val collectionForecast: List<ForecastPoint>
)

data class CustomerOutstandingSummary(
    val customerId: String,
    val customerName: String,
    val totalOutstanding: Double,
    val overdueAmount: Double,
    val territory: String,
    val lastActiveDate: String
)

data class AgeingBucket(
    val bucketLabel: String, // e.g., "0-30 Days", "31-60 Days", etc.
    val amount: Double,
    val percentage: Double
)

data class ForecastPoint(
    val timeframe: String, // Week 1, Week 2, etc. or Date
    val projectedAmount: Double,
    val probabilityIndex: Double // 0.0 to 1.0 confidence score
)

// ==========================================
// 3. REMINDER EFFECTIVENESS STATISTICS
// ==========================================
data class ReminderEffectivenessReport(
    val sentCount: Int,
    val deliveredCount: Int,
    val failedCount: Int,
    val convertedCount: Int, // Converted to payment
    val conversionRate: Double, // in %
    val totalCollectionRealized: Double,
    val dailyActivity: List<ReminderActivityPoint>
)

data class ReminderActivityPoint(
    val date: String,
    val sent: Int,
    val delivered: Int,
    val paymentsReceivedCount: Int,
    val paymentsAmount: Double
)

// ==========================================
// 4. FOLLOW-UP REPORT MODELS
// ==========================================
data class FollowUpReport(
    val openCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
    val teamFollowUpEfficiency: List<UserFollowUpPerformance>
)

data class UserFollowUpPerformance(
    val userId: String,
    val userName: String,
    val assignedCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
    val completionRate: Double // e.g. 85.0 %
)

// ==========================================
// 5. SALES TEAM REPORTS MODELS
// ==========================================
data class SalesTeamReport(
    val visitsCompleted: Int,
    val collectionsAchieved: Double,
    val followUpsCompleted: Int,
    val territoryPerformance: List<TerritoryPerformancePoint>
)

data class TerritoryPerformancePoint(
    val territory: String,
    val activeForceCount: Int,
    val visitsCount: Int,
    val collectionAmount: Double,
    val targetAchievementsPercent: Double
)

// ==========================================
// 6. CUSTOM REPORT BUILDER DATA MODELS
// ==========================================
data class CustomReportSpec(
    val reportId: String = "",
    val title: String,
    val sourceModule: String, // "Outstanding", "Collections", "Reminders", "Follow-Ups", "Sales"
    val measureColumns: List<String>,
    val startDate: String,
    val endDate: String,
    val customerId: String? = null,
    val userId: String? = null,
    val territory: String? = null,
    val headers: List<String> = emptyList(),
    val resultRows: List<List<String>> = emptyList()
)

// ==========================================
// 7. EXPORT DATA CENTER MODELS
// ==========================================
data class ExportRecord(
    val id: String,
    val fileName: String,
    val format: String, // "PDF", "XLSX", "CSV"
    val sizeBytes: Long,
    val scopeText: String,
    val timestamp: String,
    val status: String, // "Completed", "Processing", "Failed"
    val downloadUrl: String
)
