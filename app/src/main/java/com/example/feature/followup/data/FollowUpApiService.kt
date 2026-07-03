package com.example.feature.followup.data

import retrofit2.Response
import retrofit2.http.*

data class FollowUpDashboardStatsDto(
    val totalOpen: Int,
    val dueToday: Int,
    val overdue: Int,
    val completed: Int
)

data class FollowUpDto(
    val id: String,
    val customerId: String,
    val customerName: String,
    val type: String,
    val notes: String,
    val priority: String,
    val dueDate: String,
    val assignedUserId: String,
    val assignedUserName: String,
    val status: String,
    val outcome: String?,
    val promisedAmount: Double?,
    val promisedDate: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateFollowUpRequestDto(
    val customerId: String,
    val customerName: String,
    val type: String,
    val notes: String,
    val priority: String,
    val dueDate: String,
    val assignedUserId: String,
    val assignedUserName: String
)

data class UpdateFollowUpStatusRequestDto(
    val status: String,
    val outcome: String?,
    val promisedAmount: Double? = null,
    val promisedDate: String? = null
)

data class TimelineNodeDto(
    val id: String,
    val date: String,
    val title: String,
    val description: String,
    val author: String,
    val statusColor: String?
)

data class ActivityLogItemDto(
    val id: String,
    val timestamp: String,
    val action: String,
    val details: String,
    val operatorName: String
)

data class FollowUpDetailDto(
    val followUp: FollowUpDto,
    val customerInfo: ContactInfoDto,
    val outstandingSummary: OutstandingSummaryDto,
    val interactionHistory: List<FollowUpDto>,
    val notesTimeline: List<TimelineNodeDto>,
    val activityLog: List<ActivityLogItemDto>
)

data class ContactInfoDto(
    val phone: String,
    val email: String,
    val address: String,
    val keyContactPerson: String
)

data class OutstandingSummaryDto(
    val totalOutstanding: Double,
    val overdue30Days: Double,
    val overdue60Days: Double,
    val overdue90Days: Double,
    val overdueOver90Days: Double
)

interface FollowUpApiService {
    @GET("api/v1/followups/stats")
    suspend fun getDashboardStats(): Response<FollowUpDashboardStatsDto>

    @GET("api/v1/followups")
    suspend fun getFollowUps(
        @Query("query") query: String?,
        @Query("type") type: String?,
        @Query("priority") priority: String?,
        @Query("status") status: String?,
        @Query("assignedUser") assignedUser: String?,
        @Query("customer") customer: String?,
        @Query("sort") sort: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<FollowUpDto>>

    @POST("api/v1/followups")
    suspend fun createFollowUp(
        @Body request: CreateFollowUpRequestDto
    ): Response<FollowUpDto>

    @GET("api/v1/followups/{id}")
    suspend fun getFollowUpDetail(
        @Path("id") id: String
    ): Response<FollowUpDetailDto>

    @PUT("api/v1/followups/{id}/status")
    suspend fun updateFollowUpStatus(
        @Path("id") id: String,
        @Body request: UpdateFollowUpStatusRequestDto
    ): Response<FollowUpDto>

    @POST("api/v1/followups/{id}/notes")
    suspend fun addFollowUpNote(
        @Path("id") id: String,
        @Body payload: Map<String, String>
    ): Response<Unit>
}
