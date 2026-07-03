package com.example.feature.approval.data.repository

import com.example.core.common.Resource
import com.example.feature.approval.data.api.ApprovalApiService
import com.example.feature.approval.data.db.ApprovalDao
import com.example.feature.approval.data.db.LocalApproval
import com.example.feature.approval.data.dto.*
import com.example.feature.approval.domain.IApprovalRepository
import com.example.feature.approval.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ApprovalRepositoryImpl(
    private val apiService: ApprovalApiService,
    private val approvalDao: ApprovalDao
) : IApprovalRepository {

    override fun getApprovals(companyId: String, forceRefresh: Boolean): Flow<Resource<List<ApprovalRequest>>> = flow {
        emit(Resource.Loading)

        // 1. Load cached records first
        val cached = approvalDao.getApprovalsByCompany(companyId)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        // Seeding defaults on first startup if local state is blank
        if (cached.isEmpty()) {
            val seedList = getMockApprovals(companyId)
            approvalDao.insertApprovals(seedList.map { it.toLocal() })
            emit(Resource.Success(seedList))
        }

        // 2. Fetch from cloud if forceRefresh or list was cached
        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getApprovals(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val entityModels = dtos.map { it.toLocal() }

                    // Retain local creations that haven't been pushed to remote
                    val localUnsynced = approvalDao.getPendingSyncApprovals()

                    // Safe overwrite
                    approvalDao.insertApprovals(entityModels)
                    approvalDao.insertApprovals(localUnsynced)

                    val updatedList = approvalDao.getApprovalsByCompany(companyId)
                    emit(Resource.Success(updatedList.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                // Return cache cleanly or inform UI of state
                val finalCached = approvalDao.getApprovalsByCompany(companyId)
                emit(Resource.Success(finalCached.map { it.toDomain() }))
            }
        }
    }

    override suspend fun getApprovalById(id: String): ApprovalRequest? {
        return approvalDao.getApprovalById(id)?.toDomain()
    }

    override suspend fun performAction(
        id: String,
        status: ApprovalStatus,
        remarks: String,
        operatorId: String,
        operatorName: String,
        operatorRole: String
    ): Boolean {
        val existing = approvalDao.getApprovalById(id) ?: return false
        
        // Update local object
        val currentHistory = existing.history.toMutableList()
        val formattedDate = "23-Jun-2026" // Current system date or formatted timestamp
        currentHistory.add(
            WorkflowStep(
                id = UUID.randomUUID().toString().take(6),
                date = formattedDate,
                action = when (status) {
                    ApprovalStatus.APPROVED -> "Approved"
                    ApprovalStatus.REJECTED -> "Rejected"
                    ApprovalStatus.HELD -> "Held"
                    ApprovalStatus.ESCALATED -> "Escalated"
                    ApprovalStatus.CHANGES_REQUESTED -> "Requested Changes"
                    else -> "Reviewed"
                },
                executorName = operatorName,
                executorRole = operatorRole,
                remarks = remarks
            )
        )

        // Update active chain stage
        val updatedChain = existing.chain.map { step ->
            if (step.role == operatorRole && step.status == "Pending") {
                step.copy(status = status.name, userName = operatorName, userId = operatorId)
            } else {
                step
            }
        }

        val updatedLocal = existing.copy(
            status = status.name,
            remarks = remarks,
            history = currentHistory,
            chain = updatedChain,
            pendingSync = true
        )

        approvalDao.insertApproval(updatedLocal)

        // Remote synchronization
        try {
            val reqDto = ApprovalActionDto(
                action = status.name,
                remarks = remarks,
                operatorId = operatorId,
                operatorName = operatorName,
                operatorRole = operatorRole
            )
            val response = apiService.performAction(id, reqDto)
            if (response.isSuccessful && response.body() != null) {
                val syncedEntity = response.body()!!.toLocal().copy(pendingSync = false)
                approvalDao.insertApproval(syncedEntity)
                return true
            }
        } catch (_: Exception) {
            // Keep the local state modified and let the sync worker upload later
        }
        return true
    }

    override suspend fun getAnalytics(companyId: String, forceRefresh: Boolean): Flow<Resource<ApprovalAnalytics>> = flow {
        emit(Resource.Loading)
        
        // Calculate dynamic stats from local DB first as reliable fallback
        val cached = approvalDao.getApprovalsByCompany(companyId)
        val stats = calculateLocalAnalytics(cached)
        emit(Resource.Success(stats))

        if (forceRefresh) {
            try {
                val response = apiService.getAnalytics(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    val analytics = ApprovalAnalytics(
                        avgApprovalTimeHours = dto.avgApprovalTimeHours,
                        approvalRatePercent = dto.approvalRatePercent,
                        rejectionRatePercent = dto.rejectionRatePercent,
                        pendingBottlenecks = dto.pendingBottlenecks
                    )
                    emit(Resource.Success(analytics))
                }
            } catch (_: Exception) {
                // Fallback to local
            }
        }
    }

    override suspend fun createApprovalRequest(approvalRequest: ApprovalRequest): Boolean {
        approvalDao.insertApproval(approvalRequest.toLocal().copy(pendingSync = true))
        try {
            val response = apiService.createApproval(approvalRequest.toDto())
            if (response.isSuccessful && response.body() != null) {
                approvalDao.insertApproval(response.body()!!.toLocal().copy(pendingSync = false))
                return true
            }
        } catch (_: Exception) {}
        return true
    }

    private fun calculateLocalAnalytics(list: List<LocalApproval>): ApprovalAnalytics {
        if (list.isEmpty()) {
            return ApprovalAnalytics(
                avgApprovalTimeHours = 4.5,
                approvalRatePercent = 88.0,
                rejectionRatePercent = 12.0,
                pendingBottlenecks = listOf("Credit Limits", "Inventory Adjustments")
            )
        }
        val total = list.size
        val approved = list.count { it.status == ApprovalStatus.APPROVED.name }
        val rejected = list.count { it.status == ApprovalStatus.REJECTED.name }
        
        val appRate = if (total > 0) (approved.toDouble() / total) * 100 else 100.0
        val rejRate = if (total > 0) (rejected.toDouble() / total) * 100 else 0.0

        val bottlenecks = list.filter { it.status == ApprovalStatus.PENDING.name }
            .groupBy { it.type }
            .map { "${it.key} (${it.value.size} pending)" }
            .take(3)

        return ApprovalAnalytics(
            avgApprovalTimeHours = 3.8,
            approvalRatePercent = String.format("%.1f", appRate).toDouble(),
            rejectionRatePercent = String.format("%.1f", rejRate).toDouble(),
            pendingBottlenecks = bottlenecks.ifEmpty { listOf("None") }
        )
    }

    // Converters Extensions
    private fun LocalApproval.toDomain(): ApprovalRequest {
        return ApprovalRequest(
            id = id,
            companyId = companyId,
            tenantId = tenantId,
            requesterId = requesterId,
            requesterName = requesterName,
            type = ApprovalType.valueOf(type),
            title = title,
            description = description,
            date = date,
            status = ApprovalStatus.valueOf(status),
            amount = amount,
            refId = refId,
            remarks = remarks,
            history = history,
            chain = chain
        )
    }

    private fun ApprovalRequest.toLocal(): LocalApproval {
        return LocalApproval(
            id = id,
            companyId = companyId,
            tenantId = tenantId,
            requesterId = requesterId,
            requesterName = requesterName,
            type = type.name,
            title = title,
            description = description,
            date = date,
            status = status.name,
            amount = amount,
            refId = refId,
            remarks = remarks,
            history = history,
            chain = chain
        )
    }

    private fun ApprovalRequestDto.toLocal(): LocalApproval {
        return LocalApproval(
            id = id,
            companyId = companyId,
            tenantId = tenantId,
            requesterId = requesterId,
            requesterName = requesterName,
            type = type,
            title = title,
            description = description,
            date = date,
            status = status,
            amount = amount,
            refId = refId,
            remarks = remarks,
            history = history.map { it.toLocal() },
            chain = chain.map { it.toLocal() },
            pendingSync = false
        )
    }

    private fun ApprovalRequest.toDto(): ApprovalRequestDto {
        return ApprovalRequestDto(
            id = id,
            companyId = companyId,
            tenantId = tenantId,
            requesterId = requesterId,
            requesterName = requesterName,
            type = type.name,
            title = title,
            description = description,
            date = date,
            status = status.name,
            amount = amount,
            refId = refId,
            remarks = remarks,
            history = history.map { it.toDto() },
            chain = chain.map { it.toDto() }
        )
    }

    private fun WorkflowStepDto.toLocal(): WorkflowStep = WorkflowStep(id, date, action, executorName, executorRole, remarks)
    private fun ApprovalChainStepDto.toLocal(): ApprovalChainStep = ApprovalChainStep(role, userId, userName, status, sequence)

    private fun WorkflowStep.toDto(): WorkflowStepDto = WorkflowStepDto(id, date, action, executorName, executorRole, remarks)
    private fun ApprovalChainStep.toDto(): ApprovalChainStepDto = ApprovalChainStepDto(role, userId, userName, status, sequence)

    private fun getMockApprovals(companyId: String): List<ApprovalRequest> {
        return listOf(
            ApprovalRequest(
                id = "app_101",
                companyId = companyId,
                tenantId = "tenant_global",
                requesterId = "user_sale1",
                requesterName = "Rohan Sharma",
                type = ApprovalType.QUOTATION,
                title = "Special Discount Waiver - Quotation #Q-9843",
                description = "Requested additional 15% discount for Alpha Logistics to secure critical enterprise order.",
                date = "22-Jun-2026",
                status = ApprovalStatus.PENDING,
                amount = 24500.0,
                refId = "q_9843",
                remarks = "Enterprise customer requesting custom pricing waiver.",
                history = listOf(
                    WorkflowStep("ws_1", "22-Jun-2026", "Submitted", "Rohan Sharma", "Sales Manager", "Please approve this special waiver.")
                ),
                chain = listOf(
                    ApprovalChainStep("Accountant", null, null, "Pending", 1),
                    ApprovalChainStep("Super Admin", null, null, "Pending", 2)
                )
            ),
            ApprovalRequest(
                id = "app_102",
                companyId = companyId,
                tenantId = "tenant_global",
                requesterId = "user_acct2",
                requesterName = "Anjali Goel",
                type = ApprovalType.CREDIT_LIMIT,
                title = "Credit Limit Extension Request - Beta Retailers",
                description = "Credit limit extension from $50,000 to $85,000 to process pending logistics invoice.",
                date = "21-Jun-2026",
                status = ApprovalStatus.PENDING,
                amount = 35000.0,
                refId = "c_994",
                remarks = "Ledger shows outstanding, but customer has issued post-dated cheques.",
                history = listOf(
                    WorkflowStep("ws_2", "21-Jun-2026", "Submitted", "Anjali Goel", "Accountant", "PDC received, safe to proceed.")
                ),
                chain = listOf(
                    ApprovalChainStep("Company Admin", null, null, "Pending", 1)
                )
            ),
            ApprovalRequest(
                id = "app_103",
                companyId = companyId,
                tenantId = "tenant_global",
                requesterId = "user_inv1",
                requesterName = "Vikram Singh",
                type = ApprovalType.INVENTORY_ADJUSTMENT,
                title = "Physical Inventory Variance Correction",
                description = "Adjustment of -12 units for Steel Girders (Godown Alpha) due to freight shipping damage.",
                date = "20-Jun-2026",
                status = ApprovalStatus.APPROVED,
                amount = 8900.0,
                refId = "i_adj_12",
                remarks = "Approved during monthly inventory reconciliation check.",
                history = listOf(
                    WorkflowStep("ws_3", "20-Jun-2026", "Submitted", "Vikram Singh", "Accountant", "Material damaged in transit."),
                    WorkflowStep("ws_4", "20-Jun-2026", "Approved", "Anjali Goel", "Company Admin", "Adjustment authorized.")
                ),
                chain = listOf(
                    ApprovalChainStep("Company Admin", "user_acct2", "Anjali Goel", "Approved", 1)
                )
            ),
            ApprovalRequest(
                id = "app_104",
                companyId = companyId,
                tenantId = "tenant_global",
                requesterId = "user_sale2",
                requesterName = "Priya Nair",
                type = ApprovalType.VOUCHER,
                title = "Expense Reimbursement Voucher Exception - #VCH-2034",
                description = "Entertainment expenses exceed standard allowance by 25% for high-priority client dinner.",
                date = "19-Jun-2026",
                status = ApprovalStatus.REJECTED,
                amount = 1200.0,
                refId = "vch_2034",
                remarks = "Rejected - Exceeds pre-allocated client entertainment cap.",
                history = listOf(
                    WorkflowStep("ws_5", "19-Jun-2026", "Submitted", "Priya Nair", "Sales Manager", "Important client retention meet."),
                    WorkflowStep("ws_6", "20-Jun-2026", "Rejected", "Anjali Goel", "Company Admin", "Limit breached without prior email sanction.")
                ),
                chain = listOf(
                    ApprovalChainStep("Company Admin", "user_acct3", "Anjali Goel", "Rejected", 1)
                )
            )
        )
    }
}
