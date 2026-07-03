package com.example.feature.reports.domain

import kotlinx.coroutines.flow.Flow

interface IReportsRepository {
    fun getExecutiveDashboardReport(
        companyId: String,
        startDate: String?,
        endDate: String?
    ): Flow<Result<ExecutiveDashboardReport>>

    fun getOutstandingReport(
        companyId: String,
        customerId: String?,
        territory: String?
    ): Flow<Result<OutstandingReport>>

    fun getReminderEffectivenessReport(
        companyId: String,
        startDate: String?,
        endDate: String?
    ): Flow<Result<ReminderEffectivenessReport>>

    fun getFollowUpReport(
        companyId: String,
        userId: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Result<FollowUpReport>>

    fun getSalesTeamReport(
        companyId: String,
        territory: String?,
        userId: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Result<SalesTeamReport>>

    fun runCustomReport(
        companyId: String,
        spec: CustomReportSpec
    ): Flow<Result<CustomReportSpec>>

    fun getExportHistory(
        companyId: String
    ): Flow<Result<List<ExportRecord>>>

    suspend fun triggerExport(
        companyId: String,
        reportType: String,
        format: String, // "PDF", "XLSX", "CSV"
        filters: Map<String, String>
    ): Result<ExportRecord>
}
