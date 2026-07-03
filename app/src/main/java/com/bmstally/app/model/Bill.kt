package com.bmstally.app.model

data class Bill(
    val bill_name: String? = null,
    val ledger_name: String? = null,
    val due_date: String? = null,
    val amount: Double = 0.0,
    val pending_amount: Double = 0.0
)
