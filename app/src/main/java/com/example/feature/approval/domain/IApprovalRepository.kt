package com.example.feature.approval.domain

import com.example.core.common.Resource
import com.example.feature.approval.domain.models.ApprovalAnalytics
import com.example.feature.approval.domain.models.ApprovalRequest
import com.example.feature.approval.domain.models.ApprovalStatus
import kotlinx.coroutines.flow.Flow

interface IApprovalRepository {
    fun getApprovals(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<ApprovalRequest>>>
    suspend fun getApprovalById(id: String): ApprovalRequest?
    suspend fun performAction(
        id: String,
        status: ApprovalStatus,
        remarks: String,
        operatorId: String,
        operatorName: String,
        operatorRole: String
    ): Boolean
    suspend fun getAnalytics(companyId: String, forceRefresh: Boolean = false): Flow<Resource<ApprovalAnalytics>>
    suspend fun createApprovalRequest(approvalRequest: ApprovalRequest): Boolean
}
