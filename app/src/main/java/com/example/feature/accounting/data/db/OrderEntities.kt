package com.example.feature.accounting.data.db

import androidx.room.*
import java.math.BigDecimal

@Entity(tableName = "orders")
data class LocalOrder(
    @PrimaryKey val orderId: String,
    val companyId: String,
    val partyId: String,
    val partyName: String,
    val amount: BigDecimal,
    val date: String,
    val status: String, // "Draft", "Approved", "Shipped", "Delivered", "Canceled"
    val remarks: String,
    val itemsSummary: String, // "Product X (Pkg of 10) x2; Product Y x1"
    val pendingSync: Boolean = false
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: LocalOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<LocalOrder>)

    @Query("SELECT * FROM orders WHERE companyId = :companyId ORDER BY date DESC")
    suspend fun getOrdersByCompany(companyId: String): List<LocalOrder>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): LocalOrder?

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("SELECT * FROM orders WHERE pendingSync = 1")
    suspend fun getPendingOrders(): List<LocalOrder>

    @Update
    suspend fun updateOrder(order: LocalOrder)
}
