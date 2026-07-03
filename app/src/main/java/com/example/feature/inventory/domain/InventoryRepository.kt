package com.example.feature.inventory.domain

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface InventoryRepository {

    // --- Core Master Feeds ---
    fun getStockItems(companyId: String): Flow<List<StockItem>>
    suspend fun getStockItemById(itemId: String): StockItem?
    fun getGodowns(companyId: String): Flow<List<Godown>>
    suspend fun getGodownById(godownId: String): Godown?
    fun getStockLevels(companyId: String): Flow<List<StockLevel>>
    fun getStockLevelsByItem(companyId: String, itemId: String): Flow<List<StockLevel>>
    fun getTransactions(companyId: String): Flow<List<StockTransaction>>

    // --- Category / Brand / UOM helpers ---
    suspend fun getCategories(companyId: String): List<String>
    suspend fun getBrands(companyId: String): List<String>
    suspend fun getUnitsOfMeasure(companyId: String): List<String>

    // --- Offline Transactions & Sync Commands ---
    suspend fun createLocalStockItem(
        companyId: String,
        name: String,
        sku: String,
        category: String,
        brand: String,
        uom: String,
        description: String,
        purchasePrice: BigDecimal,
        sellingPrice: BigDecimal,
        minReorderLevel: BigDecimal
    ): StockItem

    suspend fun createLocalGodown(
        companyId: String,
        name: String,
        location: String,
        manager: String
    ): Godown

    suspend fun recordStockTransaction(
        companyId: String,
        itemId: String,
        godownId: String,
        type: String, // "IN", "OUT", "PURCHASE", "SALES", "TRANSFER", "ADJUSTMENT"
        quantity: BigDecimal,
        unitPrice: BigDecimal,
        referenceId: String?,
        date: String
    ): StockTransaction

    suspend fun transferStock(
        companyId: String,
        itemId: String,
        sourceGodownId: String,
        destinationGodownId: String,
        quantity: BigDecimal,
        date: String
    ): Boolean

    suspend fun adjustStock(
        companyId: String,
        itemId: String,
        godownId: String,
        adjustmentType: String, // "IN_ADJUSTMENT", "OUT_ADJUSTMENT"
        quantity: BigDecimal,
        reason: String,
        date: String
    ): Boolean

    // --- Valuation Calculators ---
    suspend fun calculateValuation(
        companyId: String,
        itemId: String,
        method: String // "FIFO", "Weighted Average", "Standard Cost"
    ): ValuationReport

    // --- Inventory Reports ---
    suspend fun getStockSummary(companyId: String): List<StockSummaryItem>
    suspend fun getLowStockReport(companyId: String): List<LowStockReportItem>
    suspend fun getDeadStockReport(companyId: String, inactiveDaysThreshold: Int = 90): List<DeadStockReportItem>
    suspend fun getFastMovingItemsReport(companyId: String): List<FastMovingItem>
    suspend fun getStockAgeingReport(companyId: String): List<StockAgeingReportItem>

    // --- Sync triggers ---
    suspend fun refreshInventoryPayload(companyId: String): Boolean
}
