package com.bmstally.app.data.repository

import com.bmstally.app.data.MockDataService
import com.bmstally.app.data.auth.TokenManager
import com.bmstally.app.data.local.dao.*
import com.bmstally.app.data.local.entity.*
import com.bmstally.app.data.remote.BmsApiService
import com.bmstally.app.data.remote.LoginRequest
import com.bmstally.app.model.*
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>()
}

@Singleton
class BmsRepository @Inject constructor(
    private val api: BmsApiService,
    private val tokenManager: TokenManager,
    private val mockData: MockDataService,
    private val itemDao: ItemDao,
    private val companyDao: CompanyDao,
    private val dashboardStatDao: DashboardStatDao,
    private val outstandingItemDao: OutstandingItemDao,
    private val followUpDao: FollowUpDao,
    private val checkInDao: CheckInDao,
    private val userDao: UserDao,
    private val reminderDao: ReminderDao,
    private val transactionDao: TransactionDao
) {
    private var cachedTenantId: String = ""

    fun setTenantId(id: String) { cachedTenantId = id }

    suspend fun login(tenantCode: String, username: String, password: String): AppResult<Tenant> {
        return try {
            val response = api.login(LoginRequest(tenantCode, username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.accessToken = body.accessToken
                tokenManager.refreshToken = body.refreshToken
                tokenManager.tenantId = body.tenant.id
                tokenManager.tenantCode = tenantCode
                tokenManager.userName = body.userName
                tokenManager.userEmail = body.userEmail
                cachedTenantId = body.tenant.id
                cacheCompanies(body.tenant.id, body.companies)
                AppResult.Success(body.tenant)
            } else {
                fallbackLogin(tenantCode, username, password)
            }
        } catch (e: Exception) {
            Timber.w(e, "API login failed, falling back to mock")
            fallbackLogin(tenantCode, username, password)
        }
    }

    private suspend fun fallbackLogin(tenantCode: String, username: String, password: String): AppResult<Tenant> {
        return try {
            val tenantId = mockData.login(tenantCode, username, password)
            if (tenantId != null) {
                val tenant = mockData.getTenantByCode(tenantCode)!!
                tokenManager.accessToken = "mock-token-$tenantId"
                tokenManager.refreshToken = "mock-refresh-$tenantId"
                tokenManager.tenantId = tenantId
                tokenManager.tenantCode = tenantCode
                tokenManager.userName = username
                tokenManager.userEmail = "$username@${tenantCode.lowercase()}.com"
                cachedTenantId = tenantId
                cacheCompanies(tenantId, mockData.getCompanies(tenantId))
                AppResult.Success(tenant)
            } else {
                AppResult.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            AppResult.Error("Login failed: ${e.message}", e)
        }
    }

    private suspend fun cacheCompanies(tenantId: String, companies: List<Company>) {
        companyDao.deleteByTenant(tenantId)
        companyDao.insertAll(companies.map { CompanyEntity.fromModel(tenantId, it) })
    }

    fun logout() {
        tokenManager.clear()
        cachedTenantId = ""
    }

    val isLoggedIn: Boolean get() = tokenManager.isLoggedIn
    val savedTenantId: String? get() = tokenManager.tenantId

    fun getCompanies(): List<Company> {
        val tid = cachedTenantId.ifEmpty { return emptyList() }
        return runCatching {
            runBlocking { companyDao.getByTenant(tid) }.map { it.toModel() }
        }.getOrDefault(mockData.getCompanies(tid))
    }

    fun getDashboardStats(): List<DashboardStat> {
        val tid = cachedTenantId.ifEmpty { return emptyList() }
        return runCatching {
            runBlocking { dashboardStatDao.getByTenant(tid) }.map { it.toModel() }
        }.getOrDefault(mockData.getDashboardStats(tid))
    }

    fun getOutstandingSummary(): String {
        val tid = cachedTenantId.ifEmpty { return "" }
        return mockData.getOutstandingSummary(tid)
    }

    fun getOutstandingLedgers(): List<OutstandingItem> {
        val tid = cachedTenantId.ifEmpty { return emptyList() }
        return runCatching {
            runBlocking { outstandingItemDao.getByTenant(tid) }.map { it.toModel() }
        }.getOrDefault(mockData.getOutstandingLedgers(tid))
    }

    fun getOutstandingGroups(): List<Pair<String, String>> {
        return mockData.getOutstandingGroups(cachedTenantId)
    }

    fun getItems(): List<Item> {
        val tid = cachedTenantId.ifEmpty { return emptyList() }
        return runCatching {
            runBlocking { itemDao.getByTenant(tid) }.map { it.toModel() }
        }.getOrDefault(mockData.getItems(tid))
    }

    suspend fun createItem(item: Item): AppResult<Item> {
        return try {
            itemDao.insert(ItemEntity.fromModel(cachedTenantId, item))
            AppResult.Success(item)
        } catch (e: Exception) {
            AppResult.Error("Failed to create item: ${e.message}", e)
        }
    }

    fun getParties(): List<String> = mockData.getParties(cachedTenantId)
    fun getSalesEntries() = mockData.getSalesEntries()
    fun getReports() = mockData.getReports()
    fun getEntryTypes() = mockData.getEntryTypes()
    fun getItemCategories() = mockData.getItemCategories(cachedTenantId)
    fun getItemGroups() = mockData.getItemGroups(cachedTenantId)

    fun getFollowUps() = mockData.getFollowUps(cachedTenantId)
    fun getCheckIns() = mockData.getCheckIns(cachedTenantId)
    fun getUsers() = mockData.getUsers(cachedTenantId)
    fun getReminders() = mockData.getReminders(cachedTenantId)
    fun getTransactions() = mockData.getTransactions(cachedTenantId)

    fun getLedgers(): List<Ledger> = mockData.getLedgers(cachedTenantId)

    fun getLedgerDetail(ledgerGuid: String): Ledger? =
        mockData.getLedgers(cachedTenantId).find { it.ledger_guid == ledgerGuid }

    fun getLedgerVouchers(ledgerGuid: String): List<Voucher> =
        mockData.getLedgerVouchers(ledgerGuid)

    fun getLedgerInvoices(ledgerGuid: String): List<Invoice> =
        mockData.getLedgerInvoices(ledgerGuid)

    fun getLedgerBills(ledgerGuid: String): List<Bill> =
        mockData.getLedgerBills(ledgerGuid)

    fun getLedgerAgeing(ledgerGuid: String): List<Ageing> =
        mockData.getLedgerAgeing(ledgerGuid)

    fun getOrders(): List<Order> = mockData.getOrders()
    fun getMonthlySummary(year: Int): List<MonthlySummary> = mockData.getMonthlySummary(year)
    fun getDeleteHistory(): List<DeleteHistoryRecord> = mockData.getDeleteHistory()
    fun restoreDeleteHistory(id: Int): Boolean = mockData.restoreDeleteHistory(id)
    fun getRequests(): List<UserRequest> = mockData.getRequests()
    fun sendRequest(message: String): UserRequest = mockData.sendRequest(message)

    fun getVouchers(): List<Voucher> = mockData.getVouchers()

    fun createVoucher(voucher: Voucher): Voucher = mockData.createVoucher(voucher)

    fun updateVoucher(id: String, voucher: Voucher): Voucher? = mockData.updateVoucher(id, voucher)

    fun deleteVoucher(id: String): Boolean = mockData.deleteVoucher(id)
}
