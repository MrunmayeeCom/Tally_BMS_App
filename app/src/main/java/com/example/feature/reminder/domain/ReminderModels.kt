package com.example.feature.reminder.domain

import com.example.feature.outstanding.domain.OutstandingItem

data class ReminderDashboardData(
    val totalActive: Int,
    val todayScheduled: Int,
    val overdue: Int,
    val completed: Int
)

data class ReminderItem(
    val id: String,
    val partyId: String,
    val partyName: String,
    val billId: String?,
    val billNumber: String?,
    val type: String, // "WhatsApp", "SMS", "Email", "Custom/Call"
    val message: String,
    val scheduleDateTime: String,
    val assigneeId: String,
    val assigneeName: String,
    val status: String, // "Sent", "Failed", "Pending", "Completed"
    val createdAt: String
)

data class AutoReminderRule(
    val id: String,
    val name: String,
    val triggerType: String, // "outstanding_age", "amount_based", "customer_category", "recovery_stage"
    val triggerValue: String, // e.g. "30" (days), "50000" (amount), "Premium" (category), "Reminder Sent" (stage)
    val communicationType: String, // "WhatsApp", "SMS", "Email"
    val templateId: String,
    val isActive: Boolean
)

data class ReminderTemplate(
    val id: String,
    val name: String,
    val type: String, // "WhatsApp", "SMS", "Email", "Custom"
    val subject: String?,
    val body: String
)

data class ReminderSchedulerRule(
    val id: String,
    val ruleName: String,
    val frequency: String, // "Daily", "Weekly", "Monthly", "Custom"
    val timeOfDay: String,
    val dayOfWeekOrMonth: String?,
    val isActive: Boolean
)

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
