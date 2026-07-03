package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.OutstandingItemEntity

@Dao
interface OutstandingItemDao {
    @Query("SELECT * FROM outstanding_items WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<OutstandingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OutstandingItemEntity>)

    @Query("DELETE FROM outstanding_items WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
