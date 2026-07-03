package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.DashboardStatEntity

@Dao
interface DashboardStatDao {
    @Query("SELECT * FROM dashboard_stats WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<DashboardStatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<DashboardStatEntity>)

    @Query("DELETE FROM dashboard_stats WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
