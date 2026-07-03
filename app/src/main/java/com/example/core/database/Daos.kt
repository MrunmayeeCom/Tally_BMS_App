package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers WHERE companyId = :companyId ORDER BY name ASC")
    fun getLedgersByCompany(companyId: String): Flow<List<LocalLedger>>

    @Query("SELECT * FROM ledgers WHERE ledgerId = :ledgerId")
    suspend fun getLedgerById(ledgerId: String): LocalLedger?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgers(ledgers: List<LocalLedger>)

    @Query("DELETE FROM ledgers WHERE companyId = :companyId")
    suspend fun deleteCompanyLedgers(companyId: String)
}

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers WHERE companyId = :companyId ORDER BY date DESC")
    fun getVouchersByCompany(companyId: String): Flow<List<LocalVoucher>>

    @Query("SELECT * FROM vouchers WHERE pendingSync = 1")
    suspend fun getPendingSyncVouchers(): List<LocalVoucher>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<LocalVoucher>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: LocalVoucher)

    @Query("UPDATE vouchers SET pendingSync = 0 WHERE voucherId = :voucherId")
    suspend fun markSynced(voucherId: String)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE companyId = :companyId ORDER BY dueDate ASC")
    fun getBillsByCompany(companyId: String): Flow<List<LocalBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<LocalBill>)

    @Query("UPDATE bills SET recoveryState = :state, promisedDate = :promisedDate WHERE billId = :billId")
    suspend fun updateRecoveryState(billId: String, state: String, promisedDate: String?)
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY checkInTime DESC")
    fun getAllCheckIns(): Flow<List<LocalCheckIn>>

    @Query("SELECT * FROM check_ins WHERE pendingSync = 1")
    suspend fun getPendingSyncCheckIns(): List<LocalCheckIn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: LocalCheckIn)

    @Query("UPDATE check_ins SET pendingSync = 0 WHERE checkInId = :checkInId")
    suspend fun markSynced(checkInId: String)
}

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups ORDER BY dueDate ASC")
    fun getAllFollowUps(): Flow<List<LocalFollowUp>>

    @Query("SELECT * FROM follow_ups WHERE pendingSync = 1")
    suspend fun getPendingSyncFollowUps(): List<LocalFollowUp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: LocalFollowUp)

    @Query("UPDATE follow_ups SET pendingSync = 0 WHERE followUpId = :followUpId")
    suspend fun markSynced(followUpId: String)
}
