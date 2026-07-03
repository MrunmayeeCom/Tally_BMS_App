package com.example.feature.approval.domain.models

import com.squareup.moshi.JsonClass

enum class ApprovalType {
    QUOTATION,
    ORDER,
    VOUCHER,
    CREDIT_LIMIT,
    DISCOUNT,
    INVENTORY_ADJUSTMENT,
    USER_REQUEST
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    HELD,
    ESCALATED,
    CHANGES_REQUESTED
}

@JsonClass(generateAdapter = true)
data class WorkflowStep(
    val id: String,
    val date: String,
    val action: String,
    val executorName: String,
    val executorRole: String,
    val remarks: String?
)

@JsonClass(generateAdapter = true)
data class ApprovalChainStep(
    val role: String,
    val userId: String?,
    val userName: String?,
    val status: String,
    val sequence: Int
)

@JsonClass(generateAdapter = true)
data class ApprovalRequest(
    val id: String,
    val companyId: String,
    val tenantId: String,
    val requesterId: String,
    val requesterName: String,
    val type: ApprovalType,
    val title: String,
    val description: String,
    val date: String,
    val status: ApprovalStatus,
    val amount: Double?,
    val refId: String?,
    val remarks: String?,
    val history: List<WorkflowStep>,
    val chain: List<ApprovalChainStep>
)

data class ApprovalAnalytics(
    val avgApprovalTimeHours: Double,
    val approvalRatePercent: Double,
    val rejectionRatePercent: Double,
    val pendingBottlenecks: List<String>
)
