package com.example.feature.quotation.domain.models

import java.math.BigDecimal

data class Quotation(
    val id: String,
    val companyId: String,
    val tenantId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val billingAddress: String,
    val date: String,
    val expiryDate: String,
    val status: QuotationStatus,
    val subTotal: BigDecimal,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val grandTotal: BigDecimal,
    val remarks: String,
    val managerNotes: String?,
    val revisionRequestNotes: String?,
    val items: List<QuotationItem>,
    val pendingSync: Boolean = false,
    val isSynced: Boolean = true
)

enum class QuotationStatus {
    DRAFT, SENT, VIEWED, APPROVED, REJECTED, EXPIRED;

    companion object {
        fun fromString(value: String): QuotationStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DRAFT
        }
    }
}

data class QuotationItem(
    val itemId: String,
    val itemName: String,
    val sku: String,
    val quantity: BigDecimal,
    val rate: BigDecimal,
    val discountPercent: BigDecimal,
    val gstPercent: BigDecimal,
    val taxAmount: BigDecimal,
    val rowTotal: BigDecimal
)

data class QuotationAnalytics(
    val totalQuotedValue: BigDecimal,
    val wonValue: BigDecimal,
    val lostValue: BigDecimal,
    val draftValue: BigDecimal,
    val conversionRatePercent: Double,
    val quotationsCreatedCount: Int,
    val wonCount: Int,
    val lostCount: Int,
    val pendingApprovalCount: Int
)
