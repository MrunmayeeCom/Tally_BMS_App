package com.bmstally.app.model

data class DeleteHistoryRecord(
    val id: Int,
    val entityType: String,
    val entityName: String,
    val companyGuid: String,
    val deletedAt: String
)
