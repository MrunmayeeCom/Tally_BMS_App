package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
