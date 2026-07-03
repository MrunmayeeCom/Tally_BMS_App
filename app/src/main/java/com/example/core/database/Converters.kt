package com.example.core.database

import androidx.room.TypeConverter
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromPostings(postings: List<VoucherPosting>?): String? {
        if (postings == null) return null
        return postings.joinToString(";^;") { p ->
            val escapedName = p.ledgerName.replace(",", "\\,").replace(";", "\\;")
            "${p.ledgerId},^,${escapedName},^,${p.isDebit},^,${p.amount.toPlainString()}"
        }
    }

    @TypeConverter
    fun toPostings(data: String?): List<VoucherPosting>? {
        if (data == null) return null
        if (data.isEmpty()) return emptyList()

        val list = mutableListOf<VoucherPosting>()
        val entries = data.split(";^;")
        for (entry in entries) {
            if (entry.isEmpty()) continue
            val parts = entry.split(",^,")
            if (parts.size >= 4) {
                val ledgerId = parts[0]
                val ledgerName = parts[1].replace("\\,", ",").replace("\\;", ";")
                val isDebit = parts[2].toBoolean()
                val amount = BigDecimal(parts[3])
                list.add(VoucherPosting(ledgerId, ledgerName, isDebit, amount))
            }
        }
        return list
    }
}
