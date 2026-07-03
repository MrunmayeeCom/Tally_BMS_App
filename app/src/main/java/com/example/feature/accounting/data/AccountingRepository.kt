package com.example.feature.accounting.data

import android.content.Context
import com.example.core.common.Resource
import com.example.core.common.networkBoundResource
import com.example.core.database.*
import com.example.core.network.ApiService
import com.example.core.network.LedgerDto
import com.example.core.network.VoucherDto
import com.example.core.network.BillDto
import com.example.core.session.SessionManager
import com.example.feature.accounting.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

enum class LedgerGroup(val isAssetOrExpense: Boolean) {
    SUNDRY_DEBTORS(true),
    SUNDRY_CREDITORS(false),
    SALES_ACCOUNT(false),
    PURCHASE_ACCOUNT(true),
    DIRECT_EXPENSES(true),
    INDIRECT_EXPENSES(true),
    BANK_ACCOUNTS(true),
    CASH_IN_HAND(true),
    CAPITAL_ACCOUNT(false),
    GENERAL_LIABILITIES(false);

    companion object {
        fun fromString(groupStr: String): LedgerGroup {
            return when {
                groupStr.contains("Debtor", ignoreCase = true) -> SUNDRY_DEBTORS
                groupStr.contains("Creditor", ignoreCase = true) -> SUNDRY_CREDITORS
                groupStr.contains("Sales", ignoreCase = true) -> SALES_ACCOUNT
                groupStr.contains("Purchase", ignoreCase = true) -> PURCHASE_ACCOUNT
                groupStr.contains("Direct Expense", ignoreCase = true) -> DIRECT_EXPENSES
                groupStr.contains("Indirect Expense", ignoreCase = true) -> INDIRECT_EXPENSES
                groupStr.contains("Expense", ignoreCase = true) -> INDIRECT_EXPENSES
                groupStr.contains("Bank", ignoreCase = true) -> BANK_ACCOUNTS
                groupStr.contains("Cash", ignoreCase = true) -> CASH_IN_HAND
                groupStr.contains("Capital", ignoreCase = true) -> CAPITAL_ACCOUNT
                else -> GENERAL_LIABILITIES
            }
        }
    }
}

class AccountingRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val ledgerDao: LedgerDao,
    private val voucherDao: VoucherDao,
    private val billDao: BillDao,
    private val sessionManager: SessionManager
) : IAccountingRepository {

    // Helper to validate repository-level RBAC for write commands
    private suspend fun checkWritePermission() {
        val role = sessionManager.userRole.first() ?: "Auditor"
        val allowedRoles = setOf("Admin", "Owner", "DataEntry", "Manager")
        if (!allowedRoles.contains(role)) {
            throw SecurityException("Access Denied: Role '$role' is unauthorized to perform write operations in Accounting Core.")
        }
    }

    override fun getLedgers(companyId: String, forceRefresh: Boolean): Flow<Resource<List<LocalLedger>>> {
        return networkBoundResource(
            query = { ledgerDao.getLedgersByCompany(companyId) },
            fetch = {
                val response = apiService.getLedgers()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    throw Exception("Could not fetch ledgers: ${response.code()}")
                }
            },
            saveFetchResult = { remoteList ->
                val localList = remoteList.map { dto ->
                    LocalLedger(
                        ledgerId = dto.ledgerId,
                        companyId = companyId,
                        tallyGuid = dto.tallyGuid,
                        name = dto.name,
                        group = dto.group,
                        balance = BigDecimal(dto.balance.toString())
                    )
                }
                ledgerDao.deleteCompanyLedgers(companyId)
                ledgerDao.insertLedgers(localList)
            },
            shouldFetch = { cached -> cached.isEmpty() || forceRefresh }
        )
    }

    override fun getVouchers(companyId: String, forceRefresh: Boolean): Flow<Resource<List<LocalVoucher>>> {
        return networkBoundResource(
            query = { voucherDao.getVouchersByCompany(companyId) },
            fetch = {
                val response = apiService.getVouchers()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    throw Exception("Could not fetch vouchers: ${response.code()}")
                }
            },
            saveFetchResult = { remoteList ->
                val localList = remoteList.map { dto ->
                    val bigDecimalAmount = BigDecimal(dto.amount.toString())
                    val postings = deriveDoubleEntryPostings(
                        type = dto.type,
                        partyId = dto.partyLedgerId,
                        partyName = dto.partyName,
                        amount = bigDecimalAmount
                    )
                    LocalVoucher(
                        voucherId = dto.voucherId,
                        companyId = companyId,
                        type = dto.type,
                        partyLedgerId = dto.partyLedgerId,
                        partyName = dto.partyName,
                        amount = bigDecimalAmount,
                        date = dto.date,
                        source = dto.source,
                        postings = postings
                    )
                }
                voucherDao.insertVouchers(localList)
            },
            shouldFetch = { cached -> cached.isEmpty() || forceRefresh }
        )
    }

    // Function to generate the double entry postings automatically based on accounting standards
    private fun deriveDoubleEntryPostings(
        type: String,
        partyId: String,
        partyName: String,
        amount: BigDecimal
    ): List<VoucherPosting> {
        return when (type.lowercase(Locale.ROOT)) {
            "sales" -> listOf(
                VoucherPosting(ledgerId = partyId, ledgerName = partyName, isDebit = true, amount = amount),
                VoucherPosting(ledgerId = "L_SALES", ledgerName = "Sales Account", isDebit = false, amount = amount)
            )
            "purchase" -> listOf(
                VoucherPosting(ledgerId = "L_PURCHASE", ledgerName = "Purchase Account", isDebit = true, amount = amount),
                VoucherPosting(ledgerId = partyId, ledgerName = partyName, isDebit = false, amount = amount)
            )
            "receipt" -> listOf(
                VoucherPosting(ledgerId = "L_BANK", ledgerName = "Bank Account", isDebit = true, amount = amount),
                VoucherPosting(ledgerId = partyId, ledgerName = partyName, isDebit = false, amount = amount)
            )
            "payment" -> listOf(
                VoucherPosting(ledgerId = partyId, ledgerName = partyName, isDebit = true, amount = amount),
                VoucherPosting(ledgerId = "L_BANK", ledgerName = "Bank Account", isDebit = false, amount = amount)
            )
            else -> listOf(
                VoucherPosting(ledgerId = partyId, ledgerName = partyName, isDebit = true, amount = amount),
                VoucherPosting(ledgerId = "L_GENERAL", ledgerName = "General Ledger", isDebit = false, amount = amount)
            )
        }
    }

    override suspend fun createLocalVoucher(
        companyId: String,
        type: String,
        partyId: String,
        partyName: String,
        amount: BigDecimal,
        date: String
    ): LocalVoucher {
        // Step 1: RBAC security validation
        checkWritePermission()

        // Step 2: Derive complete postings for standard double-entry
        val postings = deriveDoubleEntryPostings(type, partyId, partyName, amount)

        // Step 3: Strict Double-Entry Validation
        val totalDebit = postings.filter { it.isDebit }.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.amount) }
        val totalCredit = postings.filter { !it.isDebit }.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.amount) }
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw IllegalArgumentException("Double-Entry Error: Vouchers must be balanced! Total Debits ($totalDebit) must equal Total Credits ($totalCredit)")
        }

        val voucherDto = VoucherDto(
            voucherId = UUID.randomUUID().toString(),
            companyId = companyId,
            type = type,
            partyLedgerId = partyId,
            partyName = partyName,
            amount = amount.toDouble(),
            date = date,
            source = "app"
        )

        // Step 4: Perform ledger transactional balance updates (Deterministic, Auditable change)
        postings.forEach { posting ->
            val ledgerId = posting.ledgerId
            val existingLedger = ledgerDao.getLedgerById(ledgerId)
            if (existingLedger != null) {
                val groupEnum = LedgerGroup.fromString(existingLedger.group)
                val newBal = if (posting.isDebit) {
                    if (groupEnum.isAssetOrExpense) {
                        existingLedger.balance.add(posting.amount)
                    } else {
                        existingLedger.balance.subtract(posting.amount)
                    }
                } else {
                    if (groupEnum.isAssetOrExpense) {
                        existingLedger.balance.subtract(posting.amount)
                    } else {
                        existingLedger.balance.add(posting.amount)
                    }
                }
                val updatedLedger = existingLedger.copy(balance = newBal, lastUpdated = System.currentTimeMillis())
                ledgerDao.insertLedgers(listOf(updatedLedger))
            } else {
                val groupStr = when (posting.ledgerId) {
                    "L_SALES" -> "Sales Account"
                    "L_PURCHASE" -> "Purchase Account"
                    "L_BANK" -> "Bank Accounts"
                    else -> "Sundry Debtors"
                }
                val groupEnum = LedgerGroup.fromString(groupStr)
                val initialBal = if (posting.isDebit) {
                    if (groupEnum.isAssetOrExpense) posting.amount else posting.amount.negate()
                } else {
                    if (groupEnum.isAssetOrExpense) posting.amount.negate() else posting.amount
                }
                val newLedger = LocalLedger(
                    ledgerId = posting.ledgerId,
                    companyId = companyId,
                    tallyGuid = UUID.randomUUID().toString(),
                    name = posting.ledgerName,
                    group = groupStr,
                    balance = initialBal
                )
                ledgerDao.insertLedgers(listOf(newLedger))
            }
        }

        // Try pushing to remote API
        try {
            apiService.createVoucher(voucherDto)
        } catch (e: Exception) {
            // Treat as offline waiting for sync worker
        }

        // Save local voucher as pending sync Room-backend
        val voucher = LocalVoucher(
            voucherId = voucherDto.voucherId,
            companyId = companyId,
            type = type,
            partyLedgerId = partyId,
            partyName = partyName,
            amount = amount,
            date = date,
            source = "app",
            pendingSync = true,
            postings = postings
        )
        voucherDao.insertVoucher(voucher)

        // Queue WorkManager Sync Worker
        try {
            val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.core.sync.SyncWorker>()
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .build()
            androidx.work.WorkManager.getInstance(context).enqueue(syncRequest)
        } catch (e: Exception) {
            // Safe fallback if WorkManager context not active
        }

        return voucher
    }

    override fun getBills(companyId: String, forceRefresh: Boolean): Flow<Resource<List<LocalBill>>> {
        return networkBoundResource(
            query = { billDao.getBillsByCompany(companyId) },
            fetch = {
                val response = apiService.getBills()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    throw Exception("Could not fetch bills: ${response.code()}")
                }
            },
            saveFetchResult = { remoteList ->
                val localList = remoteList.map { dto ->
                    LocalBill(
                        billId = dto.billId,
                        companyId = companyId,
                        partyId = dto.partyId,
                        partyName = dto.partyName,
                        voucherId = dto.voucherId,
                        amount = BigDecimal(dto.amount.toString()),
                        dueDate = dto.dueDate,
                        status = dto.status
                    )
                }
                billDao.insertBills(localList)
            },
            shouldFetch = { cached -> cached.isEmpty() || forceRefresh }
        )
    }

    override suspend fun updateRecoveryStateLocal(billId: String, state: String, promisedDate: String?) {
        checkWritePermission()
        billDao.updateRecoveryState(billId, state, promisedDate)
        try {
            apiService.updatePipelineState(
                billId = billId,
                request = com.example.core.network.UpdatePipelineStateRequest(state, promisedDate)
            )
        } catch (e: Exception) {
            // Local fallback holds offline truth
        }
    }

    override suspend fun getLedgerDetails(ledgerId: String): LocalLedger? {
        return ledgerDao.getLedgerById(ledgerId)
    }

    override suspend fun createLedger(
        companyId: String,
        name: String,
        group: String,
        openingBalance: BigDecimal
    ): LocalLedger {
        // RBAC Check
        checkWritePermission()

        val ledgerId = UUID.randomUUID().toString()
        val ledgerDto = LedgerDto(
            ledgerId = ledgerId,
            companyId = companyId,
            tallyGuid = UUID.randomUUID().toString(),
            name = name,
            group = group,
            balance = openingBalance.toDouble()
        )
        
        try {
            apiService.createLedger(ledgerDto)
        } catch (e: Exception) {
            // Local offline
        }

        val localLedger = LocalLedger(
            ledgerId = ledgerId,
            companyId = companyId,
            tallyGuid = ledgerDto.tallyGuid,
            name = name,
            group = group,
            balance = openingBalance
        )
        ledgerDao.insertLedgers(listOf(localLedger))
        return localLedger
    }

    override suspend fun getVoucherById(voucherId: String): LocalVoucher? {
        return null
    }

    override suspend fun getBillById(billId: String): LocalBill? {
        return null
    }

    override fun queryTransactions(
        companyId: String,
        searchQuery: String?,
        startDate: String?,
        endDate: String?,
        voucherType: String?,
        ledgerId: String?,
        page: Int,
        pageSize: Int
    ): Flow<Resource<List<LocalVoucher>>> = flow {
        emit(Resource.Loading)
        try {
            val allVouchers = voucherDao.getVouchersByCompany(companyId).first()
            val filtered = allVouchers.filter { voucher ->
                val matchesSearch = searchQuery.isNullOrEmpty() || 
                        voucher.partyName.contains(searchQuery, ignoreCase = true) ||
                        voucher.voucherId.contains(searchQuery, ignoreCase = true)
                
                val matchesType = voucherType.isNullOrEmpty() || voucherType == "All" ||
                        voucher.type.equals(voucherType, ignoreCase = true)

                val matchesLedger = ledgerId.isNullOrEmpty() || voucher.partyLedgerId == ledgerId

                val matchesDate = matchesDateRange(voucher.date, startDate, endDate)
                
                matchesSearch && matchesType && matchesLedger && matchesDate
            }

            val startIndex = (page - 1) * pageSize
            val paginatedList = if (startIndex < filtered.size) {
                val endIndex = minOf(startIndex + pageSize, filtered.size)
                filtered.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            emit(Resource.Success(paginatedList))
        } catch (e: Exception) {
            emit(Resource.Error(e, "Failed to query transactions: ${e.message}"))
        }
    }

    private fun matchesDateRange(dateStr: String, startStr: String?, endStr: String?): Boolean {
        if (startStr.isNullOrEmpty() && endStr.isNullOrEmpty()) return true
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = format.parse(dateStr) ?: return true
            val start = startStr?.takeIf { it.isNotEmpty() }?.let { format.parse(it) }
            val end = endStr?.takeIf { it.isNotEmpty() }?.let { format.parse(it) }

            val afterStart = start == null || !date.before(start)
            val beforeEnd = end == null || !date.after(end)
            afterStart && beforeEnd
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun getTrialBalance(companyId: String): TrialBalanceSummary {
        val ledgers = ledgerDao.getLedgersByCompany(companyId).first()
        val items = ledgers.map { ledger ->
            val groupEnum = LedgerGroup.fromString(ledger.group)
            val bal = ledger.balance
            val debit: BigDecimal
            val credit: BigDecimal

            if (groupEnum.isAssetOrExpense) {
                if (bal >= BigDecimal.ZERO) {
                    debit = bal
                    credit = BigDecimal.ZERO
                } else {
                    debit = BigDecimal.ZERO
                    credit = bal.abs()
                }
            } else {
                if (bal >= BigDecimal.ZERO) {
                    debit = BigDecimal.ZERO
                    credit = bal
                } else {
                    debit = bal.abs()
                    credit = BigDecimal.ZERO
                }
            }

            TrialBalanceItem(
                ledgerName = ledger.name,
                groupName = groupEnum.name,
                debitAmount = debit,
                creditAmount = credit
            )
        }
        val totalDebit = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.debitAmount) }
        val totalCredit = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.creditAmount) }

        return TrialBalanceSummary(
            items = items,
            totalDebit = totalDebit,
            totalCredit = totalCredit
        )
    }

    override suspend fun getProfitLossSummary(companyId: String): ProfitLossSummary {
        val ledgers = ledgerDao.getLedgersByCompany(companyId).first()
        
        // Revenue
        val revenueLedgers = ledgers.filter { LedgerGroup.fromString(it.group) == LedgerGroup.SALES_ACCOUNT }
        val revenueItems = revenueLedgers.map { it.name to it.balance }
        val revenueTotal = revenueLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        // Purchases + Operational expenses
        val cogsLedgers = ledgers.filter { 
            val g = LedgerGroup.fromString(it.group)
            g == LedgerGroup.PURCHASE_ACCOUNT || g == LedgerGroup.DIRECT_EXPENSES 
        }
        val cogsTotal = cogsLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        // Operating Expenses
        val expenseLedgers = ledgers.filter { LedgerGroup.fromString(it.group) == LedgerGroup.INDIRECT_EXPENSES }
        val expenseItems = expenseLedgers.map { it.name to it.balance }
        val expenseTotal = expenseLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        val grossProfit = revenueTotal.subtract(cogsTotal)
        val netProfit = grossProfit.subtract(expenseTotal)

        return ProfitLossSummary(
            revenue = ProfitLossSection("Revenue", revenueTotal, revenueItems),
            expense = ProfitLossSection("Operating Expenses", expenseTotal, expenseItems),
            grossProfit = grossProfit,
            netProfit = netProfit
        )
    }

    override suspend fun getBalanceSheetSummary(companyId: String): BalanceSheetSummary {
        val ledgers = ledgerDao.getLedgersByCompany(companyId).first()
        val pl = getProfitLossSummary(companyId)

        // Assets
        val assetLedgers = ledgers.filter { 
            val g = LedgerGroup.fromString(it.group)
            g == LedgerGroup.SUNDRY_DEBTORS || g == LedgerGroup.BANK_ACCOUNTS || g == LedgerGroup.CASH_IN_HAND
        }
        val assetItems = assetLedgers.map { it.name to it.balance }
        val assetTotal = assetLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        // Liabilities
        val liabilityLedgers = ledgers.filter { 
            val g = LedgerGroup.fromString(it.group)
            g == LedgerGroup.SUNDRY_CREDITORS || g == LedgerGroup.GENERAL_LIABILITIES
        }
        val liabilityItems = liabilityLedgers.map { it.name to it.balance }
        val liabilityTotal = liabilityLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        // Equity = Base Capital + retain earnings (Net Profit)
        val equityLedgers = ledgers.filter { LedgerGroup.fromString(it.group) == LedgerGroup.CAPITAL_ACCOUNT }
        val baseEquity = equityLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }
        val equityTotal = baseEquity.add(pl.netProfit)

        val equityItems = equityLedgers.map { it.name to it.balance } + ("Retained Earnings" to pl.netProfit)

        return BalanceSheetSummary(
            assets = BalanceSheetSubSection("Assets", assetTotal, assetItems),
            liabilities = BalanceSheetSubSection("Liabilities", liabilityTotal, liabilityItems),
            equity = BalanceSheetSubSection("Equity / Capital", equityTotal, equityItems),
            totalAssets = assetTotal,
            totalLiabilitiesAndEquity = liabilityTotal.add(equityTotal)
        )
    }

    override suspend fun getCashFlowSnapshot(companyId: String): CashFlowSnapshot {
        val vouchers = voucherDao.getVouchersByCompany(companyId).first()
        val cashLedgers = ledgerDao.getLedgersByCompany(companyId).first().filter {
            val g = LedgerGroup.fromString(it.group)
            g == LedgerGroup.BANK_ACCOUNTS || g == LedgerGroup.CASH_IN_HAND
        }
        val cashLedgerIds = cashLedgers.map { it.ledgerId }.toSet()

        // Opening cash balance initially equals current balances of CASH_IN_HAND and BANK_ACCOUNTS minus impacts
        val currentCashBalance = cashLedgers.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.balance) }

        var cumulativeInflow = BigDecimal.ZERO
        var cumulativeOutflow = BigDecimal.ZERO

        val items = vouchers.map { v ->
            var inflow = BigDecimal.ZERO
            var outflow = BigDecimal.ZERO

            // Loop through double entry postings
            v.postings.forEach { p ->
                if (cashLedgerIds.isEmpty() || cashLedgerIds.contains(p.ledgerId)) {
                    if (p.isDebit) {
                        inflow = inflow.add(p.amount)
                    } else {
                        outflow = outflow.add(p.amount)
                    }
                }
            }

            cumulativeInflow = cumulativeInflow.add(inflow)
            cumulativeOutflow = cumulativeOutflow.add(outflow)

            CashFlowItem(
                date = v.date,
                description = v.partyName,
                type = v.type,
                inflow = inflow,
                outflow = outflow,
                balance = currentCashBalance
            )
        }

        // Adjust running balance sequentially backward
        var runningBalance = currentCashBalance
        val processedItems = items.map { item ->
            val computed = item.copy(balance = runningBalance)
            runningBalance = runningBalance.subtract(item.inflow).add(item.outflow)
            computed
        }

        val netCashFlow = cumulativeInflow.subtract(cumulativeOutflow)
        val openingBalance = runningBalance

        return CashFlowSnapshot(
            items = processedItems,
            netCashFlow = netCashFlow,
            openingBalance = openingBalance,
            closingBalance = currentCashBalance
        )
    }
}
