package com.bmstally.app.model

data class Company(
    val id: String,
    val name: String,
    val gstin: String? = null
)
