package com.example.feature.quotation.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import com.example.core.common.Resource
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.CrmCustomerDetail
import com.example.feature.inventory.domain.StockItem
import com.example.feature.quotation.domain.models.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationDashboardScreen(
    viewModel: QuotationViewModel,
    onNavigateToList: () -> Unit,
    onNavigateToCreate: (String?) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val quotationsRes by viewModel.quotationsState.collectAsState()
    val analyticsRes by viewModel.analyticsState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadQuotations(forceRefresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quotation Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreate(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_quotation_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Quotation")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role & User Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active User: $userName", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Designated Role: ${userRole ?: "Sales Agent"}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Icon(Icons.Default.AccountCircle, contentDescription = "User info", modifier = Modifier.size(36.dp))
                }
            }

            // Quick Operations Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToList,
                    modifier = Modifier.weight(1f).testTag("view_all_quotes_btn")
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Browse Quotes")
                }
                Button(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f).testTag("view_analytics_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Analytics")
                }
            }

            // Analytics KPI Grid Overview
            Text("Quotation Matrix Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            when (val res = analyticsRes) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Text("Error loading analytics: ${res.message}", color = MaterialTheme.colorScheme.error)
                }
                is Resource.Success -> {
                    val data = res.data
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiSquareCard(
                                title = "Gross Quoted",
                                value = formatMoney(data.totalQuotedValue),
                                icon = Icons.Default.ShoppingCart,
                                modifier = Modifier.weight(1f)
                            )
                            KpiSquareCard(
                                title = "Conversion Rate",
                                value = "${String.format("%.1f", data.conversionRatePercent)}%",
                                icon = Icons.Default.Star,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiSquareCard(
                                title = "Won Value",
                                value = formatMoney(data.wonValue),
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f),
                                contentColor = Color(0xFF2E7D32)
                            )
                            KpiSquareCard(
                                title = "Review Pipeline",
                                value = "${data.pendingApprovalCount} pending",
                                icon = Icons.Default.Refresh,
                                modifier = Modifier.weight(1f),
                                contentColor = Color(0xFFEF6C00)
                            )
                        }
                    }
                }
            }

            // Recent Quotations Header & List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Outbound Quotes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateToList) {
                    Text("View All")
                }
            }

            when (val r = quotationsRes) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Text("Failed to retrieve quotation updates.", color = MaterialTheme.colorScheme.error)
                }
                is Resource.Success -> {
                    val data = r.data.take(5)
                    if (data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No quotations created yet.", textAlign = TextAlign.Center, color = Color.Gray)
                        }
                    } else {
                        data.forEach { q ->
                            QuotationCompactRow(
                                quotation = q,
                                onClick = { onNavigateToList() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiSquareCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.height(110.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = 0.8f))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun QuotationCompactRow(
    quotation: Quotation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(quotation.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(quotation.id, color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(quotation.date, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(formatMoney(quotation.grandTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                StatusBadge(quotation.status)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationListScreen(
    viewModel: QuotationViewModel,
    onNavigateToCreate: (String?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val quotationsRes by viewModel.quotationsState.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val tabs = listOf("All", "Draft", "Sent", "Approved", "Rejected", "Expired")

    LaunchedEffect(Unit) {
        viewModel.loadQuotations(forceRefresh = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quotation Records", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by customer, quote reference...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("quotation_search_input"),
                singleLine = true
            )

            // Dynamic Tab selection
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(statusFilter).coerceAtLeast(0),
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = statusFilter == tab,
                        onClick = { viewModel.setStatusFilter(tab) },
                        text = { Text(tab) }
                    )
                }
            }

            // Results Display
            when (val res = quotationsRes) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to retrieve listings.", color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Success -> {
                    val list = res.data
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No quotation files match this filter.", textAlign = TextAlign.Center, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(list) { q ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(q.id) }
                                        .testTag("quotation_item_${q.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(q.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(q.id, color = Color.Gray, fontSize = 12.sp)
                                            }
                                            StatusBadge(q.status)
                                        }

                                        Spacer(Modifier.height(12.dp))
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                        Spacer(Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Date Created", fontSize = 12.sp, color = Color.Gray)
                                                Text(q.date, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Gross Value (incl GST)", fontSize = 12.sp, color = Color.Gray)
                                                Text(formatMoney(q.grandTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        if (!q.isSynced || q.pendingSync) {
                                            Spacer(Modifier.height(8.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Warning, contentDescription = "Offline Pending", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Pending Sync", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditQuotationScreen(
    viewModel: QuotationViewModel,
    quotationId: String?,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val stockItems by viewModel.inventoryItems.collectAsState()
    val isSaving by viewModel.isProcessing.collectAsState()
    val saveSuccess by viewModel.saveSuccessFeedback.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Screen local States
    var selectedCustomer by remember { mutableStateOf<CrmCustomer?>(null) }
    var selectedCustomerDetail by remember { mutableStateOf<CrmCustomerDetail?>(null) }
    var selectedItemsList = remember { mutableStateListOf<QuotationItem>() }
    var remarksInput by remember { mutableStateOf("") }
    var discountPercentInput by remember { mutableStateOf("0") }

    // Dialog state for adding visual row item
    var showItemModal by remember { mutableStateOf(false) }
    var selectCustomerModal by remember { mutableStateOf(false) }

    LaunchedEffect(quotationId) {
        if (!quotationId.isNullOrBlank()) {
            val q = viewModel.currentQuotationDetail.value
            if (q != null && q.id == quotationId) {
                // Populate existing values
                remarksInput = q.remarks
                discountPercentInput = q.items.firstOrNull()?.discountPercent?.toPlainString() ?: "0"
                selectedItemsList.clear()
                selectedItemsList.addAll(q.items)
                
                // Set Selected Customer placeholder from existing quotation
                selectedCustomer = CrmCustomer(
                    id = q.customerId,
                    name = q.customerName,
                    outstandingAmount = 0.0,
                    overdueAmount = 0.0,
                    statusBadge = "Active",
                    stateCode = "29",
                    salesRepName = ""
                )
                selectedCustomerDetail = CrmCustomerDetail(
                    id = q.customerId,
                    name = q.customerName,
                    contactInfo = com.example.feature.crm.domain.ContactInfo(
                        phone = q.customerPhone,
                        email = q.customerEmail,
                        address = q.billingAddress,
                        keyContactPerson = ""
                    ),
                    ledgerSummary = com.example.feature.crm.domain.LedgerSummary(
                        creditLimit = 100000.0,
                        openingBalance = 0.0,
                        closingBalance = 0.0,
                        lastPaymentAmount = 0.0,
                        lastPaymentDate = ""
                    ),
                    outstandingSummary = com.example.feature.crm.domain.OutstandingSummary(
                        totalOutstanding = 0.0,
                        overdue30Days = 0.0,
                        overdue60Days = 0.0,
                        overdue90Days = 0.0,
                        overdueOver90Days = 0.0
                    ),
                    lastTransaction = com.example.feature.crm.domain.Transaction(
                        id = "",
                        date = "",
                        type = "",
                        amount = 0.0,
                        status = ""
                    ),
                    statusBadge = "Active",
                    salesRepName = ""
                )
            } else {
                viewModel.getQuotationById(quotationId)
            }
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetFeedback()
            onNavigateBack()
        }
    }

    // Totals calculated reactively
    val totals = remember(selectedItemsList.size, discountPercentInput) {
        val discPercent = discountPercentInput.toDoubleOrNull() ?: 0.0
        viewModel.calculateTotals(selectedItemsList.toList(), discPercent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (quotationId != null) "Edit Quotation" else "Prepare Quotation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Customer Card Selector
            Text("Client & Billing Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectCustomerModal = true }
                    .testTag("select_customer_trigger"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedCustomer == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Associate CRM Customer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedCustomer!!.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        selectedCustomerDetail?.let { detail ->
                            Text("Phone: ${detail.contactInfo.phone}", fontSize = 13.sp)
                            Text("Email: ${detail.contactInfo.email}", fontSize = 13.sp)
                            Text("Billing address: ${detail.contactInfo.address}", fontSize = 13.sp, color = Color.Gray)
                        } ?: run {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Section 2: Items list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selectable Inventory Items", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { showItemModal = true },
                    modifier = Modifier.testTag("add_item_popup_trigger")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Line")
                }
            }

            if (selectedItemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, Color.LightGray.copy(0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No line items added yet.", color = Color.Gray)
                }
            } else {
                selectedItemsList.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("SKU: ${item.sku} | Quantity: ${item.quantity} | Rate: ${formatMoney(item.rate)}", fontSize = 12.sp, color = Color.Gray)
                                Text("Discount: ${item.discountPercent}% | GST Tax: ${item.gstPercent}%", fontSize = 12.sp, color = Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                                Text(formatMoney(item.rowTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { selectedItemsList.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Value Summary Pane
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Calculated Valuation summary", fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = Color.LightGray)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sub-total", color = Color.Gray)
                        Text(formatMoney(totals.first))
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Primary Discount (%)", color = Color.Gray)
                        OutlinedTextField(
                            value = discountPercentInput,
                            onValueChange = { discountPercentInput = it },
                            modifier = Modifier.width(80.dp).height(50.dp).testTag("discount_total_input"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Taxes (GST Auto-Calculated)", color = Color.Gray)
                        Text(formatMoney(totals.second))
                     }

                    HorizontalDivider(color = Color.LightGray)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Grand Total Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(formatMoney(totals.third), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                }
            }

            // Remarks field
            OutlinedTextField(
                value = remarksInput,
                onValueChange = { remarksInput = it },
                label = { Text("Quotation terms/remarks") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("remarks_input"),
                maxLines = 3
            )

            // Submit Button
            Button(
                onClick = {
                    if (selectedCustomer != null && selectedItemsList.isNotEmpty()) {
                        viewModel.saveQuotation(
                            id = quotationId ?: "",
                            customerId = selectedCustomer!!.id,
                            customerName = selectedCustomer!!.name,
                            customerPhone = selectedCustomerDetail?.contactInfo?.phone ?: "",
                            customerEmail = selectedCustomerDetail?.contactInfo?.email ?: "",
                            customerAddress = selectedCustomerDetail?.contactInfo?.address ?: "",
                            items = selectedItemsList.toList(),
                            remarks = remarksInput,
                            discountPercentTotal = discountPercentInput.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("save_quotation_submit_btn"),
                enabled = selectedCustomer != null && selectedItemsList.isNotEmpty() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(if (quotationId != null) "Update Draft Specifications" else "Commit & Generate Quote")
                }
            }
        }
    }

    // Modal: Select CRM Customer
    if (selectCustomerModal) {
        Dialog(onDismissRequest = { selectCustomerModal = false }) {
            Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(400.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select customer from CRM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(customers) { c ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCustomer = c
                                        selectCustomerModal = false
                                        coroutineScope.launch {
                                            try {
                                                selectedCustomerDetail = viewModel.getCustomerDetail(c.id)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .testTag("crm_customer_${c.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(c.name, fontWeight = FontWeight.Bold)
                                        Text("ID: ${c.id} | Sales Rep: ${c.salesRepName}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { selectCustomerModal = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Modal: Add Inventory Line Item
    if (showItemModal) {
        var localSelectedItem by remember { mutableStateOf<StockItem?>(null) }
        var inputQuantity by remember { mutableStateOf("1") }
        var inputRate by remember { mutableStateOf("0") }
        var inputDiscount by remember { mutableStateOf("0") }
        var inputGst by remember { mutableStateOf("18.00") }

        Dialog(onDismissRequest = { showItemModal = false }) {
            Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(500.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Inventory Line Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Selection Row
                    Text("Select Stock Material", fontWeight = FontWeight.SemiBold)
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(stockItems) { s ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            localSelectedItem = s
                                            inputRate = s.sellingPrice.toPlainString()
                                        }
                                        .border(
                                            width = if (localSelectedItem?.id == s.id) 2.dp else 0.dp,
                                            color = if (localSelectedItem?.id == s.id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp)) {
                                        Column {
                                            Text(s.name, fontWeight = FontWeight.Bold)
                                            Text("SKU: ${s.sku} | Price: ${formatMoney(s.sellingPrice)}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (localSelectedItem != null) {
                        Text("Selected: ${localSelectedItem!!.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputQuantity,
                                onValueChange = { inputQuantity = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f).testTag("popup_quantity_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputRate,
                                onValueChange = { inputRate = it },
                                label = { Text("Rate") },
                                modifier = Modifier.weight(1f).testTag("popup_rate_input"),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputDiscount,
                                onValueChange = { inputDiscount = it },
                                label = { Text("Discount %") },
                                modifier = Modifier.weight(1f).testTag("popup_discount_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputGst,
                                onValueChange = { inputGst = it },
                                label = { Text("GST %") },
                                modifier = Modifier.weight(1f).testTag("popup_gst_input"),
                                singleLine = true
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showItemModal = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val item = localSelectedItem
                                if (item != null) {
                                    val qty = BigDecimal(inputQuantity.ifBlank { "1" })
                                    val rateVal = BigDecimal(inputRate.ifBlank { "0" })
                                    val discPercent = BigDecimal(inputDiscount.ifBlank { "0" })
                                    val gstPercent = BigDecimal(inputGst.ifBlank { "18" })

                                    val costRow = qty.multiply(rateVal)
                                    val discountVal = costRow.multiply(discPercent.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))
                                    val netCost = costRow.subtract(discountVal)
                                    val taxVal = netCost.multiply(gstPercent.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))
                                    val totalVal = netCost.add(taxVal)

                                    selectedItemsList.add(
                                        QuotationItem(
                                            itemId = item.id,
                                            itemName = item.name,
                                            sku = item.sku,
                                            quantity = qty,
                                            rate = rateVal,
                                            discountPercent = discPercent,
                                            gstPercent = gstPercent,
                                            taxAmount = taxVal.setScale(2, RoundingMode.HALF_UP),
                                            rowTotal = totalVal.setScale(2, RoundingMode.HALF_UP)
                                        )
                                    )
                                    showItemModal = false
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("popup_commit_row_btn"),
                            enabled = localSelectedItem != null
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationDetailScreen(
    viewModel: QuotationViewModel,
    quotationId: String,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToApproval: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val qDetail by viewModel.currentQuotationDetail.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isConverting by viewModel.isProcessing.collectAsState()

    LaunchedEffect(quotationId) {
        viewModel.getQuotationById(quotationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quotation details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (qDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val q = qDetail!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer context and Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(q.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(q.id, color = Color.Gray, fontSize = 13.sp)
                    }
                    StatusBadge(q.status)
                }

                HorizontalDivider(color = Color.LightGray)

                // Date parameters
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Invoice Date", fontSize = 12.sp, color = Color.Gray)
                        Text(q.date, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expiration Limit", fontSize = 12.sp, color = Color.Gray)
                        Text(q.expiryDate, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }
                }

                // Billing and key contacts
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Client Contact Spec", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("Phone: ${q.customerPhone}", fontSize = 13.sp)
                        Text("Email: ${q.customerEmail}", fontSize = 13.sp)
                        Text("Billing: ${q.billingAddress}", fontSize = 13.sp, color = Color.Gray)
                    }
                }

                // Items list
                Text("Quoted Material & Prices", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                q.items.forEach { line ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(line.itemName, fontWeight = FontWeight.Bold)
                                Text(formatMoney(line.rowTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("SKU: ${line.sku} | Quantity: ${line.quantity} | Rate: ${formatMoney(line.rate)}", fontSize = 12.sp, color = Color.Gray)
                            Text("Discount: ${line.discountPercent}% | Taxes (GST): ${line.gstPercent}% (${formatMoney(line.taxAmount)})", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // Cumulative pricing summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Commercial Subtotal", fontSize = 13.sp, color = Color.Gray)
                            Text(formatMoney(q.subTotal), fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Discounts Applied", fontSize = 13.sp, color = Color.Gray)
                            Text("- " + formatMoney(q.discountAmount), fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("GST Taxes Paid", fontSize = 13.sp, color = Color.Gray)
                            Text(formatMoney(q.gstAmount), fontSize = 13.sp)
                        }
                        HorizontalDivider(color = Color.LightGray)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Grand sum (incl GST)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(formatMoney(q.grandTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                        }
                    }
                }

                // Remarks field
                if (q.remarks.isNotBlank()) {
                    Text("Outbound Quotation Notes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Gray)
                    Text(q.remarks, fontSize = 13.sp)
                }

                // Manager Notes
                if (!q.managerNotes.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Manager Verification Notes", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Spacer(Modifier.height(4.dp))
                            Text(q.managerNotes, fontSize = 13.sp)
                        }
                    }
                }

                // Revision Notes
                if (!q.revisionRequestNotes.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Revision Request Specifications", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Spacer(Modifier.height(4.dp))
                            Text(q.revisionRequestNotes, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                // Context Actions list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Actions based on state
                    if (q.status == QuotationStatus.DRAFT) {
                        Button(
                            onClick = {
                                viewModel.calculateTotals(q.items) // recalculate
                                onNavigateToEdit(q.id)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("edit_quotation_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Modify Specs")
                        }

                        Button(
                            onClick = { viewModel.updateQuotationStatus(q.id, QuotationStatus.SENT) },
                            modifier = Modifier.fillMaxWidth().testTag("send_quotation_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Publish as Sent")
                        }
                    }

                    if (q.status == QuotationStatus.SENT || q.status == QuotationStatus.VIEWED) {
                        Button(
                            onClick = { viewModel.updateQuotationStatus(q.id, QuotationStatus.VIEWED) },
                            modifier = Modifier.fillMaxWidth().testTag("mark_viewed_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Mark Viewed by Client")
                        }

                        // Manager review permissions
                        val isManager = userRole == "Manager" || userRole == "Company Admin" || userRole == "Super Admin"
                        if (isManager) {
                            Button(
                                onClick = { onNavigateToApproval(q.id) },
                                modifier = Modifier.fillMaxWidth().testTag("verify_review_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Perform Verification / Approval")
                            }
                        } else {
                            Text(
                                "Request Manager Approval to progress this Quotation to a live Order entry.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (q.status == QuotationStatus.APPROVED) {
                        Button(
                            onClick = { viewModel.convertToOrder(q.id) },
                            modifier = Modifier.fillMaxWidth().testTag("convert_to_order_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            enabled = !isConverting
                        ) {
                            if (isConverting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Convert To Sales Order Book")
                            }
                        }
                    }

                    // Clone is always allowed
                    OutlinedButton(
                        onClick = { viewModel.cloneQuotation(q.id); onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().testTag("clone_quotation_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clone/Re-prepare quotation")
                    }

                    // Delete is allowed for draft
                    if (q.status == QuotationStatus.DRAFT) {
                        Button(
                            onClick = { viewModel.deleteQuotation(q.id); onNavigateBack() },
                            modifier = Modifier.fillMaxWidth().testTag("delete_quotation_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Discard Draft")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationApprovalScreen(
    viewModel: QuotationViewModel,
    quotationId: String,
    onNavigateBack: () -> Unit
) {
    val qDetail by viewModel.currentQuotationDetail.collectAsState()
    val isSaving by viewModel.isProcessing.collectAsState()

    var notesInput by remember { mutableStateOf("") }
    var revisionNotesInput by remember { mutableStateOf("") }

    LaunchedEffect(quotationId) {
        viewModel.getQuotationById(quotationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manager Review & Approval", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (qDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val q = qDetail!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Financial indicators integrated with CRM limits
                Text("Client Credit Vetting", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Customer Name", fontSize = 13.sp, color = Color.Gray)
                            Text(q.customerName, fontWeight = FontWeight.SemiBold)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Quote Valuation", fontSize = 13.sp, color = Color.Gray)
                            Text(formatMoney(q.grandTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Credit Limits", fontSize = 13.sp, color = Color.Gray)
                            Text(formatMoney(BigDecimal("150000.00")), color = Color.DarkGray)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Current Ledger Balance", fontSize = 13.sp, color = Color.Gray)
                            Text(formatMoney(BigDecimal("45000.00")), color = Color.Red)
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                // Input Section 1: Notes for approvals
                Text("Approval Feedback Notes", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    placeholder = { Text("Write notes detailing permission bounds or guidelines...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("manager_notes_input"),
                    maxLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.manageApproval(q.id, QuotationStatus.APPROVED, notesInput)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).testTag("approve_action_btn"),
                        enabled = !isSaving
                    ) {
                        Text("Approve")
                    }

                    Button(
                        onClick = {
                            viewModel.manageApproval(q.id, QuotationStatus.REJECTED, notesInput)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f).testTag("reject_action_btn"),
                        enabled = !isSaving
                    ) {
                        Text("Reject")
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                // Input Section 2: Revision requests
                Text("Request Clarification/Revision", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = revisionNotesInput,
                    onValueChange = { revisionNotesInput = it },
                    placeholder = { Text("List item revisions or pricing edits required by the sales representative...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("revision_notes_input"),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        viewModel.requestRevision(q.id, revisionNotesInput)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                    modifier = Modifier.fillMaxWidth().testTag("revision_action_btn"),
                    enabled = revisionNotesInput.isNotBlank() && !isSaving
                ) {
                    Text("Send Back for Revision")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationAnalyticsScreen(
    viewModel: QuotationViewModel,
    onNavigateBack: () -> Unit
) {
    val analyticsRes by viewModel.analyticsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadQuotations(forceRefresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commercial Quotation Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val res = analyticsRes) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to retrieve metrics.", color = MaterialTheme.colorScheme.error)
                }
            }
            is Resource.Success -> {
                val stats = res.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Core KPI Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Quotation-to-Order Conversion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            
                            // Visual circular meter mockup representation
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                                CircularProgressIndicator(
                                    progress = { (stats.conversionRatePercent / 100).toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 8.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${String.format("%.1f", stats.conversionRatePercent)}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            
                            Text("Based on total quote count: ${stats.quotationsCreatedCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    // Value split bars
                    Text("Valuation Split Matrix", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    ValuationBar(
                        label = "Gross Scope Created",
                        value = formatMoney(stats.totalQuotedValue),
                        percent = 1f,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ValuationBar(
                        label = "Approved (Won Values)",
                        value = formatMoney(stats.wonValue),
                        percent = if (stats.totalQuotedValue > BigDecimal.ZERO) stats.wonValue.divide(stats.totalQuotedValue, 2, RoundingMode.HALF_UP).toFloat() else 0f,
                        color = Color(0xFF2E7D32)
                    )

                    ValuationBar(
                        label = "Rejected / Expired (Lost Values)",
                        value = formatMoney(stats.lostValue),
                        percent = if (stats.totalQuotedValue > BigDecimal.ZERO) stats.lostValue.divide(stats.totalQuotedValue, 2, RoundingMode.HALF_UP).toFloat() else 0f,
                        color = Color.Red
                    )

                    ValuationBar(
                        label = "Draft Specifications",
                        value = formatMoney(stats.draftValue),
                        percent = if (stats.totalQuotedValue > BigDecimal.ZERO) stats.draftValue.divide(stats.totalQuotedValue, 2, RoundingMode.HALF_UP).toFloat() else 0f,
                        color = Color.Gray
                    )

                    HorizontalDivider(color = Color.LightGray)

                    // Count splits
                    Text("Quotation Quantities Ledger", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountBlock(label = "Won", count = stats.wonCount, color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                        CountBlock(label = "Lost", count = stats.lostCount, color = Color(0xFFFFEBEE), textColor = Color.Red, modifier = Modifier.weight(1f))
                        CountBlock(label = "Pending Approvals", count = stats.pendingApprovalCount, color = Color(0xFFFFF3E0), textColor = Color(0xFFEF6C00), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ValuationBar(
    label: String,
    value: String,
    percent: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 13.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.LightGray.copy(0.2f)
        )
    }
}

@Composable
fun CountBlock(
    label: String,
    count: Int,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 12.sp, color = textColor.copy(0.8f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
        }
    }
}

// Global visual helpers
@Composable
fun StatusBadge(status: QuotationStatus) {
    val (bgColor, textColor) = when (status) {
        QuotationStatus.DRAFT -> Color.LightGray to Color.DarkGray
        QuotationStatus.SENT -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        QuotationStatus.VIEWED -> Color(0xFFEDE7F6) to Color(0xFF5E35B1)
        QuotationStatus.APPROVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        QuotationStatus.REJECTED -> Color(0xFFFFEBEE) to Color.Red
        QuotationStatus.EXPIRED -> Color(0xFFECEFF1) to Color(0xFF37474F)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.name, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatMoney(value: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // Rupee support default
    return format.format(value.toDouble())
}
