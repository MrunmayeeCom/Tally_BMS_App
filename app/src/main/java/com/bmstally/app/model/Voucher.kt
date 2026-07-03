package com.bmstally.app.model

data class Voucher(
    val id: String,
    val voucher_date: String? = null,
    val voucher_type: String? = null,
    val reference_no: String? = null,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0,
    val status: String? = null,
    val party: String? = null,
    val ledger_name: String? = null,
    val narration: String? = null,
    val entries: List<LedgerEntry> = emptyList()
)

data class LedgerEntry(
    val ledger_name: String = "",
    val is_debit: Boolean = true,
    val amount: Double = 0.0
)
