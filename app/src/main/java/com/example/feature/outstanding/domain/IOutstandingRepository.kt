package com.example.feature.outstanding.domain

interface IOutstandingRepository {
    suspend fun getDashboardData(): OutstandingDashboardData

    suspend fun getOutstandingItems(
        query: String?,
        type: String?, // "Receivable" or "Payable"
        group: String?, // e.g. "Sundry Debtors"
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<OutstandingItem>

    suspend fun getOutstandingDetail(partyId: String): OutstandingDetail

    suspend fun getAgeingReport(type: String): AgeingReportData

    suspend fun getRecoveryPipeline(): List<OutstandingItem>

    suspend fun updateRecoveryStatus(
        partyId: String,
        status: String,
        nextActionDate: String?,
        executive: String?
    ): Boolean
}
