package com.example.core.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.TallyBMSApp
import com.example.core.network.*
import com.example.feature.accounting.data.db.LocalOrder
import java.math.BigDecimal

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val tag = "SyncWorker"

    override suspend fun doWork(): Result {
        Log.d(tag, "Initiating global offline database synchronizer...")
        val container = (applicationContext as TallyBMSApp).container
        val db = container.database
        val apiService = container.apiService

        var totalSuccesses = 0
        var totalAttempts = 0

        // 1. Synchronize Vouchers
        try {
            val voucherDao = db.voucherDao()
            val pendingVouchers = voucherDao.getPendingSyncVouchers()
            if (pendingVouchers.isNotEmpty()) {
                Log.d(tag, "Syncing ${pendingVouchers.size} pending vouchers...")
                for (v in pendingVouchers) {
                    totalAttempts++
                    val response = apiService.createVoucher(
                        VoucherDto(
                            voucherId = v.voucherId,
                            companyId = v.companyId,
                            type = v.type,
                            partyLedgerId = v.partyLedgerId,
                            partyName = v.partyName,
                            amount = v.amount.toDouble(),
                            date = v.date,
                            source = v.source
                        )
                    )
                    if (response.isSuccessful) {
                        voucherDao.markSynced(v.voucherId)
                        totalSuccesses++
                        Log.d(tag, "Synced voucher ${v.voucherId} successfully.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Voucher sync loop error: ${e.message}", e)
        }

        // 2. Synchronize Check-Ins
        try {
            val checkInDao = db.checkInDao()
            val pendingCheckIns = checkInDao.getPendingSyncCheckIns()
            if (pendingCheckIns.isNotEmpty()) {
                Log.d(tag, "Syncing ${pendingCheckIns.size} pending check-ins...")
                for (ci in pendingCheckIns) {
                    totalAttempts++
                    val response = apiService.checkIn(
                        CheckInRequest(
                            customerUserId = ci.customerUserId,
                            latitude = ci.latitude,
                            longitude = ci.longitude,
                            photoUrl = ci.photoUrl,
                            notes = ci.notes
                        )
                    )
                    if (response.isSuccessful) {
                        checkInDao.markSynced(ci.checkInId)
                        totalSuccesses++
                        Log.d(tag, "Synced check-in ${ci.checkInId} successfully.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "CheckIn sync loop error: ${e.message}", e)
        }

        // 3. Synchronize Follow-Ups
        try {
            val followUpDao = db.followUpDao()
            val pendingFollowUps = followUpDao.getPendingSyncFollowUps()
            if (pendingFollowUps.isNotEmpty()) {
                Log.d(tag, "Syncing ${pendingFollowUps.size} pending follow-ups...")
                for (f in pendingFollowUps) {
                    totalAttempts++
                    val response = apiService.createFollowUp(
                        CreateFollowUpRequest(
                            customerId = f.customerId,
                            dueDate = f.dueDate,
                            notes = f.notes ?: "",
                            assignedTo = f.assignedTo
                        )
                    )
                    if (response.isSuccessful) {
                        followUpDao.markSynced(f.followUpId)
                        totalSuccesses++
                        Log.d(tag, "Synced follow-up ${f.followUpId} successfully.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "FollowUp sync loop error: ${e.message}", e)
        }

        // 4. Synchronize CRM Customer Interactions
        try {
            val crmDao = db.crmDao()
            val pendingInteractions = crmDao.getPendingInteractions()
            if (pendingInteractions.isNotEmpty()) {
                Log.d(tag, "Syncing ${pendingInteractions.size} user interactions...")
                for (interact in pendingInteractions) {
                    totalAttempts++
                    // Post to checkin or timeline endpoint of specific customer as timeline note
                    val response = apiService.createFollowUp(
                        CreateFollowUpRequest(
                            customerId = interact.customerId,
                            dueDate = interact.interactionDate.take(10), // Extract date string
                            notes = "[${interact.interactionType}] ${interact.details}. Outcome: ${interact.outcome}",
                            assignedTo = "Sales Rep"
                        )
                    )
                    if (response.isSuccessful) {
                        crmDao.updateInteraction(interact.copy(pendingSync = false))
                        totalSuccesses++
                        Log.d(tag, "Synced interaction ${interact.id} successfully.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "CRM interaction sync error: ${e.message}", e)
        }

        // 5. Synchronize Sales/Purchase Orders
        try {
            val orderDao = db.orderDao()
            val pendingOrders = orderDao.getPendingOrders()
            if (pendingOrders.isNotEmpty()) {
                Log.d(tag, "Syncing ${pendingOrders.size} local orders...")
                for (o in pendingOrders) {
                    totalAttempts++
                    val response = apiService.createOrder(
                        OrderDto(
                            orderId = o.orderId,
                            companyId = o.companyId,
                            partyId = o.partyId,
                            partyName = o.partyName,
                            amount = o.amount,
                            date = o.date,
                            status = o.status,
                            remarks = o.remarks,
                            itemsSummary = o.itemsSummary
                        )
                    )
                    if (response.isSuccessful) {
                        orderDao.insertOrder(o.copy(pendingSync = false))
                        totalSuccesses++
                        Log.d(tag, "Synced order ${o.orderId} successfully.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Order sync loop error: ${e.message}", e)
        }

        Log.d(tag, "Sync operation complete. Successes: $totalSuccesses / Total attempts check: $totalAttempts")
        return if (totalAttempts == 0 || totalSuccesses == totalAttempts) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
