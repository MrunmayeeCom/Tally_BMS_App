package com.bmstally.app.data.remote

import com.bmstally.app.data.MockDataService
import com.bmstally.app.model.*
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.*

interface BmsApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @GET("companies")
    suspend fun getCompanies(): Response<List<Company>>

    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<List<DashboardStat>>

    @GET("outstanding/summary")
    suspend fun getOutstandingSummary(): Response<StringWrapper>

    @GET("outstanding/ledgers")
    suspend fun getOutstandingLedgers(): Response<List<OutstandingItem>>

    @GET("outstanding/groups")
    suspend fun getOutstandingGroups(): Response<List<PairWrapper>>

    @GET("items")
    suspend fun getItems(): Response<List<Item>>

    @POST("items")
    suspend fun createItem(@Body item: Item): Response<Item>

    @GET("parties")
    suspend fun getParties(): Response<List<String>>

    @GET("sales-entries")
    suspend fun getSalesEntries(): Response<List<SalesEntry>>

    @GET("reports")
    suspend fun getReports(): Response<List<ReportItem>>

    @GET("entry-types")
    suspend fun getEntryTypes(): Response<List<String>>

    @GET("follow-ups")
    suspend fun getFollowUps(): Response<List<MockDataService.FollowUp>>

    @GET("check-ins")
    suspend fun getCheckIns(): Response<List<MockDataService.CheckIn>>

    @GET("users")
    suspend fun getUsers(): Response<List<MockDataService.AppUser>>

    @GET("reminders")
    suspend fun getReminders(): Response<List<MockDataService.Reminder>>

    @GET("transactions")
    suspend fun getTransactions(): Response<List<MockDataService.Transaction>>

    @GET("items/categories")
    suspend fun getItemCategories(): Response<List<String>>

    @GET("items/groups")
    suspend fun getItemGroups(): Response<List<String>>

    @GET("ledger")
    suspend fun getLedgers(): Response<List<Ledger>>

    @GET("ledger/{ledgerGuid}")
    suspend fun getLedgerDetail(@Path("ledgerGuid") ledgerGuid: String): Response<Ledger>

    @GET("ledger/{ledgerGuid}/vouchers")
    suspend fun getLedgerVouchers(@Path("ledgerGuid") ledgerGuid: String): Response<List<Voucher>>

    @GET("ledger/{ledgerGuid}/invoices")
    suspend fun getLedgerInvoices(@Path("ledgerGuid") ledgerGuid: String): Response<List<Invoice>>

    @GET("ledger/{ledgerGuid}/bills")
    suspend fun getLedgerBills(@Path("ledgerGuid") ledgerGuid: String): Response<List<Bill>>

    @GET("ledger/{ledgerGuid}/ageing")
    suspend fun getLedgerAgeing(@Path("ledgerGuid") ledgerGuid: String): Response<List<Ageing>>

    @GET("voucher")
    suspend fun getVouchers(): Response<List<Voucher>>

    @POST("voucher")
    suspend fun createVoucher(@Body voucher: Voucher): Response<Voucher>

    @PUT("voucher/{id}")
    suspend fun updateVoucher(@Path("id") id: String, @Body voucher: Voucher): Response<Voucher>

    @DELETE("voucher/{id}")
    suspend fun deleteVoucher(@Path("id") id: String): Response<Unit>

    @GET("voucher/types")
    suspend fun getVoucherTypes(): Response<List<String>>

    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>

    @GET("api/reports/monthly-summary")
    suspend fun getMonthlySummary(@Query("year") year: Int, @Query("company_guid") companyGuid: String): Response<List<MonthlySummary>>

    @GET("ledger/deleted/history")
    suspend fun getDeleteHistory(): Response<List<DeleteHistoryRecord>>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    @POST("request-to-admin")
    suspend fun sendRequest(@Body message: Map<String, String>): Response<Map<String, Any>>

    @GET("admin/requests")
    suspend fun getAdminRequests(): Response<List<UserRequest>>
}

data class LoginRequest(
    val tenantCode: String,
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tenant: Tenant,
    val companies: List<Company>,
    val userName: String,
    val userEmail: String
)

data class RefreshRequest(val refreshToken: String)

data class StringWrapper(val value: String)
data class PairWrapper(val key: String, val value: String)
