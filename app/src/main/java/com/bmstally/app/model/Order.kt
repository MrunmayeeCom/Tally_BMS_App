package com.bmstally.app.model

data class Order(
    val id: String,
    val orderNo: String,
    val type: String,
    val date: String,
    val customer: String,
    val amount: Double,
    val dueDate: String,
    val status: String
)
