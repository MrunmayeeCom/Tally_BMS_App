package com.example.feature.salesteam.domain

import kotlinx.coroutines.flow.Flow

interface ISalesTeamRepository {
    suspend fun getDashboardStats(companyId: String): SalesTeamDashboardStats

    suspend fun getTeamMembers(companyId: String, query: String? = null, territory: String? = null, role: String? = null): List<SalesTeamMember>

    suspend fun getMemberDetail(userId: String): SalesTeamMember?

    suspend fun getMemberPerformance(userId: String, timeframe: String): SalesRepPerformanceMetrics

    suspend fun performCheckIn(
        userId: String,
        userName: String,
        customerId: String,
        customerName: String,
        latitude: Double,
        longitude: Double,
        address: String,
        remarks: String? = null
    ): CheckInRecord

    suspend fun performCheckOut(
        checkInId: String,
        latitude: Double,
        longitude: Double,
        address: String,
        remarks: String? = null
    ): CheckInRecord

    suspend fun getCurrentCheckIn(userId: String): CheckInRecord?

    suspend fun getVisitHistory(companyId: String, customerId: String? = null, userId: String? = null): List<CustomerVisit>

    suspend fun createCustomerVisit(
        customerId: String,
        customerName: String,
        userId: String,
        userName: String,
        notes: String,
        outcomeBadge: String,
        collectedAmount: Double,
        nextActionPlanned: String?,
        nextVisitDate: String?,
        latitude: Double,
        longitude: Double,
        address: String
    ): CustomerVisit

    suspend fun getActivityFeed(companyId: String, filterType: String? = null): List<SalesTeamActivityEvent>

    suspend fun getCheckInHistory(userId: String? = null, companyId: String): List<CheckInRecord>

    suspend fun updateUserStatusAndTerritory(userId: String, status: String, territory: String, role: String): Boolean
}
