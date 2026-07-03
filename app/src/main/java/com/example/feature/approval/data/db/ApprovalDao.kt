package com.example.feature.approval.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM approvals WHERE companyId = :companyId ORDER BY date DESC")
    suspend fun getApprovalsByCompany(companyId: String): List<LocalApproval>

    @Query("SELECT * FROM approvals WHERE id = :id")
    suspend fun getApprovalById(id: String): LocalApproval?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovals(approvals: List<LocalApproval>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: LocalApproval)

    @Query("SELECT * FROM approvals WHERE pendingSync = 1")
    suspend fun getPendingSyncApprovals(): List<LocalApproval>

    @Query("UPDATE approvals SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Update
    suspend fun updateApproval(approval: LocalApproval)
}
