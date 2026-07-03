package com.example.feature.quotation.data.dto

import java.math.BigDecimal

data class QuotationDto(
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
    val status: String,
    val subTotal: BigDecimal,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val grandTotal: BigDecimal,
    val remarks: String,
    val managerNotes: String?,
    val revisionRequestNotes: String?,
    val items: List<QuotationItemDto>
)

data class QuotationItemDto(
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

data class CreateQuotationRequest(
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val billingAddress: String,
    val date: String,
    val expiryDate: String,
    val subTotal: BigDecimal,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val grandTotal: BigDecimal,
    val remarks: String,
    val items: List<QuotationItemDto>
)

data class UpdateQuotationStatusRequest(
    val status: String,
    val managerNotes: String? = null,
    val revisionRequestNotes: String? = null
)

data class QuotationAnalyticsDto(
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

data class ConvertOrderResponseDto(
    val success: Boolean,
    val orderId: String,
    val message: String
)
