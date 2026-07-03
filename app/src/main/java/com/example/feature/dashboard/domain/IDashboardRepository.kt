package com.example.feature.dashboard.domain

interface IDashboardRepository {
    suspend fun getDashboardData(companyGuid: String = ""): DashboardData
}
