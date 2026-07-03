package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmstally.app.data.repository.AppResult
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class ItemViewMode { LIST, GRID }

enum class ItemFilter(val label: String) {
    ALL("Show All"),
    IN_STOCK("In Stock"),
    NOT_IN_STOCK("Not In Stock"),
    NEGATIVE("Negative Stock"),
    BELOW_REORDER("Below Reorder Level")
}

data class AggregatedItemData(
    val name: String,
    val itemCount: Int,
    val totalQuantity: Int
)

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private var allItems: List<Item> = emptyList()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryTab = MutableStateFlow(0)
    val categoryTab: StateFlow<Int> = _categoryTab.asStateFlow()

    private val _filter = MutableStateFlow(ItemFilter.ALL)
    val filter: StateFlow<ItemFilter> = _filter.asStateFlow()

    private val _viewMode = MutableStateFlow(ItemViewMode.LIST)
    val viewMode: StateFlow<ItemViewMode> = _viewMode.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pageSize = MutableStateFlow(100)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    val tabs = listOf("Item", "Group", "Category")

    fun setTenantId(id: String) {
        repository.setTenantId(id)
    }

    fun loadItems() {
        allItems = repository.getItems()
        applyFilters()
        Timber.i("Loaded ${allItems.size} items")
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
        _currentPage.value = 1
        applyFilters()
    }

    fun setCategoryTab(t: Int) {
        _categoryTab.value = t
        _searchQuery.value = ""
        _currentPage.value = 1
    }

    fun setFilter(f: ItemFilter) {
        _filter.value = f
        _currentPage.value = 1
        applyFilters()
    }

    fun toggleView() {
        _viewMode.value = if (_viewMode.value == ItemViewMode.LIST) ItemViewMode.GRID else ItemViewMode.LIST
    }

    fun setPage(page: Int) { _currentPage.value = page.coerceIn(1, totalPages); applyFilters() }
    fun setPageSize(size: Int) { _pageSize.value = size; _currentPage.value = 1; applyFilters() }

    fun filteredItems(): List<Item> {
        var result = allItems
        val q = _searchQuery.value
        if (q.isNotEmpty()) {
            result = result.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    it.group.contains(q, ignoreCase = true)
            }
        }
        return when (_filter.value) {
            ItemFilter.IN_STOCK -> result.filter { it.isInStock }
            ItemFilter.NOT_IN_STOCK -> result.filter { !it.isInStock }
            ItemFilter.NEGATIVE -> result.filter { it.quantity < 0 }
            ItemFilter.BELOW_REORDER -> result.filter { it.isBelowReorderLevel }
            ItemFilter.ALL -> result
        }
    }

    val filtered: List<Item> get() = filteredItems()

    val totalRecords: Int get() = filtered.size
    val totalPages: Int get() = maxOf(1, (totalRecords + _pageSize.value - 1) / _pageSize.value)

    val paginatedItems: List<Item>
        get() {
            val start = (_currentPage.value - 1) * _pageSize.value
            return filtered.drop(start).take(_pageSize.value)
        }

    val totalStockValue: Int get() = allItems.sumOf { it.stockValue }
    val lowStockCount: Int get() = allItems.count { it.isBelowReorderLevel }

    val groupData: List<AggregatedItemData>
        get() {
            val f = filtered
            return f.groupBy { it.group.ifEmpty { "Uncategorized" } }
                .map { (key, items) ->
                    AggregatedItemData(key, items.size, items.sumOf { it.quantity })
                }.sortedBy { it.name }
        }

    val categoryData: List<AggregatedItemData>
        get() {
            val f = filtered
            return f.groupBy { it.category }
                .map { (key, items) ->
                    AggregatedItemData(key, items.size, items.sumOf { it.quantity })
                }.sortedBy { it.name }
        }

    val totalQuantity: Int get() = allItems.sumOf { it.quantity }

    val summaryLabel: String
        get() = when (_categoryTab.value) {
            1 -> "Total Groups"
            2 -> "Total Categories"
            else -> "Total Items"
        }

    val summaryCount: Int
        get() = when (_categoryTab.value) {
            1 -> groupData.size
            2 -> categoryData.size
            else -> filtered.size
        }

    fun addItem(item: Item) {
        viewModelScope.launch {
            when (repository.createItem(item)) {
                is AppResult.Success -> {
                    allItems = listOf(item) + allItems
                    applyFilters()
                    Timber.i("Item created: ${item.name}")
                }
                is AppResult.Error -> {
                    Timber.w("Failed to create item: ${item.name}")
                }
            }
        }
    }

    private fun applyFilters() {
        _items.value = paginatedItems
    }
}
