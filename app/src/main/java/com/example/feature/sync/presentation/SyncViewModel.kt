package com.example.feature.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.sync.domain.models.*
import com.example.feature.sync.domain.repository.ISyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SyncViewModel(
    private val repository: ISyncRepository
) : ViewModel() {

    val stats: StateFlow<SyncDashboardStats> = repository.getDashboardStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncDashboardStats(
                lastSyncTime = "--:--:--",
                nextScheduledSync = "--:--:--",
                syncHealthStatus = "Healthy",
                pendingSyncCount = 0,
                failedSyncCount = 0,
                isActiveSyncProgress = false,
                progressPercentage = 0f
            )
        )

    val queue: StateFlow<List<SyncQueueRecord>> = repository.getQueueRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val errors: StateFlow<List<SyncErrorRecord>> = repository.getErrorRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val jobs: StateFlow<List<WorkManagerJobInfo>> = repository.getWorkManagerJobs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val analytics: StateFlow<SyncAnalyticsData> = repository.getAnalytics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncAnalyticsData(
                totalRecordsSynced = 0,
                averageSyncTimeMs = 0,
                successRate = 100f,
                failureRate = 0f,
                hourlySyncCounts = emptyMap()
            )
        )

    fun syncAll() {
        viewModelScope.launch {
            repository.triggerSyncAll()
        }
    }

    fun syncType(recordType: String) {
        viewModelScope.launch {
            repository.triggerSyncType(recordType)
        }
    }

    fun retryRecord(id: String) {
        viewModelScope.launch {
            repository.retryRecord(id)
        }
    }

    fun clearError(id: String) {
        viewModelScope.launch {
            repository.clearError(id)
        }
    }

    fun clearAllErrors() {
        viewModelScope.launch {
            repository.clearAllErrors()
        }
    }
}
