package com.example.feature.inventory.data.dto

import java.math.BigDecimal

data class StockItemDto(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val brand: String,
    val uom: String,
    val description: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val minReorderLevel: Double,
    val totalCurrentStock: Double = 0.0,
    val totalAvailableStock: Double = 0.0,
    val totalReservedStock: Double = 0.0
)

data class GodownDto(
    val id: String,
    val name: String,
    val location: String,
    val manager: String,
    val status: String = "Active"
)

data class StockLevelDto(
    val id: String,
    val itemId: String,
    val godownId: String,
    val godownName: String,
    val currentStock: Double,
    val reservedStock: Double,
    val availableStock: Double
)

data class StockTransactionDto(
    val id: String,
    val itemId: String,
    val itemName: String,
    val godownId: String,
    val godownName: String,
    val type: String, // "IN", "OUT", "PURCHASE", "SALES", "TRANSFER", "ADJUSTMENT"
    val quantity: Double,
    val unitPrice: Double,
    val referenceId: String?,
    val date: String,
    val timestamp: Long
)

data class StockTransferRequest(
    val itemId: String,
    val sourceGodownId: String,
    val destinationGodownId: String,
    val quantity: Double,
    val date: String
)

data class AdjustmentRequest(
    val itemId: String,
    val godownId: String,
    val adjustmentType: String, // "IN_ADJUSTMENT", "OUT_ADJUSTMENT"
    val quantity: Double,
    val reason: String,
    val date: String
)
