package com.example.feature.sync.domain.repository

import com.example.feature.sync.domain.models.*
import kotlinx.coroutines.flow.Flow

interface ISyncRepository {
    fun getDashboardStats(): Flow<SyncDashboardStats>
    fun getQueueRecords(): Flow<List<SyncQueueRecord>>
    fun getErrorRecords(): Flow<List<SyncErrorRecord>>
    fun getWorkManagerJobs(): Flow<List<WorkManagerJobInfo>>
    fun getAnalytics(): Flow<SyncAnalyticsData>

    suspend fun triggerSyncAll()
    suspend fun triggerSyncType(recordType: String)
    suspend fun retryRecord(id: String)
    suspend fun clearError(id: String)
    suspend fun clearAllErrors()
}
