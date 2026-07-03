package com.example.feature.salesteam.domain

data class SalesTeamDashboardStats(
    val activeUsersCount: Int,
    val todayVisitsCount: Int,
    val collectionsTodayAmount: Double,
    val openFollowUpsCount: Int,
    val pendingTasksCount: Int
)

data class SalesTeamMember(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String, // "Sales Executive", "Sales Manager", "Recovery Agent"
    val status: String, // "Active", "Inactive"
    val territory: String, // e.g. "Mumbai South", "Delhi NCR"
    val todayCheckInStatus: String, // "Checked-In", "Not Checked-In", "Checked-Out"
    val todayCheckInTime: String? = null,
    val todayCheckOutTime: String? = null,
    val todayVisitCount: Int = 0,
    val performanceProgress: Float = 0.0f // target ratio (0.0 .. 1.0)
)

data class SalesRepPerformanceMetrics(
    val repId: String,
    val repName: String,
    val visitsCompleted: Int,
    val visitsTarget: Int,
    val collectionsAchieved: Double,
    val collectionsTarget: Double,
    val followUpsCompleted: Int,
    val followUpsTarget: Int,
    val recoverySuccessRate: Double, // percentage, e.g. 84.5
    val dailyProgress: List<PerformanceChartPoint>,
    val weeklyProgress: List<PerformanceChartPoint>,
    val monthlyProgress: List<PerformanceChartPoint>
)

data class PerformanceChartPoint(
    val label: String, // Date, Week number, or Month name
    val target: Double,
    val achievement: Double
)

data class GPSLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class CheckInRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val customerId: String,
    val customerName: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val checkInLocation: GPSLocation,
    val checkOutLocation: GPSLocation?,
    val isLocationValid: Boolean,
    val remarks: String?
)

data class CustomerVisit(
    val id: String,
    val customerId: String,
    val customerName: String,
    val performedByUserId: String,
    val performedByUserName: String,
    val visitStartTime: String,
    val visitEndTime: String?,
    val notes: String,
    val outcomeBadge: String, // "Promise to Pay", "Collected", "Not Available", "Rescheduled"
    val collectedAmount: Double,
    val nextActionPlanned: String?,
    val nextVisitDate: String?,
    val location: GPSLocation
)

data class SalesTeamActivityEvent(
    val id: String,
    val type: String, // "Check-In", "Check-Out", "Collection", "Follow-Up", "Reminder", "Visit"
    val timestamp: String,
    val performedByUserName: String,
    val description: String,
    val amount: Double? = null,
    val customerName: String? = null
)
