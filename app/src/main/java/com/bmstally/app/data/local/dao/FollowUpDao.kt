package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.FollowUpEntity

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<FollowUpEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FollowUpEntity>)

    @Query("DELETE FROM follow_ups WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
