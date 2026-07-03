package com.example.feature.reports.data

import android.util.Log
import com.example.feature.reports.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class ReportsRepositoryImpl(
    private val apiService: ReportsApiService
) : IReportsRepository {

    private val tag = "ReportsRepositoryImpl"

    // Local state for dynamically stored/generated custom reports and trigger history.
    private val mockCustomReports = mutableMapOf<String, CustomReportSpec>()
    private val mockExports = mutableListOf<ExportRecord>()

    init {
        // Pre-populate some historical export entries
        mockExports.add(
            ExportRecord(
                id = "ext_101",
                fileName = "Executive_Q2_Summary.pdf",
                format = "PDF",
                sizeBytes = 1424102,
                scopeText = "Revenue & Collections Q2 2026",
                timestamp = "2026-06-19 14:32:00",
                status = "Completed",
                downloadUrl = "https://example.com/exports/ext_101.pdf"
            )
        )
        mockExports.add(
            ExportRecord(
                id = "ext_102",
                fileName = "Outstanding_Ageing_June.xlsx",
                format = "XLSX",
                sizeBytes = 422010,
                scopeText = "All Clients Ageing Analysis",
                timestamp = "2026-06-18 09:15:00",
                status = "Completed",
                downloadUrl = "https://example.com/exports/ext_102.xlsx"
            )
        )
    }

    override fun getExecutiveDashboardReport(
        companyId: String,
        startDate: String?,
        endDate: String?
    ): Flow<Result<ExecutiveDashboardReport>> = flow {
        try {
            val response = apiService.getExecutiveDashboard(companyId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.success(generateMockExecutiveReport(startDate, endDate)))
            }
        } catch (e: Exception) {
            Log.e(tag, "getExecutiveDashboardReport API failed: ${e.message}, falling back to offline mock", e)
            emit(Result.success(generateMockExecutiveReport(startDate, endDate)))
        }
    }

    override fun getOutstandingReport(
        companyId: String,
        customerId: String?,
        territory: String?
    ): Flow<Result<OutstandingReport>> = flow {
        try {
            val response = apiService.getOutstandingReport(companyId, customerId, territory)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.success(generateMockOutstandingReport(customerId, territory)))
            }
        } catch (e: Exception) {
            Log.e(tag, "getOutstandingReport API failed: ${e.message}, falling back to offline mock", e)
            emit(Result.success(generateMockOutstandingReport(customerId, territory)))
        }
    }

    override fun getReminderEffectivenessReport(
        companyId: String,
        startDate: String?,
        endDate: String?
    ): Flow<Result<ReminderEffectivenessReport>> = flow {
        try {
            val response = apiService.getReminderEffectiveness(companyId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.success(generateMockReminderReport(startDate, endDate)))
            }
        } catch (e: Exception) {
            Log.e(tag, "getReminderEffectivenessReport API failed: ${e.message}, falling back to offline mock", e)
            emit(Result.success(generateMockReminderReport(startDate, endDate)))
        }
    }

    override fun getFollowUpReport(
        companyId: String,
        userId: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Result<FollowUpReport>> = flow {
        try {
            val response = apiService.getFollowUpReport(companyId, userId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.success(generateMockFollowUpReport(userId, startDate, endDate)))
            }
        } catch (e: Exception) {
            Log.e(tag, "getFollowUpReport API failed: ${e.message}, falling back to offline mock", e)
            emit(Result.success(generateMockFollowUpReport(userId, startDate, endDate)))
        }
    }

    override fun getSalesTeamReport(
        companyId: String,
        territory: String?,
        userId: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Result<SalesTeamReport>> = flow {
        try {
            val response = apiService.getSalesTeamReport(companyId, territory, userId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                emit(Result.success(generateMockSalesReport(territory, userId, startDate, endDate)))
            }
        } catch (e: Exception) {
            Log.e(tag, "getSalesTeamReport API failed: ${e.message}, falling back to offline mock", e)
            emit(Result.success(generateMockSalesReport(territory, userId, startDate, endDate)))
        }
    }

    override fun runCustomReport(
        companyId: String,
        spec: CustomReportSpec
    ): Flow<Result<CustomReportSpec>> = flow {
        try {
            val dto = CustomReportSpecDto.fromDomain(spec)
            val response = apiService.runCustomReport(companyId, dto)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.toDomain()))
            } else {
                val compiledSpec = evaluateCustomReportLocal(spec)
                mockCustomReports[compiledSpec.reportId] = compiledSpec
                emit(Result.success(compiledSpec))
            }
        } catch (e: Exception) {
            Log.e(tag, "runCustomReport API failed: ${e.message}, calculating offline", e)
            val compiledSpec = evaluateCustomReportLocal(spec)
            mockCustomReports[compiledSpec.reportId] = compiledSpec
            emit(Result.success(compiledSpec))
        }
    }

    override fun getExportHistory(companyId: String): Flow<Result<List<ExportRecord>>> = flow {
        try {
            val response = apiService.getExportHistory(companyId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.map { it.toDomain() }))
            } else {
                emit(Result.success(mockExports.toList()))
            }
        } catch (e: Exception) {
            Log.e(tag, "getExportHistory API failed: ${e.message}, using cached offline memory", e)
            emit(Result.success(mockExports.toList()))
        }
    }

    override suspend fun triggerExport(
        companyId: String,
        reportType: String,
        format: String,
        filters: Map<String, String>
    ): Result<ExportRecord> {
        return try {
            val requestDto = TriggerExportRequestDto(reportType, format, filters)
            val response = apiService.triggerExport(companyId, requestDto)
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!.toDomain()
                mockExports.add(0, result)
                Result.success(result)
            } else {
                val result = generateMockExport(reportType, format, filters)
                mockExports.add(0, result)
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(tag, "triggerExport API failed: ${e.message}, processing locally", e)
            val result = generateMockExport(reportType, format, filters)
            mockExports.add(0, result)
            Result.success(result)
        }
    }

    // ==========================================
    // OFFLINE FALLBACK MOCK BUILDERS
    // ==========================================

    private fun generateMockExecutiveReport(startDate: String?, endDate: String?): ExecutiveDashboardReport {
        val scalar = when {
            startDate != null && endDate != null -> 0.85
            else -> 1.0
        }
        return ExecutiveDashboardReport(
            totalRevenue = 2845000.0 * scalar,
            totalOutstanding = 1248000.0 * scalar,
            collectionsAchieved = 1597000.0 * scalar,
            recoverySuccessRate = 56.1 * scalar,
            teamPerformance = listOf(
                RepPerformanceSummary("u101", "Rajesh Kumar", "Sales Champion", 42, 450000.0 * scalar, 78.4),
                RepPerformanceSummary("u102", "Anita Desai", "Territory Lead", 38, 380000.0 * scalar, 65.2),
                RepPerformanceSummary("u103", "Vikram Singh", "Customer Rep", 31, 290000.0 * scalar, 58.0),
                RepPerformanceSummary("u104", "Sunita Nair", "Retention Expert", 29, 320000.0 * scalar, 70.5),
                RepPerformanceSummary("u105", "Rahul Mehta", "Junior Representative", 22, 157000.0 * scalar, 42.1)
            )
        )
    }

    private fun generateMockOutstandingReport(customerId: String?, territory: String?): OutstandingReport {
        val lists = listOf(
            CustomerOutstandingSummary("cust_01", "Acme Corp Ltd", 450000.0, 150000.0, "North", "2026-06-18"),
            CustomerOutstandingSummary("cust_02", "Zeta Solutions", 320000.0, 80000.0, "South", "2026-06-15"),
            CustomerOutstandingSummary("cust_03", "Omega Distributors", 195000.0, 195000.0, "West", "2026-06-10"),
            CustomerOutstandingSummary("cust_04", "Matrix Info Systems", 150000.0, 10000.0, "East", "2026-06-19"),
            CustomerOutstandingSummary("cust_05", "Vardhaman Metals", 133000.0, 50000.0, "North", "2026-06-12")
        ).filter {
            (customerId == null || it.customerId == customerId) &&
            (territory == null || it.territory.equals(territory, ignoreCase = true))
        }

        val finalTotal = lists.sumOf { it.totalOutstanding }
        val ageing = listOf(
            AgeingBucket("0-30 Days", finalTotal * 0.45, 45.0),
            AgeingBucket("31-60 Days", finalTotal * 0.30, 30.0),
            AgeingBucket("61-90 Days", finalTotal * 0.15, 15.0),
            AgeingBucket("90+ Days", finalTotal * 0.10, 10.0)
        )

        return OutstandingReport(
            customerOutstanding = lists,
            ageingBuckets = ageing,
            recoveryStatusCounts = mapOf(
                "Promised" to 12,
                "Reminded" to 22,
                "Disputed" to 3,
                "Unassigned" to 8
            ),
            collectionForecast = listOf(
                ForecastPoint("Week 1 (June 21-27)", finalTotal * 0.35, 0.90),
                ForecastPoint("Week 2 (June 28-July 04)", finalTotal * 0.25, 0.75),
                ForecastPoint("Week 3 (July 05-11)", finalTotal * 0.20, 0.60),
                ForecastPoint("Week 4 (July 12-18)", finalTotal * 0.15, 0.45)
            )
        )
    }

    private fun generateMockReminderReport(startDate: String?, endDate: String?): ReminderEffectivenessReport {
        return ReminderEffectivenessReport(
            sentCount = 384,
            deliveredCount = 372,
            failedCount = 12,
            convertedCount = 145,
            conversionRate = 37.76,
            totalCollectionRealized = 842000.0,
            dailyActivity = listOf(
                ReminderActivityPoint("2026-06-14", 45, 44, 18, 92000.0),
                ReminderActivityPoint("2026-06-15", 52, 51, 22, 134000.0),
                ReminderActivityPoint("2026-06-16", 61, 59, 21, 115000.0),
                ReminderActivityPoint("2026-06-17", 49, 47, 19, 98000.0),
                ReminderActivityPoint("2026-06-18", 78, 75, 31, 187000.0),
                ReminderActivityPoint("2026-06-19", 99, 96, 34, 216000.0)
            )
        )
    }

    private fun generateMockFollowUpReport(userId: String?, startDate: String?, endDate: String?): FollowUpReport {
        val fullList = listOf(
            UserFollowUpPerformance("u101", "Rajesh Kumar", 50, 42, 4, 84.0),
            UserFollowUpPerformance("u102", "Anita Desai", 45, 38, 2, 84.4),
            UserFollowUpPerformance("u103", "Vikram Singh", 40, 31, 6, 77.5),
            UserFollowUpPerformance("u104", "Sunita Nair", 35, 29, 3, 82.8),
            UserFollowUpPerformance("u105", "Rahul Mehta", 30, 22, 5, 73.3)
        ).filter { userId == null || it.userId == userId }

        return FollowUpReport(
            openCount = fullList.sumOf { it.assignedCount - it.completedCount },
            completedCount = fullList.sumOf { it.completedCount },
            overdueCount = fullList.sumOf { it.overdueCount },
            teamFollowUpEfficiency = fullList
        )
    }

    private fun generateMockSalesReport(territory: String?, userId: String?, startDate: String?, endDate: String?): SalesTeamReport {
        val territories = listOf(
            TerritoryPerformancePoint("North", 3, 75, 607000.0, 92.5),
            TerritoryPerformancePoint("South", 2, 50, 430000.0, 85.0),
            TerritoryPerformancePoint("West", 1, 31, 290000.0, 78.0),
            TerritoryPerformancePoint("East", 1, 29, 270000.0, 81.5)
        ).filter { territory == null || it.territory.equals(territory, ignoreCase = true) }

        return SalesTeamReport(
            visitsCompleted = territories.sumOf { it.visitsCount },
            collectionsAchieved = territories.sumOf { it.collectionAmount },
            followUpsCompleted = 162,
            territoryPerformance = territories
        )
    }

    private fun evaluateCustomReportLocal(spec: CustomReportSpec): CustomReportSpec {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val computedId = "rpt_" + UUID.randomUUID().toString().take(6)
        
        val headers = spec.measureColumns.toMutableList().apply {
            add(0, "Record Date")
            add(0, "Customer Info")
        }

        val rows = when (spec.sourceModule) {
            "Outstanding" -> listOf(
                listOf("Acme Corp Ltd", format.format(Date()), "₹4,50,000", "₹1,50,000", "North Zone"),
                listOf("Zeta Solutions", format.format(Date()), "₹3,20,000", "₹80,000", "South Zone"),
                listOf("Omega Distributors", format.format(Date()), "₹1,95,000", "₹1,95,000", "West Zone")
            )
            "Collections" -> listOf(
                listOf("Matrix Systems", format.format(Date()), "₹1,50,000", "₹10,000", "East Zone"),
                listOf("Vardhaman Metals", format.format(Date()), "₹1,33,000", "₹50,000", "North Zone")
            )
            else -> listOf(
                listOf("Dynamic Corp", format.format(Date()), "₹1,20,000", "15 Units", "HQ Segment"),
                listOf("Stellar Tech", format.format(Date()), "₹3,50,000", "42 Units", "Retail Segment")
            )
        }

        return spec.copy(
            reportId = computedId,
            headers = headers,
            resultRows = rows.map { row -> row.take(headers.size) }
        )
    }

    private fun generateMockExport(
        reportType: String,
        format: String,
        filters: Map<String, String>
    ): ExportRecord {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val id = "ext_" + UUID.randomUUID().toString().take(6)
        val name = "${reportType}_Export_${id}.${format.lowercase()}"
        val size = (120000..1800000).random().toLong()
        val scopes = filters.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            .ifEmpty { "Full Dataset Master" }

        return ExportRecord(
            id = id,
            fileName = name,
            format = format,
            sizeBytes = size,
            scopeText = "Scope: $scopes",
            timestamp = dateFormat.format(Date()),
            status = "Completed",
            downloadUrl = "https://example.com/exports/$name"
        )
    }
}
