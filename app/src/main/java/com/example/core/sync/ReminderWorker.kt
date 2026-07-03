package com.example.core.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.TallyBMSApp
import com.example.feature.reminder.domain.AutoReminderRule

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val tag = "ReminderWorker"
    private val channelId = "tally_bms_reminders_channel"

    override suspend fun doWork(): Result {
        Log.d(tag, "Executing scheduled background dunning rules run...")
        val container = (applicationContext as TallyBMSApp).container
        val reminderRepo = container.reminderRepository

        try {
            val schedulers = reminderRepo.getReminderSchedulerRules()
            val activeSchedulers = schedulers.filter { it.isActive }
            
            val autoRules = reminderRepo.getAutoReminderRules()
            val activeRules = autoRules.filter { it.isActive }

            if (activeSchedulers.isEmpty()) {
                Log.d(tag, "No active reminder scheduler rules found at this time.")
                return Result.success()
            }

            // Create notification channel prior to triggering the alert
            createNotificationChannel()

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Run dunning mock simulation for each active rule to match clients & notify admin
            for ((index, scheduler) in activeSchedulers.withIndex()) {
                val matchedRulesText = activeRules.joinToString(separator = ", ") { it.name }
                val contentText = "Executed scheduler: '${scheduler.ruleName}' (${scheduler.frequency}). Processing active rules: [$matchedRulesText]."
                
                val builder = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setContentTitle("TallyBMS Scheduled Reminders")
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))

                notificationManager.notify(1001 + index, builder.build())
                Log.d(tag, "Notification triggered for run configuration rules: '${scheduler.ruleName}'")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Failed to run automated reminder scheduler rules: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TallyBMS Automated Reminders"
            val descriptionText = "Triggers daily, weekly, and monthly dunning schedules and invoicing status notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
