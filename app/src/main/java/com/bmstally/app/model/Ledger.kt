package com.bmstally.app.model

data class Ledger(
    val ledger_guid: String,
    val name: String,
    val type: String,
    val parent_group: String? = null,
    val opening_balance: Double = 0.0,
    val closing_balance: Double = 0.0,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val date: String? = null,
    val voucher_type: String? = null,
    val reference_no: String? = null,
    val category: String? = null,
    val email: String? = null,
    val phone: String? = null
) {
    val nature: String
        get() {
            val group = (parent_group ?: "").lowercase()
            return when {
                group.contains("capital") || group.contains("income") || group.contains("liability") -> "Cr"
                group.contains("asset") || group.contains("expense") || group.contains("debtor") -> "Dr"
                else -> "Dr"
            }
        }

    val outstanding: Double get() = closing_balance

    val dueDays: Int
        get() = if (date != null) {
            try {
                val epoch = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .parse(date!!)?.time ?: 0L
                ((System.currentTimeMillis() - epoch) / (1000 * 60 * 60 * 24)).toInt()
            } catch (_: Exception) { 0 }
        } else 0
}
