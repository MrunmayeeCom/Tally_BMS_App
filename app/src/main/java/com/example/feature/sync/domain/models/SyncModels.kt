package com.example.feature.sync.domain.models

import java.io.Serializable

data class SyncDashboardStats(
    val lastSyncTime: String,
    val nextScheduledSync: String,
    val syncHealthStatus: String, // "Healthy", "Warning", "Critical"
    val pendingSyncCount: Int,
    val failedSyncCount: Int,
    val isActiveSyncProgress: Boolean,
    val progressPercentage: Float // 0f to 1f
) : Serializable

data class SyncQueueRecord(
    val id: String,
    val recordType: String, // "Voucher", "Customer", "Ledger", "Inventory", "Order", "FollowUp", "CheckIn"
    val description: String,
    val priority: String, // "High", "Medium", "Low"
    val retryCount: Int,
    val status: String, // "Pending", "Running", "Failed"
    val timestamp: Long
) : Serializable

data class SyncErrorRecord(
    val id: String,
    val recordType: String,
    val description: String,
    val errorMessage: String,
    val failedAt: String,
    val retryCount: Int
) : Serializable

data class WorkManagerJobInfo(
    val jobName: String,
    val status: String, // "Enqueued", "Running", "Succeeded", "Failed", "Blocked", "Cancelled"
    val lastExecution: String,
    val nextExecution: String,
    val jobHealth: String // "Excellent", "Suspended", "Degraded"
) : Serializable

data class SyncAnalyticsData(
    val totalRecordsSynced: Int,
    val averageSyncTimeMs: Long,
    val successRate: Float, // e.g. 98.5f
    val failureRate: Float, // e.g. 1.5f
    val hourlySyncCounts: Map<String, Int> // e.g., "09:00" -> 15
) : Serializable
