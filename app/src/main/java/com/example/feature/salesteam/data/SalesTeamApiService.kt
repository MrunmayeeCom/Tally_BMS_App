package com.example.feature.salesteam.data

import retrofit2.Response
import retrofit2.http.*

// === DTO Classes ===

data class SalesTeamDashboardStatsDto(
    val activeUsersCount: Int,
    val todayVisitsCount: Int,
    val collectionsTodayAmount: Double,
    val openFollowUpsCount: Int,
    val pendingTasksCount: Int
)

data class SalesTeamMemberDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val status: String,
    val territory: String,
    val todayCheckInStatus: String,
    val todayCheckInTime: String? = null,
    val todayCheckOutTime: String? = null,
    val todayVisitCount: Int = 0,
    val performanceProgress: Float = 0.0f
)

data class PerformanceChartPointDto(
    val label: String,
    val target: Double,
    val achievement: Double
)

data class SalesRepPerformanceMetricsDto(
    val repId: String,
    val repName: String,
    val visitsCompleted: Int,
    val visitsTarget: Int,
    val collectionsAchieved: Double,
    val collectionsTarget: Double,
    val followUpsCompleted: Int,
    val followUpsTarget: Int,
    val recoverySuccessRate: Double,
    val dailyProgress: List<PerformanceChartPointDto>,
    val weeklyProgress: List<PerformanceChartPointDto>,
    val monthlyProgress: List<PerformanceChartPointDto>
)

data class GPSLocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class CheckInRecordDto(
    val id: String,
    val userId: String,
    val userName: String,
    val customerId: String,
    val customerName: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val checkInLocation: GPSLocationDto,
    val checkOutLocation: GPSLocationDto?,
    val isLocationValid: Boolean,
    val remarks: String?
)

data class CustomerVisitDto(
    val id: String,
    val customerId: String,
    val customerName: String,
    val performedByUserId: String,
    val performedByUserName: String,
    val visitStartTime: String,
    val visitEndTime: String?,
    val notes: String,
    val outcomeBadge: String,
    val collectedAmount: Double,
    val nextActionPlanned: String?,
    val nextVisitDate: String?,
    val location: GPSLocationDto
)

data class SalesTeamActivityEventDto(
    val id: String,
    val type: String,
    val timestamp: String,
    val performedByUserName: String,
    val description: String,
    val amount: Double? = null,
    val customerName: String? = null
)

// === Request Payloads ===

data class TeamCheckInRequest(
    val userId: String,
    val userName: String,
    val customerId: String,
    val customerName: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val remarks: String?
)

data class TeamCheckOutRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val remarks: String?
)

data class TeamCreateVisitRequest(
    val customerId: String,
    val customerName: String,
    val userId: String,
    val userName: String,
    val notes: String,
    val outcomeBadge: String,
    val collectedAmount: Double,
    val nextActionPlanned: String?,
    val nextVisitDate: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class MemberStatusUpdateRequest(
    val status: String,
    val territory: String,
    val role: String
)

// === Retrofit API Contract ===

interface SalesTeamApiService {

    @GET("/api/v1/sales-team/dashboard")
    suspend fun getDashboardStats(
        @Query("companyId") companyId: String
    ): Response<SalesTeamDashboardStatsDto>

    @GET("/api/v1/sales-team/members")
    suspend fun getTeamMembers(
        @Query("companyId") companyId: String,
        @Query("query") query: String?,
        @Query("territory") territory: String?,
        @Query("role") role: String?
    ): Response<List<SalesTeamMemberDto>>

    @GET("/api/v1/sales-team/members/{userId}")
    suspend fun getMemberDetail(
        @Path("userId") userId: String
    ): Response<SalesTeamMemberDto>

    @GET("/api/v1/sales-team/targets")
    suspend fun getMemberPerformance(
        @Query("userId") userId: String,
        @Query("timeframe") timeframe: String
    ): Response<SalesRepPerformanceMetricsDto>

    @POST("/api/v1/sales-team/check-in")
    suspend fun performCheckIn(
        @Body request: TeamCheckInRequest
    ): Response<CheckInRecordDto>

    @PUT("/api/v1/sales-team/check-in/{id}/check-out")
    suspend fun performCheckOut(
        @Path("id") checkInId: String,
        @Body request: TeamCheckOutRequest
    ): Response<CheckInRecordDto>

    @GET("/api/v1/sales-team/check-ins/current")
    suspend fun getCurrentCheckIn(
        @Query("userId") userId: String
    ): Response<CheckInRecordDto>

    @GET("/api/v1/sales-team/check-ins")
    suspend fun getCheckInHistory(
        @Query("userId") userId: String?,
        @Query("companyId") companyId: String
    ): Response<List<CheckInRecordDto>>

    @GET("/api/v1/sales-team/visits")
    suspend fun getVisitHistory(
        @Query("companyId") companyId: String,
        @Query("customerId") customerId: String?,
        @Query("userId") userId: String?
    ): Response<List<CustomerVisitDto>>

    @POST("/api/v1/sales-team/visits")
    suspend fun createCustomerVisit(
        @Body request: TeamCreateVisitRequest
    ): Response<CustomerVisitDto>

    @GET("/api/v1/sales-team/activities")
    suspend fun getActivityFeed(
        @Query("companyId") companyId: String,
        @Query("filterType") filterType: String?
    ): Response<List<SalesTeamActivityEventDto>>

    @PUT("/api/v1/sales-team/members/{userId}/status")
    suspend fun updateMemberStatus(
        @Path("userId") userId: String,
        @Body request: MemberStatusUpdateRequest
    ): Response<Boolean>
}
