package com.example.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.inventory.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class InventoryViewModel(
    val inventoryRepository: InventoryRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    // Active session bindings
    val currentCompanyId: Flow<String?> = sessionManager.companyId
    val userRole: Flow<String?> = sessionManager.userRole

    // Master list states
    private val _itemMasterState = MutableStateFlow(ItemMasterState())
    val itemMasterState: StateFlow<ItemMasterState> = _itemMasterState.asStateFlow()

    // Item detail states
    private val _itemDetailState = MutableStateFlow(ItemDetailState())
    val itemDetailState: StateFlow<ItemDetailState> = _itemDetailState.asStateFlow()

    // Stock Summary state
    private val _stockSummaryState = MutableStateFlow(StockSummaryState())
    val stockSummaryState: StateFlow<StockSummaryState> = _stockSummaryState.asStateFlow()

    // Warehouse state
    private val _warehouseState = MutableStateFlow(WarehouseState())
    val warehouseState: StateFlow<WarehouseState> = _warehouseState.asStateFlow()

    // Reports states
    private val _reportsState = MutableStateFlow(InventoryReportsState())
    val reportsState: StateFlow<InventoryReportsState> = _reportsState.asStateFlow()

    // Historical transactions state
    private val _transactionState = MutableStateFlow(TransactionHistoryState())
    val transactionState: StateFlow<TransactionHistoryState> = _transactionState.asStateFlow()

    // Create Operation States
    private val _createStatus = MutableStateFlow<Boolean?>(null)
    val createStatus: StateFlow<Boolean?> = _createStatus.asStateFlow()

    fun resetCreateStatus() {
        _createStatus.value = null
    }

    // --- Loading commands ---

    fun loadItemMaster(companyId: String) {
        viewModelScope.launch {
            // Get flows
            combine(
                inventoryRepository.getStockItems(companyId),
                flow { emit(inventoryRepository.getCategories(companyId)) },
                flow { emit(inventoryRepository.getBrands(companyId)) },
                flow { emit(inventoryRepository.getUnitsOfMeasure(companyId)) }
            ) { items, categories, brands, uoms ->
                ItemMasterState(
                    items = items,
                    filteredItems = items,
                    categories = categories,
                    brands = brands,
                    uoms = uoms
                )
            }.collect { state ->
                _itemMasterState.value = state
                // Re-apply filter on current states
                applyItemFilters()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _itemMasterState.update { it.copy(searchQuery = query) }
        applyItemFilters()
    }

    fun selectCategoryFilter(category: String?) {
        _itemMasterState.update { it.copy(selectedCategory = category) }
        applyItemFilters()
    }

    fun selectBrandFilter(brand: String?) {
        _itemMasterState.update { it.copy(selectedBrand = brand) }
        applyItemFilters()
    }

    private fun applyItemFilters() {
        val current = _itemMasterState.value
        val filtered = current.items.filter { item ->
            val matchesSearch = item.name.contains(current.searchQuery, ignoreCase = true) ||
                    item.sku.contains(current.searchQuery, ignoreCase = true)
            val matchesCategory = current.selectedCategory == null || item.category == current.selectedCategory
            val matchesBrand = current.selectedBrand == null || item.brand == current.selectedBrand
            matchesSearch && matchesCategory && matchesBrand
        }
        _itemMasterState.update { it.copy(filteredItems = filtered) }
    }

    fun loadItemDetail(itemId: String, companyId: String) {
        viewModelScope.launch {
            val item = inventoryRepository.getStockItemById(itemId)
            if (item != null) {
                val levels = inventoryRepository.getStockLevelsByItem(companyId, itemId).first()
                val history = inventoryRepository.getTransactions(companyId).first()
                    .filter { it.itemId == itemId }
                val valuation = inventoryRepository.calculateValuation(companyId, itemId, _itemDetailState.value.selectedValuationMethod)

                _itemDetailState.value = ItemDetailState(
                    item = item,
                    stockLevels = levels,
                    history = history,
                    selectedValuationMethod = _itemDetailState.value.selectedValuationMethod,
                    valuationReport = valuation
                )
            }
        }
    }

    fun updateDetailValuationMethod(itemId: String, companyId: String, method: String) {
        viewModelScope.launch {
            _itemDetailState.update { it.copy(selectedValuationMethod = method) }
            val item = _itemDetailState.value.item
            if (item != null) {
                val valuation = inventoryRepository.calculateValuation(companyId, itemId, method)
                _itemDetailState.update { it.copy(valuationReport = valuation) }
            }
        }
    }

    fun loadStockSummary(companyId: String) {
        viewModelScope.launch {
            val summary = inventoryRepository.getStockSummary(companyId)
            val totalValuation = summary.sumOf { it.totalValue }
            _stockSummaryState.value = StockSummaryState(
                summaryItems = summary,
                filteredSummary = summary,
                totalValuation = totalValuation,
                selectedCategory = _stockSummaryState.value.selectedCategory,
                selectedValuationMethod = _stockSummaryState.value.selectedValuationMethod
            )
            applySummaryFilters()
        }
    }

    fun selectSummaryCategoryFilter(companyId: String, category: String?) {
        _stockSummaryState.update { it.copy(selectedCategory = category) }
        applySummaryFilters()
    }

    fun updateSummaryValuationMethod(companyId: String, method: String) {
        viewModelScope.launch {
            _stockSummaryState.update { it.copy(selectedValuationMethod = method) }
            val updatedSummary = _stockSummaryState.value.summaryItems.map { summaryItem ->
                val valuationReport = inventoryRepository.calculateValuation(companyId, summaryItem.item.id, method)
                summaryItem.copy(totalValue = valuationReport.totalValuation)
            }
            val totalValuation = updatedSummary.sumOf { it.totalValue }
            _stockSummaryState.update {
                it.copy(
                    summaryItems = updatedSummary,
                    totalValuation = totalValuation
                )
            }
            applySummaryFilters()
        }
    }

    private fun applySummaryFilters() {
        val current = _stockSummaryState.value
        val filtered = current.summaryItems.filter { item ->
            current.selectedCategory == null || item.item.category == current.selectedCategory
        }
        val currentFilterValuation = filtered.sumOf { it.totalValue }
        _stockSummaryState.update {
            it.copy(
                filteredSummary = filtered,
                totalValuation = currentFilterValuation
            )
        }
    }

    fun loadGodowns(companyId: String) {
        viewModelScope.launch {
            inventoryRepository.getGodowns(companyId).collect { list ->
                _warehouseState.update { it.copy(godowns = list) }
                if (list.isNotEmpty() && _warehouseState.value.selectedGodown == null) {
                    selectGodown(companyId, list.first().id)
                }
            }
        }
    }

    fun selectGodown(companyId: String, godownId: String) {
        viewModelScope.launch {
            val godown = inventoryRepository.getGodownById(godownId)
            val levels = inventoryRepository.getStockLevels(companyId).first()
                .filter { it.godownId == godownId }

            _warehouseState.update {
                it.copy(
                    selectedGodown = godown,
                    selectedGodownLevels = levels
                )
            }
        }
    }

    fun loadTransactions(companyId: String) {
        viewModelScope.launch {
            inventoryRepository.getTransactions(companyId).collect { list ->
                _transactionState.value = TransactionHistoryState(
                    transactions = list,
                    filteredTransactions = list
                )
                applyTransactionFilters()
            }
        }
    }

    fun selectTransactionTypeFilter(type: String?) {
        _transactionState.update { it.copy(selectedType = type) }
        applyTransactionFilters()
    }

    private fun applyTransactionFilters() {
        val current = _transactionState.value
        val filtered = current.transactions.filter { tx ->
            current.selectedType == null || tx.type == current.selectedType
        }
        _transactionState.update { it.copy(filteredTransactions = filtered) }
    }

    fun loadReports(companyId: String) {
        viewModelScope.launch {
            val low = inventoryRepository.getLowStockReport(companyId)
            val dead = inventoryRepository.getDeadStockReport(companyId)
            val fast = inventoryRepository.getFastMovingItemsReport(companyId)
            val ageing = inventoryRepository.getStockAgeingReport(companyId)

            _reportsState.value = InventoryReportsState(
                lowStock = low,
                deadStock = dead,
                fastMoving = fast,
                ageingReport = ageing
            )
        }
    }

    // --- Action triggers ---

    fun createStockItem(
        companyId: String,
        name: String,
        sku: String,
        category: String,
        brand: String,
        uom: String,
        description: String,
        purchasePrice: BigDecimal,
        sellingPrice: BigDecimal,
        minReorderLevel: BigDecimal,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                inventoryRepository.createLocalStockItem(
                    companyId, name, sku, category, brand, uom, description, purchasePrice, sellingPrice, minReorderLevel
                )
                loadItemMaster(companyId)
                _createStatus.value = true
                onSuccess()
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    fun createGodown(
        companyId: String,
        name: String,
        location: String,
        manager: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                inventoryRepository.createLocalGodown(companyId, name, location, manager)
                loadGodowns(companyId)
                _createStatus.value = true
                onSuccess()
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    fun addStockIn(
        companyId: String,
        itemId: String,
        godownId: String,
        quantity: BigDecimal,
        price: java.math.BigDecimal,
        referenceId: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                inventoryRepository.recordStockTransaction(
                    companyId, itemId, godownId, "IN", quantity, price, referenceId, date
                )
                refreshStates(companyId, itemId)
                _createStatus.value = true
                onSuccess()
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    fun addStockOut(
        companyId: String,
        itemId: String,
        godownId: String,
        quantity: BigDecimal,
        price: java.math.BigDecimal,
        referenceId: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                inventoryRepository.recordStockTransaction(
                    companyId, itemId, godownId, "OUT", quantity, price, referenceId, date
                )
                refreshStates(companyId, itemId)
                _createStatus.value = true
                onSuccess()
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    fun transferStock(
        companyId: String,
        itemId: String,
        sourceGodownId: String,
        destinationGodownId: String,
        quantity: BigDecimal,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = inventoryRepository.transferStock(
                    companyId, itemId, sourceGodownId, destinationGodownId, quantity, date
                )
                if (result) {
                    refreshStates(companyId, itemId)
                    _createStatus.value = true
                    onSuccess()
                } else {
                    _createStatus.value = false
                }
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    fun adjustStock(
        companyId: String,
        itemId: String,
        godownId: String,
        adjustmentType: String,
        quantity: BigDecimal,
        reason: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = inventoryRepository.adjustStock(
                    companyId, itemId, godownId, adjustmentType, quantity, reason, date
                )
                if (result) {
                    refreshStates(companyId, itemId)
                    _createStatus.value = true
                    onSuccess()
                } else {
                    _createStatus.value = false
                }
            } catch (e: Exception) {
                _createStatus.value = false
            }
        }
    }

    private fun refreshStates(companyId: String, itemId: String? = null) {
        loadStockSummary(companyId)
        loadGodowns(companyId)
        loadTransactions(companyId)
        loadReports(companyId)
        if (itemId != null) {
            loadItemDetail(itemId, companyId)
        }
    }
}
