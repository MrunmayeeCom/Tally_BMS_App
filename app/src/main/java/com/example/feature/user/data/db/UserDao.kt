package com.example.feature.user.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users_profile WHERE companyId = :companyId")
    fun getUsersForCompany(companyId: String): Flow<List<LocalUser>>

    @Query("SELECT * FROM users_profile WHERE id = :id")
    suspend fun getUserById(id: String): LocalUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<LocalUser>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)

    @Delete
    suspend fun deleteUser(user: LocalUser)

    @Query("DELETE FROM users_profile WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT * FROM users_activity WHERE userId = :userId ORDER BY timestamp DESC")
    fun getActivityForUser(userId: String): Flow<List<LocalUserActivity>>

    @Query("SELECT * FROM users_activity ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<LocalUserActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LocalUserActivity)

    @Query("DELETE FROM user_role_assignments WHERE userId = :userId")
    suspend fun removeAssignmentsForUser(userId: String)
}
