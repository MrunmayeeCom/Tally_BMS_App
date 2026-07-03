package com.example.feature.followup.domain

interface IFollowUpRepository {
    suspend fun getDashboardStats(): FollowUpDashboardStats

    suspend fun getFollowUps(
        query: String? = null,
        type: String? = null,
        priority: String? = null,
        status: String? = null,
        assignedUser: String? = null,
        customer: String? = null,
        sort: String? = null,
        page: Int = 1,
        pageSize: Int = 50
    ): List<FollowUp>

    suspend fun createFollowUp(
        customerId: String,
        customerName: String,
        type: String,
        notes: String,
        priority: String,
        dueDate: String,
        assignedUserId: String,
        assignedUserName: String
    ): FollowUp

    suspend fun getFollowUpDetail(id: String): FollowUpDetail

    suspend fun updateFollowUpStatus(
        id: String,
        status: String,
        outcome: String?,
        promisedAmount: Double? = null,
        promisedDate: String? = null
    ): FollowUp

    suspend fun addFollowUpNote(
        id: String,
        notes: String,
        operatorName: String
    ): Boolean
}
