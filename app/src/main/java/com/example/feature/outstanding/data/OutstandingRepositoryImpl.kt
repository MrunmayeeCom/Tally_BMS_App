package com.example.feature.outstanding.data

import android.util.Log
import com.example.feature.outstanding.domain.*

class OutstandingRepositoryImpl(
    private val apiService: OutstandingApiService
) : IOutstandingRepository {

    private val tag = "OutstandingRepoImpl"

    // InMemory cache state of mock updates to make the interactive actions function in the UI (e.g. updating recovery tracking)
    private val recoveryStates = mutableMapOf<String, RecoveryTrackData>()

    init {
        // Prepopulate some interactive states
        recoveryStates["cust_01"] = RecoveryTrackData(
            recoveryStatus = "Reminder Sent",
            assignedExecutive = "Rajesh Kumar",
            followUpCount = 3,
            lastContactDate = "2026-06-15",
            nextActionDate = "2026-06-25"
        )
        recoveryStates["cust_03"] = RecoveryTrackData(
            recoveryStatus = "Promised",
            assignedExecutive = "Anita Desai",
            followUpCount = 5,
            lastContactDate = "2026-06-10",
            nextActionDate = "2026-06-22"
        )
    }

    override suspend fun getDashboardData(): OutstandingDashboardData {
        return try {
            val response = apiService.getDashboardData()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                OutstandingDashboardData(
                    totalReceivables = body.totalReceivables,
                    totalPayables = body.totalPayables,
                    overdueAmount = body.overdueAmount,
                    collectionsThisMonth = body.collectionsThisMonth
                )
            } else {
                getMockDashboardData()
            }
        } catch (e: Exception) {
            Log.e(tag, "getDashboardData API failed: ${e.message}, using mock", e)
            getMockDashboardData()
        }
    }

    override suspend fun getOutstandingItems(
        query: String?,
        type: String?,
        group: String?,
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<OutstandingItem> {
        return try {
            val response = apiService.getOutstandingItems(query, type, group, sort, page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapItemDtoToDomain(it) }
            } else {
                getMockOutstandingItems(query, type, group, sort)
            }
        } catch (e: Exception) {
            Log.e(tag, "getOutstandingItems API failed: ${e.message}, using mock", e)
            getMockOutstandingItems(query, type, group, sort)
        }
    }

    override suspend fun getOutstandingDetail(partyId: String): OutstandingDetail {
        return try {
            val response = apiService.getOutstandingDetail(partyId)
            if (response.isSuccessful && response.body() != null) {
                mapDetailDtoToDomain(response.body()!!)
            } else {
                getMockOutstandingDetail(partyId)
            }
        } catch (e: Exception) {
            Log.e(tag, "getOutstandingDetail API failed: ${e.message}, using mock", e)
            getMockOutstandingDetail(partyId)
        }
    }

    override suspend fun getAgeingReport(type: String): AgeingReportData {
        return try {
            val response = apiService.getAgeingReport(type)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                AgeingReportData(
                    totalAmount = body.totalAmount,
                    buckets = body.buckets.map {
                        AgeingBucket(it.range, it.amount, it.count, it.percentage)
                    },
                    criticalAccounts = body.criticalAccounts.map { mapItemDtoToDomain(it) }
                )
            } else {
                getMockAgeingReport(type)
            }
        } catch (e: Exception) {
            Log.e(tag, "getAgeingReport API failed: ${e.message}, using mock", e)
            getMockAgeingReport(type)
        }
    }

    override suspend fun getRecoveryPipeline(): List<OutstandingItem> {
        return try {
            val response = apiService.getRecoveryPipeline()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapItemDtoToDomain(it) }
            } else {
                getMockOutstandingItems(null, "Receivable", null, null)
            }
        } catch (e: Exception) {
            Log.e(tag, "getRecoveryPipeline API failed: ${e.message}, using mock", e)
            getMockOutstandingItems(null, "Receivable", null, null)
        }
    }

    override suspend fun updateRecoveryStatus(
        partyId: String,
        status: String,
        nextActionDate: String?,
        executive: String?
    ): Boolean {
        // Try calling remote, falls back gracefully to updating InMemory state
        try {
            val response = apiService.updateRecoveryStatus(
                UpdateRecoveryStatusRequest(partyId, status, nextActionDate, executive)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                // Succeeded remotely
            }
        } catch (e: Exception) {
            Log.w(tag, "updateRecoveryStatus remote failed: ${e.message}. Updating in-memory fallback state.")
        }

        val current = recoveryStates[partyId] ?: RecoveryTrackData(
            recoveryStatus = status,
            assignedExecutive = executive ?: "Unassigned",
            followUpCount = 0,
            lastContactDate = "2026-06-20",
            nextActionDate = nextActionDate
        )
        recoveryStates[partyId] = current.copy(
            recoveryStatus = status,
            assignedExecutive = executive ?: current.assignedExecutive,
            followUpCount = current.followUpCount + 1,
            lastContactDate = "2026-06-20",
            nextActionDate = nextActionDate
        )
        return true
    }

    // === Mapper Helpers ===

    private fun mapItemDtoToDomain(dto: OutstandingItemDto): OutstandingItem {
        val overrideTrack = recoveryStates[dto.partyId]
        return OutstandingItem(
            partyId = dto.partyId,
            partyName = dto.partyName,
            type = dto.type,
            groupName = dto.groupName,
            outstandingAmount = dto.outstandingAmount,
            overdueAmount = dto.overdueAmount,
            lastContactDate = overrideTrack?.lastContactDate ?: dto.lastContactDate,
            assignedExecutive = overrideTrack?.assignedExecutive ?: dto.assignedExecutive,
            billingState = overrideTrack?.recoveryStatus ?: dto.billingState
        )
    }

    private fun mapDetailDtoToDomain(dto: OutstandingDetailDto): OutstandingDetail {
        val overrideTrack = recoveryStates[dto.partyId]
        return OutstandingDetail(
            partyId = dto.partyId,
            partyName = dto.partyName,
            type = dto.type,
            phone = dto.phone,
            email = dto.email,
            ledgerClosingBalance = dto.ledgerClosingBalance,
            creditLimit = dto.creditLimit,
            outstandingAmount = dto.outstandingAmount,
            billingHistory = dto.billingHistory.map {
                OutstandingInvoice(it.billId, it.date, it.amount, it.pendingAmount, it.dueDate, it.daysOverdue, it.status)
            },
            paymentHistory = dto.paymentHistory.map {
                OutstandingPayment(it.paymentId, it.date, it.amount, it.mode)
            },
            recoveryTracking = if (overrideTrack != null) overrideTrack else RecoveryTrackData(
                dto.recoveryTracking.recoveryStatus,
                dto.recoveryTracking.assignedExecutive,
                dto.recoveryTracking.followUpCount,
                dto.recoveryTracking.lastContactDate,
                dto.recoveryTracking.nextActionDate
            )
        )
    }

    // === Mock Resilient Database ===

    private fun getMockDashboardData() = OutstandingDashboardData(
        totalReceivables = 1438300.0,
        totalPayables = 475000.0,
        overdueAmount = 641300.0,
        collectionsThisMonth = 380400.0
    )

    private fun getMockOutstandingItems(
        query: String?,
        type: String?,
        group: String?,
        sort: String?
    ): List<OutstandingItem> {
        val rawItems = listOf(
            OutstandingItem(
                partyId = "cust_01",
                partyName = "Acme Distributors Pvt Ltd",
                type = "Receivable",
                groupName = "Sundry Debtors",
                outstandingAmount = 284300.0,
                overdueAmount = 159300.0,
                lastContactDate = "2026-06-15",
                assignedExecutive = "Rajesh Kumar",
                billingState = "Reminder Sent"
            ),
            OutstandingItem(
                partyId = "cust_02",
                partyName = "Starlight Retail Enterprises",
                type = "Receivable",
                groupName = "Sundry Debtors",
                outstandingAmount = 42000.0,
                overdueAmount = 0.0,
                lastContactDate = "2026-06-18",
                assignedExecutive = "Anita Desai",
                billingState = "Normal"
            ),
            OutstandingItem(
                partyId = "cust_03",
                partyName = "Vertex Corporate Solutions",
                type = "Receivable",
                groupName = "Sundry Debtors",
                outstandingAmount = 750000.0,
                overdueAmount = 430000.0,
                lastContactDate = "2026-06-10",
                assignedExecutive = "Rajesh Kumar",
                billingState = "Promised"
            ),
            OutstandingItem(
                partyId = "cust_04",
                partyName = "Global Warehousing Agency",
                type = "Receivable",
                groupName = "Sundry Debtors",
                outstandingAmount = 312000.0,
                overdueAmount = 20000.0,
                lastContactDate = "2026-06-19",
                assignedExecutive = "Anita Desai",
                billingState = "Paid"
            ),
            OutstandingItem(
                partyId = "vend_01",
                partyName = "Sigma Electronics Supply",
                type = "Payable",
                groupName = "Sundry Creditors",
                outstandingAmount = 180000.0,
                overdueAmount = 30000.0,
                lastContactDate = "2026-06-05",
                assignedExecutive = "Finance Desk",
                billingState = "Normal"
            ),
            OutstandingItem(
                partyId = "vend_02",
                partyName = "Omega Logistics Ltd",
                type = "Payable",
                groupName = "Sundry Creditors",
                outstandingAmount = 295000.0,
                overdueAmount = 0.0,
                lastContactDate = null,
                assignedExecutive = "Finance Desk",
                billingState = "Normal"
            )
        )

        var list = rawItems

        // Apply type filter
        if (type != null) {
            list = list.filter { it.type.equals(type, ignoreCase = true) }
        }

        // Apply Search
        if (!query.isNullOrBlank()) {
            list = list.filter {
                it.partyName.contains(query, ignoreCase = true) ||
                        it.groupName.contains(query, ignoreCase = true)
            }
        }

        // Apply Group filter
        if (!group.isNullOrBlank() && group != "All") {
            list = list.filter { it.groupName.equals(group, ignoreCase = true) }
        }

        // Apply Sort
        list = when (sort?.lowercase()?.trim()) {
            "outstanding desc" -> list.sortedByDescending { it.outstandingAmount }
            "outstanding asc" -> list.sortedBy { it.outstandingAmount }
            "overdue desc" -> list.sortedByDescending { it.overdueAmount }
            "party name" -> list.sortedBy { it.partyName }
            else -> list.sortedByDescending { it.overdueAmount } // Default sorted by overdue
        }

        // Real-time update check
        return list.map { item ->
            val overrideTrack = recoveryStates[item.partyId]
            if (overrideTrack != null) {
                item.copy(
                    lastContactDate = overrideTrack.lastContactDate,
                    assignedExecutive = overrideTrack.assignedExecutive,
                    billingState = overrideTrack.recoveryStatus
                )
            } else {
                item
            }
        }
    }

    private fun getMockOutstandingDetail(partyId: String): OutstandingDetail {
        val type = if (partyId.startsWith("vend_")) "Payable" else "Receivable"
        val isAcme = partyId == "cust_01"

        val billing = if (isAcme) {
            listOf(
                OutstandingInvoice("INV-26-8801", "2026-05-01", 150000.0, 150000.0, "2026-05-15", 36, "Overdue"),
                OutstandingInvoice("INV-26-9042", "2026-05-20", 134300.0, 134300.0, "2026-06-03", 17, "Overdue")
            )
        } else {
            listOf(
                OutstandingInvoice("INV-26-4412", "2026-05-10", 80000.0, 42000.0, "2026-05-24", 27, "Partial")
            )
        }

        val payments = if (isAcme) {
            listOf(
                OutstandingPayment("PAY-9921", "2026-04-12", 45000.0, "Bank Transfer"),
                OutstandingPayment("PAY-1002", "2026-05-02", 50000.0, "Cheque Cleared")
            )
        } else {
            listOf(
                OutstandingPayment("PAY-4412", "2026-05-20", 38000.0, "Cash Match")
            )
        }

        val overrideTrack = recoveryStates[partyId]
        val track = overrideTrack ?: RecoveryTrackData(
            recoveryStatus = if (isAcme) "Reminder Sent" else "Normal",
            assignedExecutive = if (isAcme) "Rajesh Kumar" else "Anita Desai",
            followUpCount = if (isAcme) 3 else 1,
            lastContactDate = if (isAcme) "2026-06-15" else "2026-06-18",
            nextActionDate = if (isAcme) "2026-06-25" else null
        )

        return OutstandingDetail(
            partyId = partyId,
            partyName = if (partyId == "cust_01") "Acme Distributors Pvt Ltd" else if (partyId == "cust_03") "Vertex Corporate Solutions" else "Starlight Retail Enterprises",
            type = type,
            phone = if (isAcme) "+91 98765 43210" else "+91 87654 32109",
            email = if (isAcme) "billing@acmedistributors.com" else "info@starlightretail.in",
            ledgerClosingBalance = if (isAcme) 284300.0 else 42000.0,
            creditLimit = if (isAcme) 500000.0 else 100000.0,
            outstandingAmount = if (isAcme) 284300.0 else 42000.0,
            billingHistory = billing,
            paymentHistory = payments,
            recoveryTracking = track
        )
    }

    private fun getMockAgeingReport(type: String): AgeingReportData {
        val totalAmount = if (type == "Payable") 475000.0 else 1438300.0
        val buckets = if (type == "Payable") {
            listOf(
                AgeingBucket("Current", 295000.0, 1, 62.1),
                AgeingBucket("0-30 Days", 150000.0, 1, 31.6),
                AgeingBucket("31-60 Days", 30000.0, 1, 6.3),
                AgeingBucket("61-90 Days", 0.0, 0, 0.0),
                AgeingBucket("90+ Days", 0.0, 0, 0.0)
            )
        } else {
            listOf(
                AgeingBucket("Current", 42000.0, 1, 2.9),
                AgeingBucket("0-30 Days", 446300.0, 2, 31.0),
                AgeingBucket("31-60 Days", 150000.0, 1, 10.4),
                AgeingBucket("61-90 Days", 400000.0, 1, 27.8),
                AgeingBucket("90+ Days", 400000.0, 1, 27.8)
            )
        }

        val list = getMockOutstandingItems(null, type, null, null)
            .filter { it.overdueAmount > 0 }

        return AgeingReportData(
            totalAmount = totalAmount,
            buckets = buckets,
            criticalAccounts = list
        )
    }
}
