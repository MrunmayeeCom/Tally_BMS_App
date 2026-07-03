package com.example.feature.inventory.domain

import java.math.BigDecimal

data class StockItem(
    val id: String,
    val companyId: String,
    val name: String,
    val sku: String,
    val category: String,
    val brand: String,
    val uom: String, // Unit of Measure
    val description: String,
    val purchasePrice: BigDecimal,
    val sellingPrice: BigDecimal,
    val minReorderLevel: BigDecimal,
    val isSynced: Boolean = true
)

data class Godown(
    val id: String,
    val companyId: String,
    val name: String,
    val location: String,
    val manager: String,
    val status: String = "Active"
)

data class StockLevel(
    val id: String,
    val companyId: String,
    val itemId: String,
    val godownId: String,
    val godownName: String,
    val currentStock: BigDecimal,
    val reservedStock: BigDecimal,
    val availableStock: BigDecimal
)

data class StockTransaction(
    val id: String,
    val companyId: String,
    val itemId: String,
    val itemName: String,
    val godownId: String,
    val godownName: String,
    val type: String, // "IN", "OUT", "PURCHASE", "SALES", "TRANSFER", "ADJUSTMENT"
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val referenceId: String?, // Voucher ID / Bill ID
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

// Valuation Outputs
data class ValuationReport(
    val itemId: String,
    val itemName: String,
    val currentStock: BigDecimal,
    val valuationMethod: String, // "FIFO", "Weighted Average", "Standard Cost"
    val unitValue: BigDecimal,
    val totalValuation: BigDecimal
)

// Summary / Report items
data class StockSummaryItem(
    val item: StockItem,
    val totalCurrentStock: BigDecimal,
    val totalAvailableStock: BigDecimal,
    val totalReservedStock: BigDecimal,
    val totalValue: BigDecimal
)

data class LowStockReportItem(
    val item: StockItem,
    val currentStock: BigDecimal,
    val reorderLevel: BigDecimal,
    val shortage: BigDecimal
)

data class DeadStockReportItem(
    val item: StockItem,
    val currentStock: BigDecimal,
    val lastMovementDate: String,
    val daysInactive: Int,
    val totalValue: BigDecimal
)

data class FastMovingItem(
    val item: StockItem,
    val totalUnitsSold: BigDecimal,
    val turnOverRate: BigDecimal,
    val popularityRank: Int
)

data class StockAgeingBucket(
    val lessThan30Days: BigDecimal,
    val age30To60Days: BigDecimal,
    val age61To90Days: BigDecimal,
    val over90Days: BigDecimal
)

data class StockAgeingReportItem(
    val item: StockItem,
    val totalStock: BigDecimal,
    val totalValue: BigDecimal,
    val ageing: StockAgeingBucket
)
