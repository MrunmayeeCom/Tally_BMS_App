package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.CheckInEntity

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<CheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CheckInEntity>)

    @Query("DELETE FROM check_ins WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
