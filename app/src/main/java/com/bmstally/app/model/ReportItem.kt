package com.bmstally.app.model

data class ReportItem(val title: String)

data class SalesEntry(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
    val hasArrow: Boolean = false
)
