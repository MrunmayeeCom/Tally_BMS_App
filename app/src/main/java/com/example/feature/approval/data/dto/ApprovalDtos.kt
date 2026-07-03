package com.example.feature.approval.data.dto

import com.example.feature.approval.domain.models.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkflowStepDto(
    val id: String,
    val date: String,
    val action: String,
    val executorName: String,
    val executorRole: String,
    val remarks: String?
)

@JsonClass(generateAdapter = true)
data class ApprovalChainStepDto(
    val role: String,
    val userId: String?,
    val userName: String?,
    val status: String,
    val sequence: Int
)

@JsonClass(generateAdapter = true)
data class ApprovalRequestDto(
    val id: String,
    val companyId: String,
    val tenantId: String,
    val requesterId: String,
    val requesterName: String,
    val type: String,
    val title: String,
    val description: String,
    val date: String,
    val status: String,
    val amount: Double?,
    val refId: String?,
    val remarks: String?,
    val history: List<WorkflowStepDto>,
    val chain: List<ApprovalChainStepDto>
)

@JsonClass(generateAdapter = true)
data class ApprovalActionDto(
    val action: String, // "APPROVE", "REJECT", "HOLD", "ESCALATE", "REQUEST_CHANGES"
    val remarks: String,
    val operatorId: String,
    val operatorName: String,
    val operatorRole: String
)

@JsonClass(generateAdapter = true)
data class ApprovalAnalyticsDto(
    val avgApprovalTimeHours: Double,
    val approvalRatePercent: Double,
    val rejectionRatePercent: Double,
    val pendingBottlenecks: List<String>
)
