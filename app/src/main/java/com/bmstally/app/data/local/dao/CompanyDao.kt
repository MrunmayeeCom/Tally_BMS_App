package com.bmstally.app.data.local.dao

import androidx.room.*
import com.bmstally.app.data.local.entity.CompanyEntity

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies WHERE tenantId = :tenantId")
    suspend fun getByTenant(tenantId: String): List<CompanyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companies: List<CompanyEntity>)

    @Query("DELETE FROM companies WHERE tenantId = :tenantId")
    suspend fun deleteByTenant(tenantId: String)
}
