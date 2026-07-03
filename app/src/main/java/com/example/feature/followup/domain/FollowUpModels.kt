package com.example.feature.followup.domain

import com.example.feature.crm.domain.ContactInfo
import com.example.feature.crm.domain.OutstandingSummary
import com.example.feature.reminder.domain.ReminderItem

data class FollowUp(
    val id: String,
    val customerId: String,
    val customerName: String,
    val type: String,          // e.g., "Call", "Visit", "Email", "WhatsApp"
    val notes: String,
    val priority: String,      // "High", "Medium", "Low"
    val dueDate: String,       // "yyyy-MM-dd"
    val assignedUserId: String,
    val assignedUserName: String,
    val status: String,        // "Open", "In Progress", "Waiting Response", "Promised Payment", "Completed", "Cancelled"
    val outcome: String? = null,
    val promisedAmount: Double? = null,
    val promisedDate: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class FollowUpDashboardStats(
    val totalOpen: Int,
    val dueToday: Int,
    val overdue: Int,
    val completed: Int
)

data class TimelineNode(
    val id: String,
    val date: String,
    val title: String,
    val description: String,
    val author: String,
    val statusColor: String? = null
)

data class ActivityLogItem(
    val id: String,
    val timestamp: String,
    val action: String,
    val details: String,
    val operatorName: String
)

data class FollowUpDetail(
    val followUp: FollowUp,
    val customerInfo: ContactInfo,
    val outstandingSummary: OutstandingSummary,
    val interactionHistory: List<FollowUp>,
    val reminderHistory: List<ReminderItem>,
    val notesTimeline: List<TimelineNode>,
    val activityLog: List<ActivityLogItem>
)

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
