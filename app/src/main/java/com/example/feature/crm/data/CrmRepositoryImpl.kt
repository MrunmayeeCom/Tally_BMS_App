package com.example.feature.crm.data

import android.util.Log
import com.example.core.network.ApiService
import com.example.core.network.CrmCustomerDto
import com.example.core.network.CrmCustomerDetailDto
import com.example.core.network.CrmTimelineEventDto
import com.example.feature.crm.domain.*
import com.example.feature.crm.data.db.CrmDao
import com.example.feature.crm.data.db.LocalCustomer
import com.example.feature.crm.data.db.LocalCustomerTimeline

class CrmRepositoryImpl(
    private val apiService: ApiService,
    private val crmDao: CrmDao
) : ICrmRepository {

    private val tag = "CrmRepositoryImpl"

    override suspend fun getCustomers(
        query: String?,
        filter: String?,
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<CrmCustomer> {
        try {
            val response = apiService.getCustomers(query, filter, sort, page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val domainList = dtos.map { mapCustomerDtoToDomain(it) }
                // Persist list to Local database
                val localEntities = dtos.map { dto ->
                    val existing = crmDao.getCustomerById(dto.id)
                    LocalCustomer(
                        id = dto.id,
                        name = dto.name,
                        outstandingAmount = dto.outstandingAmount,
                        overdueAmount = dto.overdueAmount,
                        statusBadge = dto.statusBadge,
                        stateCode = dto.stateCode,
                        salesRepName = dto.salesRepName,
                        phone = existing?.phone ?: "",
                        email = existing?.email ?: "",
                        address = existing?.address ?: "",
                        keyContactPerson = existing?.keyContactPerson ?: "",
                        creditLimit = existing?.creditLimit ?: 0.0,
                        openingBalance = existing?.openingBalance ?: 0.0,
                        closingBalance = existing?.closingBalance ?: 0.0,
                        lastPaymentAmount = existing?.lastPaymentAmount ?: 0.0,
                        lastPaymentDate = existing?.lastPaymentDate ?: "",
                        overdue30Days = existing?.overdue30Days ?: 0.0,
                        overdue60Days = existing?.overdue60Days ?: 0.0,
                        overdue90Days = existing?.overdue90Days ?: 0.0,
                        overdueOver90Days = existing?.overdueOver90Days ?: 0.0,
                        lastTransactionId = existing?.lastTransactionId ?: "",
                        lastTransactionDate = existing?.lastTransactionDate ?: "",
                        lastTransactionType = existing?.lastTransactionType ?: "",
                        lastTransactionAmount = existing?.lastTransactionAmount ?: 0.0,
                        lastTransactionStatus = existing?.lastTransactionStatus ?: ""
                    )
                }
                crmDao.insertCustomers(localEntities)
                return domainList
            } else {
                Log.w(tag, "Customers API unsuccessful: ${response.code()}. Falling back to cached client registries.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Customers API failed: ${e.message}, returning resilient local storage.", e)
        }

        // Offline / Cache retrieve operations
        val cached = if (!query.isNullOrBlank()) {
            crmDao.searchCustomers(query)
        } else {
            crmDao.getAllCustomers()
        }

        if (cached.isEmpty()) {
            // Seed DB with mock data if completely empty offline
            val mocks = getMockCustomers(null, null, null, 1, 100)
            val entitiesToSeed = mocks.map { mock ->
                LocalCustomer(
                    id = mock.id,
                    name = mock.name,
                    outstandingAmount = mock.outstandingAmount,
                    overdueAmount = mock.overdueAmount,
                    statusBadge = mock.statusBadge,
                    stateCode = mock.stateCode,
                    salesRepName = mock.salesRepName,
                    phone = "+91 99000 88000",
                    email = "info@${mock.name.lowercase().replace(" ", "")}.com",
                    address = "Offline Stored Address, India",
                    keyContactPerson = "Manager",
                    creditLimit = 1000000.0,
                    openingBalance = mock.outstandingAmount,
                    closingBalance = mock.outstandingAmount,
                    lastPaymentAmount = 50000.0,
                    lastPaymentDate = "2026-06-01",
                    overdue30Days = mock.outstandingAmount * 0.5,
                    overdue60Days = mock.outstandingAmount * 0.3,
                    overdue90Days = mock.outstandingAmount * 0.2,
                    overdueOver90Days = 0.0,
                    lastTransactionId = "vouch_off_1",
                    lastTransactionDate = "2026-06-10",
                    lastTransactionType = "Invoice",
                    lastTransactionAmount = mock.outstandingAmount,
                    lastTransactionStatus = "Unpaid"
                )
            }
            crmDao.insertCustomers(entitiesToSeed)
            return getMockCustomers(query, filter, sort, page, pageSize)
        }

        var filteredList = cached.map {
            CrmCustomer(
                id = it.id,
                name = it.name,
                outstandingAmount = it.outstandingAmount,
                overdueAmount = it.overdueAmount,
                statusBadge = it.statusBadge,
                stateCode = it.stateCode,
                salesRepName = it.salesRepName
            )
        }

        // Apply filters locally on DB results
        if (!filter.isNullOrBlank() && filter != "All") {
            filteredList = filteredList.filter { it.statusBadge.equals(filter, ignoreCase = true) }
        }

        // Apply sort
        filteredList = when (sort) {
            "Outstanding (High to Low)" -> filteredList.sortedByDescending { it.outstandingAmount }
            "Overdue (High to Low)" -> filteredList.sortedByDescending { it.overdueAmount }
            "Name (A-Z)" -> filteredList.sortedBy { it.name }
            else -> filteredList
        }

        // Slicing/Paging
        val startOffset = (page - 1) * pageSize
        if (startOffset >= filteredList.size) return emptyList()
        val endOffset = (startOffset + pageSize).coerceAtMost(filteredList.size)
        return filteredList.subList(startOffset, endOffset)
    }

    override suspend fun getCustomerDetail(customerId: String): CrmCustomerDetail {
        try {
            val response = apiService.getCustomerDetail(customerId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val domainDetail = mapCustomerDetailDtoToDomain(dto)

                // Cache detail fields back into local database
                val existing = crmDao.getCustomerById(customerId)
                val updatedCustomer = LocalCustomer(
                    id = dto.id,
                    name = dto.name,
                    outstandingAmount = dto.outstandingSummary.totalOutstanding,
                    overdueAmount = dto.outstandingSummary.overdue30Days + dto.outstandingSummary.overdue60Days,
                    statusBadge = dto.statusBadge,
                    stateCode = existing?.stateCode ?: "DL",
                    salesRepName = dto.salesRepName,
                    phone = dto.contactInfo.phone,
                    email = dto.contactInfo.email,
                    address = dto.contactInfo.address,
                    keyContactPerson = dto.contactInfo.keyContactPerson,
                    creditLimit = dto.ledgerSummary.creditLimit,
                    openingBalance = dto.ledgerSummary.openingBalance,
                    closingBalance = dto.ledgerSummary.closingBalance,
                    lastPaymentAmount = dto.ledgerSummary.lastPaymentAmount,
                    lastPaymentDate = dto.ledgerSummary.lastPaymentDate,
                    overdue30Days = dto.outstandingSummary.overdue30Days,
                    overdue60Days = dto.outstandingSummary.overdue60Days,
                    overdue90Days = dto.outstandingSummary.overdue90Days,
                    overdueOver90Days = dto.outstandingSummary.overdueOver90Days,
                    lastTransactionId = dto.lastTransaction.id,
                    lastTransactionDate = dto.lastTransaction.date,
                    lastTransactionType = dto.lastTransaction.type,
                    lastTransactionAmount = dto.lastTransaction.amount,
                    lastTransactionStatus = dto.lastTransaction.status
                )
                crmDao.insertCustomer(updatedCustomer)
                return domainDetail
            } else {
                Log.w(tag, "Customer Detail API unsuccessful: ${response.code()}. Falling back to offline dataset.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Customer Detail API failed: ${e.message}, serving offline profile views.", e)
        }

        // Load offline stored info from DB
        val local = crmDao.getCustomerById(customerId)
        if (local != null) {
            return CrmCustomerDetail(
                id = local.id,
                name = local.name,
                contactInfo = ContactInfo(
                    phone = local.phone,
                    email = local.email,
                    address = local.address,
                    keyContactPerson = local.keyContactPerson
                ),
                ledgerSummary = LedgerSummary(
                    creditLimit = local.creditLimit,
                    openingBalance = local.openingBalance,
                    closingBalance = local.closingBalance,
                    lastPaymentAmount = local.lastPaymentAmount,
                    lastPaymentDate = local.lastPaymentDate
                ),
                outstandingSummary = OutstandingSummary(
                    totalOutstanding = local.outstandingAmount,
                    overdue30Days = local.overdue30Days,
                    overdue60Days = local.overdue60Days,
                    overdue90Days = local.overdue90Days,
                    overdueOver90Days = local.overdueOver90Days
                ),
                lastTransaction = Transaction(
                    id = local.lastTransactionId,
                    date = local.lastTransactionDate,
                    type = local.lastTransactionType,
                    amount = local.lastTransactionAmount,
                    status = local.lastTransactionStatus
                ),
                statusBadge = local.statusBadge,
                salesRepName = local.salesRepName
            )
        }
        return getMockCustomerDetail(customerId)
    }

    override suspend fun getCustomerTimeline(customerId: String): List<CrmTimelineEvent> {
        try {
            val response = apiService.getCustomerTimeline(customerId)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val domainHistory = dtos.map { mapTimelineEventDtoToDomain(it) }

                // Cache to room database
                val localEntities = dtos.map {
                    LocalCustomerTimeline(
                        id = it.id,
                        customerId = customerId,
                        type = it.type,
                        date = it.date,
                        description = it.description,
                        performedBy = it.performedBy,
                        outcomeStatus = it.outcomeStatus
                    )
                }
                crmDao.insertTimelineEvents(localEntities)
                return domainHistory
            } else {
                Log.w(tag, "Customer Timeline API unsuccessful: ${response.code()}. Rendering local interaction log.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Customer Timeline API failed: ${e.message}, using cached offline timeline stream.", e)
        }

        // Read Local database timeline events
        val localEvents = crmDao.getTimelineForCustomer(customerId)
        if (localEvents.isNotEmpty()) {
            return localEvents.map {
                CrmTimelineEvent(
                    id = it.id,
                    type = it.type,
                    date = it.date,
                    description = it.description,
                    performedBy = it.performedBy,
                    outcomeStatus = it.outcomeStatus
                )
            }
        }
        return getMockCustomerTimeline(customerId)
    }

    // === Mapper Methods ===

    private fun mapCustomerDtoToDomain(dto: CrmCustomerDto): CrmCustomer {
        return CrmCustomer(
            id = dto.id,
            name = dto.name,
            outstandingAmount = dto.outstandingAmount,
            overdueAmount = dto.overdueAmount,
            statusBadge = dto.statusBadge,
            stateCode = dto.stateCode,
            salesRepName = dto.salesRepName
        )
    }

    private fun mapCustomerDetailDtoToDomain(dto: CrmCustomerDetailDto): CrmCustomerDetail {
        return CrmCustomerDetail(
            id = dto.id,
            name = dto.name,
            contactInfo = ContactInfo(
                phone = dto.contactInfo.phone,
                email = dto.contactInfo.email,
                address = dto.contactInfo.address,
                keyContactPerson = dto.contactInfo.keyContactPerson
            ),
            ledgerSummary = LedgerSummary(
                creditLimit = dto.ledgerSummary.creditLimit,
                openingBalance = dto.ledgerSummary.openingBalance,
                closingBalance = dto.ledgerSummary.closingBalance,
                lastPaymentAmount = dto.ledgerSummary.lastPaymentAmount,
                lastPaymentDate = dto.ledgerSummary.lastPaymentDate
            ),
            outstandingSummary = OutstandingSummary(
                totalOutstanding = dto.outstandingSummary.totalOutstanding,
                overdue30Days = dto.outstandingSummary.overdue30Days,
                overdue60Days = dto.outstandingSummary.overdue60Days,
                overdue90Days = dto.outstandingSummary.overdue90Days,
                overdueOver90Days = dto.outstandingSummary.overdueOver90Days
            ),
            lastTransaction = Transaction(
                id = dto.lastTransaction.id,
                date = dto.lastTransaction.date,
                type = dto.lastTransaction.type,
                amount = dto.lastTransaction.amount,
                status = dto.lastTransaction.status
            ),
            statusBadge = dto.statusBadge,
            salesRepName = dto.salesRepName
        )
    }

    private fun mapTimelineEventDtoToDomain(dto: CrmTimelineEventDto): CrmTimelineEvent {
        return CrmTimelineEvent(
            id = dto.id,
            type = dto.type,
            date = dto.date,
            description = dto.description,
            performedBy = dto.performedBy,
            outcomeStatus = dto.outcomeStatus
        )
    }

    // === Mock Offline Database Generators ===

    private fun getMockCustomers(
        query: String?,
        filter: String?,
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<CrmCustomer> {
        val rawList = listOf(
            CrmCustomer("cust_01", "Acme Distributors Pvt Ltd", 284300.0, 159300.0, "Risky", "DL", "Rajesh Kumar"),
            CrmCustomer("cust_02", "Starlight Retail Enterprises", 42000.0, 0.0, "Active", "MH", "Anita Desai"),
            CrmCustomer("cust_03", "Vertex Corporate Solutions", 750000.0, 430000.0, "Cr Overdue", "DL", "Rajesh Kumar"),
            CrmCustomer("cust_04", "Star Group Bulk Agency", 0.0, 0.0, "Dormant", "KA", "Anita Desai"),
            CrmCustomer("cust_05", "Jupiter Electricals & Cables", 125000.0, 45000.0, "Active", "GJ", "Amit Patel"),
            CrmCustomer("cust_06", "Apex Machinery Hub Co", 312000.0, 20000.0, "Active", "KA", "Anita Desai"),
            CrmCustomer("cust_07", "Global Warehousing Agency", 89000.0, 89000.0, "Risky", "MH", "Amit Patel"),
            CrmCustomer("cust_08", "Vanguard Industries Corp", 0.0, 0.0, "Active", "GJ", "Rajesh Kumar")
        )

        // 1. Filter
        var filteredList = if (!filter.isNullOrBlank() && filter != "All") {
            rawList.filter { it.statusBadge.equals(filter, ignoreCase = true) }
        } else {
            rawList
        }

        // 2. Search Query
        if (!query.isNullOrBlank()) {
            val q = query.lowercase()
            filteredList = filteredList.filter {
                it.name.lowercase().contains(q) || it.salesRepName.lowercase().contains(q) || it.stateCode.lowercase().contains(q)
            }
        }

        // 3. Sorting
        filteredList = when (sort) {
            "Outstanding (High to Low)" -> filteredList.sortedByDescending { it.outstandingAmount }
            "Overdue (High to Low)" -> filteredList.sortedByDescending { it.overdueAmount }
            "Name (A-Z)" -> filteredList.sortedBy { it.name }
            else -> filteredList
        }

        // 4. Pagination slicing
        val startOffset = (page - 1) * pageSize
        if (startOffset >= filteredList.size) return emptyList()
        val endOffset = (startOffset + pageSize).coerceAtMost(filteredList.size)
        return filteredList.subList(startOffset, endOffset)
    }

    private fun getMockCustomerDetail(customerId: String): CrmCustomerDetail {
        val customersDetails = mapOf(
            "cust_01" to CrmCustomerDetail(
                id = "cust_01",
                name = "Acme Distributors Pvt Ltd",
                contactInfo = ContactInfo(
                    phone = "+91 98765 43210",
                    email = "orders@acmedistributors.com",
                    address = "Plot No. 42, Okhla Industrial Area Phase-III, New Delhi - 110020",
                    keyContactPerson = "Mr. Suresh Singhania (Director of Procurement)"
                ),
                ledgerSummary = LedgerSummary(
                    creditLimit = 500000.0,
                    openingBalance = 150000.0,
                    closingBalance = 284300.0,
                    lastPaymentAmount = 100000.0,
                    lastPaymentDate = "2026-06-10"
                ),
                outstandingSummary = OutstandingSummary(
                    totalOutstanding = 284300.0,
                    overdue30Days = 125000.0,
                    overdue60Days = 85000.0,
                    overdue90Days = 44300.0,
                    overdueOver90Days = 30000.0
                ),
                lastTransaction = Transaction(
                    id = "vouch_9829",
                    date = "2026-06-15",
                    type = "Invoice",
                    amount = 134300.0,
                    status = "Pending Payment"
                ),
                statusBadge = "Risky",
                salesRepName = "Rajesh Kumar"
            ),
            "cust_02" to CrmCustomerDetail(
                id = "cust_02",
                name = "Starlight Retail Enterprises",
                contactInfo = ContactInfo(
                    phone = "+91 99887 76655",
                    email = "billing@starlightretail.in",
                    address = "Showroom 12, Level 2, Galleria Towers, Bandra West, Mumbai - 400050",
                    keyContactPerson = "Mrs. Priyanka Sen (Accounts Lead)"
                ),
                ledgerSummary = LedgerSummary(
                    creditLimit = 300000.0,
                    openingBalance = 5000.0,
                    closingBalance = 42000.0,
                    lastPaymentAmount = 85000.0,
                    lastPaymentDate = "2026-06-18"
                ),
                outstandingSummary = OutstandingSummary(
                    totalOutstanding = 42000.0,
                    overdue30Days = 42000.0,
                    overdue60Days = 0.0,
                    overdue90Days = 0.0,
                    overdueOver90Days = 0.0
                ),
                lastTransaction = Transaction(
                    id = "vouch_9781",
                    date = "2026-06-18",
                    type = "Receipt Payment",
                    amount = 85000.0,
                    status = "Cleared"
                ),
                statusBadge = "Active",
                salesRepName = "Anita Desai"
            ),
            "cust_03" to CrmCustomerDetail(
                id = "cust_03",
                name = "Vertex Corporate Solutions",
                contactInfo = ContactInfo(
                    phone = "+91 91234 56789",
                    email = "infra@vertexcorp.org",
                    address = "E-24, Second Floor, Sector 63, Noida, Uttar Pradesh - 201301",
                    keyContactPerson = "Mr. Raghav Malhotra (VP Operations)"
                ),
                ledgerSummary = LedgerSummary(
                    creditLimit = 1000000.0,
                    openingBalance = 80000.0,
                    closingBalance = 750000.0,
                    lastPaymentAmount = 25000.0,
                    lastPaymentDate = "2026-05-30"
                ),
                outstandingSummary = OutstandingSummary(
                    totalOutstanding = 750000.0,
                    overdue30Days = 320000.0,
                    overdue60Days = 250000.0,
                    overdue90Days = 150000.0,
                    overdueOver90Days = 30000.0
                ),
                lastTransaction = Transaction(
                    id = "vouch_9104",
                    date = "2026-06-03",
                    type = "Invoice",
                    amount = 320000.0,
                    status = "Unpaid"
                ),
                statusBadge = "Cr Overdue",
                salesRepName = "Rajesh Kumar"
            )
        )

        return customersDetails[customerId] ?: CrmCustomerDetail(
            id = customerId,
            name = "Interactive Customer Hub ($customerId)",
            contactInfo = ContactInfo(
                phone = "+91 99999 88888",
                email = "contact@customerhub.net",
                address = "12, Business Boulevard, Corporate Enclave",
                keyContactPerson = "Senior Procurement Executive"
            ),
            ledgerSummary = LedgerSummary(
                creditLimit = 400000.0,
                openingBalance = 0.0,
                closingBalance = 125000.0,
                lastPaymentAmount = 20000.0,
                lastPaymentDate = "2026-06-01"
            ),
            outstandingSummary = OutstandingSummary(
                totalOutstanding = 125000.0,
                overdue30Days = 85000.0,
                overdue60Days = 40000.0,
                overdue90Days = 0.0,
                overdueOver90Days = 0.0
            ),
            lastTransaction = Transaction(
                id = "vouch_custom",
                date = "2026-06-12",
                type = "Invoice",
                amount = 40000.0,
                status = "Pending"
            ),
            statusBadge = "Active",
            salesRepName = "Amit Patel"
        )
    }

    private fun getMockCustomerTimeline(customerId: String): List<CrmTimelineEvent> {
        return listOf(
            CrmTimelineEvent(
                id = "ev_01",
                type = "Collection Visit",
                date = "2026-06-18 11:30 AM",
                description = "Met Mr. Suresh. Collected check of ₹1,00,000 against invoice #INV-291.",
                performedBy = "Rajesh Kumar",
                outcomeStatus = "Collected"
            ),
            CrmTimelineEvent(
                id = "ev_02",
                type = "Overdue Reminder",
                date = "2026-06-15 04:12 PM",
                description = "Automated WhatsApp and email dunning reminder sent to key contacts.",
                performedBy = "TallySync Auto-Agent",
                outcomeStatus = "Delivered"
            ),
            CrmTimelineEvent(
                id = "ev_03",
                type = "Follow-up Call",
                date = "2026-06-12 02:00 PM",
                description = "Customer requested a short payment grace period until June 18 due to banking cycles.",
                performedBy = "Rajesh Kumar",
                outcomeStatus = "Rescheduled"
            ),
            CrmTimelineEvent(
                id = "ev_04",
                type = "Check-In Visit",
                date = "2026-06-05 10:45 AM",
                description = "Routine field relationship check-in. Client expressed interest in the new bulk discounts.",
                performedBy = "Amit Patel",
                outcomeStatus = "Completed"
            ),
            CrmTimelineEvent(
                id = "ev_05",
                type = "Status Note",
                date = "2026-05-28 09:15 AM",
                description = "Credit ceiling temporarily bumped to 5L by finance team approval.",
                performedBy = "System Administrator",
                outcomeStatus = "Success"
            )
        )
    }
}
