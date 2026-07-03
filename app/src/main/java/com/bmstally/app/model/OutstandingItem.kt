package com.bmstally.app.model

data class OutstandingItem(
    val partyName: String,
    val creditInfo: String,
    val amount: String,
    val isCredit: Boolean,
    val meta: String,
    val paymentInfo: String
)
