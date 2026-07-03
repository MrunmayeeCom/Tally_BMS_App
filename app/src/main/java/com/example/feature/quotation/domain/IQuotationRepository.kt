package com.example.feature.quotation.domain

import com.example.core.common.Resource
import com.example.feature.quotation.domain.models.*
import kotlinx.coroutines.flow.Flow

interface IQuotationRepository {
    fun getQuotations(companyId: String, forceRefresh: Boolean = false): Flow<Resource<List<Quotation>>>
    suspend fun getQuotationById(id: String): Quotation?
    suspend fun createQuotation(companyId: String, tenantId: String, quotation: Quotation): Quotation
    suspend fun updateQuotation(quotation: Quotation): Quotation
    suspend fun deleteQuotation(id: String)
    suspend fun updateQuotationStatus(id: String, status: QuotationStatus, managerNotes: String? = null, revisionRequestNotes: String? = null): Quotation
    suspend fun convertToOrder(id: String): Boolean
    fun getAnalytics(companyId: String, forceRefresh: Boolean = false): Flow<Resource<QuotationAnalytics>>
}
