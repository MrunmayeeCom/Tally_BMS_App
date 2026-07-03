package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.ReminderEntity

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Query("DELETE FROM reminders WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
