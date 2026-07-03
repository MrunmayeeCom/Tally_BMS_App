package com.example.feature.quotation.data.repository

import com.example.core.common.Resource
import com.example.feature.accounting.data.db.LocalOrder
import com.example.feature.accounting.data.db.OrderDao
import com.example.feature.quotation.data.api.QuotationApiService
import com.example.feature.quotation.data.db.*
import com.example.feature.quotation.data.dto.*
import com.example.feature.quotation.domain.IQuotationRepository
import com.example.feature.quotation.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.util.UUID

class QuotationRepositoryImpl(
    private val apiService: QuotationApiService,
    private val quotationDao: QuotationDao,
    private val orderDao: OrderDao
) : IQuotationRepository {

    override fun getQuotations(companyId: String, forceRefresh: Boolean): Flow<Resource<List<Quotation>>> = flow {
        emit(Resource.Loading)
        
        // 1. Load cached records first
        val cached = quotationDao.getQuotationsByCompany(companyId)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        // Seeding defaults on first startup if local state is blank
        if (cached.isEmpty()) {
            val seedList = getMockQuotations(companyId)
            quotationDao.insertQuotations(seedList.map { it.toLocal() })
            emit(Resource.Success(seedList))
        }

        // 2. Fetch from cloud if forceRefresh or list was cached
        if (forceRefresh || cached.isEmpty()) {
            try {
                val response = apiService.getQuotations(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val entityModels = dtos.map { it.toLocal() }
                    
                    // Retain local creations that haven't been pushed to remote
                    val localUnsynced = quotationDao.getPendingQuotations()
                    
                    // Safe overwrite
                    quotationDao.insertQuotations(entityModels)
                    quotationDao.insertQuotations(localUnsynced)
                    
                    val updatedList = quotationDao.getQuotationsByCompany(companyId)
                    emit(Resource.Success(updatedList.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                // Return cache cleanly or inform UI of state
                val finalCached = quotationDao.getQuotationsByCompany(companyId)
                emit(Resource.Success(finalCached.map { it.toDomain() }))
            }
        }
    }

    override suspend fun getQuotationById(id: String): Quotation? {
        return quotationDao.getQuotationById(id)?.toDomain()
    }

    override suspend fun createQuotation(companyId: String, tenantId: String, quotation: Quotation): Quotation {
        val sanitized = quotation.copy(
            id = if (quotation.id.isBlank()) "q_${UUID.randomUUID().toString().take(8)}" else quotation.id,
            companyId = companyId,
            tenantId = tenantId,
            pendingSync = true,
            isSynced = false
        )
        quotationDao.insertQuotation(sanitized.toLocal())

        // Background synchronization attempt
        try {
            val req = CreateQuotationRequest(
                customerId = sanitized.customerId,
                customerName = sanitized.customerName,
                customerPhone = sanitized.customerPhone,
                customerEmail = sanitized.customerEmail,
                billingAddress = sanitized.billingAddress,
                date = sanitized.date,
                expiryDate = sanitized.expiryDate,
                subTotal = sanitized.subTotal,
                discountAmount = sanitized.discountAmount,
                gstAmount = sanitized.gstAmount,
                grandTotal = sanitized.grandTotal,
                remarks = sanitized.remarks,
                items = sanitized.items.map { it.toDto() }
            )
            val response = apiService.createQuotation(req)
            if (response.isSuccessful && response.body() != null) {
                val syncedEntity = response.body()!!.toLocal().copy(pendingSync = false, isSynced = true)
                quotationDao.insertQuotation(syncedEntity)
                return syncedEntity.toDomain()
            }
        } catch (_: Exception) {}

        return sanitized
    }

    override suspend fun updateQuotation(quotation: Quotation): Quotation {
        val updated = quotation.copy(pendingSync = true, isSynced = false)
        quotationDao.insertQuotation(updated.toLocal())

        try {
            val req = CreateQuotationRequest(
                customerId = updated.customerId,
                customerName = updated.customerName,
                customerPhone = updated.customerPhone,
                customerEmail = updated.customerEmail,
                billingAddress = updated.billingAddress,
                date = updated.date,
                expiryDate = updated.expiryDate,
                subTotal = updated.subTotal,
                discountAmount = updated.discountAmount,
                gstAmount = updated.gstAmount,
                grandTotal = updated.grandTotal,
                remarks = updated.remarks,
                items = updated.items.map { it.toDto() }
            )
            val response = apiService.updateQuotation(updated.id, req)
            if (response.isSuccessful && response.body() != null) {
                val syncedEntity = response.body()!!.toLocal().copy(pendingSync = false, isSynced = true)
                quotationDao.insertQuotation(syncedEntity)
                return syncedEntity.toDomain()
            }
        } catch (_: Exception) {}

        return updated
    }

    override suspend fun deleteQuotation(id: String) {
        quotationDao.deleteQuotation(id)
        try {
            apiService.deleteQuotation(id)
        } catch (_: Exception) {}
    }

    override suspend fun updateQuotationStatus(
        id: String,
        status: QuotationStatus,
        managerNotes: String?,
        revisionRequestNotes: String?
    ): Quotation {
        val cached = quotationDao.getQuotationById(id) ?: throw IllegalArgumentException("Quotation not found")
        val updated = cached.copy(
            status = status.name,
            managerNotes = managerNotes ?: cached.managerNotes,
            revisionRequestNotes = revisionRequestNotes ?: cached.revisionRequestNotes,
            pendingSync = true,
            isSynced = false
        )
        quotationDao.insertQuotation(updated)

        try {
            val req = UpdateQuotationStatusRequest(
                status = status.name,
                managerNotes = managerNotes,
                revisionRequestNotes = revisionRequestNotes
            )
            val response = apiService.updateQuotationStatus(id, req)
            if (response.isSuccessful && response.body() != null) {
                val syncedEntity = response.body()!!.toLocal().copy(pendingSync = false, isSynced = true)
                quotationDao.insertQuotation(syncedEntity)
                return syncedEntity.toDomain()
            }
        } catch (_: Exception) {}

        return updated.toDomain()
    }

    override suspend fun convertToOrder(id: String): Boolean {
        val quotation = quotationDao.getQuotationById(id) ?: return false
        
        // Mark quotation approved/linked
        val finalQuotation = quotation.copy(status = QuotationStatus.APPROVED.name, isSynced = false, pendingSync = true)
        quotationDao.insertQuotation(finalQuotation)

        // Create actual order entry locally to meet Integration requirements!
        val itemsSummary = finalQuotation.items.joinToString("; ") { "${it.itemName} x${it.quantity}" }
        val localOrder = LocalOrder(
            orderId = "ord_${UUID.randomUUID().toString().take(6)}",
            companyId = finalQuotation.companyId,
            partyId = finalQuotation.customerId,
            partyName = finalQuotation.customerName,
            amount = finalQuotation.grandTotal,
            date = finalQuotation.date,
            status = "Approved",
            remarks = "Converted from Quotation Ref: ${finalQuotation.id}. ${finalQuotation.remarks}",
            itemsSummary = itemsSummary,
            pendingSync = true
        )
        orderDao.insertOrder(localOrder)

        try {
            // Push to cloud link
            val response = apiService.convertToOrder(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val syncedQuo = finalQuotation.copy(isSynced = true, pendingSync = false)
                quotationDao.insertQuotation(syncedQuo)
                return true
            }
        } catch (_: Exception) {}

        // Returns true as it is stored locally in the integrated Order Book database!
        return true
    }

    override fun getAnalytics(companyId: String, forceRefresh: Boolean): Flow<Resource<QuotationAnalytics>> = flow {
        emit(Resource.Loading)
        
        // Fast offline calculations
        val cachedQuotations = quotationDao.getQuotationsByCompany(companyId)
        val offlineAnalytics = computeOfflineAnalytics(cachedQuotations)
        emit(Resource.Success(offlineAnalytics))

        if (forceRefresh || cachedQuotations.isEmpty()) {
            try {
                val response = apiService.getAnalytics(companyId)
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    emit(Resource.Success(QuotationAnalytics(
                        totalQuotedValue = dto.totalQuotedValue,
                        wonValue = dto.wonValue,
                        lostValue = dto.lostValue,
                        draftValue = dto.draftValue,
                        conversionRatePercent = dto.conversionRatePercent,
                        quotationsCreatedCount = dto.quotationsCreatedCount,
                        wonCount = dto.wonCount,
                        lostCount = dto.lostCount,
                        pendingApprovalCount = dto.pendingApprovalCount
                    )))
                }
            } catch (_: Exception) {}
        }
    }

    private fun computeOfflineAnalytics(list: List<LocalQuotation>): QuotationAnalytics {
        var total = BigDecimal.ZERO
        var won = BigDecimal.ZERO
        var lost = BigDecimal.ZERO
        var drafts = BigDecimal.ZERO
        var wonCount = 0
        var lostCount = 0
        var draftsCount = 0
        var approvalsCount = 0

        list.forEach { item ->
            total += item.grandTotal
            when (item.status) {
                "Approved" -> {
                    won += item.grandTotal
                    wonCount++
                }
                "Rejected", "Expired" -> {
                    lost += item.grandTotal
                    lostCount++
                }
                "Draft" -> {
                    drafts += item.grandTotal
                    draftsCount++
                }
                "Sent", "Viewed" -> {
                    approvalsCount++
                }
            }
        }

        val totalDec = wonCount + lostCount
        val conversion = if (totalDec > 0) {
            (wonCount.toDouble() / totalDec.toDouble()) * 100.0
        } else {
            0.0
        }

        return QuotationAnalytics(
            totalQuotedValue = total,
            wonValue = won,
            lostValue = lost,
            draftValue = drafts,
            conversionRatePercent = conversion,
            quotationsCreatedCount = list.size,
            wonCount = wonCount,
            lostCount = lostCount,
            pendingApprovalCount = approvalsCount
        )
    }

    private fun getMockQuotations(companyId: String): List<Quotation> {
        return listOf(
            Quotation(
                id = "QT-2026-001",
                companyId = companyId,
                tenantId = "tenant_global",
                customerId = "cust_01",
                customerName = "Acme Distributors Pvt Ltd",
                customerPhone = "+91 98765 43210",
                customerEmail = "procure@acme.com",
                billingAddress = "45, Industrial Belt, Phase 2, Bangalore, India",
                date = "2026-06-21",
                expiryDate = "2026-07-21",
                status = QuotationStatus.SENT,
                subTotal = BigDecimal("45000.00"),
                discountAmount = BigDecimal("2250.00"),
                gstAmount = BigDecimal("7695.00"),
                grandTotal = BigDecimal("50445.00"),
                remarks = "Prices valid for 30 days. Stock subject to availability.",
                managerNotes = null,
                revisionRequestNotes = null,
                items = listOf(
                    QuotationItem("stock_001", "Premium Copper Cable (Red)", "ELC-COP-RED-01", BigDecimal("50.00"), BigDecimal("900.00"), BigDecimal("5.00"), BigDecimal("18.00"), BigDecimal("7695.00"), BigDecimal("50445.00"))
                )
            ),
            Quotation(
                id = "QT-2026-002",
                companyId = companyId,
                tenantId = "tenant_global",
                customerId = "cust_02",
                customerName = "Starlight Retail Enterprises",
                customerPhone = "+91 87654 32109",
                customerEmail = "accounts@starlightretail.in",
                billingAddress = "Commercial Block 9A, Salt Lake, Kolkata",
                date = "2026-06-20",
                expiryDate = "2026-07-20",
                status = QuotationStatus.DRAFT,
                subTotal = BigDecimal("15000.00"),
                discountAmount = BigDecimal("1500.00"),
                gstAmount = BigDecimal("2430.00"),
                grandTotal = BigDecimal("15930.00"),
                remarks = "Special promotional distributor rate.",
                managerNotes = "Needs review of gross ledger weight",
                revisionRequestNotes = null,
                items = listOf(
                    QuotationItem("stock_002", "Smart LED Panel 24W", "ELC-LED-PAN-24", BigDecimal("15.00"), BigDecimal("1000.00"), BigDecimal("10.00"), BigDecimal("18.00"), BigDecimal("2430.00"), BigDecimal("15930.00"))
                )
            ),
            Quotation(
                id = "QT-2026-003",
                companyId = companyId,
                tenantId = "tenant_global",
                customerId = "cust_03",
                customerName = "Vertex Corporate Solutions",
                customerPhone = "+91 76543 21098",
                customerEmail = "facilities@vertexcorp.com",
                billingAddress = "Tower B, tech park ground, Pune",
                date = "2026-06-18",
                expiryDate = "2026-07-18",
                status = QuotationStatus.APPROVED,
                subTotal = BigDecimal("115000.00"),
                discountAmount = BigDecimal("5000.00"),
                gstAmount = BigDecimal("19800.00"),
                grandTotal = BigDecimal("129800.00"),
                remarks = "Bulk delivery scheduled for early July.",
                managerNotes = "Approved as per volume quota discount criteria.",
                revisionRequestNotes = null,
                items = listOf(
                    QuotationItem("stock_003", "Heavy Duty Air Circulator", "ELX-FAN-IND-90", BigDecimal("10.00"), BigDecimal("11500.00"), BigDecimal("4.35"), BigDecimal("18.00"), BigDecimal("19800.00"), BigDecimal("129800.00"))
                )
            )
        )
    }

    // Mapper helper extensions inside class scope
    private fun LocalQuotation.toDomain(): Quotation = Quotation(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        customerEmail = customerEmail,
        billingAddress = billingAddress,
        date = date,
        expiryDate = expiryDate,
        status = QuotationStatus.fromString(status),
        subTotal = subTotal,
        discountAmount = discountAmount,
        gstAmount = gstAmount,
        grandTotal = grandTotal,
        remarks = remarks,
        managerNotes = managerNotes,
        revisionRequestNotes = revisionRequestNotes,
        items = items.map { it.toDomain() },
        pendingSync = pendingSync,
        isSynced = isSynced
    )

    private fun LocalQuotationItem.toDomain(): QuotationItem = QuotationItem(
        itemId = itemId,
        itemName = itemName,
        sku = sku,
        quantity = quantity,
        rate = rate,
        discountPercent = discountPercent,
        gstPercent = gstPercent,
        taxAmount = taxAmount,
        rowTotal = rowTotal
    )

    private fun QuotationItem.toLocal(): LocalQuotationItem = LocalQuotationItem(
        itemId = itemId,
        itemName = itemName,
        sku = sku,
        quantity = quantity,
        rate = rate,
        discountPercent = discountPercent,
        gstPercent = gstPercent,
        taxAmount = taxAmount,
        rowTotal = rowTotal
    )

    private fun Quotation.toLocal(): LocalQuotation = LocalQuotation(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        customerEmail = customerEmail,
        billingAddress = billingAddress,
        date = date,
        expiryDate = expiryDate,
        status = status.name,
        subTotal = subTotal,
        discountAmount = discountAmount,
        gstAmount = gstAmount,
        grandTotal = grandTotal,
        remarks = remarks,
        managerNotes = managerNotes,
        revisionRequestNotes = revisionRequestNotes,
        items = items.map { it.toLocal() },
        pendingSync = pendingSync,
        isSynced = isSynced
    )

    private fun QuotationDto.toLocal(): LocalQuotation = LocalQuotation(
        id = id,
        companyId = companyId,
        tenantId = tenantId,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        customerEmail = customerEmail,
        billingAddress = billingAddress,
        date = date,
        expiryDate = expiryDate,
        status = status,
        subTotal = subTotal,
        discountAmount = discountAmount,
        gstAmount = gstAmount,
        grandTotal = grandTotal,
        remarks = remarks,
        managerNotes = managerNotes,
        revisionRequestNotes = revisionRequestNotes,
        items = items.map { it.toLocal() },
        pendingSync = false,
        isSynced = true
    )

    private fun QuotationItemDto.toLocal(): LocalQuotationItem = LocalQuotationItem(
        itemId = itemId,
        itemName = itemName,
        sku = sku,
        quantity = quantity,
        rate = rate,
        discountPercent = discountPercent,
        gstPercent = gstPercent,
        taxAmount = taxAmount,
        rowTotal = rowTotal
    )

    private fun QuotationItem.toDto(): QuotationItemDto = QuotationItemDto(
        itemId = itemId,
        itemName = itemName,
        sku = sku,
        quantity = quantity,
        rate = rate,
        discountPercent = discountPercent,
        gstPercent = gstPercent,
        taxAmount = taxAmount,
        rowTotal = rowTotal
    )
}
