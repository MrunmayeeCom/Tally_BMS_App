package com.example.feature.reminder.data

import retrofit2.Response
import retrofit2.http.*

interface ReminderApiService {
    @GET("api/v1/reminders/dashboard")
    suspend fun getReminderDashboard(): Response<ReminderDashboardResponseDto>

    @GET("api/v1/reminders")
    suspend fun getReminders(
        @Query("query") query: String?,
        @Query("status") status: String?,
        @Query("type") type: String?
    ): Response<List<ReminderItemDto>>

    @POST("api/v1/reminders/manual")
    suspend fun createManualReminder(
        @Body request: CreateManualReminderRequest
    ): Response<ReminderItemDto>

    @GET("api/v1/reminders/rules")
    suspend fun getAutoReminderRules(): Response<List<AutoReminderRuleDto>>

    @POST("api/v1/reminders/rules")
    suspend fun createAutoReminderRule(
        @Body request: AutoReminderRuleDto
    ): Response<AutoReminderRuleDto>

    @PUT("api/v1/reminders/rules/{id}/toggle")
    suspend fun toggleAutoReminderRule(
        @Path("id") id: String,
        @Query("isActive") isActive: Boolean
    ): Response<ToggleResponseDto>

    @GET("api/v1/reminders/templates")
    suspend fun getReminderTemplates(): Response<List<ReminderTemplateDto>>

    @POST("api/v1/reminders/templates")
    suspend fun createReminderTemplate(
        @Body request: ReminderTemplateDto
    ): Response<ReminderTemplateDto>

    @PUT("api/v1/reminders/templates/{id}")
    suspend fun updateReminderTemplate(
        @Path("id") id: String,
        @Body request: ReminderTemplateDto
    ): Response<ReminderTemplateDto>

    @GET("api/v1/reminders/schedulers")
    suspend fun getReminderSchedulers(): Response<List<ReminderSchedulerRuleDto>>

    @POST("api/v1/reminders/schedulers")
    suspend fun createReminderScheduler(
        @Body request: ReminderSchedulerRuleDto
    ): Response<ReminderSchedulerRuleDto>

    @PUT("api/v1/reminders/schedulers/{id}/toggle")
    suspend fun toggleReminderScheduler(
        @Path("id") id: String,
        @Query("isActive") isActive: Boolean
    ): Response<ToggleResponseDto>
}

// Data Transfer Objects
data class ReminderDashboardResponseDto(
    val totalActive: Int,
    val todayScheduled: Int,
    val overdue: Int,
    val completed: Int
)

data class ReminderItemDto(
    val id: String,
    val partyId: String,
    val partyName: String,
    val billId: String?,
    val billNumber: String?,
    val type: String,
    val message: String,
    val scheduleDateTime: String,
    val assigneeId: String,
    val assigneeName: String,
    val status: String,
    val createdAt: String
)

data class CreateManualReminderRequest(
    val partyId: String,
    val partyName: String,
    val billId: String?,
    val billNumber: String?,
    val type: String,
    val message: String,
    val scheduleDateTime: String,
    val assigneeName: String
)

data class AutoReminderRuleDto(
    val id: String,
    val name: String,
    val triggerType: String,
    val triggerValue: String,
    val communicationType: String,
    val templateId: String,
    val isActive: Boolean
)

data class ReminderTemplateDto(
    val id: String,
    val name: String,
    val type: String,
    val subject: String?,
    val body: String
)

data class ReminderSchedulerRuleDto(
    val id: String,
    val ruleName: String,
    val frequency: String,
    val timeOfDay: String,
    val dayOfWeekOrMonth: String?,
    val isActive: Boolean
)

data class ToggleResponseDto(
    val success: Boolean,
    val id: String,
    val isActive: Boolean
)
