package com.example.core.network

import retrofit2.Response
import retrofit2.http.*

// Authentication models
data class LoginRequest(val username: String, val password: String, val loginType: String)
data class LoginResponse(
    val token: String,
    val user: UserDto?
)

data class UserDto(
    val id: String?,
    val email: String?,
    val name: String?,
    val role: String?
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    val otpCode: String,
    val newPass: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)

// Company Information models
data class CompanyResponse(
    val company_guid: String,
    val name: String
)

data class CompanyListResponse(val success: Boolean, val data: List<CompanyResponse>)

data class ActiveCompanyRequest(val company_guid: String)
data class ActiveCompanyResponse(val success: Boolean, val company_guid: String?)
data class SetActiveCompanyRequest(val company_guid: String)
data class SetActiveCompanyResponse(val success: Boolean, val company_guid: String?)

// Ledger Information Models
data class LedgerDto(
    val ledgerId: String,
    val companyId: String,
    val tallyGuid: String,
    val name: String,
    val group: String,
    val balance: Double
)

// Voucher Information Models
data class VoucherDto(
    val voucherId: String,
    val companyId: String,
    val type: String, // "Sales", "Purchase", "Payment", "Receipt"
    val partyLedgerId: String,
    val partyName: String,
    val amount: Double,
    val date: String,
    val source: String // "tally" or "app"
)

// Bill / Outstanding models
data class BillDto(
    val billId: String,
    val companyId: String,
    val partyId: String,
    val partyName: String,
    val voucherId: String,
    val amount: Double,
    val dueDate: String,
    val status: String // "open", "partial", "closed"
)

data class OutstandingPipelineDto(
    val pipelineId: String,
    val billId: String,
    val state: String, // "Pending", "Reminder Sent", "Promised", "Closed"
    val promisedDate: String?,
    val lastUpdated: String
)

data class UpdatePipelineStateRequest(
    val state: String,
    val promisedDate: String? = null
)

// Field interactions models
data class CheckInRequest(
    val customerUserId: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
    val notes: String?
)

data class CheckInResponse(
    val checkInId: String,
    val userId: String,
    val customerUserId: String,
    val checkInTime: String,
    val checkOutTime: String?
)

data class OutOfOfficeRequest(
    val checkInId: String,
    val checkOutTime: String,
    val notes: String?
)

data class FollowUpDto(
    val followUpId: String,
    val customerId: String,
    val customerName: String,
    val assignedTo: String,
    val dueDate: String,
    val status: String, // "Pending", "Completed", "Overdue"
    val outcome: String?
)

data class CreateFollowUpRequest(
    val customerId: String,
    val dueDate: String,
    val notes: String,
    val assignedTo: String
)

data class UpdateFollowUpRequest(
    val status: String,
    val outcome: String?
)

// Dashboard information models
data class DashboardSummaryDto(
    val receivables: Double,
    val payables: Double,
    val pending_bills: Int,
    val cleared_bills: Int
)

data class DashboardIncomeExpenseDto(
    val month: String,
    val income: Double,
    val expense: Double
)

data class DashboardBillDto(
    val ledger_name: String,
    val pending_amount: Double,
    val due_date: String
)

data class DashboardResponseDto(
    val summary: DashboardSummaryDto,
    val incomeExpense: List<DashboardIncomeExpenseDto>,
    val bills: List<DashboardBillDto>
)

data class OrderDto(
    val orderId: String,
    val companyId: String,
    val partyId: String,
    val partyName: String,
    val amount: java.math.BigDecimal,
    val date: String,
    val status: String,
    val remarks: String,
    val itemsSummary: String
)

data class CrmCustomerDto(
    val id: String,
    val name: String,
    val outstandingAmount: Double,
    val overdueAmount: Double,
    val statusBadge: String, // e.g., "Active", "Cr Overdue", "Dormant", "Risky"
    val stateCode: String,
    val salesRepName: String
)

data class ContactInfoDto(
    val phone: String,
    val email: String,
    val address: String,
    val keyContactPerson: String
)

data class LedgerSummaryDto(
    val creditLimit: Double,
    val openingBalance: Double,
    val closingBalance: Double,
    val lastPaymentAmount: Double,
    val lastPaymentDate: String
)

data class OutstandingSummaryDto(
    val totalOutstanding: Double,
    val overdue30Days: Double,
    val overdue60Days: Double,
    val overdue90Days: Double,
    val overdueOver90Days: Double
)

data class TransactionDto(
    val id: String,
    val date: String,
    val type: String, // Invoice, Payment, Journal
    val amount: Double,
    val status: String
)

data class CrmCustomerDetailDto(
    val id: String,
    val name: String,
    val contactInfo: ContactInfoDto,
    val ledgerSummary: LedgerSummaryDto,
    val outstandingSummary: OutstandingSummaryDto,
    val lastTransaction: TransactionDto,
    val statusBadge: String,
    val salesRepName: String
)

data class CrmTimelineEventDto(
    val id: String,
    val type: String, // "Follow-up", "Visit", "Reminder", "Note", "Collection"
    val date: String,
    val description: String,
    val performedBy: String,
    val outcomeStatus: String? = null
)

interface ApiService {

    // === Auth & Identity ===
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @GET("company")
    suspend fun getCompanies(): Response<CompanyListResponse>

    @GET("company/active")
    suspend fun getActiveCompany(): Response<ActiveCompanyResponse>

    @POST("company/set-active")
    suspend fun setActiveCompany(@Body request: SetActiveCompanyRequest): Response<SetActiveCompanyResponse>

    // === Accounting ===
    @GET("api/v1/ledgers")
    suspend fun getLedgers(): Response<List<LedgerDto>>

    @GET("api/v1/ledgers/{id}")
    suspend fun getLedgerDetails(@Path("id") ledgerId: String): Response<LedgerDto>

    @POST("api/v1/ledgers")
    suspend fun createLedger(@Body ledger: LedgerDto): Response<LedgerDto>

    @GET("api/v1/vouchers")
    suspend fun getVouchers(
        @Query("type") type: String? = null,
        @Query("ledgerId") ledgerId: String? = null
    ): Response<List<VoucherDto>>

    @POST("api/v1/vouchers")
    suspend fun createVoucher(@Body voucher: VoucherDto): Response<VoucherDto>

    @GET("api/v1/bills")
    suspend fun getBills(@Query("status") status: String? = null): Response<List<BillDto>>

    // === Outstanding ===
    @GET("api/v1/outstanding/pipeline/{billId}")
    suspend fun getOutstandingPipeline(@Path("billId") billId: String): Response<OutstandingPipelineDto>

    @PUT("api/v1/outstanding/pipeline/{billId}/state")
    suspend fun updatePipelineState(
        @Path("billId") billId: String,
        @Body request: UpdatePipelineStateRequest
    ): Response<OutstandingPipelineDto>

    // === Sales & CRM ===
    @POST("api/v1/check-ins")
    suspend fun checkIn(@Body request: CheckInRequest): Response<CheckInResponse>

    @PUT("api/v1/check-ins/{id}/check-out")
    suspend fun checkOut(
        @Path("id") checkInId: String,
        @Body request: OutOfOfficeRequest
    ): Response<CheckInResponse>

    @GET("api/v1/follow-ups")
    suspend fun getFollowUps(): Response<List<FollowUpDto>>

    @POST("api/v1/follow-ups")
    suspend fun createFollowUp(@Body request: CreateFollowUpRequest): Response<FollowUpDto>

    @PUT("api/v1/follow-ups/{id}")
    suspend fun updateFollowUp(
        @Path("id") followUpId: String,
        @Body request: UpdateFollowUpRequest
    ): Response<FollowUpDto>

    // === Dashboard ===
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(@Query("company_guid") companyGuid: String): Response<DashboardSummaryDto>

    @GET("dashboard/income-expense")
    suspend fun getDashboardIncomeExpense(@Query("company_guid") companyGuid: String): Response<List<DashboardIncomeExpenseDto>>

    @GET("dashboard/bills")
    suspend fun getDashboardBills(@Query("company_guid") companyGuid: String): Response<List<DashboardBillDto>>

    // === CRM ===
    @GET("api/v1/crm/customers")
    suspend fun getCustomers(
        @Query("query") query: String?,
        @Query("filter") filter: String?,
        @Query("sort") sort: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<CrmCustomerDto>>

    @GET("api/v1/crm/customers/{customerId}")
    suspend fun getCustomerDetail(
        @Path("customerId") customerId: String
    ): Response<CrmCustomerDetailDto>

    @GET("api/v1/crm/customers/{customerId}/timeline")
    suspend fun getCustomerTimeline(
        @Path("customerId") customerId: String
    ): Response<List<CrmTimelineEventDto>>

    // === Orders ===
    @GET("api/v1/orders")
    suspend fun getOrders(@Query("companyId") companyId: String): Response<List<OrderDto>>

    @POST("api/v1/orders")
    suspend fun createOrder(@Body order: OrderDto): Response<OrderDto>

    @PUT("api/v1/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body statusMap: Map<String, String>
    ): Response<Unit>
}
