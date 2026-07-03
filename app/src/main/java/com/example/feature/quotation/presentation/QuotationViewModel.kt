package com.example.feature.quotation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.session.SessionManager
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.CrmCustomerDetail
import com.example.feature.inventory.domain.InventoryRepository
import com.example.feature.inventory.domain.StockItem
import com.example.feature.quotation.domain.IQuotationRepository
import com.example.feature.quotation.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class QuotationViewModel(
    private val quotationRepository: IQuotationRepository,
    private val crmRepository: ICrmRepository,
    private val inventoryRepository: InventoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _companyId = MutableStateFlow("")
    val companyId: StateFlow<String> = _companyId.asStateFlow()

    private val _tenantId = MutableStateFlow("")
    val tenantId: StateFlow<String> = _tenantId.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // UI States
    private val _quotationsState = MutableStateFlow<Resource<List<Quotation>>>(Resource.Loading)
    val quotationsState: StateFlow<Resource<List<Quotation>>> = _quotationsState.asStateFlow()

    private val _analyticsState = MutableStateFlow<Resource<QuotationAnalytics>>(Resource.Loading)
    val analyticsState: StateFlow<Resource<QuotationAnalytics>> = _analyticsState.asStateFlow()

    private val _customers = MutableStateFlow<List<CrmCustomer>>(emptyList())
    val customers: StateFlow<List<CrmCustomer>> = _customers.asStateFlow()

    private val _inventoryItems = MutableStateFlow<List<StockItem>>(emptyList())
    val inventoryItems: StateFlow<List<StockItem>> = _inventoryItems.asStateFlow()

    private val _currentQuotationDetail = MutableStateFlow<Quotation?>(null)
    val currentQuotationDetail: StateFlow<Quotation?> = _currentQuotationDetail.asStateFlow()

    private val _saveSuccessFeedback = MutableStateFlow(false)
    val saveSuccessFeedback: StateFlow<Boolean> = _saveSuccessFeedback.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Selected Tab / Filters
    private val _statusFilter = MutableStateFlow<String>("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect Session details
            sessionManager.companyId.collectLatest { id ->
                if (!id.isNullOrBlank()) {
                    _companyId.value = id
                    loadStaticData(id)
                    loadQuotations(forceRefresh = false)
                }
            }
        }
        viewModelScope.launch {
            sessionManager.tenantId.collectLatest { id ->
                if (!id.isNullOrBlank()) _tenantId.value = id
            }
        }
        viewModelScope.launch {
            sessionManager.userRole.collectLatest { role ->
                _userRole.value = role
            }
        }
        viewModelScope.launch {
            sessionManager.userName.collectLatest { name ->
                _userName.value = name ?: "Default Sales Rep"
            }
        }
    }

    fun loadQuotations(forceRefresh: Boolean = false) {
        val cid = _companyId.value
        if (cid.isBlank()) return

        viewModelScope.launch {
            combine(
                quotationRepository.getQuotations(cid, forceRefresh),
                _statusFilter,
                _searchQuery
            ) { resource, filter, query ->
                when (resource) {
                    is Resource.Loading -> Resource.Loading
                    is Resource.Error -> resource
                    is Resource.Success -> {
                        val filtered = resource.data.filter { q ->
                            val matchesSearch = q.customerName.contains(query, ignoreCase = true) ||
                                                q.id.contains(query, ignoreCase = true)
                            val matchesFilter = when (filter) {
                                "All" -> true
                                else -> q.status.name.equals(filter, ignoreCase = true)
                            }
                            matchesSearch && matchesFilter
                        }
                        Resource.Success(filtered)
                    }
                }
            }.collect {
                _quotationsState.value = it
            }
        }

        viewModelScope.launch {
            quotationRepository.getAnalytics(cid, forceRefresh).collect {
                _analyticsState.value = it
            }
        }
    }

    private fun loadStaticData(cid: String) {
        viewModelScope.launch {
            try {
                // Fetch up to 100 customers
                val customerList = crmRepository.getCustomers(null, null, null, 1, 100)
                _customers.value = customerList
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                inventoryRepository.getStockItems(cid).collectLatest { items ->
                    _inventoryItems.value = items
                }
            } catch (_: Exception) {}
        }
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
        loadQuotations(forceRefresh = false)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadQuotations(forceRefresh = false)
    }

    fun getQuotationById(id: String) {
        viewModelScope.launch {
            _currentQuotationDetail.value = quotationRepository.getQuotationById(id)
        }
    }

    // Calculation help for Create / Edit Quotation
    fun calculateTotals(
        items: List<QuotationItem>,
        discountPercentTotal: Double = 0.0
    ): Triple<BigDecimal, BigDecimal, BigDecimal> {
        var subTotal = BigDecimal.ZERO
        var gstTotal = BigDecimal.ZERO

        items.forEach { item ->
            // Subtotal = sum of (quantity * rate - individual discounts)
            val rowTotalSub = item.quantity.multiply(item.rate)
            val individualDiscount = rowTotalSub.multiply(item.discountPercent.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))
            val netRow = rowTotalSub.subtract(individualDiscount)
            subTotal = subTotal.add(netRow)

            // Tax = net row * gstPercent / 100
            val rowTax = netRow.multiply(item.gstPercent.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))
            gstTotal = gstTotal.add(rowTax)
        }

        val totalDiscount = subTotal.multiply(BigDecimal(discountPercentTotal).divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))
        val netSubTotal = subTotal.subtract(totalDiscount)
        val grandTotal = netSubTotal.add(gstTotal)

        return Triple(
            netSubTotal.setScale(2, RoundingMode.HALF_UP),
            gstTotal.setScale(2, RoundingMode.HALF_UP),
            grandTotal.setScale(2, RoundingMode.HALF_UP)
        )
    }

    fun saveQuotation(
        id: String = "",
        customerId: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        customerAddress: String,
        items: List<QuotationItem>,
        remarks: String,
        discountPercentTotal: Double = 0.0
    ) {
        if (_companyId.value.isBlank()) return
        _isProcessing.value = true

        viewModelScope.launch {
            val (sub, gst, grand) = calculateTotals(items, discountPercentTotal)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val cal = Calendar.getInstance().apply { add(Calendar.DATE, 30) }
            val expDateStr = sdf.format(cal.time)

            val quotation = Quotation(
                id = id,
                companyId = _companyId.value,
                tenantId = _tenantId.value.ifBlank { "tenant_global" },
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                billingAddress = customerAddress,
                date = dateStr,
                expiryDate = expDateStr,
                status = QuotationStatus.DRAFT,
                subTotal = sub,
                discountAmount = sub.multiply(BigDecimal(discountPercentTotal).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)),
                gstAmount = gst,
                grandTotal = grand,
                remarks = remarks,
                managerNotes = null,
                revisionRequestNotes = null,
                items = items
            )

            if (id.isBlank()) {
                quotationRepository.createQuotation(_companyId.value, _tenantId.value, quotation)
            } else {
                val existing = quotationRepository.getQuotationById(id)
                val toSave = quotation.copy(
                    status = existing?.status ?: QuotationStatus.DRAFT,
                    managerNotes = existing?.managerNotes,
                    revisionRequestNotes = existing?.revisionRequestNotes
                )
                quotationRepository.updateQuotation(toSave)
            }

            _isProcessing.value = false
            _saveSuccessFeedback.value = true
            loadQuotations(forceRefresh = true)
        }
    }

    fun resetFeedback() {
        _saveSuccessFeedback.value = false
    }

    suspend fun getCustomerDetail(customerId: String): CrmCustomerDetail {
        return crmRepository.getCustomerDetail(customerId)
    }

    fun cloneQuotation(id: String) {
        viewModelScope.launch {
            val q = quotationRepository.getQuotationById(id) ?: return@launch
            val clonedId = "QT-CLONE-${UUID.randomUUID().toString().take(4).uppercase()}"
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val cal = Calendar.getInstance().apply { add(Calendar.DATE, 30) }
            val expDateStr = sdf.format(cal.time)

            val cloned = q.copy(
                id = clonedId,
                date = dateStr,
                expiryDate = expDateStr,
                status = QuotationStatus.DRAFT,
                managerNotes = null,
                revisionRequestNotes = null,
                pendingSync = true,
                isSynced = false
            )
            quotationRepository.createQuotation(cloned.companyId, cloned.tenantId, cloned)
            loadQuotations(forceRefresh = true)
        }
    }

    fun deleteQuotation(id: String) {
        viewModelScope.launch {
            quotationRepository.deleteQuotation(id)
            loadQuotations(forceRefresh = true)
        }
    }

    // Quotation Workflow Status Updates
    fun updateQuotationStatus(id: String, status: QuotationStatus) {
        viewModelScope.launch {
            _isProcessing.value = true
            quotationRepository.updateQuotationStatus(id, status)
            _isProcessing.value = false
            loadQuotations(forceRefresh = true)
            getQuotationById(id) // Sync local detail state
        }
    }

    // Manager Approval Workflow Controls
    fun manageApproval(
        id: String,
        status: QuotationStatus, // APPROVED or REJECTED
        notes: String
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            quotationRepository.updateQuotationStatus(
                id = id,
                status = status,
                managerNotes = notes,
                revisionRequestNotes = null
            )
            _isProcessing.value = false
            loadQuotations(forceRefresh = true)
            getQuotationById(id)
        }
    }

    fun requestRevision(id: String, revisionNotes: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            quotationRepository.updateQuotationStatus(
                id = id,
                status = QuotationStatus.DRAFT,
                managerNotes = null,
                revisionRequestNotes = revisionNotes
            )
            _isProcessing.value = false
            loadQuotations(forceRefresh = true)
            getQuotationById(id)
        }
    }

    fun convertToOrder(id: String) {
        _isProcessing.value = true
        viewModelScope.launch {
            val success = quotationRepository.convertToOrder(id)
            _isProcessing.value = false
            if (success) {
                _saveSuccessFeedback.value = true
                loadQuotations(forceRefresh = true)
                getQuotationById(id)
            }
        }
    }
}
