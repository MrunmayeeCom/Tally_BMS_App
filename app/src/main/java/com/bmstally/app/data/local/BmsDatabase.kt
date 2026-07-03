package com.bmstally.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bmstally.app.data.local.dao.*
import com.bmstally.app.data.local.entity.*

@Database(
    entities = [
        ItemEntity::class,
        CompanyEntity::class,
        DashboardStatEntity::class,
        OutstandingItemEntity::class,
        FollowUpEntity::class,
        CheckInEntity::class,
        UserEntity::class,
        ReminderEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BmsDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun companyDao(): CompanyDao
    abstract fun dashboardStatDao(): DashboardStatDao
    abstract fun outstandingItemDao(): OutstandingItemDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun checkInDao(): CheckInDao
    abstract fun userDao(): UserDao
    abstract fun reminderDao(): ReminderDao
    abstract fun transactionDao(): TransactionDao
}
