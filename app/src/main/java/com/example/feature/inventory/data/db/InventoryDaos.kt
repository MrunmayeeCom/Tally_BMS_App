package com.example.feature.inventory.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface StockItemDao {
    @Query("SELECT * FROM stock_items WHERE companyId = :companyId ORDER BY name ASC")
    fun getItemsByCompany(companyId: String): Flow<List<LocalStockItem>>

    @Query("SELECT * FROM stock_items WHERE id = :id")
    suspend fun getStockItemById(id: String): LocalStockItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LocalStockItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LocalStockItem)

    @Delete
    suspend fun deleteItem(item: LocalStockItem)

    @Query("DELETE FROM stock_items WHERE companyId = :companyId")
    suspend fun deleteCompanyItems(companyId: String)
}

@Dao
interface GodownDao {
    @Query("SELECT * FROM godowns WHERE companyId = :companyId ORDER BY name ASC")
    fun getGodownsByCompany(companyId: String): Flow<List<LocalGodown>>

    @Query("SELECT * FROM godowns WHERE id = :id")
    suspend fun getGodownById(id: String): LocalGodown?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGodowns(godowns: List<LocalGodown>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGodown(godown: LocalGodown)

    @Query("DELETE FROM godowns WHERE companyId = :companyId")
    suspend fun deleteCompanyGodowns(companyId: String)
}

@Dao
interface StockLevelDao {
    @Query("SELECT * FROM stock_levels WHERE companyId = :companyId")
    fun getStockLevelsByCompany(companyId: String): Flow<List<LocalStockLevel>>

    @Query("SELECT * FROM stock_levels WHERE companyId = :companyId AND itemId = :itemId")
    fun getStockLevelsByItem(companyId: String, itemId: String): Flow<List<LocalStockLevel>>

    @Query("SELECT * FROM stock_levels WHERE itemId = :itemId AND godownId = :godownId")
    suspend fun getStockLevelByItemAndGodown(itemId: String, godownId: String): LocalStockLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockLevels(levels: List<LocalStockLevel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockLevel(level: LocalStockLevel)

    @Query("DELETE FROM stock_levels WHERE companyId = :companyId")
    suspend fun deleteCompanyStockLevels(companyId: String)
}

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions WHERE companyId = :companyId ORDER BY timestamp DESC")
    fun getTransactionsByCompany(companyId: String): Flow<List<LocalStockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE companyId = :companyId AND itemId = :itemId ORDER BY timestamp DESC")
    fun getTransactionsByItem(companyId: String, itemId: String): Flow<List<LocalStockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LocalStockTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<LocalStockTransaction>)

    @Query("DELETE FROM stock_transactions WHERE companyId = :companyId")
    suspend fun deleteCompanyTransactions(companyId: String)
}
