package com.example.feature.reminder.domain

interface IReminderRepository {
    suspend fun getReminderDashboard(): ReminderDashboardData
    suspend fun getReminders(query: String?, status: String?, type: String?): List<ReminderItem>
    suspend fun createManualReminder(
        partyId: String,
        partyName: String,
        billId: String?,
        billNo: String?,
        type: String,
        message: String,
        scheduleDateTime: String,
        assigneeName: String
    ): ReminderItem
    
    suspend fun getAutoReminderRules(): List<AutoReminderRule>
    suspend fun createAutoReminderRule(rule: AutoReminderRule): AutoReminderRule
    suspend fun toggleAutoReminderRule(id: String, isActive: Boolean): Boolean
    
    suspend fun getReminderTemplates(): List<ReminderTemplate>
    suspend fun createReminderTemplate(template: ReminderTemplate): ReminderTemplate
    suspend fun updateReminderTemplate(template: ReminderTemplate): ReminderTemplate
    
    suspend fun getReminderSchedulerRules(): List<ReminderSchedulerRule>
    suspend fun createReminderSchedulerRule(rule: ReminderSchedulerRule): ReminderSchedulerRule
    suspend fun toggleReminderSchedulerRule(id: String, isActive: Boolean): Boolean
}
