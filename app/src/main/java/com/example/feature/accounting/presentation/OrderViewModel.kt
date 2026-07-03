package com.example.feature.accounting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.feature.accounting.data.db.LocalOrder
import com.example.feature.accounting.domain.IOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class OrderViewModel(
    private val orderRepository: IOrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<LocalOrder>>>(Resource.Success(emptyList()))
    val ordersState: StateFlow<Resource<List<LocalOrder>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<LocalOrder?>(null)
    val orderDetailState: StateFlow<LocalOrder?> = _orderDetailState.asStateFlow()

    private val _createOrderSuccess = MutableStateFlow<Boolean>(false)
    val createOrderSuccess: StateFlow<Boolean> = _createOrderSuccess.asStateFlow()

    fun loadOrders(companyId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            orderRepository.getOrders(companyId, forceRefresh).collect { resource ->
                _ordersState.value = resource
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            val order = orderRepository.getOrderById(orderId)
            _orderDetailState.value = order
        }
    }

    fun createOrder(
        companyId: String,
        partyId: String,
        partyName: String,
        amount: BigDecimal,
        remarks: String,
        itemsSummary: String
    ) {
        viewModelScope.launch {
            _ordersState.value = Resource.Loading
            orderRepository.createOrder(companyId, partyId, partyName, amount, remarks, itemsSummary)
            _createOrderSuccess.value = true
            loadOrders(companyId, forceRefresh = true)
        }
    }

    fun updateOrderStatus(companyId: String, orderId: String, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
            loadOrderDetail(orderId)
            loadOrders(companyId, forceRefresh = true)
        }
    }

    fun resetCreateState() {
        _createOrderSuccess.value = false
    }

    class Factory(private val orderRepository: IOrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderViewModel(orderRepository) as T
        }
    }
}
