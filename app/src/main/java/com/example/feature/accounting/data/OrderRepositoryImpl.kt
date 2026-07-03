package com.example.feature.accounting.data

import android.util.Log
import com.example.core.common.Resource
import com.example.core.network.ApiService
import com.example.core.network.OrderDto
import com.example.feature.accounting.data.db.LocalOrder
import com.example.feature.accounting.data.db.OrderDao
import com.example.feature.accounting.domain.IOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.util.UUID

class OrderRepositoryImpl(
    private val apiService: ApiService,
    private val orderDao: OrderDao
) : IOrderRepository {

    private val tag = "OrderRepositoryImpl"

    override fun getOrders(companyId: String, forceRefresh: Boolean): Flow<Resource<List<LocalOrder>>> = flow {
        emit(Resource.Loading)
        
        // Load whatever we have locally first
        val cached = orderDao.getOrdersByCompany(companyId)
        emit(Resource.Success(cached))

        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getOrders(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val localOrders = dtos.map { dto ->
                        LocalOrder(
                            orderId = dto.orderId,
                            companyId = dto.companyId,
                            partyId = dto.partyId,
                            partyName = dto.partyName,
                            amount = dto.amount,
                            date = dto.date,
                            status = dto.status,
                            remarks = dto.remarks,
                            itemsSummary = dto.itemsSummary,
                            pendingSync = false
                        )
                    }
                    orderDao.insertOrders(localOrders)
                    val updated = orderDao.getOrdersByCompany(companyId)
                    emit(Resource.Success(updated))
                } else {
                    Log.w(tag, "Failed to fetch orders from server: ${response.code()}; using local storage.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching remote orders: ${e.message}; offline access guaranteed.", e)
            }
        }
        
        // Seed some mock orders if database is totally empty both offline & online
        val finalCheck = orderDao.getOrdersByCompany(companyId)
        if (finalCheck.isEmpty()) {
            val mockOrders = listOf(
                LocalOrder("ord_101", companyId, "cust_01", "Acme Distributors Pvt Ltd", BigDecimal("15200.00"), "2026-06-20", "Approved", "Urgent delivery needed", "Stock Item A x50; Stock Item B x100"),
                LocalOrder("ord_102", companyId, "cust_02", "Starlight Retail Enterprises", BigDecimal("45000.00"), "2026-06-19", "Draft", "Requires sales mgr vetting", "Stock Item C x12"),
                LocalOrder("ord_103", companyId, "cust_03", "Vertex Corporate Solutions", BigDecimal("125000.00"), "2026-06-18", "Canceled", "Sufficient stock unavailable", "Stock Item D x200")
            )
            orderDao.insertOrders(mockOrders)
            emit(Resource.Success(orderDao.getOrdersByCompany(companyId)))
        }
    }

    override suspend fun getOrderById(orderId: String): LocalOrder? {
        return orderDao.getOrderById(orderId)
    }

    override suspend fun createOrder(
        companyId: String,
        partyId: String,
        partyName: String,
        amount: BigDecimal,
        remarks: String,
        itemsSummary: String
    ): LocalOrder {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val newOrder = LocalOrder(
            orderId = "ord_" + UUID.randomUUID().toString().take(6),
            companyId = companyId,
            partyId = partyId,
            partyName = partyName,
            amount = amount,
            date = dateStr,
            status = "Draft",
            remarks = remarks,
            itemsSummary = itemsSummary,
            pendingSync = true
        )
        // Persist locally first
        orderDao.insertOrder(newOrder)

        // Try pushing to remote
        try {
            val response = apiService.createOrder(
                OrderDto(
                    orderId = newOrder.orderId,
                    companyId = newOrder.companyId,
                    partyId = newOrder.partyId,
                    partyName = newOrder.partyName,
                    amount = newOrder.amount,
                    date = newOrder.date,
                    status = newOrder.status,
                    remarks = newOrder.remarks,
                    itemsSummary = newOrder.itemsSummary
                )
            )
            if (response.isSuccessful) {
                // Succeeded, clear pendingSync
                val syncedOrder = newOrder.copy(pendingSync = false)
                orderDao.insertOrder(syncedOrder)
                return syncedOrder
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to create order on server. Stored locally to sync later.", e)
        }
        return newOrder
    }

    override suspend fun updateOrderStatus(orderId: String, status: String) {
        orderDao.updateOrderStatus(orderId, status)
        try {
            val response = apiService.updateOrderStatus(orderId, mapOf("status" to status))
            if (!response.isSuccessful) {
                Log.w(tag, "Failed to update order status on server: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to push status update on server; saved locally.", e)
        }
    }

    override suspend fun getPendingOrders(): List<LocalOrder> {
        return orderDao.getPendingOrders()
    }
}
