package com.bmstally.app.model

data class Invoice(
    val id: String? = null,
    val invoice_date: String? = null,
    val invoice_no: String? = null,
    val invoice_type: String? = null,
    val party_name: String? = null,
    val total_amount: Double = 0.0
)
