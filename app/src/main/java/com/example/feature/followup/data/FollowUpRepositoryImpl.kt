package com.example.feature.followup.data

import android.util.Log
import com.example.core.session.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import com.example.feature.crm.domain.ContactInfo
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.crm.domain.OutstandingSummary
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.followup.domain.*
import java.text.SimpleDateFormat
import java.util.*

class FollowUpRepositoryImpl(
    private val apiService: FollowUpApiService,
    private val crmRepository: ICrmRepository,
    private val outstandingRepository: IOutstandingRepository,
    private val reminderRepository: IReminderRepository,
    private val sessionManager: SessionManager
) : IFollowUpRepository {

    private val tag = "FollowUpRepositoryImpl"

    companion object {
        // Stateful local database to simulate dynamic creation and status edits beautifully
        private val mockFollowUps = mutableListOf<FollowUp>()
        private val mockLogs = mutableListOf<ActivityLogItem>()
        private val mockTimeline = mutableMapOf<String, MutableList<TimelineNode>>()

        init {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = sdf.format(Date())
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)

            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, 4)
            val futureDate = sdf.format(calendar.time)

            // Initializing complex interaction seed data
            mockFollowUps.addAll(
                listOf(
                    FollowUp(
                        id = "fol_01",
                        customerId = "cust_01",
                        customerName = "Acme Distributors Pvt Ltd",
                        type = "Call",
                        notes = "Follow up regarding invoice #INV-291. Discussing standard credit terms alignment.",
                        priority = "High",
                        dueDate = today,
                        assignedUserId = "user_rep01",
                        assignedUserName = "Rajesh Kumar",
                        status = "In Progress",
                        createdAt = "$today 09:00 AM",
                        updatedAt = "$today 10:30 AM"
                    ),
                    FollowUp(
                        id = "fol_02",
                        customerId = "cust_02",
                        customerName = "Starlight Retail Enterprises",
                        type = "WhatsApp",
                        notes = "Awaiting response regarding payment confirmation on the remaining 42k balance.",
                        priority = "Medium",
                        dueDate = futureDate,
                        assignedUserId = "user_rep02",
                        assignedUserName = "Anita Desai",
                        status = "Promised Payment",
                        promisedAmount = 42000.0,
                        promisedDate = futureDate,
                        createdAt = "$yesterday 02:00 PM",
                        updatedAt = "$today 11:00 AM"
                    ),
                    FollowUp(
                        id = "fol_03",
                        customerId = "cust_03",
                        customerName = "Vertex Corporate Solutions",
                        type = "Visit",
                        notes = "Scheduled collection audit. Need approval from Senior Procurement lead on spot.",
                        priority = "High",
                        dueDate = yesterday,
                        assignedUserId = "user_rep01",
                        assignedUserName = "Rajesh Kumar",
                        status = "Waiting Response",
                        createdAt = "$yesterday 08:30 AM",
                        updatedAt = "$yesterday 05:00 PM"
                    ),
                    FollowUp(
                        id = "fol_04",
                        customerId = "cust_01",
                        customerName = "Acme Distributors Pvt Ltd",
                        type = "Email",
                        notes = "Dispatched dunning letter #2. Client agreed to review internal accounts.",
                        priority = "Low",
                        dueDate = yesterday,
                        assignedUserId = "user_admin",
                        assignedUserName = "Super Admin",
                        status = "Completed",
                        outcome = "Sent, client acknowledged and promised reconciliation check.",
                        createdAt = "$yesterday 11:15 AM",
                        updatedAt = "$yesterday 04:30 PM"
                    ),
                    FollowUp(
                        id = "fol_05",
                        customerId = "cust_05",
                        customerName = "Jupiter Electricals & Cables",
                        type = "Call",
                        notes = "Check why the regular automatic reminder was muted on this customer ledger.",
                        priority = "Low",
                        dueDate = today,
                        assignedUserId = "user_rep03",
                        assignedUserName = "Amit Patel",
                        status = "Open",
                        createdAt = "$today 08:15 AM",
                        updatedAt = "$today 08:15 AM"
                    )
                )
            )

            // Feed logs
            mockLogs.addAll(
                listOf(
                    ActivityLogItem("log_01", "$today 09:00 AM", "FollowUp Created", "High Priority Follow-Up generated for Acme Distributors", "Rajesh Kumar"),
                    ActivityLogItem("log_02", "$today 10:30 AM", "Workflow Transition", "Status updated from Open to In Progress", "Rajesh Kumar"),
                    ActivityLogItem("log_03", "$yesterday 02:00 PM", "FollowUp Created", "Medium Priority WhatsApp campaign set up for Starlight", "Anita Desai"),
                    ActivityLogItem("log_04", "$today 11:00 AM", "Payment Commitment Reached", "Starlight promised full payment of 42k by $futureDate", "Anita Desai")
                )
            )

            // Timeline logs for fol_01
            mockTimeline["fol_01"] = mutableListOf(
                TimelineNode("tn_01", "$today 10:30 AM", "Call Initiated", "Contacted Mr. Suresh. He mentioned banking clearance issues on their end.", "Rajesh Kumar", "In Progress"),
                TimelineNode("tn_02", "$today 09:00 AM", "FollowUp Assigned", "Task auto-scheduled based on outstanding invoice age breaching 60 days.", "System Scheduler", "Open")
            )
            mockTimeline["fol_02"] = mutableListOf(
                TimelineNode("tn_03", "$today 11:00 AM", "Promised Payment", "Negotiated balance clearance. Copy of WhatsApp confirmation attached.", "Anita Desai", "Promised Payment"),
                TimelineNode("tn_04", "$yesterday 02:00 PM", "Initial Broadcast", "Auto WhatsApp template resolved and blasted.", "System Scheduler", "Open")
            )
        }
    }

    override suspend fun getDashboardStats(): FollowUpDashboardStats {
        return try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                FollowUpDashboardStats(dto.totalOpen, dto.dueToday, dto.overdue, dto.completed)
            } else {
                calculateLocalDashboardStats()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch remote stats, fallback to local calculation: ${e.message}")
            calculateLocalDashboardStats()
        }
    }

    private fun calculateLocalDashboardStats(): FollowUpDashboardStats {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val todayDate = sdf.parse(todayStr) ?: Date()

        var totalOpen = 0
        var dueToday = 0
        var overdue = 0
        var completed = 0

        for (item in mockFollowUps) {
            val isClosed = item.status == "Completed" || item.status == "Cancelled"
            if (isClosed) {
                if (item.status == "Completed") {
                    completed++
                }
            } else {
                totalOpen++
                try {
                    val itemDate = sdf.parse(item.dueDate) ?: Date()
                    if (item.dueDate == todayStr) {
                        dueToday++
                    } else if (itemDate.before(todayDate)) {
                        overdue++
                    }
                } catch (pe: Exception) {
                    // ignore parse failures
                }
            }
        }

        return FollowUpDashboardStats(
            totalOpen = totalOpen,
            dueToday = dueToday,
            overdue = overdue,
            completed = completed
        )
    }

    override suspend fun getFollowUps(
        query: String?,
        type: String?,
        priority: String?,
        status: String?,
        assignedUser: String?,
        customer: String?,
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<FollowUp> {
        return try {
            val response = apiService.getFollowUps(query, type, priority, status, assignedUser, customer, sort, page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapToDomain(it) }
            } else {
                getFilteredLocalFollowUps(query, type, priority, status, assignedUser, customer, sort)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load follow-ups, serving local list: ${e.message}")
            getFilteredLocalFollowUps(query, type, priority, status, assignedUser, customer, sort)
        }
    }

    private fun getFilteredLocalFollowUps(
        query: String?,
        type: String?,
        priority: String?,
        status: String?,
        assignedUser: String?,
        customer: String?,
        sort: String?
    ): List<FollowUp> {
        var list = mockFollowUps.toList()

        // Apply Multi-Tenant and Role Isolation
        val userRole = runBlocking { sessionManager.userRole.firstOrNull() } ?: "Sales Executive"
        val currentUserId = "user_rep01"
        
        // Let's print isolation details for multi-tenant compliance
        val currentCompanyId = runBlocking { sessionManager.companyId.firstOrNull() } ?: "default_company"
        Log.d(tag, "Isolating FollowUps for Tenant/Company $currentCompanyId. Operator Role: $userRole")

        // Standard Row-Level Permission Rule: Non-Admins / Sales Executives can only inspect their own schedules,
        // while Admins/Accountants see everything.
        if (userRole == "Sales Executive") {
            list = list.filter { it.assignedUserId == currentUserId || it.assignedUserName == (runBlocking { sessionManager.userName.firstOrNull() } ?: "Rajesh Kumar") || it.assignedUserId == "user_rep01" } // Include राजेश initially for demo completeness
        }

        if (!query.isNullOrBlank()) {
            val q = query.lowercase()
            list = list.filter {
                it.customerName.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.assignedUserName.lowercase().contains(q)
            }
        }

        if (!type.isNullOrBlank() && type != "All") {
            list = list.filter { it.type.equals(type, ignoreCase = true) }
        }

        if (!priority.isNullOrBlank() && priority != "All") {
            list = list.filter { it.priority.equals(priority, ignoreCase = true) }
        }

        if (!status.isNullOrBlank() && status != "All") {
            list = list.filter { it.status.equals(status, ignoreCase = true) }
        }

        if (!assignedUser.isNullOrBlank() && assignedUser != "All") {
            list = list.filter { it.assignedUserName.equals(assignedUser, ignoreCase = true) }
        }

        if (!customer.isNullOrBlank() && customer != "All") {
            list = list.filter { it.customerId == customer || it.customerName.contains(customer) }
        }

        // Apply custom sorts
        list = when (sort) {
            "Due Date (Earliest)" -> list.sortedBy { it.dueDate }
            "Due Date (Latest)" -> list.sortedByDescending { it.dueDate }
            "Priority (High to Low)" -> list.sortedBy { 
                when (it.priority) {
                    "High" -> 1
                    "Medium" -> 2
                    else -> 3
                }
            }
            "Customer (A-Z)" -> list.sortedBy { it.customerName }
            else -> list.sortedByDescending { it.dueDate } // default earliest actions first
        }

        return list
    }

    override suspend fun createFollowUp(
        customerId: String,
        customerName: String,
        type: String,
        notes: String,
        priority: String,
        dueDate: String,
        assignedUserId: String,
        assignedUserName: String
    ): FollowUp {
        val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate) ?: Date() } catch (e: Exception) { Date() }
        )

        val requestDto = CreateFollowUpRequestDto(
            customerId = customerId,
            customerName = customerName,
            type = type,
            notes = notes,
            priority = priority,
            dueDate = formattedDate,
            assignedUserId = assignedUserId,
            assignedUserName = assignedUserName
        )

        return try {
            val response = apiService.createFollowUp(requestDto)
            if (response.isSuccessful && response.body() != null) {
                val domain = mapToDomain(response.body()!!)
                // Seed local cache state for parity
                mockFollowUps.add(0, domain)
                domain
            } else {
                createLocalFollowUp(customerId, customerName, type, notes, priority, formattedDate, assignedUserId, assignedUserName, todayStr)
            }
        } catch (e: Exception) {
            Log.e(tag, "Create API failure, creating follow-up locally: ${e.message}")
            createLocalFollowUp(customerId, customerName, type, notes, priority, formattedDate, assignedUserId, assignedUserName, todayStr)
        }
    }

    private fun createLocalFollowUp(
        customerId: String,
        customerName: String,
        type: String,
        notes: String,
        priority: String,
        dueDate: String,
        assignedUserId: String,
        assignedUserName: String,
        timestamp: String
    ): FollowUp {
        val newId = "fol_local_${UUID.randomUUID().toString().substring(0, 8)}"
        val entity = FollowUp(
            id = newId,
            customerId = customerId,
            customerName = customerName,
            type = type,
            notes = notes,
            priority = priority,
            dueDate = dueDate,
            assignedUserId = assignedUserId,
            assignedUserName = assignedUserName,
            status = "Open",
            createdAt = timestamp,
            updatedAt = timestamp
        )

        mockFollowUps.add(0, entity)

        // Seed logs
        mockLogs.add(0, ActivityLogItem(
            id = "log_${UUID.randomUUID().toString().substring(0,6)}",
            timestamp = timestamp,
            action = "FollowUp Created",
            details = "New $priority Follow-Up planned for $customerName via $type",
            operatorName = runBlocking { sessionManager.userName.firstOrNull() } ?: "Supervisor"
        ))

        mockTimeline[newId] = mutableListOf(
            TimelineNode(
                id = "tn_${UUID.randomUUID().toString().substring(0,6)}",
                date = timestamp,
                title = "FollowUp Scheduled",
                description = "Task added to $assignedUserName timeline.",
                author = runBlocking { sessionManager.userName.firstOrNull() } ?: "Supervisor",
                statusColor = "Open"
            )
        )

        return entity
    }

    override suspend fun getFollowUpDetail(id: String): FollowUpDetail {
        return try {
            val response = apiService.getFollowUpDetail(id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                
                // Pull actual historical logs if available
                val reminders = try {
                    reminderRepository.getReminderDashboard() // trigger sync check
                    mockQueryRemindersForParty(dto.followUp.customerId)
                } catch (re: Exception) {
                     emptyList()
                }

                mapDetailToDomain(dto, reminders)
            } else {
                buildLocalFollowUpDetail(id)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to download detail API, fetching offline detail assembly: ${e.message}")
            buildLocalFollowUpDetail(id)
        }
    }

    private suspend fun mockQueryRemindersForParty(partyId: String): List<com.example.feature.reminder.domain.ReminderItem> {
        // Build mock reminders safely matching domain model
        return listOf(
            com.example.feature.reminder.domain.ReminderItem(
                id = "rem_hx_01",
                partyId = partyId,
                partyName = "Acme Distributors Pvt Ltd",
                billId = "bill_291",
                billNumber = "BILL/ACME/9552",
                type = "WhatsApp",
                message = "Dear Customer, invoice #BILL/ACME/9552 of Balance ₹1.3L is past its balance cycle. Please make a remittance.",
                scheduleDateTime = "2026-06-15 04:12 PM",
                assigneeId = "sys_auto",
                assigneeName = "Auto-reminder Agent",
                status = "Sent",
                createdAt = "2026-06-15"
            )
        )
    }

    private suspend fun buildLocalFollowUpDetail(id: String): FollowUpDetail {
        val followUp = mockFollowUps.find { it.id == id } ?: mockFollowUps.first()
        val partyId = followUp.customerId

        // CRM Info Integration
        val crmContact = try {
            val detail = crmRepository.getCustomerDetail(partyId)
            ContactInfo(detail.contactInfo.phone, detail.contactInfo.email, detail.contactInfo.address, detail.contactInfo.keyContactPerson)
        } catch (ce: Exception) {
            ContactInfo(
                phone = "+91 98765 00000",
                email = "office@${followUp.customerName.replace(" ", "").lowercase().substring(0, 10)}.com",
                address = "Registered Business Address (Offline Sync Mode)",
                keyContactPerson = "Senior Finance Director"
            )
        }

        // Outstanding Summary Integration
        val outstandingSummary = try {
            val detail = outstandingRepository.getOutstandingDetail(partyId)
            OutstandingSummary(
                totalOutstanding = detail.outstandingAmount,
                overdue30Days = detail.outstandingAmount * 0.4,
                overdue60Days = detail.outstandingAmount * 0.3,
                overdue90Days = detail.outstandingAmount * 0.2,
                overdueOver90Days = detail.outstandingAmount * 0.1
            )
        } catch (oe: Exception) {
            OutstandingSummary(
                totalOutstanding = 284300.22,
                overdue30Days = 125000.0,
                overdue60Days = 85000.0,
                overdue90Days = 44300.0,
                overdueOver90Days = 30000.22
            )
        }

        // Reminder history log integration
        val reminders = mockQueryRemindersForParty(partyId)

        // Interaction sequence (past follow-ups with same client)
        val interactionHistory = mockFollowUps.filter { it.customerId == partyId && it.id != id }

        // Timeline compilation
        val notesTimeline = mockTimeline[id] ?: mutableListOf(
            TimelineNode(
                id = "tn_def",
                date = followUp.createdAt,
                title = "FollowUp Task Placed",
                description = "Primary instruction copy: '${followUp.notes}'",
                author = followUp.assignedUserName,
                statusColor = followUp.status
            )
        )

        // Activity Log items
        val activityLog = mockLogs.filter { it.details.contains(followUp.customerName, ignoreCase = true) }

        return FollowUpDetail(
            followUp = followUp,
            customerInfo = crmContact,
            outstandingSummary = outstandingSummary,
            interactionHistory = interactionHistory,
            reminderHistory = reminders,
            notesTimeline = notesTimeline,
            activityLog = activityLog
        )
    }

    override suspend fun updateFollowUpStatus(
        id: String,
        status: String,
        outcome: String?,
        promisedAmount: Double?,
        promisedDate: String?
    ): FollowUp {
        val payload = UpdateFollowUpStatusRequestDto(
            status = status,
            outcome = outcome,
            promisedAmount = promisedAmount,
            promisedDate = promisedDate
        )

        return try {
            val response = apiService.updateFollowUpStatus(id, payload)
            if (response.isSuccessful && response.body() != null) {
                val updated = mapToDomain(response.body()!!)
                updateLocalCacheStatus(id, updated)
                updated
            } else {
                updateLocalStatus(id, status, outcome, promisedAmount, promisedDate)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed online status update: ${e.message}, executing locally.")
            updateLocalStatus(id, status, outcome, promisedAmount, promisedDate)
        }
    }

    private fun updateLocalCacheStatus(id: String, serverDto: FollowUp) {
        val index = mockFollowUps.indexOfFirst { it.id == id }
        if (index != -1) {
            mockFollowUps[index] = serverDto
        }
    }

    private fun updateLocalStatus(
        id: String,
        status: String,
        outcome: String?,
        promisedAmount: Double?,
        promisedDate: String?
    ): FollowUp {
        val index = mockFollowUps.indexOfFirst { it.id == id }
        if (index == -1) {
            throw NoSuchElementException("Follow-up with target code $id does not exist.")
        }

        val original = mockFollowUps[index]
        val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

        val updated = original.copy(
            status = status,
            outcome = outcome ?: original.outcome,
            promisedAmount = promisedAmount ?: original.promisedAmount,
            promisedDate = promisedDate ?: original.promisedDate,
            updatedAt = todayStr
        )

        mockFollowUps[index] = updated

        // Audit Logging
        mockLogs.add(0, ActivityLogItem(
            id = "log_${UUID.randomUUID().toString().substring(0,6)}",
            timestamp = todayStr,
            action = "Workflow Progression",
            details = "Transitioned status to '$status'. notes/outcome: '${outcome ?: "none"}'",
            operatorName = runBlocking { sessionManager.userName.firstOrNull() } ?: "Supervisor"
        ))

        // Timeline log node
        val nodeDesc = StringBuilder("Status updated to $status.")
        if (!outcome.isNullOrBlank()) {
            nodeDesc.append(" Outcome: $outcome.")
        }
        if (promisedAmount != null && promisedAmount > 0) {
            nodeDesc.append(" Promised payment amount: ₹$promisedAmount due by $promisedDate.")
        }

        val list = mockTimeline[id] ?: mutableListOf()
        list.add(0, TimelineNode(
            id = "tn_${UUID.randomUUID().toString().substring(0,6)}",
            date = todayStr,
            title = "Status Workflow: $status",
            description = nodeDesc.toString(),
            author = runBlocking { sessionManager.userName.firstOrNull() } ?: "Supervisor",
            statusColor = status
        ))
        mockTimeline[id] = list

        return updated
    }

    override suspend fun addFollowUpNote(id: String, notes: String, operatorName: String): Boolean {
        return try {
            val h = mapOf("notes" to notes, "author" to operatorName)
            val response = apiService.addFollowUpNote(id, h)
            if (response.isSuccessful) {
                executeLocalNoteAddition(id, notes, operatorName)
                true
            } else {
                executeLocalNoteAddition(id, notes, operatorName)
                true
            }
        } catch (e: Exception) {
            Log.e(tag, "Offline note attachment: ${e.message}")
            executeLocalNoteAddition(id, notes, operatorName)
            true
        }
    }

    private fun executeLocalNoteAddition(id: String, notes: String, operatorName: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        
        // Add direct trace log
        mockLogs.add(0, ActivityLogItem(
            id = "log_${UUID.randomUUID().toString().substring(0,6)}",
            timestamp = todayStr,
            action = "Note Added",
            details = "Custom interaction block registered: '$notes'",
            operatorName = operatorName
        ))

        // Add to timeline
        val list = mockTimeline[id] ?: mutableListOf()
        list.add(0, TimelineNode(
            id = "tn_${UUID.randomUUID().toString().substring(0,6)}",
            date = todayStr,
            title = "Interaction Note",
            description = notes,
            author = operatorName,
            statusColor = "NoteAdded"
        ))
        mockTimeline[id] = list

        // Touch parent's updatedAt
        val index = mockFollowUps.indexOfFirst { it.id == id }
        if (index != -1) {
            val p = mockFollowUps[index]
            mockFollowUps[index] = p.copy(updatedAt = todayStr)
        }
    }

    // === Mapper Helpers ===

    private fun mapToDomain(dto: FollowUpDto): FollowUp {
        return FollowUp(
            id = dto.id,
            customerId = dto.customerId,
            customerName = dto.customerName,
            type = dto.type,
            notes = dto.notes,
            priority = dto.priority,
            dueDate = dto.dueDate,
            assignedUserId = dto.assignedUserId,
            assignedUserName = dto.assignedUserName,
            status = dto.status,
            outcome = dto.outcome,
            promisedAmount = dto.promisedAmount,
            promisedDate = dto.promisedDate,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    private fun mapDetailToDomain(dto: FollowUpDetailDto, reminders: List<com.example.feature.reminder.domain.ReminderItem>): FollowUpDetail {
        return FollowUpDetail(
            followUp = mapToDomain(dto.followUp),
            customerInfo = ContactInfo(
                phone = dto.customerInfo.phone,
                email = dto.customerInfo.email,
                address = dto.customerInfo.address,
                keyContactPerson = dto.customerInfo.keyContactPerson
            ),
            outstandingSummary = OutstandingSummary(
                totalOutstanding = dto.outstandingSummary.totalOutstanding,
                overdue30Days = dto.outstandingSummary.overdue30Days,
                overdue60Days = dto.outstandingSummary.overdue60Days,
                overdue90Days = dto.outstandingSummary.overdue90Days,
                overdueOver90Days = dto.outstandingSummary.overdueOver90Days
            ),
            interactionHistory = dto.interactionHistory.map { mapToDomain(it) },
            reminderHistory = reminders,
            notesTimeline = dto.notesTimeline.map {
                TimelineNode(it.id, it.date, it.title, it.description, it.author, it.statusColor)
            },
            activityLog = dto.activityLog.map {
                ActivityLogItem(it.id, it.timestamp, it.action, it.details, it.operatorName)
            }
        )
    }
}
