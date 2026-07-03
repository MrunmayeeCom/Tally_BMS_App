package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
