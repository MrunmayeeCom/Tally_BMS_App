package com.example.feature.accounting.domain

import com.example.feature.accounting.data.db.LocalOrder
import com.example.core.common.Resource
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface IOrderRepository {
    fun getOrders(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<LocalOrder>>>
    suspend fun getOrderById(orderId: String): LocalOrder?
    suspend fun createOrder(companyId: String, partyId: String, partyName: String, amount: BigDecimal, remarks: String, itemsSummary: String): LocalOrder
    suspend fun updateOrderStatus(orderId: String, status: String)
    suspend fun getPendingOrders(): List<LocalOrder>
}
