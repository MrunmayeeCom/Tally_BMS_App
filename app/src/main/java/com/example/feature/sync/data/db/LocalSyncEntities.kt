package com.example.feature.sync.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sync_queue")
data class LocalSyncQueue(
    @PrimaryKey val id: String,
    val recordType: String,
    val description: String,
    val priority: String,
    val retryCount: Int,
    val status: String,
    val timestamp: Long
)

@Entity(tableName = "sync_errors")
data class LocalSyncError(
    @PrimaryKey val id: String,
    val recordType: String,
    val description: String,
    val errorMessage: String,
    val failedAt: String,
    val retryCount: Int
)

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC")
    fun getQueueRecordsFlow(): Flow<List<LocalSyncQueue>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC")
    suspend fun getQueueRecords(): List<LocalSyncQueue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueRecord(record: LocalSyncQueue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueRecords(records: List<LocalSyncQueue>)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueRecord(id: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()

    @Query("SELECT * FROM sync_errors ORDER BY failedAt DESC")
    fun getErrorRecordsFlow(): Flow<List<LocalSyncError>>

    @Query("SELECT * FROM sync_errors ORDER BY failedAt DESC")
    suspend fun getErrorRecords(): List<LocalSyncError>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorRecord(error: LocalSyncError)

    @Query("DELETE FROM sync_errors WHERE id = :id")
    suspend fun deleteErrorRecord(id: String)

    @Query("DELETE FROM sync_errors")
    suspend fun clearErrors()
}

@Database(entities = [LocalSyncQueue::class, LocalSyncError::class], version = 1, exportSchema = false)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getDatabase(context: Context): SyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SyncDatabase::class.java,
                    "sync_management_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
