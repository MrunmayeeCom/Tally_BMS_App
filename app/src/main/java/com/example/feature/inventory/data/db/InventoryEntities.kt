package com.example.feature.inventory.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "stock_items")
data class LocalStockItem(
    @PrimaryKey val id: String,
    val companyId: String,
    val name: String,
    val sku: String,
    val category: String,
    val brand: String,
    val uom: String,
    val description: String,
    val purchasePrice: BigDecimal,
    val sellingPrice: BigDecimal,
    val minReorderLevel: BigDecimal,
    val isSynced: Boolean = true
)

@Entity(tableName = "godowns")
data class LocalGodown(
    @PrimaryKey val id: String,
    val companyId: String,
    val name: String,
    val location: String,
    val manager: String,
    val status: String = "Active"
)

@Entity(tableName = "stock_levels")
data class LocalStockLevel(
    @PrimaryKey val id: String, // Combination of itemId + godownId or unique GUID
    val companyId: String,
    val itemId: String,
    val godownId: String,
    val godownName: String,
    val currentStock: BigDecimal,
    val reservedStock: BigDecimal,
    val availableStock: BigDecimal
)

@Entity(tableName = "stock_transactions")
data class LocalStockTransaction(
    @PrimaryKey val id: String,
    val companyId: String,
    val itemId: String,
    val itemName: String,
    val godownId: String,
    val godownName: String,
    val type: String, // "IN", "OUT", "PURCHASE", "SALES", "TRANSFER", "ADJUSTMENT"
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val referenceId: String?,
    val date: String,
    val timestamp: Long
)
