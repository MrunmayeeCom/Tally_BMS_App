package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OrderListState(
    val orders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val selectedType: String = "All",
    val selectedStatus: String = "All Status",
    val currentPage: Int = 1,
    val pageSize: Int = 10
) {
    val filteredOrders: List<Order>
        get() = orders.filter { o ->
            val matchesSearch = searchQuery.isBlank() ||
                o.orderNo.contains(searchQuery, ignoreCase = true) ||
                o.customer.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == "All" || o.type == selectedType
            val matchesStatus = selectedStatus == "All Status" || o.status == selectedStatus
            matchesSearch && matchesType && matchesStatus
        }

    val totalRecords: Int get() = filteredOrders.size
    val totalPages: Int get() = maxOf(1, (totalRecords + pageSize - 1) / pageSize)

    val paginatedOrders: List<Order>
        get() {
            val start = (currentPage - 1) * pageSize
            return filteredOrders.drop(start).take(pageSize)
        }
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(OrderListState())
    val listState: StateFlow<OrderListState> = _listState.asStateFlow()

    fun loadOrders() {
        _listState.value = _listState.value.copy(orders = repository.getOrders())
    }

    fun setSearchQuery(query: String) { _listState.value = _listState.value.copy(searchQuery = query, currentPage = 1) }
    fun setSelectedType(type: String) { _listState.value = _listState.value.copy(selectedType = type, currentPage = 1) }
    fun setSelectedStatus(status: String) { _listState.value = _listState.value.copy(selectedStatus = status, currentPage = 1) }
    fun setPage(page: Int) { _listState.value = _listState.value.copy(currentPage = page.coerceIn(1, _listState.value.totalPages)) }
    fun setPageSize(size: Int) { _listState.value = _listState.value.copy(pageSize = size, currentPage = 1) }
}
