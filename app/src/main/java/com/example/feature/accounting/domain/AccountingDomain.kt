package com.example.feature.accounting.domain

import com.example.core.common.Resource
import com.example.core.database.LocalLedger
import com.example.core.database.LocalVoucher
import com.example.core.database.LocalBill
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

// Financial Summary models using BigDecimal
data class TrialBalanceItem(
    val ledgerName: String,
    val groupName: String,
    val debitAmount: BigDecimal,
    val creditAmount: BigDecimal
)

data class TrialBalanceSummary(
    val items: List<TrialBalanceItem>,
    val totalDebit: BigDecimal,
    val totalCredit: BigDecimal
)

data class ProfitLossSection(
    val name: String,
    val amount: BigDecimal,
    val items: List<Pair<String, BigDecimal>>
)

data class ProfitLossSummary(
    val revenue: ProfitLossSection,
    val expense: ProfitLossSection,
    val grossProfit: BigDecimal,
    val netProfit: BigDecimal
)

data class BalanceSheetSubSection(
    val name: String,
    val amount: BigDecimal,
    val items: List<Pair<String, BigDecimal>>
)

data class BalanceSheetSummary(
    val assets: BalanceSheetSubSection,
    val liabilities: BalanceSheetSubSection,
    val equity: BalanceSheetSubSection,
    val totalAssets: BigDecimal,
    val totalLiabilitiesAndEquity: BigDecimal
)

data class CashFlowItem(
    val date: String,
    val description: String,
    val type: String, // "Receipt", "Payment", "Contra"
    val inflow: BigDecimal,
    val outflow: BigDecimal,
    val balance: BigDecimal
)

data class CashFlowSnapshot(
    val items: List<CashFlowItem>,
    val netCashFlow: BigDecimal,
    val openingBalance: BigDecimal,
    val closingBalance: BigDecimal
)

interface IAccountingRepository {
    fun getLedgers(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<LocalLedger>>>
    fun getVouchers(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<LocalVoucher>>>
    suspend fun createLocalVoucher(companyId: String, type: String, partyId: String, partyName: String, amount: BigDecimal, date: String): LocalVoucher
    fun getBills(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<LocalBill>>>
    suspend fun updateRecoveryStateLocal(billId: String, state: String, promisedDate: String?)
    
    // Additional Accounting Core features:
    suspend fun getLedgerDetails(ledgerId: String): LocalLedger?
    suspend fun createLedger(companyId: String, name: String, group: String, openingBalance: BigDecimal): LocalLedger
    suspend fun getVoucherById(voucherId: String): LocalVoucher?
    suspend fun getBillById(billId: String): LocalBill?
    
    // Transaction Explorer query
    fun queryTransactions(
        companyId: String,
        searchQuery: String?,
        startDate: String?,
        endDate: String?,
        voucherType: String?,
        ledgerId: String?,
        page: Int,
        pageSize: Int
    ): Flow<Resource<List<LocalVoucher>>>

    // Financial calculations computed locally or simulated from database
    suspend fun getTrialBalance(companyId: String): TrialBalanceSummary
    suspend fun getProfitLossSummary(companyId: String): ProfitLossSummary
    suspend fun getBalanceSheetSummary(companyId: String): BalanceSheetSummary
    suspend fun getCashFlowSnapshot(companyId: String): CashFlowSnapshot
}
