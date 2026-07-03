package com.example.feature.quotation.data.db

import androidx.room.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.math.BigDecimal

@Entity(tableName = "quotations")
data class LocalQuotation(
    @PrimaryKey val id: String,
    val companyId: String,
    val tenantId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val billingAddress: String,
    val date: String,
    val expiryDate: String,
    val status: String, // "Draft", "Sent", "Viewed", "Approved", "Rejected", "Expired"
    val subTotal: BigDecimal,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val grandTotal: BigDecimal,
    val remarks: String,
    val managerNotes: String?,
    val revisionRequestNotes: String?,
    val items: List<LocalQuotationItem>,
    val pendingSync: Boolean = false,
    val isSynced: Boolean = true
)

data class LocalQuotationItem(
    val itemId: String,
    val itemName: String,
    val sku: String,
    val quantity: BigDecimal,
    val rate: BigDecimal,
    val discountPercent: BigDecimal,
    val gstPercent: BigDecimal,
    val taxAmount: BigDecimal,
    val rowTotal: BigDecimal
)

class BigDecimalJsonAdapter {
    @ToJson
    fun toJson(value: BigDecimal): String {
        return value.toPlainString()
    }

    @FromJson
    fun fromJson(value: String): BigDecimal {
        return BigDecimal(value)
    }
}

class QuotationConverters {
    private val moshi = Moshi.Builder()
        .add(BigDecimalJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, LocalQuotationItem::class.java)
    private val adapter = moshi.adapter<List<LocalQuotationItem>>(listType)

    @TypeConverter
    fun fromQuotationItemList(items: List<LocalQuotationItem>?): String? {
        return items?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toQuotationItemList(json: String?): List<LocalQuotationItem>? {
        return json?.let { adapter.fromJson(it) }
    }
}

@Dao
interface QuotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: LocalQuotation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotations(quotations: List<LocalQuotation>)

    @Query("SELECT * FROM quotations WHERE companyId = :companyId ORDER BY date DESC")
    suspend fun getQuotationsByCompany(companyId: String): List<LocalQuotation>

    @Query("SELECT * FROM quotations WHERE id = :id LIMIT 1")
    suspend fun getQuotationById(id: String): LocalQuotation?

    @Query("DELETE FROM quotations WHERE id = :id")
    suspend fun deleteQuotation(id: String)

    @Query("SELECT * FROM quotations WHERE pendingSync = 1")
    suspend fun getPendingQuotations(): List<LocalQuotation>

    @Update
    suspend fun updateQuotation(quotation: LocalQuotation)
}
