package com.example.feature.salesteam.data

import android.util.Log
import com.example.feature.salesteam.domain.*
import java.text.SimpleDateFormat
import java.util.*

class SalesTeamRepositoryImpl(
    private val apiService: SalesTeamApiService
) : ISalesTeamRepository {

    private val tag = "SalesTeamRepositoryImpl"

    // === Live Mock Cache to simulate mutations (Check-In, Check-Out, Visits, Activity Feed) ===
    private val mockMembers = mutableListOf(
        SalesTeamMember("user_01", "Rajesh Kumar", "rajesh.k@tallybms.com", "+91 98765 43210", "Sales Executive", "Active", "Mumbai South", "Checked-In", "09:30 AM", null, 3, 0.78f),
        SalesTeamMember("user_02", "Anjali Sharma", "anjali.s@tallybms.com", "+91 98765 43211", "Sales Executive", "Active", "Delhi NCR", "Not Checked-In", null, null, 0, 0.52f),
        SalesTeamMember("user_03", "Amit Patel", "amit.p@tallybms.com", "+91 98765 43212", "Sales Manager", "Active", "Mumbai West", "Checked-Out", "09:15 AM", "05:45 PM", 5, 0.89f),
        SalesTeamMember("user_04", "Sunita Rao", "sunita.r@tallybms.com", "+91 98765 43213", "Recovery Agent", "Active", "Bangalore Zone 1", "Checked-In", "10:05 AM", null, 2, 0.65f),
        SalesTeamMember("user_05", "Vikram Singh", "vikram.s@tallybms.com", "+91 98765 43214", "Sales Executive", "Inactive", "Kolkata H.Q.", "Not Checked-In", null, null, 0, 0.0f)
    )

    private val mockCheckIns = mutableListOf(
        CheckInRecord(
            id = "chk_101",
            userId = "user_01",
            userName = "Rajesh Kumar",
            customerId = "cust_01",
            customerName = "Reliance Retail",
            checkInTime = "2026-06-20 09:30 AM",
            checkOutTime = null,
            checkInLocation = GPSLocation(18.922, 72.834, "Colaba Causeway, Mumbai"),
            checkOutLocation = null,
            isLocationValid = true,
            remarks = "Regular collection visit"
        ),
        CheckInRecord(
            id = "chk_102",
            userId = "user_03",
            userName = "Amit Patel",
            customerId = "cust_02",
            customerName = "Tata Motors Service",
            checkInTime = "2026-06-20 09:15 AM",
            checkOutTime = "2026-06-20 11:30 AM",
            checkInLocation = GPSLocation(19.076, 72.877, "Bandra Kurla Complex, Mumbai"),
            checkOutLocation = GPSLocation(19.077, 72.878, "Bandra Kurla Complex, Mumbai"),
            isLocationValid = true,
            remarks = "Quarterly alignment meeting"
        )
    )

    private val mockVisits = mutableListOf(
        CustomerVisit(
            id = "v_201",
            customerId = "cust_01",
            customerName = "Reliance Retail",
            performedByUserId = "user_01",
            performedByUserName = "Rajesh Kumar",
            visitStartTime = "09:30 AM",
            visitEndTime = "10:45 AM",
            notes = "Discussed outstanding invoices. Received promise to clear all duedates by next Monday.",
            outcomeBadge = "Promise to Pay",
            collectedAmount = 0.0,
            nextActionPlanned = "Follow-up Call",
            nextVisitDate = "2026-06-24",
            location = GPSLocation(18.922, 72.834, "Colaba Causeway, Mumbai")
        ),
        CustomerVisit(
            id = "v_202",
            customerId = "cust_02",
            customerName = "Tata Motors Service",
            performedByUserId = "user_03",
            performedByUserName = "Amit Patel",
            visitStartTime = "09:15 AM",
            visitEndTime = "11:30 AM",
            notes = "Collected overdue balance invoice and discussed new trade agreement. Paid by cheque.",
            outcomeBadge = "Collected",
            collectedAmount = 45000.0,
            nextActionPlanned = "Deliver Receipt",
            nextVisitDate = "2026-06-22",
            location = GPSLocation(19.076, 72.877, "Bandra Kurla Complex, Mumbai")
        )
    )

    private val mockActivityFeed = mutableListOf(
        SalesTeamActivityEvent("act_301", "Check-In", "2026-06-20T09:30:00", "Rajesh Kumar", "Checked-in at Reliance Retail", customerName = "Reliance Retail"),
        SalesTeamActivityEvent("act_302", "Check-In", "2026-06-20T09:15:00", "Amit Patel", "Checked-in at Tata Motors Service", customerName = "Tata Motors Service"),
        SalesTeamActivityEvent("act_303", "Collection", "2026-06-20T11:30:00", "Amit Patel", "Collected payment of ₹45,000 via Invoice Payment", amount = 45000.0, customerName = "Tata Motors Service"),
        SalesTeamActivityEvent("act_304", "Check-Out", "2026-06-20T11:30:00", "Amit Patel", "Checked-out from Tata Motors Service", customerName = "Tata Motors Service"),
        SalesTeamActivityEvent("act_305", "Check-In", "2026-06-20T10:05:00", "Sunita Rao", "Checked-in at HDFC Bank Head Branch", customerName = "HDFC Bank"),
        SalesTeamActivityEvent("act_306", "Follow-Up", "2026-06-20T08:00:00", "Rajesh Kumar", "Logged follow-up reminder with Aditya Birla Lifestyle", customerName = "Aditya Birla")
    )

    override suspend fun getDashboardStats(companyId: String): SalesTeamDashboardStats {
        return try {
            val response = apiService.getDashboardStats(companyId)
            if (response.isSuccessful && response.body() != null) {
                mapDashboardStatsDtoToDomain(response.body()!!)
            } else {
                Log.w(tag, "Stats API unsuccessful: ${response.code()}. Using local mock statistics.")
                getMockDashboardStats()
            }
        } catch (e: Exception) {
            Log.e(tag, "Stats API failed: ${e.message}", e)
            getMockDashboardStats()
        }
    }

    override suspend fun getTeamMembers(
        companyId: String,
        query: String?,
        territory: String?,
        role: String?
    ): List<SalesTeamMember> {
        return try {
            val response = apiService.getTeamMembers(companyId, query, territory, role)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapMemberDtoToDomain(it) }
            } else {
                Log.w(tag, "Members API unsuccessful: ${response.code()}. Using mock cache.")
                getMockMembersFiltered(query, territory, role)
            }
        } catch (e: Exception) {
            Log.e(tag, "Members API failed: ${e.message}", e)
            getMockMembersFiltered(query, territory, role)
        }
    }

    override suspend fun getMemberDetail(userId: String): SalesTeamMember? {
        return try {
            val response = apiService.getMemberDetail(userId)
            if (response.isSuccessful && response.body() != null) {
                mapMemberDtoToDomain(response.body()!!)
            } else {
                Log.w(tag, "Member Detail API unsuccessful. Reading mock cache.")
                mockMembers.find { it.id == userId }
            }
        } catch (e: Exception) {
            Log.e(tag, "Member Detail API failed: ${e.message}", e)
            mockMembers.find { it.id == userId }
        }
    }

    override suspend fun getMemberPerformance(userId: String, timeframe: String): SalesRepPerformanceMetrics {
        return try {
            val response = apiService.getMemberPerformance(userId, timeframe)
            if (response.isSuccessful && response.body() != null) {
                mapPerformanceDtoToDomain(response.body()!!)
            } else {
                Log.w(tag, "Performance API unsuccessful. Using local chart simulation.")
                getMockPerformanceMetrics(userId)
            }
        } catch (e: Exception) {
            Log.e(tag, "Performance API failed: ${e.message}", e)
            getMockPerformanceMetrics(userId)
        }
    }

    override suspend fun performCheckIn(
        userId: String,
        userName: String,
        customerId: String,
        customerName: String,
        latitude: Double,
        longitude: Double,
        address: String,
        remarks: String?
    ): CheckInRecord {
        val todayStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        try {
            val request = TeamCheckInRequest(userId, userName, customerId, customerName, latitude, longitude, address, remarks)
            val response = apiService.performCheckIn(request)
            if (response.isSuccessful && response.body() != null) {
                val record = mapCheckInDtoToDomain(response.body()!!)
                mockCheckIns.add(record)
                updateMemberCheckInState(userId, "Checked-In", todayStr.substringAfter(" "))
                return record
            }
        } catch (e: Exception) {
            Log.e(tag, "CheckIn API failed: ${e.message}. Using offline-first simulation", e)
        }

        // Offline Simulation
        val newCheckIn = CheckInRecord(
            id = "chk_" + UUID.randomUUID().toString().substring(0, 6),
            userId = userId,
            userName = userName,
            customerId = customerId,
            customerName = customerName,
            checkInTime = todayStr,
            checkOutTime = null,
            checkInLocation = GPSLocation(latitude, longitude, address),
            checkOutLocation = null,
            isLocationValid = validateLocationWithinBounds(latitude, longitude),
            remarks = remarks
        )
        mockCheckIns.add(newCheckIn)
        updateMemberCheckInState(userId, "Checked-In", todayStr.substringAfter(" "))
        addActivityEvent("Check-In", userName, "Checked-in at $customerName", customerName = customerName)
        return newCheckIn
    }

    override suspend fun performCheckOut(
        checkInId: String,
        latitude: Double,
        longitude: Double,
        address: String,
        remarks: String?
    ): CheckInRecord {
        val todayStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        try {
            val request = TeamCheckOutRequest(latitude, longitude, address, remarks)
            val response = apiService.performCheckOut(checkInId, request)
            if (response.isSuccessful && response.body() != null) {
                val record = mapCheckInDtoToDomain(response.body()!!)
                replaceCheckInInCache(record)
                return record
            }
        } catch (e: Exception) {
            Log.e(tag, "CheckOut API failed: ${e.message}. Running offline transaction local merge", e)
        }

        // Offline Simulation
        val matchedIdx = mockCheckIns.indexOfFirst { it.id == checkInId }
        val updated = if (matchedIdx != -isLocationValidPlaceholder().toInt() && matchedIdx >= 0) {
            val original = mockCheckIns[matchedIdx]
            val record = original.copy(
                checkOutTime = todayStr,
                checkOutLocation = GPSLocation(latitude, longitude, address),
                isLocationValid = original.isLocationValid && validateLocationWithinBounds(latitude, longitude),
                remarks = remarks ?: original.remarks
            )
            mockCheckIns[matchedIdx] = record
            updateMemberCheckInState(original.userId, "Checked-Out", null, todayStr.substringAfter(" "))
            addActivityEvent("Check-Out", original.userName, "Checked-out from ${original.customerName}", customerName = original.customerName)
            record
        } else {
            CheckInRecord(
                id = checkInId,
                userId = "user_01",
                userName = "Rajesh Kumar",
                customerId = "cust_01",
                customerName = "Reliance Retail",
                checkInTime = "2026-06-20 09:30 AM",
                checkOutTime = todayStr,
                checkInLocation = GPSLocation(18.922, 72.834, "Colaba Causeway"),
                checkOutLocation = GPSLocation(latitude, longitude, address),
                isLocationValid = true,
                remarks = remarks
            )
        }
        return updated
    }

    override suspend fun getCurrentCheckIn(userId: String): CheckInRecord? {
        return try {
            val response = apiService.getCurrentCheckIn(userId)
            if (response.isSuccessful && response.body() != null) {
                mapCheckInDtoToDomain(response.body()!!)
            } else {
                mockCheckIns.find { it.userId == userId && it.checkOutTime == null }
            }
        } catch (e: Exception) {
            Log.e(tag, "Current CheckIn API failed: ${e.message}", e)
            mockCheckIns.find { it.userId == userId && it.checkOutTime == null }
        }
    }

    override suspend fun getCheckInHistory(userId: String?, companyId: String): List<CheckInRecord> {
        return try {
            val response = apiService.getCheckInHistory(userId, companyId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapCheckInDtoToDomain(it) }
            } else {
                if (userId != null) {
                    mockCheckIns.filter { it.userId == userId }
                } else {
                    mockCheckIns
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "CheckIn History API failed", e)
            if (userId != null) {
                mockCheckIns.filter { it.userId == userId }
            } else {
                mockCheckIns
            }
        }
    }

    override suspend fun getVisitHistory(companyId: String, customerId: String?, userId: String?): List<CustomerVisit> {
        return try {
            val response = apiService.getVisitHistory(companyId, customerId, userId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapVisitDtoToDomain(it) }
            } else {
                getMockVisitsFiltered(customerId, userId)
            }
        } catch (e: Exception) {
            Log.e(tag, "Visit History API failed: ${e.message}", e)
            getMockVisitsFiltered(customerId, userId)
        }
    }

    override suspend fun createCustomerVisit(
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
    ): CustomerVisit {
        val visitTime = SimpleDateFormat("hh:mm AM", Locale.getDefault()).format(Date())
        try {
            val request = TeamCreateVisitRequest(customerId, customerName, userId, userName, notes, outcomeBadge, collectedAmount, nextActionPlanned, nextVisitDate, latitude, longitude, address)
            val response = apiService.createCustomerVisit(request)
            if (response.isSuccessful && response.body() != null) {
                val visit = mapVisitDtoToDomain(response.body()!!)
                mockVisits.add(visit)
                return visit
            }
        } catch (e: Exception) {
            Log.e(tag, "Create Visit API failed: ${e.message}", e)
        }

        // Offline Simulation
        val newVisit = CustomerVisit(
            id = "v_" + UUID.randomUUID().toString().substring(0, 6),
            customerId = customerId,
            customerName = customerName,
            performedByUserId = userId,
            performedByUserName = userName,
            visitStartTime = visitTime,
            visitEndTime = null,
            notes = notes,
            outcomeBadge = outcomeBadge,
            collectedAmount = collectedAmount,
            nextActionPlanned = nextActionPlanned,
            nextVisitDate = nextVisitDate,
            location = GPSLocation(latitude, longitude, address)
        )
        mockVisits.add(newVisit)
        addActivityEvent("Visit", userName, "Logged visit for $customerName with status '$outcomeBadge'", collectedAmount, customerName)
        if (collectedAmount > 0) {
            addActivityEvent("Collection", userName, "Collected payments worth ₹%,.2f from $customerName".format(collectedAmount), collectedAmount, customerName)
        }
        return newVisit
    }

    override suspend fun getActivityFeed(companyId: String, filterType: String?): List<SalesTeamActivityEvent> {
        return try {
            val response = apiService.getActivityFeed(companyId, filterType)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapActivityDtoToDomain(it) }
            } else {
                if (filterType.isNullOrBlank() || filterType == "All") {
                    mockActivityFeed.sortedByDescending { it.timestamp }
                } else {
                    mockActivityFeed.filter { it.type.lowercase() == filterType.lowercase() }.sortedByDescending { it.timestamp }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Activity Feed Error: ${e.message}", e)
            if (filterType.isNullOrBlank() || filterType == "All") {
                mockActivityFeed.sortedByDescending { it.timestamp }
            } else {
                mockActivityFeed.filter { it.type.lowercase() == filterType.lowercase() }.sortedByDescending { it.timestamp }
            }
        }
    }

    override suspend fun updateUserStatusAndTerritory(
        userId: String,
        status: String,
        territory: String,
        role: String
    ): Boolean {
        try {
            val response = apiService.updateMemberStatus(userId, MemberStatusUpdateRequest(status, territory, role))
            if (response.isSuccessful && response.body() != null) {
                updateMemberLocalCache(userId, status, territory, role)
                return response.body()!!
            }
        } catch (e: Exception) {
            Log.e(tag, "Update status API failed: ${e.message}", e)
        }
        updateMemberLocalCache(userId, status, territory, role)
        return true
    }

    // === Mapper Helpers ===

    private fun mapDashboardStatsDtoToDomain(dto: SalesTeamDashboardStatsDto) = SalesTeamDashboardStats(
        activeUsersCount = dto.activeUsersCount,
        todayVisitsCount = dto.todayVisitsCount,
        collectionsTodayAmount = dto.collectionsTodayAmount,
        openFollowUpsCount = dto.openFollowUpsCount,
        pendingTasksCount = dto.pendingTasksCount
    )

    private fun mapMemberDtoToDomain(dto: SalesTeamMemberDto) = SalesTeamMember(
        id = dto.id,
        name = dto.name,
        email = dto.email,
        phone = dto.phone,
        role = dto.role,
        status = dto.status,
        territory = dto.territory,
        todayCheckInStatus = dto.todayCheckInStatus,
        todayCheckInTime = dto.todayCheckInTime,
        todayCheckOutTime = dto.todayCheckOutTime,
        todayVisitCount = dto.todayVisitCount,
        performanceProgress = dto.performanceProgress
    )

    private fun mapPerformanceDtoToDomain(dto: SalesRepPerformanceMetricsDto) = SalesRepPerformanceMetrics(
        repId = dto.repId,
        repName = dto.repName,
        visitsCompleted = dto.visitsCompleted,
        visitsTarget = dto.visitsTarget,
        collectionsAchieved = dto.collectionsAchieved,
        collectionsTarget = dto.collectionsTarget,
        followUpsCompleted = dto.followUpsCompleted,
        followUpsTarget = dto.followUpsTarget,
        recoverySuccessRate = dto.recoverySuccessRate,
        dailyProgress = dto.dailyProgress.map { mapChartPointDtoToDomain(it) },
        weeklyProgress = dto.weeklyProgress.map { mapChartPointDtoToDomain(it) },
        monthlyProgress = dto.monthlyProgress.map { mapChartPointDtoToDomain(it) }
    )

    private fun mapChartPointDtoToDomain(dto: PerformanceChartPointDto) = PerformanceChartPoint(
        label = dto.label,
        target = dto.target,
        achievement = dto.achievement
    )

    private fun mapCheckInDtoToDomain(dto: CheckInRecordDto) = CheckInRecord(
        id = dto.id,
        userId = dto.userId,
        userName = dto.userName,
        customerId = dto.customerId,
        customerName = dto.customerName,
        checkInTime = dto.checkInTime,
        checkOutTime = dto.checkOutTime,
        checkInLocation = GPSLocation(dto.checkInLocation.latitude, dto.checkInLocation.longitude, dto.checkInLocation.address),
        checkOutLocation = dto.checkOutLocation?.let { GPSLocation(it.latitude, it.longitude, it.address) },
        isLocationValid = dto.isLocationValid,
        remarks = dto.remarks
    )

    private fun mapVisitDtoToDomain(dto: CustomerVisitDto) = CustomerVisit(
        id = dto.id,
        customerId = dto.customerId,
        customerName = dto.customerName,
        performedByUserId = dto.performedByUserId,
        performedByUserName = dto.performedByUserName,
        visitStartTime = dto.visitStartTime,
        visitEndTime = dto.visitEndTime,
        notes = dto.notes,
        outcomeBadge = dto.outcomeBadge,
        collectedAmount = dto.collectedAmount,
        nextActionPlanned = dto.nextActionPlanned,
        nextVisitDate = dto.nextVisitDate,
        location = GPSLocation(dto.location.latitude, dto.location.longitude, dto.location.address)
    )

    private fun mapActivityDtoToDomain(dto: SalesTeamActivityEventDto) = SalesTeamActivityEvent(
        id = dto.id,
        type = dto.type,
        timestamp = dto.timestamp,
        performedByUserName = dto.performedByUserName,
        description = dto.description,
        amount = dto.amount,
        customerName = dto.customerName
    )

    // === Simulation Helpers ===

    private fun getMockDashboardStats() = SalesTeamDashboardStats(
        activeUsersCount = mockMembers.count { it.todayCheckInStatus == "Checked-In" },
        todayVisitsCount = mockVisits.size,
        collectionsTodayAmount = mockVisits.sumOf { it.collectedAmount },
        openFollowUpsCount = 14,
        pendingTasksCount = 8
    )

    private fun getMockMembersFiltered(query: String?, territory: String?, role: String?): List<SalesTeamMember> {
        var list = mockMembers.toList()
        if (!query.isNullOrBlank()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) || it.territory.contains(query, ignoreCase = true) }
        }
        if (!territory.isNullOrBlank() && territory != "All") {
            list = list.filter { it.territory.lowercase() == territory.lowercase() }
        }
        if (!role.isNullOrBlank() && role != "All") {
            list = list.filter { it.role.lowercase() == role.lowercase() }
        }
        return list
    }

    private fun getMockPerformanceMetrics(userId: String): SalesRepPerformanceMetrics {
        val member = mockMembers.find { it.id == userId } ?: mockMembers[0]
        return SalesRepPerformanceMetrics(
            repId = userId,
            repName = member.name,
            visitsCompleted = if (userId == "user_01") 18 else 12,
            visitsTarget = 24,
            collectionsAchieved = if (userId == "user_01") 280000.0 else 150000.0,
            collectionsTarget = 300000.0,
            followUpsCompleted = if (userId == "user_01") 22 else 10,
            followUpsTarget = 25,
            recoverySuccessRate = if (userId == "user_01") 92.5 else 75.0,
            dailyProgress = listOf(
                PerformanceChartPoint("Mon", 4.0, 3.0),
                PerformanceChartPoint("Tue", 4.0, 5.0),
                PerformanceChartPoint("Wed", 4.0, 4.0),
                PerformanceChartPoint("Thu", 4.0, 3.0),
                PerformanceChartPoint("Fri", 4.0, 4.0),
                PerformanceChartPoint("Sat", 4.0, 2.0),
                PerformanceChartPoint("Sun", 0.0, 0.0)
            ),
            weeklyProgress = listOf(
                PerformanceChartPoint("Week 1", 6.0, 5.0),
                PerformanceChartPoint("Week 2", 6.0, 7.0),
                PerformanceChartPoint("Week 3", 6.0, 6.0),
                PerformanceChartPoint("Week 4", 6.0, 4.0)
            ),
            monthlyProgress = listOf(
                PerformanceChartPoint("Apr", 20.0, 18.0),
                PerformanceChartPoint("May", 20.0, 23.0),
                PerformanceChartPoint("Jun", 24.0, 18.0)
            )
        )
    }

    private fun getMockVisitsFiltered(customerId: String?, userId: String?): List<CustomerVisit> {
        var list = mockVisits.toList()
        if (customerId != null) {
            list = list.filter { it.customerId == customerId }
        }
        if (userId != null) {
            list = list.filter { it.performedByUserId == userId }
        }
        return list
    }

    private fun updateMemberCheckInState(userId: String, status: String, checkInTime: String? = null, checkOutTime: String? = null) {
        val idx = mockMembers.indexOfFirst { it.id == userId }
        if (idx != -1) {
            val item = mockMembers[idx]
            mockMembers[idx] = item.copy(
                todayCheckInStatus = status,
                todayCheckInTime = checkInTime ?: item.todayCheckInTime,
                todayCheckOutTime = checkOutTime ?: item.todayCheckOutTime,
                todayVisitCount = if (status == "Checked-In") item.todayVisitCount + 1 else item.todayVisitCount
            )
        }
    }

    private fun updateMemberLocalCache(userId: String, status: String, territory: String, role: String) {
        val idx = mockMembers.indexOfFirst { it.id == userId }
        if (idx != -1) {
            val item = mockMembers[idx]
            mockMembers[idx] = item.copy(status = status, territory = territory, role = role)
        }
    }

    private fun replaceCheckInInCache(record: CheckInRecord) {
        val idx = mockCheckIns.indexOfFirst { it.id == record.id }
        if (idx != -1) {
            mockCheckIns[idx] = record
        } else {
            mockCheckIns.add(record)
        }
    }

    private fun addActivityEvent(type: String, user: String, desc: String, amount: Double? = null, customerName: String? = null) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        mockActivityFeed.add(0, SalesTeamActivityEvent(
            id = "act_" + UUID.randomUUID().toString().substring(0, 6),
            type = type,
            timestamp = nowStr,
            performedByUserName = user,
            description = desc,
            amount = amount,
            customerName = customerName
        ))
    }

    private fun validateLocationWithinBounds(latitude: Double, longitude: Double): Boolean {
        // Mock range validation. Returns true for any coordinate. (Gps boundaries inside the territory range)
        return latitude != 0.0 && longitude != 0.0
    }

    private fun isLocationValidPlaceholder(): String {
        return "1"
    }
}
