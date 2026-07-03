package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE tenantId = :tenantId")
    fun observeByTenant(tenantId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)

    @Query("DELETE FROM items WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
