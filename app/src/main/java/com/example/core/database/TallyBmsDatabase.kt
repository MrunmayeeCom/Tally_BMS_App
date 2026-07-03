package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.feature.inventory.data.db.*
import com.example.feature.crm.data.db.*
import com.example.feature.accounting.data.db.*
import com.example.feature.approval.data.db.*
import com.example.feature.security.data.db.*

@Database(
    entities = [
        LocalLedger::class,
        LocalVoucher::class,
        LocalBill::class,
        LocalCheckIn::class,
        LocalFollowUp::class,
        LocalStockItem::class,
        LocalGodown::class,
        LocalStockLevel::class,
        LocalStockTransaction::class,
        LocalCustomer::class,
        LocalCustomerTimeline::class,
        LocalCustomerInteraction::class,
        LocalOrder::class,
        com.example.feature.territory.data.db.LocalTerritory::class,
        com.example.feature.territory.data.db.LocalBeat::class,
        com.example.feature.quotation.data.db.LocalQuotation::class,
        LocalApproval::class,
        LocalSecurityRole::class,
        LocalUserAssignment::class,
        LocalSecurityAuditRecord::class,
        com.example.feature.user.data.db.LocalUser::class,
        com.example.feature.user.data.db.LocalUserActivity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class, com.example.feature.quotation.data.db.QuotationConverters::class, ApprovalConverters::class, SecurityConverters::class, com.example.feature.user.data.db.UserTypeConverters::class)
abstract class TallyBmsDatabase : RoomDatabase() {

    abstract fun ledgerDao(): LedgerDao
    abstract fun voucherDao(): VoucherDao
    abstract fun billDao(): BillDao
    abstract fun checkInDao(): CheckInDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun stockItemDao(): StockItemDao
    abstract fun godownDao(): GodownDao
    abstract fun stockLevelDao(): StockLevelDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun crmDao(): CrmDao
    abstract fun orderDao(): OrderDao
    abstract fun territoryDao(): com.example.feature.territory.data.db.TerritoryDao
    abstract fun quotationDao(): com.example.feature.quotation.data.db.QuotationDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun securityDao(): SecurityDao
    abstract fun userDao(): com.example.feature.user.data.db.UserDao

    companion object {
        @Volatile
        private var INSTANCE: TallyBmsDatabase? = null

        fun getDatabase(context: Context): TallyBmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TallyBmsDatabase::class.java,
                    "tally_bms_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
