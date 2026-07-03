package com.example.feature.reports.data

import com.example.feature.reports.domain.*
import retrofit2.Response
import retrofit2.http.*

// ==========================================
// RETROFIT API CONTRACT
// ==========================================
interface ReportsApiService {
    @GET("api/v1/reports/executive")
    suspend fun getExecutiveDashboard(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<ExecutiveDashboardReportDto>

    @GET("api/v1/reports/outstanding")
    suspend fun getOutstandingReport(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("customerId") customerId: String?,
        @Query("territory") territory: String?
    ): Response<OutstandingReportDto>

    @GET("api/v1/reports/reminders")
    suspend fun getReminderEffectiveness(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<ReminderEffectivenessReportDto>

    @GET("api/v1/reports/followups")
    suspend fun getFollowUpReport(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("userId") userId: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<FollowUpReportDto>

    @GET("api/v1/reports/salesteam")
    suspend fun getSalesTeamReport(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("territory") territory: String?,
        @Query("userId") userId: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Response<SalesTeamReportDto>

    @POST("api/v1/reports/custom")
    suspend fun runCustomReport(
        @Header("X-Tenant-Id") tenantId: String,
        @Body request: CustomReportSpecDto
    ): Response<CustomReportSpecDto>

    @GET("api/v1/reports/exports")
    suspend fun getExportHistory(
        @Header("X-Tenant-Id") tenantId: String
    ): Response<List<ExportRecordDto>>

    @POST("api/v1/reports/export/trigger")
    suspend fun triggerExport(
        @Header("X-Tenant-Id") tenantId: String,
        @Body request: TriggerExportRequestDto
    ): Response<ExportRecordDto>
}

// ==========================================
// DTO DEFINITIONS & MAPPING
// ==========================================

data class ExecutiveDashboardReportDto(
    val totalRevenue: Double,
    val totalOutstanding: Double,
    val collectionsAchieved: Double,
    val recoverySuccessRate: Double,
    val teamPerformance: List<RepPerformanceSummaryDto>
) {
    fun toDomain() = ExecutiveDashboardReport(
        totalRevenue = totalRevenue,
        totalOutstanding = totalOutstanding,
        collectionsAchieved = collectionsAchieved,
        recoverySuccessRate = recoverySuccessRate,
        teamPerformance = teamPerformance.map { it.toDomain() }
    )
}

data class RepPerformanceSummaryDto(
    val repId: String,
    val repName: String,
    val role: String,
    val visitsCount: Int,
    val collectionsAmount: Double,
    val recoveryRate: Double
) {
    fun toDomain() = RepPerformanceSummary(
        repId = repId,
        repName = repName,
        role = role,
        visitsCount = visitsCount,
        collectionsAmount = collectionsAmount,
        recoveryRate = recoveryRate
    )
}

data class OutstandingReportDto(
    val customerOutstanding: List<CustomerOutstandingSummaryDto>,
    val ageingBuckets: List<AgeingBucketDto>,
    val recoveryStatusCounts: Map<String, Int>,
    val collectionForecast: List<ForecastPointDto>
) {
    fun toDomain() = OutstandingReport(
        customerOutstanding = customerOutstanding.map { it.toDomain() },
        ageingBuckets = ageingBuckets.map { it.toDomain() },
        recoveryStatusCounts = recoveryStatusCounts,
        collectionForecast = collectionForecast.map { it.toDomain() }
    )
}

data class CustomerOutstandingSummaryDto(
    val customerId: String,
    val customerName: String,
    val totalOutstanding: Double,
    val overdueAmount: Double,
    val territory: String,
    val lastActiveDate: String
) {
    fun toDomain() = CustomerOutstandingSummary(
        customerId = customerId,
        customerName = customerName,
        totalOutstanding = totalOutstanding,
        overdueAmount = overdueAmount,
        territory = territory,
        lastActiveDate = lastActiveDate
    )
}

data class AgeingBucketDto(
    val bucketLabel: String,
    val amount: Double,
    val percentage: Double
) {
    fun toDomain() = AgeingBucket(
        bucketLabel = bucketLabel,
        amount = amount,
        percentage = percentage
    )
}

data class ForecastPointDto(
    val timeframe: String,
    val projectedAmount: Double,
    val probabilityIndex: Double
) {
    fun toDomain() = ForecastPoint(
        timeframe = timeframe,
        projectedAmount = projectedAmount,
        probabilityIndex = probabilityIndex
    )
}

data class ReminderEffectivenessReportDto(
    val sentCount: Int,
    val deliveredCount: Int,
    val failedCount: Int,
    val convertedCount: Int,
    val conversionRate: Double,
    val totalCollectionRealized: Double,
    val dailyActivity: List<ReminderActivityPointDto>
) {
    fun toDomain() = ReminderEffectivenessReport(
        sentCount = sentCount,
        deliveredCount = deliveredCount,
        failedCount = failedCount,
        convertedCount = convertedCount,
        conversionRate = conversionRate,
        totalCollectionRealized = totalCollectionRealized,
        dailyActivity = dailyActivity.map { it.toDomain() }
    )
}

data class ReminderActivityPointDto(
    val date: String,
    val sent: Int,
    val delivered: Int,
    val paymentsReceivedCount: Int,
    val paymentsAmount: Double
) {
    fun toDomain() = ReminderActivityPoint(
        date = date,
        sent = sent,
        delivered = delivered,
        paymentsReceivedCount = paymentsReceivedCount,
        paymentsAmount = paymentsAmount
    )
}

data class FollowUpReportDto(
    val openCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
    val teamFollowUpEfficiency: List<UserFollowUpPerformanceDto>
) {
    fun toDomain() = FollowUpReport(
        openCount = openCount,
        completedCount = completedCount,
        overdueCount = overdueCount,
        teamFollowUpEfficiency = teamFollowUpEfficiency.map { it.toDomain() }
    )
}

data class UserFollowUpPerformanceDto(
    val userId: String,
    val userName: String,
    val assignedCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
    val completionRate: Double
) {
    fun toDomain() = UserFollowUpPerformance(
        userId = userId,
        userName = userName,
        assignedCount = assignedCount,
        completedCount = completedCount,
        overdueCount = overdueCount,
        completionRate = completionRate
    )
}

data class SalesTeamReportDto(
    val visitsCompleted: Int,
    val collectionsAchieved: Double,
    val followUpsCompleted: Int,
    val territoryPerformance: List<TerritoryPerformancePointDto>
) {
    fun toDomain() = SalesTeamReport(
        visitsCompleted = visitsCompleted,
        collectionsAchieved = collectionsAchieved,
        followUpsCompleted = followUpsCompleted,
        territoryPerformance = territoryPerformance.map { it.toDomain() }
    )
}

data class TerritoryPerformancePointDto(
    val territory: String,
    val activeForceCount: Int,
    val visitsCount: Int,
    val collectionAmount: Double,
    val targetAchievementsPercent: Double
) {
    fun toDomain() = TerritoryPerformancePoint(
        territory = territory,
        activeForceCount = activeForceCount,
        visitsCount = visitsCount,
        collectionAmount = collectionAmount,
        targetAchievementsPercent = targetAchievementsPercent
    )
}

data class CustomReportSpecDto(
    val reportId: String = "",
    val title: String,
    val sourceModule: String,
    val measureColumns: List<String>,
    val startDate: String,
    val endDate: String,
    val customerId: String? = null,
    val userId: String? = null,
    val territory: String? = null,
    val headers: List<String> = emptyList(),
    val resultRows: List<List<String>> = emptyList()
) {
    fun toDomain() = CustomReportSpec(
        reportId = reportId,
        title = title,
        sourceModule = sourceModule,
        measureColumns = measureColumns,
        startDate = startDate,
        endDate = endDate,
        customerId = customerId,
        userId = userId,
        territory = territory,
        headers = headers,
        resultRows = resultRows
    )

    companion object {
        fun fromDomain(domain: CustomReportSpec) = CustomReportSpecDto(
            reportId = domain.reportId,
            title = domain.title,
            sourceModule = domain.sourceModule,
            measureColumns = domain.measureColumns,
            startDate = domain.startDate,
            endDate = domain.endDate,
            customerId = domain.customerId,
            userId = domain.userId,
            territory = domain.territory,
            headers = domain.headers,
            resultRows = domain.resultRows
        )
    }
}

data class ExportRecordDto(
    val id: String,
    val fileName: String,
    val format: String,
    val sizeBytes: Long,
    val scopeText: String,
    val timestamp: String,
    val status: String,
    val downloadUrl: String
) {
    fun toDomain() = ExportRecord(
        id = id,
        fileName = fileName,
        format = format,
        sizeBytes = sizeBytes,
        scopeText = scopeText,
        timestamp = timestamp,
        status = status,
        downloadUrl = downloadUrl
    )
}

data class TriggerExportRequestDto(
    val reportType: String,
    val format: String,
    val filters: Map<String, String>
)
