package com.example.feature.outstanding.data

import retrofit2.Response
import retrofit2.http.*

// Retrofit API Contract for Outstanding & Receivables Management
interface OutstandingApiService {
    @GET("api/v1/outstanding/dashboard")
    suspend fun getDashboardData(): Response<OutstandingDashboardDto>

    @GET("api/v1/outstanding/items")
    suspend fun getOutstandingItems(
        @Query("query") query: String?,
        @Query("type") type: String?,
        @Query("group") group: String?,
        @Query("sort") sort: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<OutstandingItemDto>>

    @GET("api/v1/outstanding/detail/{partyId}")
    suspend fun getOutstandingDetail(
        @Path("partyId") partyId: String
    ): Response<OutstandingDetailDto>

    @GET("api/v1/outstanding/aging")
    suspend fun getAgeingReport(
        @Query("type") type: String
    ): Response<AgeingReportDto>

    @GET("api/v1/outstanding/pipeline")
    suspend fun getRecoveryPipeline(): Response<List<OutstandingItemDto>>

    @POST("api/v1/outstanding/pipeline/update-status")
    suspend fun updateRecoveryStatus(
        @Body request: UpdateRecoveryStatusRequest
    ): Response<UpdateRecoveryStatusResponse>
}

// Data Transfer Objects
data class OutstandingDashboardDto(
    val totalReceivables: Double,
    val totalPayables: Double,
    val overdueAmount: Double,
    val collectionsThisMonth: Double
)

data class OutstandingItemDto(
    val partyId: String,
    val partyName: String,
    val type: String,
    val groupName: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val lastContactDate: String?,
    val assignedExecutive: String?,
    val billingState: String?
)

data class OutstandingInvoiceDto(
    val billId: String,
    val date: String,
    val amount: Double,
    val pendingAmount: Double,
    val dueDate: String,
    val daysOverdue: Int,
    val status: String
)

data class OutstandingPaymentDto(
    val paymentId: String,
    val date: String,
    val amount: Double,
    val mode: String
)

data class RecoveryTrackDto(
    val recoveryStatus: String,
    val assignedExecutive: String,
    val followUpCount: Int,
    val lastContactDate: String?,
    val nextActionDate: String?
)

data class OutstandingDetailDto(
    val partyId: String,
    val partyName: String,
    val type: String,
    val phone: String,
    val email: String,
    val ledgerClosingBalance: Double,
    val creditLimit: Double,
    val outstandingAmount: Double,
    val billingHistory: List<OutstandingInvoiceDto>,
    val paymentHistory: List<OutstandingPaymentDto>,
    val recoveryTracking: RecoveryTrackDto
)

data class AgeingBucketDto(
    val range: String,
    val amount: Double,
    val count: Int,
    val percentage: Double
)

data class AgeingReportDto(
    val totalAmount: Double,
    val buckets: List<AgeingBucketDto>,
    val criticalAccounts: List<OutstandingItemDto>
)

data class UpdateRecoveryStatusRequest(
    val partyId: String,
    val status: String,
    val nextActionDate: String?,
    val executive: String?
)

data class UpdateRecoveryStatusResponse(
    val success: Boolean,
    val message: String
)
