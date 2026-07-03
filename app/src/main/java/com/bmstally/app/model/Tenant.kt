package com.bmstally.app.model

data class Tenant(
    val id: String,
    val code: String,
    val name: String,
    val gstin: String? = null
)
