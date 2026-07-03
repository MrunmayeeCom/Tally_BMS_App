package com.example.feature.reminder.data

import android.util.Log
import com.example.feature.reminder.domain.*
import java.text.SimpleDateFormat
import java.util.*

class ReminderRepositoryImpl(
    private val apiService: ReminderApiService
) : IReminderRepository {

    private val tag = "ReminderRepoImpl"

    // In-memory stateful stores for interactive UI flow
    private val remindersList = mutableListOf<ReminderItem>()
    private val rulesList = mutableListOf<AutoReminderRule>()
    private val templatesList = mutableListOf<ReminderTemplate>()
    private val schedulersList = mutableListOf<ReminderSchedulerRule>()

    init {
        prepopulateData()
    }

    private fun prepopulateData() {
        // Prepopulate Templates
        templatesList.add(
            ReminderTemplate(
                id = "temp_01",
                name = "Friendly Payment Reminder",
                type = "WhatsApp",
                subject = null,
                body = "Dear *[Customer Name]*, this is a friendly reminder that invoice *[Invoice No]* for *[Outstanding Amount]* is due on *[Due Date]*. We appreciate your business and kind cooperation! - *[Company Name]*"
            )
        )
        templatesList.add(
            ReminderTemplate(
                id = "temp_02",
                name = "Urgent Overdue Alert",
                type = "Email",
                subject = "URGENT: Outstanding Balance Reminder",
                body = "Dear Accounts Team,\n\nWe would like to bring to your attention that your account is overdue by *[Outstanding Amount]*. Please find the attached statement. We request you to clear the balance *[Invoice No]* by *[Action Date]* to avoid any service disruptions.\n\nBest regards,\n*[Company Name]*"
            )
        )
        templatesList.add(
            ReminderTemplate(
                id = "temp_03",
                name = "SMS Speed Reminder",
                type = "SMS",
                subject = null,
                body = "TallyBMS Remind: Dear *[Customer Name]*, an amount of *[Outstanding Amount]* is past due for *[Invoice No]*. Please pay via UPI link: https://pay.tallybms.in/s/*[Invoice No]*"
            )
        )

        // Prepopulate Rules
        rulesList.add(
            AutoReminderRule(
                id = "rule_01",
                name = "Overdue Age > 30 Days Rule",
                triggerType = "outstanding_age",
                triggerValue = "30",
                communicationType = "WhatsApp",
                templateId = "temp_01",
                isActive = true
            )
        )
        rulesList.add(
            AutoReminderRule(
                id = "rule_02",
                name = "High Value Invoices (> 100,000 INR)",
                triggerType = "amount_based",
                triggerValue = "100000",
                communicationType = "Email",
                templateId = "temp_02",
                isActive = true
            )
        )
        rulesList.add(
            AutoReminderRule(
                id = "rule_03",
                name = "Risk Category Dunning Rule",
                triggerType = "customer_category",
                triggerValue = "Risky",
                communicationType = "SMS",
                templateId = "temp_03",
                isActive = false
            )
        )

        // Prepopulate Schedulers
        schedulersList.add(
            ReminderSchedulerRule(
                id = "sched_01",
                ruleName = "Standard Morning Dunning Run",
                frequency = "Daily",
                timeOfDay = "10:00 AM",
                dayOfWeekOrMonth = null,
                isActive = true
            )
        )
        schedulersList.add(
            ReminderSchedulerRule(
                id = "sched_02",
                ruleName = "Weekly Customer Summary Blast",
                frequency = "Weekly",
                timeOfDay = "09:00 AM",
                dayOfWeekOrMonth = "Monday",
                isActive = true
            )
        )
        schedulersList.add(
            ReminderSchedulerRule(
                id = "sched_03",
                ruleName = "Monthly Closeout Warning Rollout",
                frequency = "Monthly",
                timeOfDay = "04:00 PM",
                dayOfWeekOrMonth = "Day 28",
                isActive = false
            )
        )

        // Prepopulate Reminders History
        remindersList.add(
            ReminderItem(
                id = "rem_01",
                partyId = "cust_01",
                partyName = "Acme Corporation",
                billId = "bill_201",
                billNumber = "INV-2026-001",
                type = "WhatsApp",
                message = "Dear Acme Corporation, this is a friendly reminder that invoice INV-2026-001 for ₹48,500.00 is past due. Kindly clear the balance. Thank you!",
                scheduleDateTime = "2026-06-20 09:30 AM",
                assigneeId = "user_01",
                assigneeName = "Rajesh Kumar",
                status = "Completed",
                createdAt = "2026-06-20 09:15 AM"
            )
        )
        remindersList.add(
            ReminderItem(
                id = "rem_02",
                partyId = "cust_03",
                partyName = "Bharat Electronics",
                billId = "bill_203",
                billNumber = "INV-2026-003",
                type = "Email",
                message = "URGENT: Outstanding Balance Overdue. Dear Bharat Electronics, we request you to clear the balance ₹125,000.00 of INV-2026-003 immediately.",
                scheduleDateTime = "2026-06-19 11:00 AM",
                assigneeId = "user_02",
                assigneeName = "Anita Desai",
                status = "Completed",
                createdAt = "2026-06-19 10:30 AM"
            )
        )
        remindersList.add(
            ReminderItem(
                id = "rem_03",
                partyId = "cust_02",
                partyName = "Delta Pharma Ltd",
                billId = "bill_202",
                billNumber = "INV-2026-002",
                type = "SMS",
                message = "TallyBMS Remind: Dear Delta Pharma, ₹95,000.00 is overdue for INV-2026-002. Please pay now: https://pay.tallybms.in/s/INV-2026-002",
                scheduleDateTime = "2026-06-20 02:00 PM",
                assigneeId = "user_03",
                assigneeName = "Karan Singh",
                status = "Pending",
                createdAt = "2026-06-20T01:30:00"
            )
        )
        remindersList.add(
            ReminderItem(
                id = "rem_04",
                partyId = "cust_04",
                partyName = "Excel Logistics",
                billId = null,
                billNumber = null,
                type = "WhatsApp",
                message = "Hello Excel Logistics, we noticed multiple pending invoices. Please review your consolidated outstanding statement totaling ₹2,50,000.00.",
                scheduleDateTime = "2026-06-18 03:00 PM",
                assigneeId = "user_01",
                assigneeName = "Rajesh Kumar",
                status = "Failed",
                createdAt = "2026-06-18 02:45 PM"
            )
        )
    }

    override suspend fun getReminderDashboard(): ReminderDashboardData {
        try {
            val response = apiService.getReminderDashboard()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                return ReminderDashboardData(
                    totalActive = body.totalActive,
                    todayScheduled = body.todayScheduled,
                    overdue = body.overdue,
                    completed = body.completed
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "getReminderDashboard API failed, falling back to local: ${e.message}")
        }

        // Fallback or local State calculation
        val active = remindersList.count { it.status == "Pending" }
        val completed = remindersList.count { it.status == "Completed" }
        val failed = remindersList.count { it.status == "Failed" }
        val overdue = remindersList.count { 
            it.status == "Pending" && isBeforeToday(it.scheduleDateTime)
        }
        
        return ReminderDashboardData(
            totalActive = active,
            todayScheduled = active - overdue,
            overdue = overdue,
            completed = completed
        )
    }

    override suspend fun getReminders(query: String?, status: String?, type: String?): List<ReminderItem> {
        try {
            val response = apiService.getReminders(query, status, type)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { mapItemDtoToDomain(it) }
            }
        } catch (e: Exception) {
            Log.e(tag, "getReminders API failed, falling back to local: ${e.message}")
        }

        // Mock Search & Filter execution
        var list: List<ReminderItem> = remindersList
        if (!query.isNullOrBlank()) {
            list = list.filter {
                it.partyName.contains(query, ignoreCase = true) ||
                (it.billNumber != null && it.billNumber.contains(query, ignoreCase = true)) ||
                it.message.contains(query, ignoreCase = true)
            }
        }
        if (!status.isNullOrBlank() && status != "All") {
            list = list.filter { it.status.equals(status, ignoreCase = true) }
        }
        if (!type.isNullOrBlank() && type != "All") {
            list = list.filter { it.type.equals(type, ignoreCase = true) }
        }
        return list.sortedByDescending { it.createdAt }
    }

    override suspend fun createManualReminder(
        partyId: String,
        partyName: String,
        billId: String?,
        billNo: String?,
        type: String,
        message: String,
        scheduleDateTime: String,
        assigneeName: String
    ): ReminderItem {
        val request = CreateManualReminderRequest(
            partyId = partyId,
            partyName = partyName,
            billId = billId,
            billNumber = billNo,
            type = type,
            message = message,
            scheduleDateTime = scheduleDateTime,
            assigneeName = assigneeName
        )
        try {
            val response = apiService.createManualReminder(request)
            if (response.isSuccessful && response.body() != null) {
                val created = mapItemDtoToDomain(response.body()!!)
                remindersList.add(0, created)
                return created
            }
        } catch (e: Exception) {
            Log.e(tag, "createManualReminder API failed, using stateful local store: ${e.message}")
        }

        // Fallback local persistence
        val nowFormatted = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        val generatedId = "rem_" + UUID.randomUUID().toString().substring(0, 8)
        val newItem = ReminderItem(
            id = generatedId,
            partyId = partyId,
            partyName = partyName,
            billId = billId,
            billNumber = billNo,
            type = type,
            message = message,
            scheduleDateTime = scheduleDateTime,
            assigneeId = "user_current",
            assigneeName = assigneeName,
            status = "Pending",
            createdAt = nowFormatted
        )
        remindersList.add(0, newItem)
        return newItem
    }

    override suspend fun getAutoReminderRules(): List<AutoReminderRule> {
        try {
            val response = apiService.getAutoReminderRules()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { mapRuleDtoToDomain(it) }
            }
        } catch (e: Exception) {
            Log.e(tag, "getAutoReminderRules failed: ${e.message}")
        }
        return rulesList
    }

    override suspend fun createAutoReminderRule(rule: AutoReminderRule): AutoReminderRule {
        try {
            val dto = mapRuleDomainToDto(rule)
            val response = apiService.createAutoReminderRule(dto)
            if (response.isSuccessful && response.body() != null) {
                val created = mapRuleDtoToDomain(response.body()!!)
                rulesList.add(created)
                return created
            }
        } catch (e: Exception) {
            Log.e(tag, "createAutoReminderRule failed, adding locally: ${e.message}")
        }
        val generatedRule = rule.copy(id = "rule_" + UUID.randomUUID().toString().substring(0, 8))
        rulesList.add(generatedRule)
        return generatedRule
    }

    override suspend fun toggleAutoReminderRule(id: String, isActive: Boolean): Boolean {
        try {
            val response = apiService.toggleAutoReminderRule(id, isActive)
            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                if (!success) return false
            }
        } catch (e: Exception) {
            Log.e(tag, "toggleAutoReminderRule failed: ${e.message}, updating locally")
        }
        val idx = rulesList.indexOfFirst { it.id == id }
        if (idx != -1) {
            rulesList[idx] = rulesList[idx].copy(isActive = isActive)
            return true
        }
        return false
    }

    override suspend fun getReminderTemplates(): List<ReminderTemplate> {
        try {
            val response = apiService.getReminderTemplates()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { mapTemplateDtoToDomain(it) }
            }
        } catch (e: Exception) {
            Log.e(tag, "getReminderTemplates failed: ${e.message}")
        }
        return templatesList
    }

    override suspend fun createReminderTemplate(template: ReminderTemplate): ReminderTemplate {
        try {
            val dto = mapTemplateDomainToDto(template)
            val response = apiService.createReminderTemplate(dto)
            if (response.isSuccessful && response.body() != null) {
                val created = mapTemplateDtoToDomain(response.body()!!)
                templatesList.add(created)
                return created
            }
        } catch (e: Exception) {
            Log.e(tag, "createReminderTemplate failed, saving locally: ${e.message}")
        }
        val generatedTemp = template.copy(id = "temp_" + UUID.randomUUID().toString().substring(0, 8))
        templatesList.add(generatedTemp)
        return generatedTemp
    }

    override suspend fun updateReminderTemplate(template: ReminderTemplate): ReminderTemplate {
        try {
            val dto = mapTemplateDomainToDto(template)
            val response = apiService.updateReminderTemplate(template.id, dto)
            if (response.isSuccessful && response.body() != null) {
                val updated = mapTemplateDtoToDomain(response.body()!!)
                val idx = templatesList.indexOfFirst { it.id == template.id }
                if (idx != -1) {
                    templatesList[idx] = updated
                }
                return updated
            }
        } catch (e: Exception) {
            Log.e(tag, "updateReminderTemplate failed, saving locally: ${e.message}")
        }
        val idx = templatesList.indexOfFirst { it.id == template.id }
        if (idx != -1) {
            templatesList[idx] = template
        }
        return template
    }

    override suspend fun getReminderSchedulerRules(): List<ReminderSchedulerRule> {
        try {
            val response = apiService.getReminderSchedulers()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { mapSchedulerDtoToDomain(it) }
            }
        } catch (e: Exception) {
            Log.e(tag, "getReminderSchedulers failed: ${e.message}")
        }
        return schedulersList
    }

    override suspend fun createReminderSchedulerRule(rule: ReminderSchedulerRule): ReminderSchedulerRule {
        try {
            val dto = mapSchedulerDomainToDto(rule)
            val response = apiService.createReminderScheduler(dto)
            if (response.isSuccessful && response.body() != null) {
                val created = mapSchedulerDtoToDomain(response.body()!!)
                schedulersList.add(created)
                return created
            }
        } catch (e: Exception) {
            Log.e(tag, "createReminderScheduler failed, using local fallback: ${e.message}")
        }
        val generatedSched = rule.copy(id = "sched_" + UUID.randomUUID().toString().substring(0, 8))
        schedulersList.add(generatedSched)
        return generatedSched
    }

    override suspend fun toggleReminderSchedulerRule(id: String, isActive: Boolean): Boolean {
        try {
            val response = apiService.toggleReminderScheduler(id, isActive)
            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                if (!success) return false
            }
        } catch (e: Exception) {
            Log.e(tag, "toggleReminderScheduler failed: ${e.message}, updating locally")
        }
        val idx = schedulersList.indexOfFirst { it.id == id }
        if (idx != -1) {
            schedulersList[idx] = schedulersList[idx].copy(isActive = isActive)
            return true
        }
        return false
    }

    // === Helpers ===
    private fun isBeforeToday(dateTimeStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            val date = sdf.parse(dateTimeStr)
            date?.before(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun mapItemDtoToDomain(dto: ReminderItemDto) = ReminderItem(
        id = dto.id,
        partyId = dto.partyId,
        partyName = dto.partyName,
        billId = dto.billId,
        billNumber = dto.billNumber,
        type = dto.type,
        message = dto.message,
        scheduleDateTime = dto.scheduleDateTime,
        assigneeId = dto.assigneeId,
        assigneeName = dto.assigneeName,
        status = dto.status,
        createdAt = dto.createdAt
    )

    private fun mapRuleDtoToDomain(dto: AutoReminderRuleDto) = AutoReminderRule(
        id = dto.id,
        name = dto.name,
        triggerType = dto.triggerType,
        triggerValue = dto.triggerValue,
        communicationType = dto.communicationType,
        templateId = dto.templateId,
        isActive = dto.isActive
    )

    private fun mapRuleDomainToDto(rule: AutoReminderRule) = AutoReminderRuleDto(
        id = rule.id,
        name = rule.name,
        triggerType = rule.triggerType,
        triggerValue = rule.triggerValue,
        communicationType = rule.communicationType,
        templateId = rule.templateId,
        isActive = rule.isActive
    )

    private fun mapTemplateDtoToDomain(dto: ReminderTemplateDto) = ReminderTemplate(
        id = dto.id,
        name = dto.name,
        type = dto.type,
        subject = dto.subject,
        body = dto.body
    )

    private fun mapTemplateDomainToDto(template: ReminderTemplate) = ReminderTemplateDto(
        id = template.id,
        name = template.name,
        type = template.type,
        subject = template.subject,
        body = template.body
    )

    private fun mapSchedulerDtoToDomain(dto: ReminderSchedulerRuleDto) = ReminderSchedulerRule(
        id = dto.id,
        ruleName = dto.ruleName,
        frequency = dto.frequency,
        timeOfDay = dto.timeOfDay,
        dayOfWeekOrMonth = dto.dayOfWeekOrMonth,
        isActive = dto.isActive
    )

    private fun mapSchedulerDomainToDto(rule: ReminderSchedulerRule) = ReminderSchedulerRuleDto(
        id = rule.id,
        ruleName = rule.ruleName,
        frequency = rule.frequency,
        timeOfDay = rule.timeOfDay,
        dayOfWeekOrMonth = rule.dayOfWeekOrMonth,
        isActive = rule.isActive
    )
}
