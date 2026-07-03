package com.bmstally.app.di

import android.content.Context
import androidx.room.Room
import com.bmstally.app.data.local.BmsDatabase
import com.bmstally.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BmsDatabase {
        return Room.databaseBuilder(
            context,
            BmsDatabase::class.java,
            "bmstally_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideItemDao(db: BmsDatabase) = db.itemDao()
    @Provides fun provideCompanyDao(db: BmsDatabase) = db.companyDao()
    @Provides fun provideFollowUpDao(db: BmsDatabase) = db.followUpDao()
    @Provides fun provideCheckInDao(db: BmsDatabase) = db.checkInDao()
    @Provides fun provideUserDao(db: BmsDatabase) = db.userDao()
    @Provides fun provideReminderDao(db: BmsDatabase) = db.reminderDao()
    @Provides fun provideTransactionDao(db: BmsDatabase) = db.transactionDao()
    @Provides fun provideDashboardStatDao(db: BmsDatabase) = db.dashboardStatDao()
    @Provides fun provideOutstandingItemDao(db: BmsDatabase) = db.outstandingItemDao()
}
