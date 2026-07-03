package com.example

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.di.AppContainer
import com.example.core.di.AppContainerImpl
import com.example.core.sync.ReminderWorker
import com.example.core.sync.SyncWorker
import java.util.concurrent.TimeUnit

class TallyBMSApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)

        try {
            val workManager = WorkManager.getInstance(this)

            // Periodic sync worker every 15 minutes
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                "TallyBmsOfflineSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

            // Periodic reminders scheduler every 15 minutes
            val remindersRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                "TallyBmsScheduledReminders",
                ExistingPeriodicWorkPolicy.KEEP,
                remindersRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("TallyBMSApp", "Failed to queue unique background schedules", e)
        }
    }
}
