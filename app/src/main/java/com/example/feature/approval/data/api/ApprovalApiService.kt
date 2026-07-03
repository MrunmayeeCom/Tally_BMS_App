package com.example.feature.approval.data.api

import com.example.feature.approval.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApprovalApiService {
    @GET("api/v1/approvals")
    suspend fun getApprovals(
        @Query("companyId") companyId: String
    ): Response<List<ApprovalRequestDto>>

    @GET("api/v1/approvals/{id}")
    suspend fun getApprovalById(
        @Path("id") id: String
    ): Response<ApprovalRequestDto>

    @POST("api/v1/approvals")
    suspend fun createApproval(
        @Body request: ApprovalRequestDto
    ): Response<ApprovalRequestDto>

    @POST("api/v1/approvals/{id}/action")
    suspend fun performAction(
        @Path("id") id: String,
        @Body action: ApprovalActionDto
    ): Response<ApprovalRequestDto>

    @GET("api/v1/approvals/analytics")
    suspend fun getAnalytics(
        @Query("companyId") companyId: String
    ): Response<ApprovalAnalyticsDto>
}
