package com.example.feature.crm.data.db

import androidx.room.*

@Dao
interface CrmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: LocalCustomer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<LocalCustomer>)

    @Query("SELECT * FROM crm_customers")
    suspend fun getAllCustomers(): List<LocalCustomer>

    @Query("SELECT * FROM crm_customers WHERE id = :id")
    suspend fun getCustomerById(id: String): LocalCustomer?

    @Query("SELECT * FROM crm_customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdSync(id: String): LocalCustomer?

    @Query("SELECT * FROM crm_customers WHERE name LIKE '%' || :query || '%' OR salesRepName LIKE '%' || :query || '%' OR stateCode LIKE '%' || :query || '%'")
    suspend fun searchCustomers(query: String): List<LocalCustomer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvents(events: List<LocalCustomerTimeline>)

    @Query("SELECT * FROM crm_customer_timeline WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getTimelineForCustomer(customerId: String): List<LocalCustomerTimeline>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: LocalCustomerInteraction)

    @Query("SELECT * FROM crm_customer_interactions WHERE customerId = :customerId ORDER BY interactionDate DESC")
    suspend fun getInteractionsForCustomer(customerId: String): List<LocalCustomerInteraction>

    @Query("SELECT * FROM crm_customer_interactions WHERE pendingSync = 1")
    suspend fun getPendingInteractions(): List<LocalCustomerInteraction>

    @Update
    suspend fun updateInteraction(interaction: LocalCustomerInteraction)
}
