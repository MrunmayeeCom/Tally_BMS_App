package com.example.feature.inventory.presentation

import com.example.core.common.UiState
import com.example.feature.inventory.domain.*
import java.math.BigDecimal

sealed interface InventoryUiState<out T> {
    object Loading : InventoryUiState<Nothing>
    data class Success<out T>(val data: T) : InventoryUiState<T>
    data class Error(val message: String) : InventoryUiState<Nothing>
}

data class ItemMasterState(
    val items: List<StockItem> = emptyList(),
    val filteredItems: List<StockItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val uoms: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val selectedBrand: String? = null,
    val searchQuery: String = ""
)

data class ItemDetailState(
    val item: StockItem? = null,
    val stockLevels: List<StockLevel> = emptyList(),
    val history: List<StockTransaction> = emptyList(),
    val selectedValuationMethod: String = "Weighted Average",
    val valuationReport: ValuationReport? = null
)

data class StockSummaryState(
    val summaryItems: List<StockSummaryItem> = emptyList(),
    val filteredSummary: List<StockSummaryItem> = emptyList(),
    val totalValuation: BigDecimal = BigDecimal.ZERO,
    val selectedCategory: String? = null,
    val selectedValuationMethod: String = "Weighted Average"
)

data class WarehouseState(
    val godowns: List<Godown> = emptyList(),
    val selectedGodown: Godown? = null,
    val selectedGodownLevels: List<StockLevel> = emptyList()
)

data class InventoryReportsState(
    val lowStock: List<LowStockReportItem> = emptyList(),
    val deadStock: List<DeadStockReportItem> = emptyList(),
    val fastMoving: List<FastMovingItem> = emptyList(),
    val ageingReport: List<StockAgeingReportItem> = emptyList()
)

data class TransactionHistoryState(
    val transactions: List<StockTransaction> = emptyList(),
    val filteredTransactions: List<StockTransaction> = emptyList(),
    val selectedType: String? = null
)
